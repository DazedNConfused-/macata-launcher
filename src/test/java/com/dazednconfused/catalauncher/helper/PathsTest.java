package com.dazednconfused.catalauncher.helper;

import com.dazednconfused.catalauncher.Application;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class PathsTest {

    private static final String MOCKED_APPLICATION_ROOT = "/binary/root/folder";

    @Test
    void get_launcher_root_folder_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            String result = Paths.getLauncherRootFolder();

            // verify assertions ---
            assertThat(result).isEqualTo(MOCKED_APPLICATION_ROOT);
        }
    }

    @Test
    void get_launcher_files_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            String result = Paths.getLauncherFiles();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder() + "/.macatalauncher");
        }
    }

    @Test
    void get_log_file_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            String result = Paths.getLogFilePath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder() + "/.macatalauncher/logs/main.log");
        }
    }

    @Test
    void get_custom_save_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            String result = Paths.getCustomSavePath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder() + "/saves/");
        }
    }

    @Test
    void get_custom_trashed_save_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            String result = Paths.getCustomTrashedSavePath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder() + "/trashed/saves/");
        }
    }

    @Test
    void get_save_backup_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            String result = Paths.getSaveBackupPath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder() + "/backups");
        }
    }

    @Test
    void get_custom_user_dir_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            String result = Paths.getCustomUserDir();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder() + "/userdir/");
        }
    }

    @Test
    void get_custom_soundpacks_dir_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            String result = Paths.getCustomSoundpacksDir();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder() + "/userdir/sound/");
        }
    }

    @Test
    void get_custom_mods_dir_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            String result = Paths.getCustomModsDir();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder() + "/userdir/mods/");
        }
    }
}