package com.dazednconfused.catalauncher.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.dazednconfused.catalauncher.assertions.CustomFileAssertions;
import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.utils.TestUtils;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

    @Test
    void backup_current_saves_success_multiple_saves_shallow_assertions(@TempDir Path mockedSavePath, @TempDir Path mockedBackupPath, @TempDir Path mockedAssertionFolder) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.toString());
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath.toString());

            AtomicInteger calledTimes = new AtomicInteger(0);
            AtomicReference<List<Integer>> calledWith = new AtomicReference<>(new ArrayList<>());
            Consumer<Integer> MOCKED_CALLBACK = value -> {
                calledTimes.incrementAndGet();
                calledWith.get().add(value);
            };

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).exists();
            assertThat(new File(Paths.getSaveBackupPath())).exists();

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
            Optional<Thread> result = SaveManager.backupCurrentSaves(MOCKED_CALLBACK);
            result.ifPresent(Thread::run);

            // verify assertions ---
            assertThat(result).isNotEmpty();

            // callback assertions -
            assertThat(calledTimes).hasPositiveValue();
            assertThat(calledWith).matches(ints -> !ints.get().isEmpty());

            // backup assertions -
            File MOCKED_CUSTOM_BACKUP_PATH = new File(Paths.getSaveBackupPath());
            assertThat(MOCKED_CUSTOM_BACKUP_PATH.listFiles()).hasSize(1);

            File MOCKED_ACTUAL_BACKUP = Objects.requireNonNull(MOCKED_CUSTOM_BACKUP_PATH.listFiles())[0];
            assertThat(MOCKED_ACTUAL_BACKUP).exists();
            assertThat(MOCKED_ACTUAL_BACKUP.getName()).contains( // assert that trashed mod is timestamp-ed
                new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
            );

            // backup content assertions -
            File MOCKED_ASSERTION_FOLDER = mockedAssertionFolder.toFile();

            assertThat(MOCKED_ASSERTION_FOLDER).exists();
            TestUtils.unzip(MOCKED_ACTUAL_BACKUP.getPath(), mockedAssertionFolder.toString());

            assertThat(MOCKED_ASSERTION_FOLDER.listFiles()).hasSize(1);
            File MOCKED_ACTUAL_BACKUP_CONTENTS = Objects.requireNonNull(MOCKED_ASSERTION_FOLDER.listFiles())[0];

            assertThat(MOCKED_ACTUAL_BACKUP_CONTENTS)
                .isDirectoryContaining(file -> file.getName().startsWith("Braintree"))
                .isDirectoryContaining(file -> file.getName().startsWith("San Perlita"))
                .isDirectoryContaining(file -> file.getName().startsWith("Stiles"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void backup_current_saves_success_single_save_deep_assertions(@TempDir Path mockedSavePath, @TempDir Path mockedBackupPath, @TempDir Path mockedAssertionFolder) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.toString());
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath.toString());

            AtomicInteger calledTimes = new AtomicInteger(0);
            AtomicReference<List<Integer>> calledWith = new AtomicReference<>(new ArrayList<>());
            Consumer<Integer> MOCKED_CALLBACK = value -> {
                calledTimes.incrementAndGet();
                calledWith.get().add(value);
            };

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).exists();
            assertThat(new File(Paths.getSaveBackupPath())).exists();

            File MOCKED_BACKUP = new File(Paths.getCustomSavePath(), "Stiles");
            FileUtils.copyDirectory(
                TestUtils.getFromResource("save/sample/Stiles"),
                MOCKED_BACKUP
            );

            // execute test ---
            Optional<Thread> result = SaveManager.backupCurrentSaves(MOCKED_CALLBACK);
            result.ifPresent(Thread::run);

            // verify assertions ---
            assertThat(result).isNotEmpty();

            // callback assertions -
            assertThat(calledTimes).hasPositiveValue();
            assertThat(calledWith).matches(ints -> !ints.get().isEmpty());

            // backup assertions -
            File MOCKED_CUSTOM_BACKUP_PATH = new File(Paths.getSaveBackupPath());
            assertThat(MOCKED_CUSTOM_BACKUP_PATH.listFiles()).hasSize(1);

            File MOCKED_ACTUAL_BACKUP = Objects.requireNonNull(MOCKED_CUSTOM_BACKUP_PATH.listFiles())[0];
            assertThat(MOCKED_ACTUAL_BACKUP).exists();
            assertThat(MOCKED_ACTUAL_BACKUP.getName()).contains( // assert that trashed mod is timestamp-ed
                new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
            );

            // backup content assertions -
            File MOCKED_ASSERTION_FOLDER = mockedAssertionFolder.toFile();

            assertThat(MOCKED_ASSERTION_FOLDER).exists();
            TestUtils.unzip(MOCKED_ACTUAL_BACKUP.getPath(), mockedAssertionFolder.toString());

            assertThat(MOCKED_ASSERTION_FOLDER.listFiles()).hasSize(1);
            File MOCKED_ACTUAL_BACKUP_CONTENTS = Objects.requireNonNull(MOCKED_ASSERTION_FOLDER.listFiles())[0];

            CustomFileAssertions.assertThat( // assert that mod is available in trash folder
                MOCKED_ACTUAL_BACKUP_CONTENTS
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "Stiles/maps/2.0.1/70.30.1.map",
                "Stiles/maps/2.0.1/71.31.1.map",
                "Stiles/maps/2.0.1/72.30.1.map",
                "Stiles/maps/2.0.1/73.30.1.map",
                "Stiles/maps/2.0.1/69.31.1.map",
                "Stiles/maps/2.0.1/69.30.1.map",
                "Stiles/maps/2.0.0/70.30.0.map",
                "Stiles/maps/2.0.0/71.30.0.map",
                "Stiles/maps/2.0.0/70.29.0.map",
                "Stiles/maps/2.0.0/71.29.0.map",
                "Stiles/maps/2.0.0/70.31.0.map",
                "Stiles/maps/2.0.0/71.31.0.map",
                "Stiles/maps/2.0.0/69.31.0.map",
                "Stiles/maps/2.0.0/73.30.0.map",
                "Stiles/maps/2.0.0/72.30.0.map",
                "Stiles/maps/2.0.0/73.29.0.map",
                "Stiles/maps/2.0.0/72.29.0.map",
                "Stiles/maps/2.0.0/74.31.0.map",
                "Stiles/maps/2.0.0/75.31.0.map",
                "Stiles/maps/2.0.0/73.31.0.map",
                "Stiles/maps/2.0.0/72.31.0.map",
                "Stiles/maps/2.0.0/69.30.0.map",
                "Stiles/maps/2.0.0/74.30.0.map",
                "Stiles/maps/2.0.0/75.30.0.map",
                "Stiles/maps/2.0.0/74.29.0.map",
                "Stiles/maps/2.0.0/75.29.0.map",
                "Stiles/maps/2.1.-1/72.33.-1.map",
                "Stiles/maps/2.1.-1/70.34.-1.map",
                "Stiles/maps/2.1.-1/73.34.-1.map",
                "Stiles/maps/2.1.-1/69.33.-1.map",
                "Stiles/maps/2.1.-1/75.34.-1.map",
                "Stiles/maps/2.1.-1/72.34.-1.map",
                "Stiles/maps/2.1.-1/71.33.-1.map",
                "Stiles/maps/2.1.-1/74.34.-1.map",
                "Stiles/maps/2.1.1/71.34.1.map",
                "Stiles/maps/2.1.1/70.34.1.map",
                "Stiles/maps/2.1.1/71.33.1.map",
                "Stiles/maps/2.1.1/69.33.1.map",
                "Stiles/maps/2.1.1/73.32.1.map",
                "Stiles/maps/2.1.1/72.34.1.map",
                "Stiles/maps/2.1.1/73.34.1.map",
                "Stiles/maps/2.1.1/75.34.1.map",
                "Stiles/maps/2.1.1/74.34.1.map",
                "Stiles/maps/2.1.1/72.33.1.map",
                "Stiles/maps/2.0.-1/70.30.-1.map",
                "Stiles/maps/2.0.-1/69.30.-1.map",
                "Stiles/maps/2.0.-1/69.31.-1.map",
                "Stiles/maps/2.0.-1/72.30.-1.map",
                "Stiles/maps/2.0.-1/71.31.-1.map",
                "Stiles/maps/2.1.0/70.34.0.map",
                "Stiles/maps/2.1.0/71.34.0.map",
                "Stiles/maps/2.1.0/70.32.0.map",
                "Stiles/maps/2.1.0/71.32.0.map",
                "Stiles/maps/2.1.0/70.35.0.map",
                "Stiles/maps/2.1.0/71.35.0.map",
                "Stiles/maps/2.1.0/70.33.0.map",
                "Stiles/maps/2.1.0/71.33.0.map",
                "Stiles/maps/2.1.0/73.32.0.map",
                "Stiles/maps/2.1.0/72.32.0.map",
                "Stiles/maps/2.1.0/69.33.0.map",
                "Stiles/maps/2.1.0/74.35.0.map",
                "Stiles/maps/2.1.0/75.35.0.map",
                "Stiles/maps/2.1.0/69.35.0.map",
                "Stiles/maps/2.1.0/73.34.0.map",
                "Stiles/maps/2.1.0/72.34.0.map",
                "Stiles/maps/2.1.0/74.33.0.map",
                "Stiles/maps/2.1.0/75.33.0.map",
                "Stiles/maps/2.1.0/74.34.0.map",
                "Stiles/maps/2.1.0/75.34.0.map",
                "Stiles/maps/2.1.0/69.32.0.map",
                "Stiles/maps/2.1.0/73.33.0.map",
                "Stiles/maps/2.1.0/72.33.0.map",
                "Stiles/maps/2.1.0/74.32.0.map",
                "Stiles/maps/2.1.0/75.32.0.map",
                "Stiles/maps/2.1.0/73.35.0.map",
                "Stiles/maps/2.1.0/72.35.0.map",
                "Stiles/maps/2.1.0/69.34.0.map",
                "Stiles/uistate.json",
                "Stiles/cache/mods.json.1676170980000.fb",
                "Stiles/cache/worldoptions.json.1676170980000.fb",
                "Stiles/#Q2hpbiBNaWxuZXI=.sfm.json",
                "Stiles/mods.json",
                "Stiles/#Q2hpbiBNaWxuZXI=.sav",
                "Stiles/#Q2hpbiBNaWxuZXI=.ano.json",
                "Stiles/zones.json",
                "Stiles/worldoptions.json",
                "Stiles/world_timestamp.json",
                "Stiles/#Q2hpbiBNaWxuZXI=.zones.json",
                "Stiles/master.gsav",
                "Stiles/#Q2hpbiBNaWxuZXI=.seen.0.0",
                "Stiles/o.0.0",
                "Stiles/#Q2hpbiBNaWxuZXJfZGlhcnk=.json",
                "Stiles/#Q2hpbiBNaWxuZXI=.log",
                "Stiles/#Q2hpbiBNaWxuZXI=.mm1/18.7.0.mmr",
                "Stiles/#Q2hpbiBNaWxuZXI=.mm1/17.8.0.mmr",
                "Stiles/#Q2hpbiBNaWxuZXI=.mm1/18.8.0.mmr",
                "Stiles/#Q2hpbiBNaWxuZXI=.mm1/17.7.0.mmr",
                "Stiles/#Q2hpbiBNaWxuZXI=.apu.json"
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void backup_current_saves_success_empty_when_no_saves_are_found(@TempDir Path mockedSavePath, @TempDir Path mockedBackupPath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.toString());
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath.toString());

            AtomicInteger calledTimes = new AtomicInteger(0);
            AtomicReference<List<Integer>> calledWith = new AtomicReference<>(new ArrayList<>());
            Consumer<Integer> MOCKED_CALLBACK = value -> {
                calledTimes.incrementAndGet();
                calledWith.get().add(value);
            };

            // pre-test assertions ---
            assertThat(new File(Paths.getCustomSavePath())).exists().isEmptyDirectory();
            assertThat(new File(Paths.getSaveBackupPath())).exists();

            // execute test ---
            Optional<Thread> result = SaveManager.backupCurrentSaves(MOCKED_CALLBACK);

            // verify assertions ---
            assertThat(result).isEmpty();

            // callback assertions -
            assertThat(calledTimes).hasValue(0);
            assertThat(calledWith).matches(ints -> ints.get().isEmpty());

            // backup assertions -
            File MOCKED_CUSTOM_BACKUP_PATH = new File(Paths.getSaveBackupPath());
            assertThat(MOCKED_CUSTOM_BACKUP_PATH).isEmptyDirectory();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}