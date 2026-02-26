package com.dazednconfused.catalauncher.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.dazednconfused.catalauncher.configuration.ConfigurationManager;

import org.junit.jupiter.api.Test;
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
}
