package com.dazednconfused.catalauncher.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.utils.TestUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

class SaveManagerTest {

    @Test
    void list_all_backups_success(@TempDir Path mockedBackupPath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath.toString());

            // pre-test assertions ---
            assertThat(new File(Paths.getSaveBackupPath())).exists();

            File MOCKED_BACKUP_1 = new File(Paths.getSaveBackupPath(), "20230216_111637.zip");
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230216_111637.zip"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = new File(Paths.getSaveBackupPath(), "20230217_115559.zip");
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230217_115559.zip"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = new File(Paths.getSaveBackupPath(), "20240808_205649.zip");
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205649.zip"),
                MOCKED_BACKUP_3
            );

            File MOCKED_BACKUP_4 = new File(Paths.getSaveBackupPath(), "20240808_205736.zip");
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205736.zip"),
                MOCKED_BACKUP_4
            );

            // execute test ---
            List<File> result = SaveManager.listAllBackups();

            // verify assertions ---
            assertThat(result).containsExactlyInAnyOrder(
                MOCKED_BACKUP_1,
                MOCKED_BACKUP_2,
                MOCKED_BACKUP_3,
                MOCKED_BACKUP_4
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void save_files_exist_success(@TempDir Path mockedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.toString());

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).exists();

            File MOCKED_BACKUP_1 = new File(Paths.getCustomSavePath(), "20230216_111637.zip");
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230216_111637.zip"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = new File(Paths.getCustomSavePath(), "20230217_115559.zip");
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230217_115559.zip"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = new File(Paths.getCustomSavePath(), "20240808_205649.zip");
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205649.zip"),
                MOCKED_BACKUP_3
            );

            File MOCKED_BACKUP_4 = new File(Paths.getCustomSavePath(), "20240808_205736.zip");
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205736.zip"),
                MOCKED_BACKUP_4
            );

            // execute test ---
            boolean result = SaveManager.saveFilesExist();

            // verify assertions ---
            assertThat(result).isTrue();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void save_files_exist_success_empty_folder(@TempDir Path mockedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.toString());

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).exists();
            assertThat(new File(Paths.getCustomSavePath()).listFiles()).isEmpty();

            // execute test ---
            boolean result = SaveManager.saveFilesExist();

            // verify assertions ---
            assertThat(result).isFalse();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void save_files_exist_success_non_existent_folder(@TempDir Path mockedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.resolve("a/missing/folder").toString());

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).doesNotExist();

            // execute test ---
            boolean result = SaveManager.saveFilesExist();

            // verify assertions ---
            assertThat(result).isFalse();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void get_latest_save_success(@TempDir Path mockedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.toString());

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).exists();

            File MOCKED_BACKUP_1 = new File(Paths.getCustomSavePath(), "Braintree");
            FileUtils.copyDirectory(
                TestUtils.getFromResource("save/sample/Braintree"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = new File(Paths.getCustomSavePath(), "San Perlita");
            FileUtils.copyDirectory(
                TestUtils.getFromResource("save/sample/San Perlita"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = new File(Paths.getCustomSavePath(), "Stiles");
            FileUtils.copyDirectory(
                TestUtils.getFromResource("save/sample/Stiles"),
                MOCKED_BACKUP_3
            );

            // execute test ---
            Optional<File> result = SaveManager.getLatestSave();

            // verify assertions ---
            assertThat(result).isNotEmpty().contains(
                MOCKED_BACKUP_3
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void get_latest_save_success_empty_folder(@TempDir Path mockedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.toString());

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).exists();
            assertThat(new File(Paths.getCustomSavePath()).listFiles()).isEmpty();

            // execute test ---
            Optional<File> result = SaveManager.getLatestSave();

            // verify assertions ---
            assertThat(result).isEmpty();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void get_latest_save_success_non_existent_folder(@TempDir Path mockedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.resolve("a/missing/folder").toString());

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).doesNotExist();

            // execute test ---
            Optional<File> result = SaveManager.getLatestSave();

            // verify assertions ---
            assertThat(result).isEmpty();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void get_latest_save_success_only_invalid_saves_available(@TempDir Path mockedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.toString());

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).exists();

            File MOCKED_BACKUP_1 = new File(Paths.getCustomSavePath(), "Braintree");
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230216_111637.zip"),
                MOCKED_BACKUP_1
            );

            // execute test ---
            Optional<File> result = SaveManager.getLatestSave();

            // verify assertions ---
            assertThat(result).isEmpty();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}