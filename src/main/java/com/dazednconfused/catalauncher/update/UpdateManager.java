package com.dazednconfused.catalauncher.update;

import static com.dazednconfused.catalauncher.helper.Constants.GITHUB_REPOSITORY_NAME;
import static com.dazednconfused.catalauncher.helper.Constants.GITHUB_REPOSITORY_OWNER;
import static java.util.function.Predicate.not;

import com.dazednconfused.catalauncher.helper.GitInfoManager;

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

public class UpdateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateManager.class);

    private static UpdateManager instance;
    private static final Object lock = new Object(); //thread-safety singleton lock

    //Singleton
    public static UpdateManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) instance = new UpdateManager();
            }
        }
        return instance;
    }

    //Constructor
    private UpdateManager() {
    }

    /**
     * Determines if a software update is available to the user to be downloaded.
     * */
    public boolean isUpdateAvailable() {
        Optional<String> currentVersion = Optional.ofNullable(GitInfoManager.getInstance().getBuildVersion()).filter(not(String::isBlank));
        Optional<Version> latestVersionAvailable = getLatestReleaseTag();

        if (currentVersion.isEmpty() || latestVersionAvailable.isEmpty()) {
            LOGGER.info("Could not gather all the required information to determine if an update should be carried out or not.");
            LOGGER.debug("isUpdateAvailable defaulting to false. currentVersion=[{}]; latestVersionAvailable=[{}]", currentVersion, latestVersionAvailable);
            return false;
        }

        Version current = new Version(currentVersion.get());
        Version latest = latestVersionAvailable.get();

        return latest.compareTo(current) > 0;
    }

    /**
     * Opens the webpage for the latest binary release in the official remote repository.
     *
     * @implNote Shortcut operation that chains both {@link #getLatestReleaseTag()} and {@link #openReleaseInDefaultBrowser(Version)}.
     * */
    public static void openLatestReleaseInDefaultBrowser() {
        getLatestReleaseTag().ifPresent(UpdateManager::openReleaseInDefaultBrowser);
    }

    /**
     * Opens the webpage for the given binary release in the official remote repository.
     * */
    public static void openReleaseInDefaultBrowser(Version tag) {
        LOGGER.info("Opening v[{}]'s release's homepage using default browser...", tag);

        Try.run(() -> openGithubReleaseInDefaultBrowser(GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME, tag.get()))
            .onFailure(t -> LOGGER.error("There was an error opening the latest release tagged [{}] in remote repository [{}/{}]", tag, GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME, t));
    }

    /**
     * Queries for the latest binary release's tag available to the public in the official remote repository.
     * */
    public static Optional<Version> getLatestReleaseTag() {
        LOGGER.info("Querying latest release's tag from internet repository...");

        return Try.of(() -> getLatestReleaseTagFromGithub(GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME))
            .map(Version::new)
            .onFailure(t -> LOGGER.error("There was an error retrieving the latest release from remote repository [{}/{}]", GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME, t))
            .toJavaOptional();
    }

    /**
     * Queries the remote GitHub repository in search for the latest release for the given {@code owner}/{@code repo} combination.
     * */
    private static String getLatestReleaseTagFromGithub(String owner, String repo) throws IOException {
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/releases";

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();

        // Parse JSON response to get the latest release name (including pre-releases)
        String jsonResponse = response.toString();
        return jsonResponse.split("\"tag_name\":\"")[1].split("\",")[0];
    }

    /**
     * Opens the page for the remote GitHub repository in search for the latest release for the given {@code owner}/{@code repo}/{@code tag}
     * combination.
     * */
    private static void openGithubReleaseInDefaultBrowser(String owner, String repo, String tag) throws IOException {
        String latestReleaseUrl = String.format("https://github.com/%s/%s/releases/tag/%s", owner, repo, tag);

        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(java.net.URI.create(latestReleaseUrl));
        }
    }
}
