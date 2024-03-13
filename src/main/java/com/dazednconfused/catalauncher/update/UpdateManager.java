package com.dazednconfused.catalauncher.update;

import static com.dazednconfused.catalauncher.helper.Constants.GITHUB_REPOSITORY_NAME;
import static com.dazednconfused.catalauncher.helper.Constants.GITHUB_REPOSITORY_OWNER;

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

    /**
     * Queries the latest binary release available to the public in the official remote repository.
     * */
    public static void openReleaseInDefaultBrowser(String tag) {
        Try.run(() -> openGithubReleaseInDefaultBrowser(GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME, tag))
            .onFailure(t -> LOGGER.error("There was an error opening the latest release tagged [{}] in remote repository [{}/{}]", tag, GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME, t));
    }

    /**
     * Queries the latest binary release available to the public in the official remote repository.
     * */
    public static Optional<String> getLatestRelease() {
        return Try.of(() -> getLatestReleaseFromGithub(GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME))
            .onFailure(t -> LOGGER.error("There was an error retrieving the latest release from remote repository [{}/{}]", GITHUB_REPOSITORY_OWNER, GITHUB_REPOSITORY_NAME, t))
            .toJavaOptional();
    }

    /**
     * Queries the remote GitHub repository in search for the latest release from the given {@code owner}/{@code repo} combination.
     * */
    private static String getLatestReleaseFromGithub(String owner, String repo) throws IOException {
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
     * Opens the page for the remote GitHub repository in search for the latest release from the given {@code owner}/{@code repo}/{@code tag}
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
