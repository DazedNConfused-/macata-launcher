package com.dazednconfused.catalauncher.update;

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
     * Checks if the game update feature is properly configured.
     *
     * @return {@code true} if both repo owner and name are configured
     */
    public static boolean isConfigured() {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();
        return !isBlank(owner) && !isBlank(repo);
    }

    /**
     * Checks if the installed version is configured.
     *
     * @return {@code true} if installed version is set
     */
    public static boolean isInstalledVersionConfigured() {
        String installedVersion = ConfigurationManager.getInstance().getInstalledGameVersion();
        return !isBlank(installedVersion);
    }

    /**
     * Checks if the CDDA path is configured.
     *
     * @return {@code true} if CDDA path is set
     */
    public static boolean isCddaPathConfigured() {
        String cddaPath = ConfigurationManager.getInstance().getCddaPath();
        return !isBlank(cddaPath);
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
        if (isBlank(installedVersionStr)) {
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

        if (isBlank(owner) || isBlank(repo)) {
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

        if (isBlank(owner) || isBlank(repo)) {
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
            if (isBlank(cddaPath)) {
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
     * Opens the releases page in the default browser.
     */
    public static void openReleasesPageInDefaultBrowser() {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();

        if (isBlank(owner) || isBlank(repo)) {
            LOGGER.warn("Cannot open releases page: repository not configured");
            return;
        }

        String releasesUrl = String.format("https://github.com/%s/%s/releases", owner, repo);
        LOGGER.info("Opening releases page [{}]...", releasesUrl);

        Try.run(() -> {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(java.net.URI.create(releasesUrl));
            }
        }).onFailure(t -> LOGGER.error("Error opening releases page", t));
    }

    /**
     * Downloads a file from URL to destination with progress reporting.
     */
    private static boolean downloadFile(String urlString, File destination, Consumer<Integer> progressCallback) {
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
    private static File extractGameBinary(File archive, File extractDir) {
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
    private static File findAppBundle(File directory) {
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
                "hdiutil", "attach", dmgFile.getAbsolutePath(), "-nobrowse"
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
                "ditto", appBundle.getAbsolutePath(), destApp.getAbsolutePath()
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
                "xattr", "-cr", destApp.getAbsolutePath()
            );
            xattrPb.start().waitFor();
            LOGGER.debug("Cleared extended attributes from: {}", destApp.getAbsolutePath());

            // ensure executable permissions on the main binaries ---
            ProcessBuilder chmodMacOsPb = new ProcessBuilder(
                "chmod", "-R", "+x", destApp.getAbsolutePath() + "/Contents/MacOS"
            );
            chmodMacOsPb.start().waitFor();
            LOGGER.debug("Set executable permissions on: {}/Contents/MacOS", destApp.getAbsolutePath());

            // also set executable permissions on Resources where game binaries typically reside ---
            File resourcesDir = new File(destApp, "Contents/Resources");
            if (resourcesDir.exists()) {
                ProcessBuilder chmodResourcesPb = new ProcessBuilder(
                    "chmod", "-R", "+x", resourcesDir.getAbsolutePath()
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
                        "hdiutil", "detach", mountPoint, "-quiet", "-force"
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
    private static boolean moveToTrash(File file) {
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
    private static String extractFileName(String url) {
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
     * Queries GitHub API for latest release tag.
     */
    private static String getLatestReleaseTagFromGithub(String owner, String repo) throws IOException {
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/releases";

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

        // parse JSON to find first non-prerelease tag
        int idx = 0;
        String firstTag = null;
        while (true) {
            int tagIdx = jsonResponse.indexOf("\"tag_name\":\"", idx);
            if (tagIdx == -1) {
                break;
            }
            int tagStart = tagIdx + "\"tag_name\":\"".length();
            int tagEnd = jsonResponse.indexOf("\"", tagStart);
            String tagName = jsonResponse.substring(tagStart, tagEnd);

            if (firstTag == null) {
                firstTag = tagName;
            }

            int prereleaseIdx = jsonResponse.indexOf("\"prerelease\":", tagEnd);
            if (prereleaseIdx == -1) {
                break;
            }
            int prereleaseValueStart = prereleaseIdx + "\"prerelease\":".length();
            int prereleaseValueEnd = jsonResponse.indexOf(",", prereleaseValueStart);
            if (prereleaseValueEnd == -1) {
                prereleaseValueEnd = jsonResponse.indexOf("}", prereleaseValueStart);
            }
            String prereleaseValue = jsonResponse
                .substring(prereleaseValueStart, prereleaseValueEnd).trim();

            if (!prereleaseValue.equals("true")) {
                return tagName;
            }
            idx = tagEnd;
        }

        if (firstTag != null) {
            return firstTag;
        }

        throw new IOException("No releases found in repository");
    }

    /**
     * Finds the macOS asset download URL from the latest release.
     */
    private static String findMacOsAssetUrl(String owner, String repo) throws IOException {
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/releases";

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

        // find the first release's assets section
        int assetsIdx = json.indexOf("\"assets\":");
        if (assetsIdx == -1) {
            throw new IOException("No assets found in release");
        }

        // look for macOS-related asset names
        String[] macPatterns = {
            "osx", "macos", "mac", "darwin", "apple"
        };

        int nextReleaseIdx = json.indexOf("\"tag_name\":", assetsIdx + 1);
        String assetsSection = nextReleaseIdx > 0 ? json.substring(assetsIdx, nextReleaseIdx) : json.substring(assetsIdx);

        // collect all macOS asset URLs
        java.util.List<String> macOsUrls = new java.util.ArrayList<>();

        int urlIdx = 0;
        while (true) {
            int downloadUrlIdx = assetsSection.indexOf("\"browser_download_url\":\"", urlIdx);
            if (downloadUrlIdx == -1) {
                break;
            }

            int urlStart = downloadUrlIdx + "\"browser_download_url\":\"".length();
            int urlEnd = assetsSection.indexOf("\"", urlStart);
            String downloadUrl = assetsSection.substring(urlStart, urlEnd);
            String lowerUrl = downloadUrl.toLowerCase();

            // check if this is a macOS asset
            for (String pattern : macPatterns) {
                if (lowerUrl.contains(pattern)) {
                    // must be .dmg or .zip, avoid .sha256 etc
                    if (lowerUrl.endsWith(".dmg") || lowerUrl.endsWith(".zip")) {
                        macOsUrls.add(downloadUrl);
                    }
                    break;
                }
            }

            urlIdx = urlEnd;
        }

        if (macOsUrls.isEmpty()) {
            throw new IOException("No macOS download found in release assets");
        }

        // prefer tiles version over curses version
        for (String macOsUrl : macOsUrls) {
            if (macOsUrl.toLowerCase().contains("tiles")) {
                LOGGER.debug("Found macOS tiles asset: {}", macOsUrl);
                return macOsUrl;
            }
        }

        // fallback to first macOS asset if no tiles-specific one found
        LOGGER.debug("Found macOS asset (no tiles variant): {}", macOsUrls.get(0));
        return macOsUrls.get(0);
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
     * Checks if a string is null or blank.
     */
    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
