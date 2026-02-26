package com.dazednconfused.catalauncher.update;

import static java.util.function.Predicate.not;

import com.dazednconfused.catalauncher.configuration.ConfigurationManager;

import io.vavr.control.Try;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages game update checking functionality by querying GitHub releases for user-configured repositories.
 */
public class GameUpdateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameUpdateManager.class);

    /**
     * Checks if the game update feature is properly configured.
     *
     * @return {@code true} if both repo owner and name are configured, {@code false} otherwise
     */
    public static boolean isConfigured() {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();
        return !isBlank(owner) && !isBlank(repo);
    }

    /**
     * Checks if the installed version is configured.
     *
     * @return {@code true} if installed version is set, {@code false} otherwise
     */
    public static boolean isInstalledVersionConfigured() {
        String installedVersion = ConfigurationManager.getInstance().getInstalledGameVersion();
        return !isBlank(installedVersion);
    }

    /**
     * Determines if a game update is available.
     *
     * @return {@link Optional#empty()} if configuration is incomplete or an error occurs,
     *         otherwise {@code Optional.of(true)} if update available, {@code Optional.of(false)} if up to date
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

            LOGGER.debug("Comparing installed version [{}] with latest version [{}]", installed, latest);
            return Optional.of(latest.compareTo(installed) > 0);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not parse installed version [{}]: {}", installedVersionStr, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Queries for the latest game release tag from the configured GitHub repository.
     *
     * @return the latest release version, or {@link Optional#empty()} if not found or on error
     */
    public static Optional<GameVersion> getLatestGameReleaseTag() {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();

        if (isBlank(owner) || isBlank(repo)) {
            LOGGER.warn("Cannot query game releases: repository not configured");
            return Optional.empty();
        }

        LOGGER.info("Querying latest game release from [{}/{}]...", owner, repo);

        return Try.of(() -> getLatestReleaseTagFromGithub(owner, repo))
            .map(GameVersion::new)
            .onFailure(t -> LOGGER.error("Error retrieving latest game release from [{}/{}]", owner, repo, t))
            .toJavaOptional();
    }

    /**
     * Opens the latest game release page in the default browser.
     */
    public static void openLatestGameReleaseInDefaultBrowser() {
        getLatestGameReleaseTag().ifPresent(GameUpdateManager::openReleaseInDefaultBrowser);
    }

    /**
     * Opens the specified release's page in the default browser.
     *
     * @param version the release version to open
     */
    public static void openReleaseInDefaultBrowser(GameVersion version) {
        String owner = ConfigurationManager.getInstance().getGameGithubRepoOwner();
        String repo = ConfigurationManager.getInstance().getGameGithubRepoName();

        LOGGER.info("Opening [{}]'s release page using default browser...", version);

        Try.run(() -> openGithubReleaseInDefaultBrowser(owner, repo, version.toString()))
            .onFailure(t -> LOGGER.error("Error opening release [{}] for [{}/{}]", version, owner, repo, t));
    }

    /**
     * Opens the releases page (not a specific tag) in the default browser.
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
        }).onFailure(t -> LOGGER.error("Error opening releases page for [{}/{}]", owner, repo, t));
    }

    /**
     * Queries the GitHub API for the latest release tag.
     */
    private static String getLatestReleaseTagFromGithub(String owner, String repo) throws IOException {
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/releases";

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();

        String jsonResponse = response.toString();

        // Parse JSON to find the first non-prerelease tag (or first tag if all are prereleases)
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
            String prereleaseValue = jsonResponse.substring(prereleaseValueStart, prereleaseValueEnd).trim();

            boolean isPreRelease = prereleaseValue.equals("true");
            if (!isPreRelease) {
                return tagName;
            }
            idx = tagEnd;
        }

        // If no non-prerelease found, return the first tag
        if (firstTag != null) {
            return firstTag;
        }

        throw new IOException("No releases found in repository");
    }

    /**
     * Opens a specific GitHub release page in the default browser.
     */
    private static void openGithubReleaseInDefaultBrowser(String owner, String repo, String tag) throws IOException {
        String latestReleaseUrl = String.format("https://github.com/%s/%s/releases/tag/%s", owner, repo, tag);

        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(java.net.URI.create(latestReleaseUrl));
        }
    }

    /**
     * Checks if a string is null or blank.
     */
    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
