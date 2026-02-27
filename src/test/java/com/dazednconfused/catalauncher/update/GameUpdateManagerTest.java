package com.dazednconfused.catalauncher.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.dazednconfused.catalauncher.configuration.ConfigurationManager;
import com.dazednconfused.catalauncher.utils.TestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class GameUpdateManagerTest {

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
    void isInstalledVersionConfigured_returns_true_when_experimental_version_set_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getInstalledGameVersion()).thenReturn("cdda-experimental-2024-01-15-0123");

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

    @Test
    void isInstalledVersionConfigured_returns_false_when_version_empty_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getInstalledGameVersion()).thenReturn("");

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

    @Test
    void isCddaPathConfigured_returns_false_when_path_empty_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getCddaPath()).thenReturn("");

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
    void isOfficialCddaRepo_returns_true_uppercase_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CLEVERRAVEN");
            when(mockConfig.getGameGithubRepoName()).thenReturn("CATACLYSM-DDA");

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
    void isOfficialCddaRepo_returns_false_for_different_repo_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("SomeOtherRepo");

            // execute test ---
            boolean result = GameUpdateManager.isOfficialCddaRepo();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isOfficialCddaRepo_returns_false_for_cataclysm_bn_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("cataclysmbnteam");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-BN");

            // execute test ---
            boolean result = GameUpdateManager.isOfficialCddaRepo();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isOfficialCddaRepo_returns_false_when_owner_null_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn(null);
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            // execute test ---
            boolean result = GameUpdateManager.isOfficialCddaRepo();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    @Test
    void isOfficialCddaRepo_returns_false_when_repo_null_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn(null);

            // execute test ---
            boolean result = GameUpdateManager.isOfficialCddaRepo();

            // verify assertions ---
            assertThat(result).isFalse();
        }
    }

    // ========================================
    // isGameUpdateAvailable() tests
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
    void isGameUpdateAvailable_returns_empty_when_installed_version_blank_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getInstalledGameVersion()).thenReturn("   ");

            // execute test ---
            var result = GameUpdateManager.isGameUpdateAvailable();

            // verify assertions ---
            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // getLatestGameReleaseTag() tests
    // ========================================

    @Test
    void getLatestGameReleaseTag_returns_empty_when_owner_not_configured_success() {
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

    @Test
    void getLatestGameReleaseTag_returns_empty_when_repo_not_configured_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn(null);

            // execute test ---
            var result = GameUpdateManager.getLatestGameReleaseTag();

            // verify assertions ---
            assertThat(result).isEmpty();
        }
    }

    @Test
    void getLatestGameReleaseTag_returns_empty_when_both_blank_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("   ");
            when(mockConfig.getGameGithubRepoName()).thenReturn("");

            // execute test ---
            var result = GameUpdateManager.getLatestGameReleaseTag();

            // verify assertions ---
            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // getMacOsDownloadUrl() tests
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

    @Test
    void getMacOsDownloadUrl_returns_empty_when_owner_blank_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("   ");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");

            // execute test ---
            var result = GameUpdateManager.getMacOsDownloadUrl();

            // verify assertions ---
            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // downloadAndInstallUpdate() tests
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
    void downloadAndInstallUpdate_returns_false_when_cdda_path_blank_success() {
        try (MockedStatic<ConfigurationManager> mockedConfigManager = mockStatic(ConfigurationManager.class)) {

            // prepare mock data ---
            ConfigurationManager mockConfig = Mockito.mock(ConfigurationManager.class);
            mockedConfigManager.when(ConfigurationManager::getInstance).thenReturn(mockConfig);
            when(mockConfig.getGameGithubRepoOwner()).thenReturn("CleverRaven");
            when(mockConfig.getGameGithubRepoName()).thenReturn("Cataclysm-DDA");
            when(mockConfig.getCddaPath()).thenReturn("   ");

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

    // ========================================
    // extractFileName() tests
    // ========================================

    @Test
    void extractFileName_simple_url_success() {
        // execute test ---
        String result = GameUpdateManager.extractFileName("https://github.com/releases/download/v1.0/game.zip");

        // verify assertions ---
        assertThat(result).isEqualTo("game.zip");
    }

    @Test
    void extractFileName_url_with_query_params_success() {
        // execute test ---
        String result = GameUpdateManager.extractFileName("https://github.com/releases/game.dmg?token=abc123");

        // verify assertions ---
        assertThat(result).isEqualTo("game.dmg");
    }

    @Test
    void extractFileName_url_ending_with_slash_success() {
        // execute test ---
        String result = GameUpdateManager.extractFileName("https://github.com/releases/");

        // verify assertions ---
        assertThat(result).isEqualTo("download.zip");
    }

    @Test
    void extractFileName_url_no_slash_success() {
        // execute test ---
        String result = GameUpdateManager.extractFileName("game.zip");

        // verify assertions ---
        assertThat(result).isEqualTo("download.zip");
    }

    @Test
    void extractFileName_complex_filename_success() {
        // execute test ---
        String result = GameUpdateManager.extractFileName(
            "https://github.com/CleverRaven/Cataclysm-DDA/releases/download/cdda-experimental-2024-01-15-0123/cdda-osx-tiles-universal-2024-01-15-0123.dmg"
        );

        // verify assertions ---
        assertThat(result).isEqualTo("cdda-osx-tiles-universal-2024-01-15-0123.dmg");
    }

    @Test
    void extractFileName_url_with_multiple_query_params_success() {
        // execute test ---
        String result = GameUpdateManager.extractFileName(
            "https://cdn.example.com/download/game-v2.0.zip?token=abc&expires=123456"
        );

        // verify assertions ---
        assertThat(result).isEqualTo("game-v2.0.zip");
    }

    // ========================================
    // findAppBundle() tests
    // ========================================

    @Test
    void findAppBundle_finds_app_at_root_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data ---
        Path appPath = tempDir.resolve("TestGame.app");
        Files.createDirectories(appPath.resolve("Contents/MacOS"));
        Files.createFile(appPath.resolve("Contents/Info.plist"));

        // execute test ---
        File result = GameUpdateManager.findAppBundle(tempDir.toFile());

        // verify assertions ---
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TestGame.app");
    }

    @Test
    void findAppBundle_returns_null_when_no_app_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data ---
        Files.createDirectories(tempDir.resolve("SomeFolder"));
        Files.createFile(tempDir.resolve("SomeFolder/file.txt"));

        // execute test ---
        File result = GameUpdateManager.findAppBundle(tempDir.toFile());

        // verify assertions ---
        assertThat(result).isNull();
    }

    @Test
    void findAppBundle_returns_first_app_when_multiple_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data ---
        Path app1 = tempDir.resolve("AGame.app");
        Path app2 = tempDir.resolve("BGame.app");
        Files.createDirectories(app1);
        Files.createDirectories(app2);

        // execute test ---
        File result = GameUpdateManager.findAppBundle(tempDir.toFile());

        // verify assertions ---
        assertThat(result).isNotNull();
        assertThat(result.getName()).endsWith(".app");
    }

    @Test
    void findAppBundle_returns_null_for_null_directory_success() {
        // execute test ---
        File result = GameUpdateManager.findAppBundle(null);

        // verify assertions ---
        assertThat(result).isNull();
    }

    @Test
    void findAppBundle_returns_null_for_nonexistent_directory_success() {
        // prepare mock data ---
        File nonExistent = new File("/nonexistent/directory/path");

        // execute test ---
        File result = GameUpdateManager.findAppBundle(nonExistent);

        // verify assertions ---
        assertThat(result).isNull();
    }

    // ========================================
    // findLargestMacOsAsset() tests (JSON parsing)
    // ========================================

    @Test
    void findLargestMacOsAsset_selects_largest_osx_dmg_success() throws IOException {
        // prepare mock data ---
        File jsonFile = TestUtils.getFromResource("gameupdate/json/releases_latest.json");
        String json = Files.readString(jsonFile.toPath());

        // execute test ---
        String result = GameUpdateManager.findLargestMacOsAsset(json);

        // verify assertions ---
        // The OSX-Tiles.dmg (180000000 bytes) should be selected over OSX-Curses.dmg (50000000 bytes)
        assertThat(result).contains("OSX-Tiles.dmg");
        assertThat(result).doesNotContain("Curses");
    }

    @Test
    void findLargestMacOsAsset_selects_largest_from_experimental_success() throws IOException {
        // prepare mock data ---
        File jsonFile = TestUtils.getFromResource("gameupdate/json/releases_experimental.json");
        String json = Files.readString(jsonFile.toPath());

        // execute test ---
        String result = GameUpdateManager.findLargestMacOsAsset(json);

        // verify assertions ---
        // The osx-tiles (200000000 bytes) should be selected over osx-curses (55000000 bytes)
        assertThat(result).contains("osx-tiles");
        assertThat(result).doesNotContain("curses");
    }

    @Test
    void findLargestMacOsAsset_throws_when_no_macos_asset_success() {
        // prepare mock data ---
        String json = "{\"assets\": [{\"name\": \"linux.tar.gz\", \"size\": 100, \"browser_download_url\": \"http://example.com/linux.tar.gz\"}]}";

        // execute test & verify assertions ---
        assertThatThrownBy(() -> GameUpdateManager.findLargestMacOsAsset(json))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("No macOS download found");
    }

    @Test
    void findLargestMacOsAsset_recognizes_darwin_pattern_success() throws IOException {
        // prepare mock data ---
        File jsonFile = TestUtils.getFromResource("gameupdate/json/releases_macos_zip.json");
        String json = Files.readString(jsonFile.toPath());

        // execute test ---
        String result = GameUpdateManager.findLargestMacOsAsset(json);

        // verify assertions ---
        // darwin-x64.zip (120000000) should be selected over macos-arm64.zip (90000000)
        assertThat(result).contains("darwin-x64.zip");
    }

    @Test
    void findLargestMacOsAsset_ignores_sha256_files_success() throws IOException {
        // prepare mock data ---
        File jsonFile = TestUtils.getFromResource("gameupdate/json/releases_latest.json");
        String json = Files.readString(jsonFile.toPath());

        // execute test ---
        String result = GameUpdateManager.findLargestMacOsAsset(json);

        // verify assertions ---
        assertThat(result).doesNotContain(".sha256");
    }

    // ========================================
    // extractGameBinary() tests (ZIP extraction)
    // ========================================

    @Test
    void extractGameBinary_extracts_zip_with_app_bundle_success(@TempDir Path tempDir) {
        // prepare mock data ---
        File zipFile = TestUtils.getFromResource("gameupdate/zip/game-macos.zip");

        // execute test ---
        File result = GameUpdateManager.extractGameBinary(zipFile, tempDir.toFile());

        // verify assertions ---
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TestGame.app");
        assertThat(result.isDirectory()).isTrue();
        assertThat(new File(result, "Contents/Info.plist")).exists();
        assertThat(new File(result, "Contents/MacOS/TestGame")).exists();
    }

    @Test
    void extractGameBinary_returns_null_for_unknown_format_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data ---
        File unknownFile = tempDir.resolve("game.tar.gz").toFile();
        Files.createFile(unknownFile.toPath());

        // execute test ---
        File result = GameUpdateManager.extractGameBinary(unknownFile, tempDir.toFile());

        // verify assertions ---
        assertThat(result).isNull();
    }

    @Test
    void extractGameBinary_returns_app_file_directly_success(@TempDir Path tempDir) throws IOException {
        // prepare mock data ---
        Path appPath = tempDir.resolve("DirectGame.app");
        Files.createDirectories(appPath);

        // execute test ---
        File result = GameUpdateManager.extractGameBinary(appPath.toFile(), tempDir.toFile());

        // verify assertions ---
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("DirectGame.app");
    }

    // ========================================
    // extractGameBinary() tests (DMG extraction)
    // ========================================

    @Test
    void extractGameBinary_extracts_dmg_with_app_bundle_success(@TempDir Path tempDir) {
        // prepare mock data ---
        File dmgFile = TestUtils.getFromResource("gameupdate/dmg/game-macos.dmg");

        // execute test ---
        File result = GameUpdateManager.extractGameBinary(dmgFile, tempDir.toFile());

        // verify assertions ---
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TestGame.app");
        assertThat(result.isDirectory()).isTrue();
        assertThat(new File(result, "Contents/Info.plist")).exists();
        assertThat(new File(result, "Contents/MacOS/TestGame")).exists();
        assertThat(new File(result, "Contents/Resources/game.dat")).exists();
    }
}
