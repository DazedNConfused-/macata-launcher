package com.dazednconfused.catalauncher.update;

import static com.dazednconfused.catalauncher.helper.Constants.OFFICIAL_CDDA_REPOSITORY_NAME;
import static com.dazednconfused.catalauncher.helper.Constants.OFFICIAL_CDDA_REPOSITORY_OWNER;

import com.dazednconfused.catalauncher.configuration.ConfigurationManager;
import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.utils.CustomTimeUtils;

import io.vavr.control.Try;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import net.lingala.zip4j.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages game update checking and downloading functionality.
 */
public class GameUpdateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameUpdateManager.class);

    private static final int DOWNLOAD_BUFFER_SIZE = 8192;
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 60000;

    /**
     * GitHub API base URL
     */
    protected static String GITHUB_API_URL = "https://api.github.com";

    /**
     * Checks if the game update feature is properly configured.
     *
     * @return {@code true} if both repo owner and name are configured
     */
    public static boolean isConfigured() {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();
        return !StringUtils.isBlank(owner) && !StringUtils.isBlank(repo);
    }

    /**
     * Checks if the installed version is configured.
     *
     * @return {@code true} if installed version is set
     */
    public static boolean isInstalledVersionConfigured() {
        String installedVersion = ConfigurationManager.getInstance().getInstalledGameVersion();
        return !StringUtils.isBlank(installedVersion);
    }

    /**
     * Checks if the CDDA path is configured.
     *
     * @return {@code true} if CDDA path is set
     */
    public static boolean isCddaPathConfigured() {
        String cddaPath = ConfigurationManager.getInstance().getCddaPath();
        return !StringUtils.isBlank(cddaPath);
    }

    /**
     * Determines if a game update is available.
     *
     * @return {@link Optional#empty()} if configuration is incomplete,
     *         otherwise {@code Optional.of(true)} if update available
     */
    public static Optional<Boolean> isGameUpdateAvailable() {
        if (!isConfigured()) {
            LOGGER.info("Game update check skipped: repository not configured");
            return Optional.empty();
        }

        String installedVersionStr = ConfigurationManager.getInstance().getInstalledGameVersion();
        if (StringUtils.isBlank(installedVersionStr)) {
            LOGGER.info("Game update check skipped: installed version not set");
            return Optional.empty();
        }

        Optional<GameVersion> latestVersion = getLatestGameReleaseTag();
        if (latestVersion.isEmpty()) {
            LOGGER.info("Could not determine latest game release");
            return Optional.empty();
        }

        try {
            GameVersion installed = new GameVersion(installedVersionStr);
            GameVersion latest = latestVersion.get();

            LOGGER.debug("Comparing installed [{}] with latest [{}]", installed, latest);
            return Optional.of(latest.compareTo(installed) > 0);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not parse installed version [{}]: {}", installedVersionStr, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Queries for the latest game release tag.
     *
     * @return the latest release version, or empty if not found
     */
    public static Optional<GameVersion> getLatestGameReleaseTag() {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();

        if (StringUtils.isBlank(owner) || StringUtils.isBlank(repo)) {
            LOGGER.warn("Cannot query releases: repository not configured");
            return Optional.empty();
        }

        LOGGER.info("Querying latest release from [{}/{}]...", owner, repo);

        return Try.of(() -> getLatestReleaseTagFromGithub(owner, repo))
            .map(GameVersion::new)
            .onFailure(t -> LOGGER.error("Error retrieving release from [{}/{}]", owner, repo, t))
            .toJavaOptional();
    }

    /**
     * Finds the macOS download URL for the latest release.
     *
     * @return the download URL for macOS asset, or empty if not found
     */
    public static Optional<String> getMacOsDownloadUrl() {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();

        if (StringUtils.isBlank(owner) || StringUtils.isBlank(repo)) {
            return Optional.empty();
        }

        LOGGER.info("Finding macOS download URL for [{}/{}]...", owner, repo);

        return Try.of(() -> findMacOsAssetUrl(owner, repo))
            .onFailure(t -> LOGGER.error("Error finding macOS asset for [{}/{}]", owner, repo, t))
            .toJavaOptional();
    }

    /**
     * Downloads and installs the latest game version.
     *
     * @param progressCallback called with progress percentage (0-100)
     * @param statusCallback called with status messages
     * @return true if successful, false otherwise
     */
    public static boolean downloadAndInstallUpdate(Consumer<Integer> progressCallback, Consumer<String> statusCallback) {
        try {
            // validate configuration ---
            if (!isConfigured()) {
                statusCallback.accept("Error: Repository not configured");
                return false;
            }

            String cddaPath = ConfigurationManager.getInstance().getCddaPath();
            if (StringUtils.isBlank(cddaPath)) {
                statusCallback.accept("Error: CDDA path not configured");
                return false;
            }

            // find download URL ---
            statusCallback.accept("Finding download URL...");
            progressCallback.accept(5);

            Optional<String> downloadUrl = getMacOsDownloadUrl();
            if (downloadUrl.isEmpty()) {
                statusCallback.accept("Error: No macOS download found");
                return false;
            }

            // get latest version tag ---
            Optional<GameVersion> latestVersion = getLatestGameReleaseTag();
            if (latestVersion.isEmpty()) {
                statusCallback.accept("Error: Could not determine version");
                return false;
            }

            // prepare download directory ---
            statusCallback.accept("Preparing download...");
            progressCallback.accept(10);

            Path downloadsDir = Paths.getDownloadsPath();
            Files.createDirectories(downloadsDir);

            String fileName = extractFileName(downloadUrl.get());
            File downloadFile = downloadsDir.resolve(fileName).toFile();

            // download file ---
            statusCallback.accept("Downloading game binary...");
            boolean downloaded = downloadFile(downloadUrl.get(), downloadFile, progress -> {
                // scale download progress from 10% to 60%
                int scaledProgress = 10 + (int) (progress * 0.5);
                progressCallback.accept(scaledProgress);
            });

            if (!downloaded) {
                statusCallback.accept("Error: Download failed");
                return false;
            }

            // extract if needed ---
            statusCallback.accept("Extracting...");
            progressCallback.accept(65);

            File extractedApp = extractGameBinary(downloadFile, downloadsDir.toFile());
            if (extractedApp == null) {
                statusCallback.accept("Error: Extraction failed");
                return false;
            }

            // move old binary to trash ---
            statusCallback.accept("Moving old binary to trash...");
            progressCallback.accept(75);

            File oldBinary = new File(cddaPath);
            if (oldBinary.exists()) {
                if (!moveToTrash(oldBinary)) {
                    statusCallback.accept("Error: Could not move old binary to trash");
                    return false;
                }
            }

            // install new binary ---
            statusCallback.accept("Installing new binary...");
            progressCallback.accept(85);

            File destination = new File(cddaPath);
            if (extractedApp.isDirectory()) {
                FileUtils.moveDirectory(extractedApp, destination);
            } else {
                FileUtils.moveFile(extractedApp, destination);
            }

            // update configuration ---
            statusCallback.accept("Updating configuration...");
            progressCallback.accept(95);

            ConfigurationManager.getInstance().setInstalledGameVersion(
                latestVersion.get().getOriginalVersion()
            );

            // cleanup downloads ---
            if (downloadFile.exists()) {
                downloadFile.delete();
            }

            statusCallback.accept("Update complete!");
            progressCallback.accept(100);

            LOGGER.info("Game updated successfully to version [{}]",
                latestVersion.get().getOriginalVersion());

            return true;

        } catch (Exception e) {
            LOGGER.error("Error during game update", e);
            statusCallback.accept("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Opens the latest game release page in the default browser.
     */
    public static void openLatestGameReleaseInDefaultBrowser() {
        getLatestGameReleaseTag().ifPresent(GameUpdateManager::openReleaseInDefaultBrowser);
    }

    /**
     * Opens the specified release's page in the default browser.
     */
    public static void openReleaseInDefaultBrowser(GameVersion version) {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();

        LOGGER.info("Opening [{}]'s release page...", version);

        Try.run(() -> openGithubReleaseInDefaultBrowser(owner, repo, version.toString()))
            .onFailure(t -> LOGGER.error("Error opening release [{}]", version, t));
    }

    /**
     * Downloads a file from URL to destination with progress reporting.
     */
    protected static boolean downloadFile(String urlString, File destination, Consumer<Integer> progressCallback) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setInstanceFollowRedirects(true);

            // handle redirects manually for cross-protocol redirects
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                String newUrl = connection.getHeaderField("Location");
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
            }

            long fileSize = connection.getContentLengthLong();
            LOGGER.debug("Downloading {} bytes to {}...", fileSize, destination);

            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(destination)) {

                byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
                long totalRead = 0;
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;

                    if (fileSize > 0) {
                        int progress = (int) ((totalRead * 100) / fileSize);
                        progressCallback.accept(progress);
                    }
                }
            }

            connection.disconnect();
            LOGGER.info("Download complete: {}", destination);
            return true;

        } catch (IOException e) {
            LOGGER.error("Download failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Extracts the game binary from a downloaded archive.
     */
    protected static File extractGameBinary(File archive, File extractDir) {
        String name = archive.getName().toLowerCase();

        try {
            if (name.endsWith(".zip")) {
                LOGGER.debug("Extracting ZIP: {}", archive);
                try (ZipFile zipFile = new ZipFile(archive)) {
                    zipFile.extractAll(extractDir.getPath());
                }
                return findAppBundle(extractDir);

            } else if (name.endsWith(".dmg")) {
                LOGGER.debug("Processing DMG: {}", archive);
                return extractFromDmg(archive, extractDir);

            } else if (name.endsWith(".app")) {
                // Already an app bundle
                return archive;
            }

            LOGGER.warn("Unknown archive format: {}", name);
            return null;

        } catch (Exception e) {
            LOGGER.error("Extraction failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Finds a .app bundle at the root level of the given directory (no recursion).
     */
    protected static File findAppBundle(File directory) {
        if (directory == null) {
            return null;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.getName().endsWith(".app")) {
                return file;
            }
        }
        return null;
    }

    /**
     * Extracts .app from a DMG file using hdiutil.
     */
    private static File extractFromDmg(File dmgFile, File extractDir) {
        String mountPoint = null;
        try {
            // mount the DMG and parse output to find mount point ---
            ProcessBuilder mountPb = new ProcessBuilder(
                getHdiutilPath(), "attach", dmgFile.getAbsolutePath(), "-nobrowse"
            );
            mountPb.redirectErrorStream(true);
            Process mountProcess = mountPb.start();

            // read output to find mount point (last column of last line)
            String mountOutput;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(mountProcess.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                mountOutput = sb.toString();
            }

            int mountResult = mountProcess.waitFor();
            if (mountResult != 0) {
                LOGGER.error("Failed to mount DMG (exit code {}): {}", mountResult, mountOutput);
                return null;
            }

            // parse mount point from output (format: "/dev/diskX  Apple_HFS  /Volumes/Name")
            String[] lines = mountOutput.trim().split("\n");
            for (String line : lines) {
                if (line.contains("/Volumes/")) {
                    int volIdx = line.indexOf("/Volumes/");
                    mountPoint = line.substring(volIdx).trim();
                    break;
                }
            }

            if (mountPoint == null) {
                LOGGER.error("Could not determine mount point from output: {}", mountOutput);
                return null;
            }

            LOGGER.debug("DMG mounted at: {}", mountPoint);

            // find .app in mounted volume ---
            File mountDir = new File(mountPoint);
            File appBundle = findAppBundle(mountDir);
            if (appBundle == null) {
                LOGGER.error("No .app found in DMG at {}", mountPoint);
                return null;
            }

            LOGGER.debug("Found app bundle: {}", appBundle.getAbsolutePath());

            // use ditto to copy (handles macOS resource forks and permissions) ---
            File destApp = new File(extractDir, appBundle.getName());
            ProcessBuilder copyPb = new ProcessBuilder(
                getDittoPath(), appBundle.getAbsolutePath(), destApp.getAbsolutePath()
            );
            copyPb.redirectErrorStream(true);
            Process copyProcess = copyPb.start();

            // read any output/errors
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(copyProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LOGGER.debug("ditto: {}", line);
                }
            }

            int copyResult = copyProcess.waitFor();
            if (copyResult != 0) {
                LOGGER.error("Failed to copy app bundle (exit code {})", copyResult);
                return null;
            }

            LOGGER.debug("App bundle copied to: {}", destApp.getAbsolutePath());

            // remove quarantine attributes ---
            ProcessBuilder xattrPb = new ProcessBuilder(
                getXattrPath(), "-cr", destApp.getAbsolutePath()
            );
            xattrPb.start().waitFor();
            LOGGER.debug("Cleared extended attributes from: {}", destApp.getAbsolutePath());

            // ensure executable permissions on the main binaries ---
            ProcessBuilder chmodMacOsPb = new ProcessBuilder(
                getChmodPath(), "-R", "+x", destApp.getAbsolutePath() + "/Contents/MacOS"
            );
            chmodMacOsPb.start().waitFor();
            LOGGER.debug("Set executable permissions on: {}/Contents/MacOS", destApp.getAbsolutePath());

            // also set executable permissions on Resources where game binaries typically reside ---
            File resourcesDir = new File(destApp, "Contents/Resources");
            if (resourcesDir.exists()) {
                ProcessBuilder chmodResourcesPb = new ProcessBuilder(
                    getChmodPath(), "-R", "+x", resourcesDir.getAbsolutePath()
                );
                chmodResourcesPb.start().waitFor();
                LOGGER.debug("Set executable permissions on: {}", resourcesDir.getAbsolutePath());
            }

            return destApp;

        } catch (Exception e) {
            LOGGER.error("DMG extraction failed: {}", e.getMessage(), e);
            return null;
        } finally {
            // always try to unmount ---
            if (mountPoint != null) {
                try {
                    ProcessBuilder unmountPb = new ProcessBuilder(
                        getHdiutilPath(), "detach", mountPoint, "-quiet", "-force"
                    );
                    unmountPb.start().waitFor();
                    LOGGER.debug("DMG unmounted: {}", mountPoint);
                } catch (Exception e) {
                    LOGGER.warn("Failed to unmount DMG: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Moves a file or directory to the trash folder.
     */
    protected static boolean moveToTrash(File file) {
        try {
            Path trashDir = Paths.getCustomTrashedGamePath().resolve(CustomTimeUtils.getYyyyMmDdHhMmSsTimestamp());
            Files.createDirectories(trashDir);

            File destination = trashDir.resolve(file.getName()).toFile();

            if (file.isDirectory()) {
                FileUtils.moveDirectory(file, destination);
            } else {
                FileUtils.moveFile(file, destination);
            }

            LOGGER.info("Moved [{}] to trash [{}]", file, destination);
            return true;

        } catch (IOException e) {
            LOGGER.error("Failed to move to trash: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Extracts filename from URL.
     */
    protected static String extractFileName(String url) {
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < url.length() - 1) {
            String name = url.substring(lastSlash + 1);
            // Remove query parameters
            int queryStart = name.indexOf('?');
            if (queryStart > 0) {
                name = name.substring(0, queryStart);
            }
            return name;
        }
        return "download.zip";
    }

    /**
     * Checks if the configured repo is the official {@link com.dazednconfused.catalauncher.helper.Constants#OFFICIAL_CDDA_REPOSITORY_OWNER}/
     * {@link com.dazednconfused.catalauncher.helper.Constants#OFFICIAL_CDDA_REPOSITORY_NAME} repository.
     * <br><br>
     * Only this repo has stable releases; all others are experimental-only.
     */
    public static boolean isOfficialCddaRepo() {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();

        boolean result = OFFICIAL_CDDA_REPOSITORY_OWNER.equalsIgnoreCase(owner) && OFFICIAL_CDDA_REPOSITORY_NAME.equalsIgnoreCase(repo);
        LOGGER.trace("isOfficialCddaRepo: owner=[{}], repo=[{}], result=[{}]", owner, repo, result);

        return result;
    }

    /**
     * Determines whether prereleases should be included based on configuration.
     * For non-official repos, always includes prereleases (they don't have stable releases).
     */
    protected static boolean shouldIncludePrereleases() {
        boolean isOfficial = isOfficialCddaRepo();
        boolean configValue = ConfigurationManager.getInstance().isIncludePreReleaseBuilds();

        LOGGER.trace("shouldIncludePrereleases: isOfficialCddaRepo=[{}], configIncludePreReleases=[{}]", isOfficial, configValue);

        if (!isOfficial) {
            // non-official repos are all experimental, always use latest
            LOGGER.debug("Not official repo, including all prereleases...");
            return true;
        }

        LOGGER.debug("Official repo, returning config value: {}", configValue);
        return configValue;
    }

    /**
     * Queries GitHub API for latest release tag.
     * Uses /releases/latest for stable releases, /releases for prereleases.
     */
    protected static String getLatestReleaseTagFromGithub(String owner, String repo) throws IOException {
        boolean includePrereleases = shouldIncludePrereleases();

        // Use different endpoints based on whether we want stable or prerelease
        String apiUrl;
        if (includePrereleases) {
            // Get all releases (first one is latest, including prereleases)
            apiUrl = GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/releases";
        } else {
            // Use /releases/latest which returns latest non-prerelease, non-draft release
            apiUrl = GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/releases/latest";
        }

        LOGGER.debug("Fetching releases from: [{}] (includePrereleases={})", apiUrl, includePrereleases);

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        int responseCode = connection.getResponseCode();
        if (responseCode == 404 && !includePrereleases) {
            // No stable release found, fall back to latest prerelease
            LOGGER.warn("No stable release found (404), falling back to latest prerelease");
            connection.disconnect();
            return getLatestPrereleaseTag(owner, repo);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();

        String jsonResponse = response.toString();

        // parse JSON to find tag_name
        int tagIdx = jsonResponse.indexOf("\"tag_name\":\"");
        if (tagIdx == -1) {
            throw new IOException("No tag_name found in response");
        }
        int tagStart = tagIdx + "\"tag_name\":\"".length();
        int tagEnd = jsonResponse.indexOf("\"", tagStart);
        String tagName = jsonResponse.substring(tagStart, tagEnd);

        LOGGER.debug("Found release tag: [{}]", tagName);
        return tagName;
    }

    /**
     * Gets the latest prerelease tag (fallback when no stable release exists).
     */
    protected static String getLatestPrereleaseTag(String owner, String repo) throws IOException {
        String apiUrl = GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/releases";

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();

        String jsonResponse = response.toString();

        int tagIdx = jsonResponse.indexOf("\"tag_name\":\"");
        if (tagIdx == -1) {
            throw new IOException("No releases found in repository");
        }
        int tagStart = tagIdx + "\"tag_name\":\"".length();
        int tagEnd = jsonResponse.indexOf("\"", tagStart);
        return jsonResponse.substring(tagStart, tagEnd);
    }

    /**
     * Finds the macOS asset download URL from the appropriate release.
     * Uses /releases/latest for stable releases, /releases for prereleases.
     */
    protected static String findMacOsAssetUrl(String owner, String repo) throws IOException {
        boolean includePrereleases = shouldIncludePrereleases();

        // Use different endpoints based on whether we want stable or prerelease
        String apiUrl;
        if (includePrereleases) {
            apiUrl = GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/releases";
        } else {
            apiUrl = GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/releases/latest";
        }

        LOGGER.debug("Fetching assets from: {}", apiUrl);

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        int responseCode = connection.getResponseCode();
        if (responseCode == 404 && !includePrereleases) {
            // No stable release found, fall back to latest prerelease
            LOGGER.warn("No stable release found (404), falling back to prerelease assets");
            connection.disconnect();
            return findMacOsAssetUrlFromPrereleases(owner, repo);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();

        String json = response.toString();

        // For /releases endpoint, we need to extract just the first release's assets
        // For /releases/latest, the entire response is the release object
        String assetsSection;
        if (includePrereleases) {
            // Extract first release's assets section
            int assetsIdx = json.indexOf("\"assets\":");
            if (assetsIdx == -1) {
                throw new IOException("No assets found in release");
            }
            int nextReleaseIdx = json.indexOf("\"tag_name\":", assetsIdx + 1);
            assetsSection = nextReleaseIdx > 0 ? json.substring(assetsIdx, nextReleaseIdx) : json.substring(assetsIdx);
        } else {
            // The entire response is the release object
            assetsSection = json;
        }

        return findLargestMacOsAsset(assetsSection);
    }

    /**
     * Finds the largest macOS asset from the assets JSON section.
     * The tiles version is always larger than the curses version due to included graphics.
     *
     * @param assetsSection JSON section containing asset information
     * @return the download URL for the largest macOS asset
     * @throws IOException if no macOS asset is found
     */
    protected static String findLargestMacOsAsset(String assetsSection) throws IOException {
        String[] macPatterns = {"osx", "macos", "mac", "darwin", "apple"};

        String largestUrl = null;
        long largestSize = 0;

        // Parse each asset in the section
        // Assets have format: {"name":"...", "size":12345, ..., "browser_download_url":"..."}
        int searchIdx = 0;
        while (true) {
            // Find the next asset object (look for "name":" as indicator)
            int nameIdx = assetsSection.indexOf("\"name\":\"", searchIdx);
            if (nameIdx == -1) {
                break;
            }

            // Extract asset name
            int nameStart = nameIdx + "\"name\":\"".length();
            int nameEnd = assetsSection.indexOf("\"", nameStart);
            String assetName = assetsSection.substring(nameStart, nameEnd).toLowerCase();

            // Check if this is a macOS asset
            boolean isMacOs = false;
            for (String pattern : macPatterns) {
                if (assetName.contains(pattern)) {
                    isMacOs = true;
                    break;
                }
            }

            // Must be .dmg or .zip, not .sha256 or other
            if (isMacOs && (assetName.endsWith(".dmg") || assetName.endsWith(".zip"))) {
                // Find the size for this asset
                int sizeIdx = assetsSection.indexOf("\"size\":", nameEnd);
                int downloadUrlIdx = assetsSection.indexOf("\"browser_download_url\":\"", nameEnd);

                // Make sure we're still in the same asset object
                if (sizeIdx != -1 && downloadUrlIdx != -1) {
                    int sizeStart = sizeIdx + "\"size\":".length();
                    int sizeEnd = assetsSection.indexOf(",", sizeStart);
                    if (sizeEnd == -1 || sizeEnd > downloadUrlIdx) {
                        sizeEnd = assetsSection.indexOf("}", sizeStart);
                    }
                    String sizeStr = assetsSection.substring(sizeStart, sizeEnd).trim();
                    long size = Long.parseLong(sizeStr);

                    int urlStart = downloadUrlIdx + "\"browser_download_url\":\"".length();
                    int urlEnd = assetsSection.indexOf("\"", urlStart);
                    String downloadUrl = assetsSection.substring(urlStart, urlEnd);

                    LOGGER.debug("Found macOS asset: {} (size: {} bytes)", assetName, size);

                    if (size > largestSize) {
                        largestSize = size;
                        largestUrl = downloadUrl;
                    }
                }
            }

            searchIdx = nameEnd;
        }

        if (largestUrl == null) {
            throw new IOException("No macOS download found in release assets");
        }

        LOGGER.info("Selected largest macOS asset: {} ({} bytes)", largestUrl, largestSize);
        return largestUrl;
    }

    /**
     * Fallback method to find macOS asset URL from prereleases when no stable release exists.
     */
    protected static String findMacOsAssetUrlFromPrereleases(String owner, String repo) throws IOException {
        String apiUrl = GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/releases";

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();

        String json = response.toString();

        // Extract first release's assets section
        int assetsIdx = json.indexOf("\"assets\":");
        if (assetsIdx == -1) {
            throw new IOException("No assets found in releases");
        }
        int nextReleaseIdx = json.indexOf("\"tag_name\":", assetsIdx + 1);
        String assetsSection = nextReleaseIdx > 0 ? json.substring(assetsIdx, nextReleaseIdx) : json.substring(assetsIdx);

        return findLargestMacOsAsset(assetsSection);
    }

    /**
     * Opens a GitHub release page in the browser.
     */
    private static void openGithubReleaseInDefaultBrowser(String owner, String repo, String tag) throws IOException {
        String releaseUrl = String.format("https://github.com/%s/%s/releases/tag/%s", owner, repo, tag);

        Desktop desktop = Desktop.getDesktop();

        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(java.net.URI.create(releaseUrl));
        }
    }

    /**
     * Resolve the {@code hdiutil} executable to an absolute path where possible.
     */
    private static String getHdiutilPath() {
        return getNormalizedPath("hdiutil");
    }

    /**
     * Resolve the {@code ditto} executable to an absolute path where possible.
     */
    private static String getDittoPath() {
        return getNormalizedPath("ditto");
    }

    /**
     * Resolve the {@code xattr} executable to an absolute path where possible.
     */
    private static String getXattrPath() {
        return getNormalizedPath("xattr");
    }

    /**
     * Resolve the {@code chmod} executable to an absolute path where possible.
     */
    private static String getChmodPath() {
        return getNormalizedPath("chmod");
    }

    /**
     * Resolve a binary name to a normalized absolute path when possible.
     *
     * <p>This method checks the conventional macOS location {@code /usr/bin/<name>} and returns its absolute path if that
     * file exists and is executable. If the standard location is not present or not executable, the original {@code path} (binary name)
     * is returned so the system PATH can be relied upon at runtime.</p>
     *
     * @param path the binary name (for example, {@code "hdiutil"} or {@code "ditto"})
     * @return an absolute path to the binary if found in /usr/bin and executable, otherwise the
     *         original {@code path} value to be resolved via the environment PATH
     */
    private static String getNormalizedPath(String path) {
        Path binPath = java.nio.file.Paths.get("/usr/bin", path); // standard location of binaries on macOS

        if (Files.isRegularFile(binPath) && Files.isExecutable(binPath)) {
            return binPath.toAbsolutePath().toString();
        }

        return path; // fallback to relying on PATH if the standard location is not usable
    }
}
