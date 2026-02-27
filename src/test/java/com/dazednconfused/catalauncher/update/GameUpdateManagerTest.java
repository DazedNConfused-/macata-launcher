package com.dazednconfused.catalauncher.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.dazednconfused.catalauncher.configuration.ConfigurationManager;
import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.utils.TestUtils;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class GameUpdateManagerTest {

    private HttpServer mockServer;
    private int serverPort;
    private String serverBaseUrl;
    private String originalGithubApiBaseUrl;

    @BeforeEach
    void setUp() throws IOException {
        // save original GitHub API URL ---
        originalGithubApiBaseUrl = GameUpdateManager.GITHUB_API_URL;

        // start a simple HTTP server for testing HTTP methods ---
        mockServer = HttpServer.create(new InetSocketAddress(0), 0);
        serverPort = mockServer.getAddress().getPort();
        serverBaseUrl = "http://localhost:" + serverPort;
        mockServer.start();

        // redirect GitHub API calls to mock server ---
        GameUpdateManager.GITHUB_API_URL = serverBaseUrl;
    }

    @AfterEach
    void tearDown() {
        // restore original GitHub API URL ---
        GameUpdateManager.GITHUB_API_URL = originalGithubApiBaseUrl;

        if (mockServer != null) {
            mockServer.stop(0);
        }
    }

    // ========================================
    // isConfigured() tests
    // ========================================

    @Test
    void isConfigured_returns_true_when_both_owner_and_repo_set_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            // execute test ---
            boolean result = GameUpdateManager.isConfigured();

            // verify assertions ---
            assertThat(result).isTrue();
        }
    }

    @Test
    void isConfigured_returns_false_when_owner_null_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn(null);
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            // execute test ---
            boolean result = GameUpdateManager.isConfigured();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isConfigured_returns_false_when_repo_null_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn(null);

            // execute test ---
            boolean result = GameUpdateManager.isConfigured();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isConfigured_returns_false_when_owner_blank_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("   ");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            // execute test ---
            boolean result = GameUpdateManager.isConfigured();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isConfigured_returns_false_when_repo_empty_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("");

            // execute test ---
            boolean result = GameUpdateManager.isConfigured();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isConfigured_returns_false_when_both_null_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn(null);
            when(mockConfig.getGameGithubRepoName()).thenReturn(null);

            // execute test ---
            boolean result = GameUpdateManager.isConfigured();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    // ========================================
    // isInstalledVersionConfigured() tests
    // ========================================

    @Test
    void isInstalledVersionConfigured_returns_true_when_version_set_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getInstalledGameVersion()).thenReturn("0.G");

            // execute test ---
            boolean result = GameUpdateManager.isInstalledVersionConfigured();

            // verify assertions ---
            assertThat(result).isTrue();
        }
    }

    @Test
    void isInstalledVersionConfigured_returns_false_when_version_null_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getInstalledGameVersion()).thenReturn(null);

            // execute test ---
            boolean result = GameUpdateManager.isInstalledVersionConfigured();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isInstalledVersionConfigured_returns_false_when_version_blank_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getInstalledGameVersion()).thenReturn("   ");

            // execute test ---
            boolean result = GameUpdateManager.isInstalledVersionConfigured();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    // ========================================
    // isCddaPathConfigured() tests
    // ========================================

    @Test
    void isCddaPathConfigured_returns_true_when_path_set_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getCddaPath()).thenReturn("/Applications/Cataclysm.app");

            // execute test ---
            boolean result = GameUpdateManager.isCddaPathConfigured();

            // verify assertions ---
            assertThat(result).isTrue();
        }
    }

    @Test
    void isCddaPathConfigured_returns_false_when_path_null_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getCddaPath()).thenReturn(null);

            // execute test ---
            boolean result = GameUpdateManager.isCddaPathConfigured();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isCddaPathConfigured_returns_false_when_path_blank_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getCddaPath()).thenReturn("   ");

            // execute test ---
            boolean result = GameUpdateManager.isCddaPathConfigured();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    // ========================================
    // isOfficialCddaRepo() tests
    // ========================================

    @Test
    void isOfficialCddaRepo_returns_true_for_official_repo_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            // execute test ---
            boolean result = GameUpdateManager.isOfficialCddaRepo();

            // verify assertions ---
            assertThat(result).isTrue();
        }
    }

    @Test
    void isOfficialCddaRepo_returns_true_case_insensitive_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("cleverraven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("cataclysm-dda");

            // execute test ---
            boolean result = GameUpdateManager.isOfficialCddaRepo();

            // verify assertions ---
            assertThat(result).isTrue();
        }
    }

    @Test
    void isOfficialCddaRepo_returns_false_for_different_owner_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("SomeOtherUser");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            // execute test ---
            boolean result = GameUpdateManager.isOfficialCddaRepo();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isOfficialCddaRepo_returns_false_when_null_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn(null);
            when(mockConfig.getGameGithubRepoName()).thenReturn(null);

            // execute test ---
            boolean result = GameUpdateManager.isOfficialCddaRepo();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    // ========================================
    // shouldIncludePrereleases() tests
    // ========================================

    @Test
    void shouldIncludePrereleases_returns_true_for_non_official_repo_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("SomeUser");
            when(mockConfig.getGameGithubRepoName()).thenReturn("SomeRepo");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(false);

            // execute test ---
            boolean result = GameUpdateManager.shouldIncludePrereleases();

            // verify assertions ---
            assertThat(result).isTrue();
        }
    }

    @Test
    void shouldIncludePrereleases_returns_config_value_for_official_repo_true_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(true);

            // execute test ---
            boolean result = GameUpdateManager.shouldIncludePrereleases();

            // verify assertions ---
            assertThat(result).isTrue();
        }
    }

    @Test
    void shouldIncludePrereleases_returns_config_value_for_official_repo_false_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(false);

            // execute test ---
            boolean result = GameUpdateManager.shouldIncludePrereleases();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    // ========================================
    // getLatestReleaseTagFromGithub() tests
    // ========================================

    @Test
    void getLatestReleaseTagFromGithub_parses_stable_release_from_latest_endpoint_success() throws Exception {
        // prepare mock data ---
        // Use official repo name so isOfficialCddaRepo() returns true and /releases/latest is used
        String jsonResponse = "{\"tag_name\":\"0.H\",\"prerelease\":false,\"name\":\"0.H Release\"}";
        mockServer.createContext("/repos/CleverRaven/Cataclysm-DDA/releases/latest", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(false);

            // execute test ---
            String result = GameUpdateManager.getLatestReleaseTagFromGithub("CleverRaven", "Cataclysm-DDA");

            // verify assertions ---
            assertThat(result).isEqualTo("0.H");
        }
    }

    @Test
    void getLatestReleaseTagFromGithub_parses_prerelease_from_releases_endpoint_success() throws Exception {
        // prepare mock data ---
        String jsonResponse = "[{\"tag_name\":\"cdda-experimental-2024-02-01-0100\",\"prerelease\":true}]";
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("TestOwner");
            when(mockConfig.getGameGithubRepoName()).thenReturn("TestRepo");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(true);

            // execute test ---
            String result = GameUpdateManager.getLatestReleaseTagFromGithub("TestOwner", "TestRepo");

            // verify assertions ---
            assertThat(result).isEqualTo("cdda-experimental-2024-02-01-0100");
        }
    }

    @Test
    void getLatestReleaseTagFromGithub_falls_back_to_prerelease_on_404_success() throws Exception {
        // prepare mock data - /releases/latest returns 404, /releases returns data ---
        // Use official repo so it tries /releases/latest first
        mockServer.createContext("/repos/CleverRaven/Cataclysm-DDA/releases/latest", exchange -> {
            exchange.sendResponseHeaders(404, -1);
        });

        String releasesResponse = "[{\"tag_name\":\"v2.0.0-beta\",\"prerelease\":true}]";
        mockServer.createContext("/repos/CleverRaven/Cataclysm-DDA/releases", exchange -> {
            byte[] response = releasesResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(false);

            // execute test ---
            String result = GameUpdateManager.getLatestReleaseTagFromGithub("CleverRaven", "Cataclysm-DDA");

            // verify assertions - should fall back to prerelease ---
            assertThat(result).isEqualTo("v2.0.0-beta");
        }
    }

    @Test
    void getLatestReleaseTagFromGithub_throws_when_no_tag_found_success() throws Exception {
        // prepare mock data - response with no tag_name ---
        // Use official repo so it uses /releases/latest endpoint
        mockServer.createContext("/repos/CleverRaven/Cataclysm-DDA/releases/latest", exchange -> {
            String response = "{\"name\":\"Some Release\",\"prerelease\":false}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(false);

            // execute test ---
            assertThatThrownBy(() -> GameUpdateManager.getLatestReleaseTagFromGithub("CleverRaven", "Cataclysm-DDA"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("No tag_name found");
        }
    }

    @Test
    void getLatestReleaseTagFromGithub_parses_complex_tag_name_success() throws Exception {
        // prepare mock data with complex tag name ---
        // Use official repo so it uses /releases/latest endpoint
        String jsonResponse = "{\"tag_name\":\"cdda-experimental-2024-01-15-0123\",\"prerelease\":true}";
        mockServer.createContext("/repos/CleverRaven/Cataclysm-DDA/releases/latest", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(false);

            // execute test ---
            String result = GameUpdateManager.getLatestReleaseTagFromGithub("CleverRaven", "Cataclysm-DDA");

            // verify assertions ---
            assertThat(result).isEqualTo("cdda-experimental-2024-01-15-0123");
        }
    }

    // ========================================
    // getLatestPrereleaseTag() tests
    // ========================================

    @Test
    void getLatestPrereleaseTag_returns_first_tag_from_releases_success() throws Exception {
        // prepare mock data ---
        String jsonResponse = "[{\"tag_name\":\"v2.0.0-beta\",\"prerelease\":true},{\"tag_name\":\"v1.0.0\",\"prerelease\":false}]";
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        // execute test ---
        String result = GameUpdateManager.getLatestPrereleaseTag("TestOwner", "TestRepo");

        // verify assertions ---
        assertThat(result).isEqualTo("v2.0.0-beta");
    }

    @Test
    void getLatestPrereleaseTag_returns_experimental_tag_success() throws Exception {
        // prepare mock data ---
        String jsonResponse = "[{\"tag_name\":\"cdda-experimental-2024-02-15-0200\",\"prerelease\":true}]";
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        // execute test ---
        String result = GameUpdateManager.getLatestPrereleaseTag("TestOwner", "TestRepo");

        // verify assertions ---
        assertThat(result).isEqualTo("cdda-experimental-2024-02-15-0200");
        assertThat(new GameVersion(result).isExperimental()).isTrue();
    }

    @Test
    void getLatestPrereleaseTag_throws_when_no_releases_found_success() throws Exception {
        // prepare mock data - empty releases array ---
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            String response = "[]";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        // execute test ---
        assertThatThrownBy(() -> GameUpdateManager.getLatestPrereleaseTag("TestOwner", "TestRepo"))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("No releases found");
    }

    @Test
    void getLatestPrereleaseTag_throws_on_connection_error_success() {
        // Don't create any context - server will return 404
        assertThatThrownBy(() -> GameUpdateManager.getLatestPrereleaseTag("NonExistent", "Repo"))
            .isInstanceOf(IOException.class);
    }

    // ========================================
    // findMacOsAssetUrlFromPrereleases() tests
    // ========================================

    @Test
    void findMacOsAssetUrlFromPrereleases_returns_largest_macos_asset_success() throws Exception {
        // prepare mock data ---
        String jsonResponse = "[{\"tag_name\":\"v2.0.0\",\"assets\":[" +
            "{\"name\":\"game-osx-curses.dmg\",\"size\":50000000,\"browser_download_url\":\"https://example.com/curses.dmg\"}," +
            "{\"name\":\"game-osx-tiles.dmg\",\"size\":150000000,\"browser_download_url\":\"https://example.com/tiles.dmg\"}" +
            "]}]";
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        // execute test ---
        String result = GameUpdateManager.findMacOsAssetUrlFromPrereleases("TestOwner", "TestRepo");

        // verify assertions - should select the larger tiles version ---
        assertThat(result).isEqualTo("https://example.com/tiles.dmg");
    }

    @Test
    void findMacOsAssetUrlFromPrereleases_recognizes_darwin_pattern_success() throws Exception {
        // prepare mock data ---
        String jsonResponse = "[{\"tag_name\":\"v2.0.0\",\"assets\":[" +
            "{\"name\":\"game-darwin-x64.zip\",\"size\":120000000,\"browser_download_url\":\"https://example.com/darwin.zip\"}" +
            "]}]";
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        // execute test ---
        String result = GameUpdateManager.findMacOsAssetUrlFromPrereleases("TestOwner", "TestRepo");

        // verify assertions ---
        assertThat(result).isEqualTo("https://example.com/darwin.zip");
    }

    @Test
    void findMacOsAssetUrlFromPrereleases_throws_when_no_assets_found_success() throws Exception {
        // prepare mock data - releases with no assets ---
        String jsonResponse = "[{\"tag_name\":\"v2.0.0\",\"assets\":[]}]";
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        // execute test ---
        assertThatThrownBy(() -> GameUpdateManager.findMacOsAssetUrlFromPrereleases("TestOwner", "TestRepo"))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("No macOS download found");
    }

    @Test
    void findMacOsAssetUrlFromPrereleases_throws_when_no_macos_assets_success() throws Exception {
        // prepare mock data - releases with only Linux/Windows assets ---
        String jsonResponse = "[{\"tag_name\":\"v2.0.0\",\"assets\":[" +
            "{\"name\":\"game-linux.tar.gz\",\"size\":100000000,\"browser_download_url\":\"https://example.com/linux.tar.gz\"}," +
            "{\"name\":\"game-windows.zip\",\"size\":110000000,\"browser_download_url\":\"https://example.com/windows.zip\"}" +
            "]}]";
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        // execute test ---
        assertThatThrownBy(() -> GameUpdateManager.findMacOsAssetUrlFromPrereleases("TestOwner", "TestRepo"))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("No macOS download found");
    }

    @Test
    void findMacOsAssetUrlFromPrereleases_parses_first_release_assets_success() throws Exception {
        // prepare mock data - multiple releases, should use first one ---
        String jsonResponse = "[" +
            "{\"tag_name\":\"v2.0.0\",\"assets\":[{\"name\":\"game-osx-v2.dmg\",\"size\":200000000,\"browser_download_url\":\"https://example.com/v2.dmg\"}]}," +
            "{\"tag_name\":\"v1.0.0\",\"assets\":[{\"name\":\"game-osx-v1.dmg\",\"size\":150000000,\"browser_download_url\":\"https://example.com/v1.dmg\"}]}" +
            "]";
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        // execute test ---
        String result = GameUpdateManager.findMacOsAssetUrlFromPrereleases("TestOwner", "TestRepo");

        // verify assertions - should use first release's asset ---
        assertThat(result).isEqualTo("https://example.com/v2.dmg");
    }

    @Test
    void findMacOsAssetUrlFromPrereleases_throws_on_connection_error_success() {
        // Don't create any context - server will return 404
        assertThatThrownBy(() -> GameUpdateManager.findMacOsAssetUrlFromPrereleases("NonExistent", "Repo"))
            .isInstanceOf(IOException.class);
    }

    // ========================================
    // JSON parsing tests
    // ========================================

    @Test
    void prerelease_methods_parse_json_with_multiple_releases_success() throws IOException {
        // Test that the methods correctly parse JSON with multiple releases
        // by using findLargestMacOsAsset which uses the same parsing logic

        // JSON format similar to what /releases endpoint returns (array of releases)
        String jsonResponse = "[" +
            "{\"tag_name\":\"exp-2024-02-01\",\"prerelease\":true,\"assets\":[" +
            "{\"name\":\"game-osx.dmg\",\"size\":100000,\"browser_download_url\":\"https://example.com/game-osx.dmg\"}" +
            "]}," +
            "{\"tag_name\":\"exp-2024-01-15\",\"prerelease\":true,\"assets\":[" +
            "{\"name\":\"game-osx-old.dmg\",\"size\":90000,\"browser_download_url\":\"https://example.com/game-osx-old.dmg\"}" +
            "]}" +
            "]";

        // Extract assets section as the method does
        int assetsIdx = jsonResponse.indexOf("\"assets\":");
        int nextReleaseIdx = jsonResponse.indexOf("\"tag_name\":", assetsIdx + 1);
        String assetsSection = nextReleaseIdx > 0 ? jsonResponse.substring(assetsIdx, nextReleaseIdx) : jsonResponse.substring(assetsIdx);

        // Use findLargestMacOsAsset to test the parsing
        String result = GameUpdateManager.findLargestMacOsAsset(assetsSection);

        assertThat(result).isEqualTo("https://example.com/game-osx.dmg");
    }

    @Test
    void prerelease_methods_parse_json_with_empty_assets_first_release_success() throws IOException {
        // Test parsing when first release has no assets (should still work with second release)
        String jsonResponse = "[" +
            "{\"tag_name\":\"exp-2024-02-01\",\"prerelease\":true,\"assets\":[]}," +
            "{\"tag_name\":\"exp-2024-01-15\",\"prerelease\":true,\"assets\":[" +
            "{\"name\":\"game-macos.zip\",\"size\":150000,\"browser_download_url\":\"https://example.com/game-macos.zip\"}" +
            "]}" +
            "]";

        // When first release has no macOS assets, parsing the first assets section will fail
        int assetsIdx = jsonResponse.indexOf("\"assets\":");
        int nextReleaseIdx = jsonResponse.indexOf("\"tag_name\":", assetsIdx + 1);
        String firstAssetsSection = nextReleaseIdx > 0 ? jsonResponse.substring(assetsIdx, nextReleaseIdx) : jsonResponse.substring(assetsIdx);

        // First release has no assets, so this should throw
        assertThatThrownBy(() -> GameUpdateManager.findLargestMacOsAsset(firstAssetsSection))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("No macOS download found");
    }

    @Test
    void prerelease_tag_parsing_extracts_first_tag_success() {
        // Test the tag extraction logic
        String jsonResponse = "[{\"tag_name\":\"first-tag\"},{\"tag_name\":\"second-tag\"}]";

        // Simulate the parsing logic from getLatestPrereleaseTag
        int tagIdx = jsonResponse.indexOf("\"tag_name\":\"");
        assertThat(tagIdx).isGreaterThanOrEqualTo(0);

        int tagStart = tagIdx + "\"tag_name\":\"".length();
        int tagEnd = jsonResponse.indexOf("\"", tagStart);
        String extractedTag = jsonResponse.substring(tagStart, tagEnd);

        assertThat(extractedTag).isEqualTo("first-tag");
    }

    @Test
    void prerelease_tag_parsing_handles_experimental_format_success() {
        // Test parsing experimental version tags
        String jsonResponse = "[{\"tag_name\":\"cdda-experimental-2024-01-15-0123\",\"prerelease\":true}]";

        int tagIdx = jsonResponse.indexOf("\"tag_name\":\"");
        int tagStart = tagIdx + "\"tag_name\":\"".length();
        int tagEnd = jsonResponse.indexOf("\"", tagStart);
        String extractedTag = jsonResponse.substring(tagStart, tagEnd);

        assertThat(extractedTag).isEqualTo("cdda-experimental-2024-01-15-0123");

        // Verify it creates a valid GameVersion
        GameVersion version = new GameVersion(extractedTag);
        assertThat(version.isExperimental()).isTrue();
    }

    @Test
    void prerelease_assets_parsing_selects_tiles_over_curses_success() throws IOException {
        // Prerelease assets should prefer tiles (larger) over curses (smaller)
        String assetsJson = "{\"assets\":[" +
            "{\"name\":\"cdda-osx-curses.dmg\",\"size\":50000000,\"browser_download_url\":\"https://example.com/curses.dmg\"}," +
            "{\"name\":\"cdda-osx-tiles.dmg\",\"size\":150000000,\"browser_download_url\":\"https://example.com/tiles.dmg\"}" +
            "]}";

        String result = GameUpdateManager.findLargestMacOsAsset(assetsJson);

        assertThat(result).isEqualTo("https://example.com/tiles.dmg");
    }

    @Test
    void prerelease_assets_parsing_handles_universal_builds_success() throws IOException {
        // Test parsing universal macOS builds (common in CDDA releases)
        String assetsJson = "{\"assets\":[" +
            "{\"name\":\"cdda-osx-tiles-universal-2024-01-15.dmg\",\"size\":200000000," +
            "\"browser_download_url\":\"https://github.com/CleverRaven/Cataclysm-DDA/releases/download/exp/cdda-osx-tiles-universal.dmg\"}" +
            "]}";

        String result = GameUpdateManager.findLargestMacOsAsset(assetsJson);

        assertThat(result).contains("universal");
        assertThat(result).endsWith(".dmg");
    }

    // ========================================
    // isGameUpdateAvailable() tests - HAPPY PATH
    // ========================================

    @Test
    void isGameUpdateAvailable_returns_empty_when_not_configured_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn(null);
            when(mockConfig.getGameGithubRepoName()).thenReturn(null);

            // execute test ---
            var result = GameUpdateManager.isGameUpdateAvailable();

            // verify assertions ---
            assertThat(result).isEmpty();
        }
    }

    @Test
    void isGameUpdateAvailable_returns_empty_when_installed_version_not_set_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getInstalledGameVersion()).thenReturn(null);

            // execute test ---
            var result = GameUpdateManager.isGameUpdateAvailable();

            // verify assertions ---
            assertThat(result).isEmpty();
        }
    }

    @Test
    void isGameUpdateAvailable_returns_true_when_newer_version_available_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getInstalledGameVersion()).thenReturn("0.G");

            // mock getLatestGameReleaseTag to return a newer version
            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag)
                .thenReturn(Optional.of(new GameVersion("0.H")));

            // execute test ---
            var result = GameUpdateManager.isGameUpdateAvailable();

            // verify assertions ---
            assertThat(result).isPresent();
            assertThat(result.get()).isTrue();
        }
    }

    @Test
    void isGameUpdateAvailable_returns_false_when_same_version_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getInstalledGameVersion()).thenReturn("0.G");

            // mock getLatestGameReleaseTag to return the same version
            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag)
                .thenReturn(Optional.of(new GameVersion("0.G")));

            // execute test ---
            var result = GameUpdateManager.isGameUpdateAvailable();

            // verify assertions ---
            assertThat(result).isPresent();
            assertThat(result.get()).isFalse();
        }
    }

    @Test
    void isGameUpdateAvailable_returns_false_when_installed_is_newer_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getInstalledGameVersion()).thenReturn("0.H");

            // mock getLatestGameReleaseTag to return an older version
            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag)
                .thenReturn(Optional.of(new GameVersion("0.G")));

            // execute test ---
            var result = GameUpdateManager.isGameUpdateAvailable();

            // verify assertions ---
            assertThat(result).isPresent();
            assertThat(result.get()).isFalse();
        }
    }

    @Test
    void isGameUpdateAvailable_returns_true_for_experimental_update_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getInstalledGameVersion()).thenReturn("2024-01-15-0100");

            // mock getLatestGameReleaseTag to return a newer experimental
            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag)
                .thenReturn(Optional.of(new GameVersion("2024-02-01-0200")));

            // execute test ---
            var result = GameUpdateManager.isGameUpdateAvailable();

            // verify assertions ---
            assertThat(result).isPresent();
            assertThat(result.get()).isTrue();
        }
    }

    @Test
    void isGameUpdateAvailable_returns_empty_when_latest_not_found_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getInstalledGameVersion()).thenReturn("0.G");

            // mock getLatestGameReleaseTag to return empty
            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag)
                .thenReturn(Optional.empty());

            // execute test ---
            var result = GameUpdateManager.isGameUpdateAvailable();

            // verify assertions ---
            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // getLatestGameReleaseTag() tests - HAPPY PATH
    // ========================================

    @Test
    void getLatestGameReleaseTag_returns_empty_when_not_configured_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn(null);
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            // execute test ---
            var result = GameUpdateManager.getLatestGameReleaseTag();

            // verify assertions ---
            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // getMacOsDownloadUrl() tests - HAPPY PATH
    // ========================================

    @Test
    void getMacOsDownloadUrl_returns_empty_when_not_configured_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn(null);
            when(mockConfig.getGameGithubRepoName()).thenReturn(null);

            // execute test ---
            var result = GameUpdateManager.getMacOsDownloadUrl();

            // verify assertions ---
            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // downloadAndInstallUpdate() tests - FULL FLOW
    // ========================================

    @Test
    void downloadAndInstallUpdate_returns_false_when_not_configured_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn(null);
            when(mockConfig.getGameGithubRepoName()).thenReturn(null);

            StringBuilder statusMessages = new StringBuilder();

            // execute test ---
            boolean result = GameUpdateManager.downloadAndInstallUpdate(
                progress -> {},
                status -> statusMessages.append(status).append("\n")
            );

            // verify assertions ---
            assertThat(result).isFalse();
            assertThat(statusMessages.toString()).contains("Repository not configured");
        }
    }

    @Test
    void downloadAndInstallUpdate_returns_false_when_cdda_path_not_configured_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getCddaPath()).thenReturn(null);

            StringBuilder statusMessages = new StringBuilder();

            // execute test ---
            boolean result = GameUpdateManager.downloadAndInstallUpdate(
                progress -> {},
                status -> statusMessages.append(status).append("\n")
            );

            // verify assertions ---
            assertThat(result).isFalse();
            assertThat(statusMessages.toString()).contains("CDDA path not configured");
        }
    }

    @Test
    void downloadAndInstallUpdate_returns_false_when_no_download_url_found_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getCddaPath()).thenReturn("/some/path");

            mockedManager.when(GameUpdateManager::getMacOsDownloadUrl).thenReturn(Optional.empty());

            StringBuilder statusMessages = new StringBuilder();

            // execute test ---
            boolean result = GameUpdateManager.downloadAndInstallUpdate(
                progress -> {},
                status -> statusMessages.append(status).append("\n")
            );

            // verify assertions ---
            assertThat(result).isFalse();
            assertThat(statusMessages.toString()).contains("No macOS download found");
        }
    }

    @Test
    void downloadAndInstallUpdate_returns_false_when_version_not_found_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getCddaPath()).thenReturn("/some/path");

            mockedManager.when(GameUpdateManager::getMacOsDownloadUrl).thenReturn(Optional.of("http://example.com/game.zip"));
            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag).thenReturn(Optional.empty());

            StringBuilder statusMessages = new StringBuilder();

            // execute test ---
            boolean result = GameUpdateManager.downloadAndInstallUpdate(
                progress -> {},
                status -> statusMessages.append(status).append("\n")
            );

            // verify assertions ---
            assertThat(result).isFalse();
            assertThat(statusMessages.toString()).contains("Could not determine version");
        }
    }

    @Test
    void downloadAndInstallUpdate_full_success_flow_with_zip(@TempDir Path tempDir, @TempDir Path downloadDir, @TempDir Path trashDir) throws Exception {
        // prepare mock data - serve the test ZIP file ---
        File testZip = TestUtils.getFromResource("gameupdate/zip/game-macos.zip");
        byte[] zipContent = Files.readAllBytes(testZip.toPath());

        mockServer.createContext("/download/game.zip", exchange -> {
            exchange.sendResponseHeaders(200, zipContent.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(zipContent);
            }
        });

        // create old game binary to be replaced ---
        Path oldGamePath = tempDir.resolve("OldGame.app");
        Files.createDirectories(oldGamePath.resolve("Contents/MacOS"));
        Files.writeString(oldGamePath.resolve("Contents/Info.plist"), "old game");

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getCddaPath()).thenReturn(oldGamePath.toString());

            mockedPaths.when(Paths::getDownloadsPath).thenReturn(downloadDir);
            mockedPaths.when(Paths::getCustomTrashedGamePath).thenReturn(trashDir);

            mockedManager.when(GameUpdateManager::getMacOsDownloadUrl)
                .thenReturn(Optional.of(serverBaseUrl + "/download/game.zip"));
            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag)
                .thenReturn(Optional.of(new GameVersion("0.H")));

            List<Integer> progressValues = new ArrayList<>();
            StringBuilder statusMessages = new StringBuilder();

            // execute test ---
            boolean result = GameUpdateManager.downloadAndInstallUpdate(
                progressValues::add,
                status -> statusMessages.append(status).append("\n")
            );

            // verify assertions ---
            assertThat(result).isTrue();
            assertThat(statusMessages.toString()).contains("Update complete!");
            assertThat(progressValues).contains(100);

            // verify old game was moved to trash directory
            File[] trashContents = trashDir.toFile().listFiles();
            assertThat(trashContents).isNotNull();
            assertThat(trashContents.length).isGreaterThan(0);
        }
    }

    // ========================================
    // downloadFile() tests
    // ========================================

    @Test
    void downloadFile_downloads_file_successfully_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data ---
        String fileContent = "Test file content for download testing purposes";
        mockServer.createContext("/download/test.txt", exchange -> {
            byte[] bytes = fileContent.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        File destination = tempDir.resolve("downloaded.txt").toFile();
        List<Integer> progressValues = new ArrayList<>();

        // execute test ---
        boolean result = GameUpdateManager.downloadFile(
            serverBaseUrl + "/download/test.txt",
            destination,
            progressValues::add
        );

        // verify assertions ---
        assertThat(result).isTrue();
        assertThat(destination).exists();
        assertThat(Files.readString(destination.toPath())).isEqualTo(fileContent);
        assertThat(progressValues).isNotEmpty();
    }

    @Test
    void downloadFile_handles_redirect_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data ---
        String fileContent = "Redirected content here";

        mockServer.createContext("/redirect", exchange -> {
            exchange.getResponseHeaders().set("Location", serverBaseUrl + "/final");
            exchange.sendResponseHeaders(302, -1);
        });

        mockServer.createContext("/final", exchange -> {
            byte[] bytes = fileContent.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        File destination = tempDir.resolve("redirected.txt").toFile();

        // execute test ---
        boolean result = GameUpdateManager.downloadFile(
            serverBaseUrl + "/redirect",
            destination,
            progress -> {}
        );

        // verify assertions ---
        assertThat(result).isTrue();
        assertThat(destination).exists();
        assertThat(Files.readString(destination.toPath())).isEqualTo(fileContent);
    }

    @Test
    void downloadFile_returns_false_on_connection_error_success(@TempDir Path tempDir) {
        // prepare mock data ---
        File destination = tempDir.resolve("failed.txt").toFile();

        // execute test - use a non-existent endpoint ---
        boolean result = GameUpdateManager.downloadFile(
            serverBaseUrl + "/nonexistent",
            destination,
            progress -> {}
        );

        // verify assertions ---
        assertThat(result).isFalse();
    }

    @Test
    void downloadFile_reports_progress_correctly_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data - create larger content ---
        byte[] largeContent = new byte[50000];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        mockServer.createContext("/large", exchange -> {
            exchange.sendResponseHeaders(200, largeContent.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(largeContent);
            }
        });

        File destination = tempDir.resolve("large.bin").toFile();
        AtomicInteger maxProgress = new AtomicInteger(0);

        // execute test ---
        boolean result = GameUpdateManager.downloadFile(
            serverBaseUrl + "/large",
            destination,
            progress -> maxProgress.set(Math.max(maxProgress.get(), progress))
        );

        // verify assertions ---
        assertThat(result).isTrue();
        assertThat(maxProgress.get()).isEqualTo(100);
        assertThat(destination.length()).isEqualTo(largeContent.length);
    }

    @Test
    void downloadFile_downloads_binary_content_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data - binary content ---
        byte[] binaryContent = {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE, (byte) 0xFD};

        mockServer.createContext("/binary", exchange -> {
            exchange.sendResponseHeaders(200, binaryContent.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(binaryContent);
            }
        });

        File destination = tempDir.resolve("binary.dat").toFile();

        // execute test ---
        boolean result = GameUpdateManager.downloadFile(
            serverBaseUrl + "/binary",
            destination,
            progress -> {}
        );

        // verify assertions ---
        assertThat(result).isTrue();
        assertThat(Files.readAllBytes(destination.toPath())).isEqualTo(binaryContent);
    }

    // ========================================
    // moveToTrash() tests
    // ========================================

    @Test
    void moveToTrash_moves_file_successfully_success(@TempDir Path tempDir, @TempDir Path trashDir) throws IOException {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomTrashedGamePath).thenReturn(trashDir);

            File fileToTrash = tempDir.resolve("game.txt").toFile();
            Files.writeString(fileToTrash.toPath(), "game content");

            // pre-test assertions ---
            assertThat(fileToTrash).exists();

            // execute test ---
            boolean result = GameUpdateManager.moveToTrash(fileToTrash);

            // verify assertions ---
            assertThat(result).isTrue();
            assertThat(fileToTrash).doesNotExist();

            // verify file is in trash
            File[] trashContents = trashDir.toFile().listFiles();
            assertThat(trashContents).isNotNull();
            assertThat(trashContents.length).isGreaterThan(0);
        }
    }

    @Test
    void moveToTrash_moves_directory_successfully_success(@TempDir Path tempDir, @TempDir Path trashDir) throws IOException {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomTrashedGamePath).thenReturn(trashDir);

            File dirToTrash = tempDir.resolve("GameApp.app").toFile();
            Files.createDirectories(dirToTrash.toPath().resolve("Contents/MacOS"));
            Files.createFile(dirToTrash.toPath().resolve("Contents/Info.plist"));
            Files.writeString(dirToTrash.toPath().resolve("Contents/MacOS/game"), "executable");

            // pre-test assertions ---
            assertThat(dirToTrash).exists();
            assertThat(dirToTrash).isDirectory();

            // execute test ---
            boolean result = GameUpdateManager.moveToTrash(dirToTrash);

            // verify assertions ---
            assertThat(result).isTrue();
            assertThat(dirToTrash).doesNotExist();
        }
    }

    @Test
    void moveToTrash_creates_timestamped_trash_folder_success(@TempDir Path tempDir, @TempDir Path trashDir) throws IOException {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomTrashedGamePath).thenReturn(trashDir);

            File fileToTrash = tempDir.resolve("game.app").toFile();
            Files.createDirectories(fileToTrash.toPath());

            // execute test ---
            boolean result = GameUpdateManager.moveToTrash(fileToTrash);

            // verify assertions ---
            assertThat(result).isTrue();

            // verify timestamped folder was created
            File[] trashContents = trashDir.toFile().listFiles();
            assertThat(trashContents).isNotNull();
            assertThat(trashContents[0].getName()).matches("\\d{8}_\\d{6}");
        }
    }

    // ========================================
    // extractFileName() tests
    // ========================================

    @Test
    void extractFileName_simple_url_success() {
        assertThat(GameUpdateManager.extractFileName("https://github.com/releases/download/v1.0/game.zip"))
            .isEqualTo("game.zip");
    }

    @Test
    void extractFileName_url_with_query_params_success() {
        assertThat(GameUpdateManager.extractFileName("https://github.com/releases/game.dmg?token=abc123"))
            .isEqualTo("game.dmg");
    }

    @Test
    void extractFileName_url_ending_with_slash_success() {
        assertThat(GameUpdateManager.extractFileName("https://github.com/releases/"))
            .isEqualTo("download.zip");
    }

    @Test
    void extractFileName_complex_filename_success() {
        assertThat(GameUpdateManager.extractFileName(
            "https://github.com/CleverRaven/Cataclysm-DDA/releases/download/cdda-experimental-2024-01-15-0123/cdda-osx-tiles.dmg"
        )).isEqualTo("cdda-osx-tiles.dmg");
    }

    // ========================================
    // findAppBundle() tests
    // ========================================

    @Test
    void findAppBundle_finds_app_at_root_success(@TempDir Path tempDir) throws IOException {
        Path appPath = tempDir.resolve("TestGame.app");
        Files.createDirectories(appPath.resolve("Contents/MacOS"));

        File result = GameUpdateManager.findAppBundle(tempDir.toFile());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TestGame.app");
    }

    @Test
    void findAppBundle_returns_null_when_no_app_success(@TempDir Path tempDir) throws IOException {
        Files.createDirectories(tempDir.resolve("SomeFolder"));

        File result = GameUpdateManager.findAppBundle(tempDir.toFile());

        assertThat(result).isNull();
    }

    @Test
    void findAppBundle_returns_null_for_null_directory_success() {
        assertThat(GameUpdateManager.findAppBundle(null)).isNull();
    }

    @Test
    void findAppBundle_returns_null_for_nonexistent_directory_success() {
        assertThat(GameUpdateManager.findAppBundle(new File("/nonexistent/path"))).isNull();
    }

    // ========================================
    // findLargestMacOsAsset() tests
    // ========================================

    @Test
    void findLargestMacOsAsset_selects_largest_osx_dmg_success() throws IOException {
        File jsonFile = TestUtils.getFromResource("gameupdate/json/releases_latest.json");
        String json = Files.readString(jsonFile.toPath());

        String result = GameUpdateManager.findLargestMacOsAsset(json);

        assertThat(result).contains("OSX-Tiles.dmg");
        assertThat(result).doesNotContain("Curses");
    }

    @Test
    void findLargestMacOsAsset_selects_largest_from_experimental_success() throws IOException {
        File jsonFile = TestUtils.getFromResource("gameupdate/json/releases_experimental.json");
        String json = Files.readString(jsonFile.toPath());

        String result = GameUpdateManager.findLargestMacOsAsset(json);

        assertThat(result).contains("osx-tiles");
    }

    @Test
    void findLargestMacOsAsset_throws_when_no_macos_asset_success() {
        String json = "{\"assets\":[{\"name\":\"linux.tar.gz\",\"size\":100,\"browser_download_url\":\"http://x.com/linux.tar.gz\"}]}";

        assertThatThrownBy(() -> GameUpdateManager.findLargestMacOsAsset(json))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("No macOS download found");
    }

    @Test
    void findLargestMacOsAsset_recognizes_darwin_pattern_success() throws IOException {
        File jsonFile = TestUtils.getFromResource("gameupdate/json/releases_macos_zip.json");
        String json = Files.readString(jsonFile.toPath());

        String result = GameUpdateManager.findLargestMacOsAsset(json);

        assertThat(result).contains("darwin");
    }

    @Test
    void findLargestMacOsAsset_ignores_sha256_files_success() throws IOException {
        File jsonFile = TestUtils.getFromResource("gameupdate/json/releases_latest.json");
        String json = Files.readString(jsonFile.toPath());

        String result = GameUpdateManager.findLargestMacOsAsset(json);

        assertThat(result).doesNotContain(".sha256");
    }

    @Test
    void findLargestMacOsAsset_recognizes_all_mac_patterns_success() throws IOException {
        // Test osx pattern
        String osxJson = "{\"assets\":[{\"name\":\"game-osx.dmg\",\"size\":100,\"browser_download_url\":\"http://x.com/osx.dmg\"}]}";
        assertThat(GameUpdateManager.findLargestMacOsAsset(osxJson)).contains("osx");

        // Test macos pattern
        String macosJson = "{\"assets\":[{\"name\":\"game-macos.zip\",\"size\":100,\"browser_download_url\":\"http://x.com/macos.zip\"}]}";
        assertThat(GameUpdateManager.findLargestMacOsAsset(macosJson)).contains("macos");

        // Test mac pattern
        String macJson = "{\"assets\":[{\"name\":\"game-mac.dmg\",\"size\":100,\"browser_download_url\":\"http://x.com/mac.dmg\"}]}";
        assertThat(GameUpdateManager.findLargestMacOsAsset(macJson)).contains("mac");

        // Test darwin pattern
        String darwinJson = "{\"assets\":[{\"name\":\"game-darwin.zip\",\"size\":100,\"browser_download_url\":\"http://x.com/darwin.zip\"}]}";
        assertThat(GameUpdateManager.findLargestMacOsAsset(darwinJson)).contains("darwin");

        // Test apple pattern
        String appleJson = "{\"assets\":[{\"name\":\"game-apple.dmg\",\"size\":100,\"browser_download_url\":\"http://x.com/apple.dmg\"}]}";
        assertThat(GameUpdateManager.findLargestMacOsAsset(appleJson)).contains("apple");
    }

    // ========================================
    // extractGameBinary() tests - ZIP
    // ========================================

    @Test
    void extractGameBinary_extracts_zip_with_app_bundle_success(@TempDir Path tempDir) {
        File zipFile = TestUtils.getFromResource("gameupdate/zip/game-macos.zip");

        File result = GameUpdateManager.extractGameBinary(zipFile, tempDir.toFile());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TestGame.app");
        assertThat(new File(result, "Contents/Info.plist")).exists();
        assertThat(new File(result, "Contents/MacOS/TestGame")).exists();
    }

    @Test
    void extractGameBinary_returns_null_for_unknown_format_success(@TempDir Path tempDir) throws IOException {
        File unknownFile = tempDir.resolve("game.tar.gz").toFile();
        Files.createFile(unknownFile.toPath());

        File result = GameUpdateManager.extractGameBinary(unknownFile, tempDir.toFile());

        assertThat(result).isNull();
    }

    @Test
    void extractGameBinary_returns_app_file_directly_success(@TempDir Path tempDir) throws IOException {
        Path appPath = tempDir.resolve("DirectGame.app");
        Files.createDirectories(appPath);

        File result = GameUpdateManager.extractGameBinary(appPath.toFile(), tempDir.toFile());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("DirectGame.app");
    }

    // ========================================
    // extractGameBinary() tests - DMG
    // ========================================

    @Test
    @EnabledOnOs(OS.MAC) // DMG extraction requires macOS-specific hdiutil command
    void extractGameBinary_extracts_dmg_with_app_bundle_success(@TempDir Path tempDir) {
        File dmgFile = TestUtils.getFromResource("gameupdate/dmg/game-macos.dmg");

        File result = GameUpdateManager.extractGameBinary(dmgFile, tempDir.toFile());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TestGame.app");
        assertThat(new File(result, "Contents/Info.plist")).exists();
        assertThat(new File(result, "Contents/MacOS/TestGame")).exists();
        assertThat(new File(result, "Contents/Resources/game.dat")).exists();
    }

    // ========================================
    // findMacOsAssetUrl() tests via mock HTTP server
    // ========================================

    @Test
    void findMacOsAssetUrl_stable_release_uses_entire_response_as_assets_success() throws Exception {
        // prepare mock data - stable release format (single object, not array) ---
        String jsonResponse = "{\"tag_name\":\"0.G\",\"assets\":[" +
            "{\"name\":\"game-osx-tiles.dmg\",\"size\":200000000,\"browser_download_url\":\"http://example.com/tiles.dmg\"}," +
            "{\"name\":\"game-osx-curses.dmg\",\"size\":50000000,\"browser_download_url\":\"http://example.com/curses.dmg\"}" +
            "]}";

        mockServer.createContext("/repos/CleverRaven/Cataclysm-DDA/releases/latest", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(false);

            // execute test ---
            String result = GameUpdateManager.findMacOsAssetUrl("CleverRaven", "Cataclysm-DDA");

            // verify assertions - should select largest (tiles) ---
            assertThat(result).isEqualTo("http://example.com/tiles.dmg");
        }
    }

    @Test
    void findMacOsAssetUrl_prerelease_extracts_first_release_assets_section_success() throws Exception {
        // prepare mock data - prerelease format (array of releases) ---
        String jsonResponse = "[" +
            "{\"tag_name\":\"exp-2024-02\",\"assets\":[" +
            "{\"name\":\"game-osx-new.dmg\",\"size\":180000000,\"browser_download_url\":\"http://example.com/new.dmg\"}" +
            "]}," +
            "{\"tag_name\":\"exp-2024-01\",\"assets\":[" +
            "{\"name\":\"game-osx-old.dmg\",\"size\":170000000,\"browser_download_url\":\"http://example.com/old.dmg\"}" +
            "]}" +
            "]";

        // Non-official repo uses /releases endpoint (prereleases)
        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("TestOwner");
            when(mockConfig.getGameGithubRepoName()).thenReturn("TestRepo");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(true);

            // execute test ---
            String result = GameUpdateManager.findMacOsAssetUrl("TestOwner", "TestRepo");

            // verify assertions - should use FIRST release's assets only ---
            assertThat(result).isEqualTo("http://example.com/new.dmg");
        }
    }

    @Test
    void findMacOsAssetUrl_prerelease_with_official_repo_when_configured_success() throws Exception {
        // prepare mock data - official repo with prereleases enabled ---
        String jsonResponse = "[" +
            "{\"tag_name\":\"cdda-experimental-2024-02-15\",\"assets\":[" +
            "{\"name\":\"cdda-osx-tiles.dmg\",\"size\":200000000,\"browser_download_url\":\"http://example.com/exp-tiles.dmg\"}," +
            "{\"name\":\"cdda-osx-curses.dmg\",\"size\":60000000,\"browser_download_url\":\"http://example.com/exp-curses.dmg\"}" +
            "]}" +
            "]";

        mockServer.createContext("/repos/CleverRaven/Cataclysm-DDA/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(true);

            // execute test ---
            String result = GameUpdateManager.findMacOsAssetUrl("CleverRaven", "Cataclysm-DDA");

            // verify assertions - should select largest from first release ---
            assertThat(result).isEqualTo("http://example.com/exp-tiles.dmg");
        }
    }

    @Test
    void findMacOsAssetUrl_falls_back_to_prereleases_on_404_success() throws Exception {
        // prepare mock data - /releases/latest returns 404 ---
        mockServer.createContext("/repos/CleverRaven/Cataclysm-DDA/releases/latest", exchange -> {
            exchange.sendResponseHeaders(404, -1);
        });

        // Fallback endpoint returns data
        String releasesResponse = "[{\"tag_name\":\"exp-2024\",\"assets\":[" +
            "{\"name\":\"game-osx.dmg\",\"size\":150000000,\"browser_download_url\":\"http://example.com/fallback.dmg\"}" +
            "]}]";
        mockServer.createContext("/repos/CleverRaven/Cataclysm-DDA/releases", exchange -> {
            byte[] response = releasesResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(false);

            // execute test ---
            String result = GameUpdateManager.findMacOsAssetUrl("CleverRaven", "Cataclysm-DDA");

            // verify assertions - should fall back and return prerelease asset ---
            assertThat(result).isEqualTo("http://example.com/fallback.dmg");
        }
    }

    @Test
    void findMacOsAssetUrl_throws_when_no_assets_in_prerelease_json_success() throws Exception {
        // prepare mock data - prerelease JSON without "assets" key ---
        String jsonResponse = "[{\"tag_name\":\"exp-2024\",\"name\":\"Some Release\"}]";

        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("TestOwner");
            when(mockConfig.getGameGithubRepoName()).thenReturn("TestRepo");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(true);

            // execute test ---
            assertThatThrownBy(() -> GameUpdateManager.findMacOsAssetUrl("TestOwner", "TestRepo"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("No assets found in release");
        }
    }

    @Test
    void findMacOsAssetUrl_prerelease_stops_at_next_release_boundary_success() throws Exception {
        // prepare mock data - verify assets section extraction stops at next tag_name ---
        // This tests the: int nextReleaseIdx = json.indexOf("\"tag_name\":", assetsIdx + 1);
        String jsonResponse = "[" +
            "{\"tag_name\":\"exp-2024-02\",\"assets\":[" +
            "{\"name\":\"game-osx-small.dmg\",\"size\":100000000,\"browser_download_url\":\"http://example.com/small.dmg\"}" +
            "]}," +
            "{\"tag_name\":\"exp-2024-01\",\"assets\":[" +
            "{\"name\":\"game-osx-large.dmg\",\"size\":500000000,\"browser_download_url\":\"http://example.com/large.dmg\"}" +
            "]}" +
            "]";

        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("TestOwner");
            when(mockConfig.getGameGithubRepoName()).thenReturn("TestRepo");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(true);

            // execute test ---
            String result = GameUpdateManager.findMacOsAssetUrl("TestOwner", "TestRepo");

            // verify assertions - should use FIRST release's smaller asset, NOT second release's larger ---
            assertThat(result).isEqualTo("http://example.com/small.dmg");
        }
    }

    @Test
    void findMacOsAssetUrl_prerelease_single_release_uses_full_assets_section_success() throws Exception {
        // prepare mock data - single release (no next tag_name to stop at) ---
        // This tests when: nextReleaseIdx <= 0, so assetsSection = json.substring(assetsIdx)
        String jsonResponse = "[{\"tag_name\":\"exp-2024\",\"assets\":[" +
            "{\"name\":\"game-osx.dmg\",\"size\":150000000,\"browser_download_url\":\"http://example.com/only.dmg\"}" +
            "]}]";

        mockServer.createContext("/repos/TestOwner/TestRepo/releases", exchange -> {
            byte[] response = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("TestOwner");
            when(mockConfig.getGameGithubRepoName()).thenReturn("TestRepo");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(true);

            // execute test ---
            String result = GameUpdateManager.findMacOsAssetUrl("TestOwner", "TestRepo");

            // verify assertions ---
            assertThat(result).isEqualTo("http://example.com/only.dmg");
        }
    }

    @Test
    void findMacOsAssetUrl_throws_on_connection_error_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("NonExistent");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Repo");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(true);

            // Don't create any mock endpoint - will fail to connect
            assertThatThrownBy(() -> GameUpdateManager.findMacOsAssetUrl("NonExistent", "Repo"))
                .isInstanceOf(IOException.class);
        }
    }

    // ========================================
    // openReleaseInDefaultBrowser() tests
    // ========================================

    @Test
    void openReleaseInDefaultBrowser_opens_browser_with_correct_url_success() throws Exception {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<Desktop> mockedDesktop = mockStatic(Desktop.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            Desktop mockDesktopInstance = Mockito.mock(Desktop.class);
            mockedDesktop.when(Desktop::getDesktop).thenReturn(mockDesktopInstance);
            when(mockDesktopInstance.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            GameVersion version = new GameVersion("0.G");

            // execute test ---
            GameUpdateManager.openReleaseInDefaultBrowser(version);

            // verify assertions - browser should be called with correct URL ---
            Mockito.verify(mockDesktopInstance).browse(java.net.URI.create("https://github.com/CleverRaven/Cataclysm-DDA/releases/tag/0.G"));
        }
    }

    @Test
    void openReleaseInDefaultBrowser_does_nothing_when_browse_not_supported_success() throws Exception {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<Desktop> mockedDesktop = mockStatic(Desktop.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            Desktop mockDesktopInstance = Mockito.mock(Desktop.class);
            mockedDesktop.when(Desktop::getDesktop).thenReturn(mockDesktopInstance);
            when(mockDesktopInstance.isSupported(Desktop.Action.BROWSE)).thenReturn(false);

            GameVersion version = new GameVersion("0.G");

            // execute test ---
            GameUpdateManager.openReleaseInDefaultBrowser(version);

            // verify assertions - browse should NOT be called ---
            Mockito.verify(mockDesktopInstance, Mockito.never()).browse(Mockito.any());
        }
    }


    // ========================================
    // openLatestGameReleaseInDefaultBrowser() tests
    // ========================================

    @Test
    void openLatestGameReleaseInDefaultBrowser_does_nothing_when_no_release_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag).thenReturn(Optional.empty());

            // execute test ---
            GameUpdateManager.openLatestGameReleaseInDefaultBrowser();

            // verify that openReleaseInDefaultBrowser was NOT called (no release to open)
            mockedManager.verify(() -> GameUpdateManager.openReleaseInDefaultBrowser(Mockito.any()), Mockito.never());
        }
    }

    @Test
    void openLatestGameReleaseInDefaultBrowser_opens_browser_when_release_found_success() throws Exception {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<Desktop> mockedDesktop = mockStatic(Desktop.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            Desktop mockDesktopInstance = Mockito.mock(Desktop.class);
            mockedDesktop.when(Desktop::getDesktop).thenReturn(mockDesktopInstance);
            when(mockDesktopInstance.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            GameVersion latestVersion = new GameVersion("0.H");
            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag).thenReturn(Optional.of(latestVersion));

            // execute test ---
            GameUpdateManager.openLatestGameReleaseInDefaultBrowser();

            // verify assertions - browser should be called with correct URL ---
            Mockito.verify(mockDesktopInstance).browse(java.net.URI.create("https://github.com/CleverRaven/Cataclysm-DDA/releases/tag/0.H"));
        }
    }

    // ========================================
    // Integration tests - Full workflows
    // ========================================

    @Test
    void full_update_detection_workflow_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class);
             MockedStatic<GameUpdateManager> mockedManager = mockStatic(GameUpdateManager.class, Mockito.CALLS_REAL_METHODS)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getInstalledGameVersion()).thenReturn("0.F");
            when(mockConfig.isIncludePreReleaseBuilds()).thenReturn(false);

            mockedManager.when(GameUpdateManager::getLatestGameReleaseTag)
                .thenReturn(Optional.of(new GameVersion("0.G")));

            // execute test ---
            Optional<Boolean> updateAvailable = GameUpdateManager.isGameUpdateAvailable();

            // verify assertions ---
            assertThat(updateAvailable).isPresent();
            assertThat(updateAvailable.get()).isTrue();
        }
    }

    @Test
    void full_zip_extraction_and_app_discovery_workflow_success(@TempDir Path tempDir) {
        // prepare mock data ---
        File zipFile = TestUtils.getFromResource("gameupdate/zip/game-macos.zip");

        // execute test ---
        File extractedApp = GameUpdateManager.extractGameBinary(zipFile, tempDir.toFile());

        // verify assertions ---
        assertThat(extractedApp).isNotNull();
        assertThat(extractedApp.isDirectory()).isTrue();

        // verify complete app bundle structure
        File contents = new File(extractedApp, "Contents");
        assertThat(contents).exists().isDirectory();
        assertThat(new File(contents, "Info.plist")).exists();
        assertThat(new File(contents, "MacOS")).exists().isDirectory();
        assertThat(new File(contents, "Resources")).exists().isDirectory();
    }

    @Test
    @EnabledOnOs(OS.MAC) // DMG extraction requires macOS-specific hdiutil command
    void full_dmg_extraction_and_app_discovery_workflow_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data ---
        File dmgFile = TestUtils.getFromResource("gameupdate/dmg/game-macos.dmg");

        // execute test ---
        File extractedApp = GameUpdateManager.extractGameBinary(dmgFile, tempDir.toFile());

        // verify assertions ---
        assertThat(extractedApp).isNotNull();

        // verify Info.plist content
        String plistContent = Files.readString(extractedApp.toPath().resolve("Contents/Info.plist"));
        assertThat(plistContent).contains("CFBundleExecutable");
        assertThat(plistContent).contains("TestGame");
    }

    @Test
    void full_download_and_extract_workflow_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data - serve a real ZIP ---
        File testZip = TestUtils.getFromResource("gameupdate/zip/game-macos.zip");
        byte[] zipContent = Files.readAllBytes(testZip.toPath());

        mockServer.createContext("/game.zip", exchange -> {
            exchange.sendResponseHeaders(200, zipContent.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(zipContent);
            }
        });

        File downloadDest = tempDir.resolve("downloaded.zip").toFile();

        // execute download ---
        boolean downloadResult = GameUpdateManager.downloadFile(
            serverBaseUrl + "/game.zip",
            downloadDest,
            progress -> {}
        );

        // verify download ---
        assertThat(downloadResult).isTrue();
        assertThat(downloadDest).exists();

        // execute extraction ---
        File extractedApp = GameUpdateManager.extractGameBinary(downloadDest, tempDir.toFile());

        // verify extraction ---
        assertThat(extractedApp).isNotNull();
        assertThat(extractedApp.getName()).isEqualTo("TestGame.app");
    }

    @Test
    void full_trash_and_replace_workflow_success(@TempDir Path tempDir, @TempDir Path trashDir) throws IOException {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomTrashedGamePath).thenReturn(trashDir);

            // create old app
            File oldApp = tempDir.resolve("OldGame.app").toFile();
            Files.createDirectories(oldApp.toPath().resolve("Contents/MacOS"));
            Files.writeString(oldApp.toPath().resolve("Contents/Info.plist"), "OLD VERSION");

            // create new app
            File newApp = tempDir.resolve("NewGame.app").toFile();
            Files.createDirectories(newApp.toPath().resolve("Contents/MacOS"));
            Files.writeString(newApp.toPath().resolve("Contents/Info.plist"), "NEW VERSION");

            // execute trash ---
            boolean trashResult = GameUpdateManager.moveToTrash(oldApp);
            assertThat(trashResult).isTrue();
            assertThat(oldApp).doesNotExist();

            // move new app to old location ---
            FileUtils.moveDirectory(newApp, oldApp);

            // verify ---
            assertThat(oldApp).exists();
            String content = Files.readString(oldApp.toPath().resolve("Contents/Info.plist"));
            assertThat(content).isEqualTo("NEW VERSION");
        }
    }
}
