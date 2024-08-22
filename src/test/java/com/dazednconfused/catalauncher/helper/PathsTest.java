package com.dazednconfused.catalauncher.helper;

import com.dazednconfused.catalauncher.Application;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.Path;

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
            Path result = Paths.getLauncherRootFolder();

            // verify assertions ---
            assertThat(result).isEqualTo(Path.of(MOCKED_APPLICATION_ROOT));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT);
        }
    }

    @Test
    void get_launcher_files_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getLauncherFiles();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder().resolve(".macatalauncher"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/.macatalauncher");
        }
    }

    @Test
    void get_log_file_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getLogFilePath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherFiles().resolve("logs/main.log"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/.macatalauncher/logs/main.log");
        }
    }

    @Test
    void get_custom_save_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getCustomSavePath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder().resolve("saves"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/saves");
        }
    }

    @Test
    void get_custom_trashed_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getCustomTrashedPath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder().resolve("trashed"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/trashed");
        }
    }

    @Test
    void get_custom_trashed_save_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getCustomTrashedSavePath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getCustomTrashedPath().resolve("saves"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/trashed/saves");
        }
    }

    @Test
    void get_save_backup_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getSaveBackupPath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder().resolve("backups"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/backups");
        }
    }

    @Test
    void get_custom_user_dir_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getCustomUserDir();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherRootFolder().resolve("userdir"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/userdir");
        }
    }

    @Test
    void get_custom_soundpacks_dir_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getCustomSoundpacksDir();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getCustomUserDir().resolve("sound"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT+ "/userdir/sound");

        }
    }

    @Test
    void get_custom_mods_dir_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getCustomModsDir();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getCustomUserDir().resolve("mods"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/userdir/mods");
        }
    }

    @Test
    void get_custom_trashed_mods_path_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getCustomTrashedModsPath();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getCustomTrashedPath().resolve("mods"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/trashed/mods");
        }
    }

    @Test
    void get_database_directory_success() {
        try (MockedStatic<Application> mockedSystem = mockStatic(Application.class)) {

            // prepare mock data ---
            mockedSystem.when(Application::getRootFolder).thenReturn(MOCKED_APPLICATION_ROOT);

            // execute test ---
            Path result = Paths.getDatabaseDirectory();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getLauncherFiles().resolve("db"));
            assertThat(result.toString()).isEqualTo(MOCKED_APPLICATION_ROOT + "/.macatalauncher/db");
        }
    }
}