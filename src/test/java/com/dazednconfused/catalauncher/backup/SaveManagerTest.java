package com.dazednconfused.catalauncher.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.dazednconfused.catalauncher.assertions.CustomFileAssertions;
import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.helper.result.Result;
import com.dazednconfused.catalauncher.utils.TestUtils;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath);

            // pre-test assertions ---
            assertThat(Paths.getSaveBackupPath().toFile()).exists();

            File MOCKED_BACKUP_1 = Paths.getSaveBackupPath().resolve("20230216_111637.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230216_111637.zip"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = Paths.getSaveBackupPath().resolve("20230217_115559.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230217_115559.zip"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = Paths.getSaveBackupPath().resolve("20240808_205649.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205649.zip"),
                MOCKED_BACKUP_3
            );

            File MOCKED_BACKUP_4 = Paths.getSaveBackupPath().resolve("20240808_205736.zip").toFile();
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
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).exists();

            File MOCKED_BACKUP_1 = Paths.getCustomSavePath().resolve("20230216_111637.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230216_111637.zip"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = Paths.getCustomSavePath().resolve("20230217_115559.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230217_115559.zip"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = Paths.getCustomSavePath().resolve("20240808_205649.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205649.zip"),
                MOCKED_BACKUP_3
            );

            File MOCKED_BACKUP_4 = Paths.getCustomSavePath().resolve("20240808_205736.zip").toFile();
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
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).exists();
            assertThat(Paths.getCustomSavePath().toFile().listFiles()).isEmpty();

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
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.resolve("a/missing/folder"));

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).doesNotExist();

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
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).exists();

            File MOCKED_BACKUP = Paths.getCustomSavePath().resolve("Braintree").toFile();
            FileUtils.copyDirectory(
                TestUtils.getFromResource("save/sample/Stiles"),
                MOCKED_BACKUP
            );

            // execute test ---
            Optional<File> result = SaveManager.getLatestSave();

            // verify assertions ---
            assertThat(result).isNotEmpty().contains(
                MOCKED_BACKUP
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void get_latest_save_success_empty_when_latest_save_is_invalid(@TempDir Path mockedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).exists();

            File MOCKED_BACKUP = Paths.getCustomSavePath().resolve("Braintree").toFile();
            FileUtils.copyDirectory(
                TestUtils.getFromResource("save/sample/Braintree"),
                MOCKED_BACKUP
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
    void get_latest_save_success_empty_folder(@TempDir Path mockedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).exists();
            assertThat(Paths.getCustomSavePath().toFile().listFiles()).isEmpty();

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
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.resolve("a/missing/folder"));

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).doesNotExist();

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
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).exists();

            File MOCKED_BACKUP_1 = Paths.getCustomSavePath().resolve("Braintree").toFile();
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
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath);

            AtomicInteger calledTimes = new AtomicInteger(0);
            AtomicReference<List<Integer>> calledWith = new AtomicReference<>(new ArrayList<>());
            Consumer<Integer> MOCKED_CALLBACK = value -> {
                calledTimes.incrementAndGet();
                calledWith.get().add(value);
            };

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).exists();
            assertThat(Paths.getSaveBackupPath().toFile()).exists();

            File MOCKED_BACKUP_1 = Paths.getCustomSavePath().resolve("Braintree").toFile();
            FileUtils.copyDirectory(
                TestUtils.getFromResource("save/sample/Braintree"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = Paths.getCustomSavePath().resolve("San Perlita").toFile();
            FileUtils.copyDirectory(
                TestUtils.getFromResource("save/sample/San Perlita"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = Paths.getCustomSavePath().resolve("Stiles").toFile();
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
            File MOCKED_CUSTOM_BACKUP_PATH = Paths.getSaveBackupPath().toFile();
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
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath);

            AtomicInteger calledTimes = new AtomicInteger(0);
            AtomicReference<List<Integer>> calledWith = new AtomicReference<>(new ArrayList<>());
            Consumer<Integer> MOCKED_CALLBACK = value -> {
                calledTimes.incrementAndGet();
                calledWith.get().add(value);
            };

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).exists();
            assertThat(Paths.getSaveBackupPath().toFile()).exists();

            File MOCKED_BACKUP = Paths.getCustomSavePath().resolve("Stiles").toFile();
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
            File MOCKED_CUSTOM_BACKUP_PATH = Paths.getSaveBackupPath().toFile();
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

            CustomFileAssertions.assertThat(
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
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath);

            AtomicInteger calledTimes = new AtomicInteger(0);
            AtomicReference<List<Integer>> calledWith = new AtomicReference<>(new ArrayList<>());
            Consumer<Integer> MOCKED_CALLBACK = value -> {
                calledTimes.incrementAndGet();
                calledWith.get().add(value);
            };

            File MOCKED_CUSTOM_BACKUP_PATH = Paths.getSaveBackupPath().toFile();

            // pre-test assertions ---
            assertThat(Paths.getCustomSavePath().toFile()).exists().isEmptyDirectory();
            assertThat(MOCKED_CUSTOM_BACKUP_PATH).exists();

            // execute test ---
            Optional<Thread> result = SaveManager.backupCurrentSaves(MOCKED_CALLBACK);

            // verify assertions ---
            assertThat(result).isEmpty();

            // callback assertions -
            assertThat(calledTimes).hasValue(0);
            assertThat(calledWith).matches(ints -> ints.get().isEmpty());

            // backup assertions -
            assertThat(MOCKED_CUSTOM_BACKUP_PATH).isEmptyDirectory();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void delete_backup_success(@TempDir Path mockedSavePath, @TempDir Path mockedBackupPath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath);

            File MOCKED_SAVE_BACKUP_DIRECTORY = Paths.getSaveBackupPath().toFile();

            assertThat(Paths.getCustomSavePath().toFile()).exists();
            assertThat(MOCKED_SAVE_BACKUP_DIRECTORY).exists();

            File MOCKED_BACKUP_1 = Paths.getSaveBackupPath().resolve("20230216_111637.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230216_111637.zip"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = Paths.getSaveBackupPath().resolve("20230217_115559.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230217_115559.zip"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = Paths.getSaveBackupPath().resolve("20240808_205649.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205649.zip"),
                MOCKED_BACKUP_3
            );

            File MOCKED_BACKUP_4 = Paths.getSaveBackupPath().resolve("20240808_205736.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205736.zip"),
                MOCKED_BACKUP_4
            );

            // pre-test assertions ---
            assertThat(MOCKED_SAVE_BACKUP_DIRECTORY).isNotEmptyDirectory();

            CustomFileAssertions.assertThat(
                MOCKED_SAVE_BACKUP_DIRECTORY
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "20230216_111637.zip",
                "20230217_115559.zip",
                "20240808_205649.zip",
                "20240808_205736.zip"
            ));

            // execute test ---
            boolean result = SaveManager.deleteBackup(MOCKED_BACKUP_4);

            // verify assertions ---
            assertThat(result).isTrue();

            CustomFileAssertions.assertThat(
                MOCKED_SAVE_BACKUP_DIRECTORY
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "20230216_111637.zip",
                "20230217_115559.zip",
                "20240808_205649.zip"
            ));

            assertThat(SaveManager.listAllBackups()).containsExactlyInAnyOrder(
                MOCKED_BACKUP_1,
                MOCKED_BACKUP_2,
                MOCKED_BACKUP_3
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void rename_backup_success(@TempDir Path mockedSavePath, @TempDir Path mockedBackupPath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath);
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath);

            File MOCKED_SAVE_BACKUP_DIRECTORY = Paths.getSaveBackupPath().toFile();

            assertThat(Paths.getCustomSavePath().toFile()).exists();
            assertThat(MOCKED_SAVE_BACKUP_DIRECTORY).exists();

            File MOCKED_BACKUP_1 = Paths.getSaveBackupPath().resolve("20230216_111637.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230216_111637.zip"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = Paths.getSaveBackupPath().resolve("20230217_115559.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230217_115559.zip"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = Paths.getSaveBackupPath().resolve("20240808_205649.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205649.zip"),
                MOCKED_BACKUP_3
            );

            File MOCKED_BACKUP_4 = Paths.getSaveBackupPath().resolve("20240808_205736.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205736.zip"),
                MOCKED_BACKUP_4
            );

            // pre-test assertions ---
            assertThat(MOCKED_SAVE_BACKUP_DIRECTORY).isNotEmptyDirectory();

            CustomFileAssertions.assertThat(
                MOCKED_SAVE_BACKUP_DIRECTORY
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "20230216_111637.zip",
                "20230217_115559.zip",
                "20240808_205649.zip",
                "20240808_205736.zip"
            ));

            // execute test ---
            Result<Throwable, File> result = SaveManager.renameBackup(MOCKED_BACKUP_4, "aRenamedBackup");

            // verify assertions ---
            File MOCKED_RENAMED_BACKUP = result.getOrElseThrowUnchecked();
            assertThat(MOCKED_RENAMED_BACKUP).isNotNull();

            CustomFileAssertions.assertThat(
                MOCKED_SAVE_BACKUP_DIRECTORY
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "20230216_111637.zip",
                "20230217_115559.zip",
                "20240808_205649.zip",
                "aRenamedBackup.zip"
            ));

            assertThat(SaveManager.listAllBackups()).containsExactlyInAnyOrder(
                MOCKED_BACKUP_1,
                MOCKED_BACKUP_2,
                MOCKED_BACKUP_3,
                MOCKED_RENAMED_BACKUP
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void restore_backup_success_when_no_preexistent_save_to_trash_exists(@TempDir Path mockedSavePath, @TempDir Path mockedBackupPath, @TempDir Path mockedTrashedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.resolve("saves/"));
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath);
            mockedPaths.when(Paths::getCustomTrashedSavePath).thenReturn(mockedTrashedSavePath);

            File MOCKED_CUSTOM_SAVE_DIRECTORY = Paths.getCustomSavePath().toFile();
            File MOCKED_SAVE_BACKUP_DIRECTORY = Paths.getSaveBackupPath().toFile();
            File MOCKED_TRASHED_SAVE_DIRECTORY = Paths.getCustomTrashedSavePath().toFile();

            assertThat(MOCKED_CUSTOM_SAVE_DIRECTORY.mkdirs()).isTrue(); // we are using a custom relative path, which we must create first...

            assertThat(MOCKED_CUSTOM_SAVE_DIRECTORY).exists();
            assertThat(MOCKED_SAVE_BACKUP_DIRECTORY).exists();
            assertThat(MOCKED_TRASHED_SAVE_DIRECTORY).exists();

            File MOCKED_BACKUP_1 = Paths.getSaveBackupPath().resolve("20230216_111637.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230216_111637.zip"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = Paths.getSaveBackupPath().resolve("20230217_115559.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230217_115559.zip"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = Paths.getSaveBackupPath().resolve("20240808_205649.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205649.zip"),
                MOCKED_BACKUP_3
            );

            File MOCKED_BACKUP_4 = Paths.getSaveBackupPath().resolve("20240808_205736.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205736.zip"),
                MOCKED_BACKUP_4
            );

            AtomicInteger calledTimes = new AtomicInteger(0);
            AtomicReference<List<Integer>> calledWith = new AtomicReference<>(new ArrayList<>());
            Consumer<Integer> MOCKED_CALLBACK = value -> {
                calledTimes.incrementAndGet();
                calledWith.get().add(value);
            };

            // pre-test assertions ---
            assertThat(MOCKED_CUSTOM_SAVE_DIRECTORY).isEmptyDirectory();
            assertThat(MOCKED_TRASHED_SAVE_DIRECTORY).isEmptyDirectory();
            assertThat(MOCKED_SAVE_BACKUP_DIRECTORY).isNotEmptyDirectory();

            CustomFileAssertions.assertThat(
                MOCKED_SAVE_BACKUP_DIRECTORY
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "20230216_111637.zip",
                "20230217_115559.zip",
                "20240808_205649.zip",
                "20240808_205736.zip"
            ));

            // execute test ---
            Optional<Thread> result = SaveManager.restoreBackup(MOCKED_BACKUP_4, MOCKED_CALLBACK);
            result.ifPresent(Thread::run);

            // verify assertions ---
            assertThat(result).isNotEmpty();

            // callback assertions -
            assertThat(calledTimes).hasPositiveValue();
            assertThat(calledWith).matches(ints -> !ints.get().isEmpty());

            // backup assertions -
            assertThat(MOCKED_CUSTOM_SAVE_DIRECTORY).isNotEmptyDirectory();

            CustomFileAssertions.assertThat(
                MOCKED_CUSTOM_SAVE_DIRECTORY
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
                "Stiles/#Q2hpbiBNaWxuZXI=.apu.json",
                "Braintree/cache/worldoptions.json.1676227889000.fb",
                "Braintree/cache/mods.json.1676227889000.fb",
                "Braintree/mods.json",
                "Braintree/worldoptions.json",
                "Braintree/world_timestamp.json",
                "San Perlita/maps/7.6.5/235.221.5.map",
                "San Perlita/maps/7.6.5/235.220.5.map",
                "San Perlita/maps/7.6.5/236.220.5.map",
                "San Perlita/maps/7.6.5/236.221.5.map",
                "San Perlita/maps/7.6.2/235.221.2.map",
                "San Perlita/maps/7.6.2/235.220.2.map",
                "San Perlita/maps/7.6.2/236.220.2.map",
                "San Perlita/maps/7.6.2/236.221.2.map",
                "San Perlita/maps/7.6.2/234.217.2.map",
                "San Perlita/maps/7.6.3/235.221.3.map",
                "San Perlita/maps/7.6.3/235.220.3.map",
                "San Perlita/maps/7.6.3/236.220.3.map",
                "San Perlita/maps/7.6.3/236.221.3.map",
                "San Perlita/maps/7.6.4/235.221.4.map",
                "San Perlita/maps/7.6.4/235.220.4.map",
                "San Perlita/maps/7.6.4/236.220.4.map",
                "San Perlita/maps/7.6.4/236.221.4.map",
                "San Perlita/maps/7.6.1/235.221.1.map",
                "San Perlita/maps/7.6.1/237.217.1.map",
                "San Perlita/maps/7.6.1/233.218.1.map",
                "San Perlita/maps/7.6.1/238.218.1.map",
                "San Perlita/maps/7.6.1/233.223.1.map",
                "San Perlita/maps/7.6.1/232.220.1.map",
                "San Perlita/maps/7.6.1/234.221.1.map",
                "San Perlita/maps/7.6.1/238.220.1.map",
                "San Perlita/maps/7.6.1/232.218.1.map",
                "San Perlita/maps/7.6.1/235.220.1.map",
                "San Perlita/maps/7.6.1/234.223.1.map",
                "San Perlita/maps/7.6.1/232.221.1.map",
                "San Perlita/maps/7.6.1/232.223.1.map",
                "San Perlita/maps/7.6.1/234.218.1.map",
                "San Perlita/maps/7.6.1/237.221.1.map",
                "San Perlita/maps/7.6.1/235.217.1.map",
                "San Perlita/maps/7.6.1/237.223.1.map",
                "San Perlita/maps/7.6.1/238.217.1.map",
                "San Perlita/maps/7.6.1/236.220.1.map",
                "San Perlita/maps/7.6.1/233.217.1.map",
                "San Perlita/maps/7.6.1/237.218.1.map",
                "San Perlita/maps/7.6.1/236.221.1.map",
                "San Perlita/maps/7.6.1/234.217.1.map",
                "San Perlita/maps/7.6.1/236.223.1.map",
                "San Perlita/maps/7.6.1/237.220.1.map",
                "San Perlita/maps/7.6.6/235.220.6.map",
                "San Perlita/maps/7.6.-1/238.217.-1.map",
                "San Perlita/maps/7.6.-1/234.218.-1.map",
                "San Perlita/maps/7.6.-1/235.217.-1.map",
                "San Perlita/maps/7.6.-1/237.217.-1.map",
                "San Perlita/maps/7.6.-1/237.221.-1.map",
                "San Perlita/maps/7.6.-1/233.218.-1.map",
                "San Perlita/maps/7.6.-1/237.223.-1.map",
                "San Perlita/maps/7.6.-1/234.223.-1.map",
                "San Perlita/maps/7.6.-1/238.218.-1.map",
                "San Perlita/maps/7.6.-1/232.218.-1.map",
                "San Perlita/maps/7.6.-1/232.220.-1.map",
                "San Perlita/maps/7.6.-1/233.223.-1.map",
                "San Perlita/maps/7.6.0/234.222.0.map",
                "San Perlita/maps/7.6.0/238.223.0.map",
                "San Perlita/maps/7.6.0/232.219.0.map",
                "San Perlita/maps/7.6.0/237.217.0.map",
                "San Perlita/maps/7.6.0/235.221.0.map",
                "San Perlita/maps/7.6.0/235.223.0.map",
                "San Perlita/maps/7.6.0/233.218.0.map",
                "San Perlita/maps/7.6.0/234.220.0.map",
                "San Perlita/maps/7.6.0/238.221.0.map",
                "San Perlita/maps/7.6.0/234.219.0.map",
                "San Perlita/maps/7.6.0/238.218.0.map",
                "San Perlita/maps/7.6.0/233.221.0.map",
                "San Perlita/maps/7.6.0/232.222.0.map",
                "San Perlita/maps/7.6.0/235.218.0.map",
                "San Perlita/maps/7.6.0/232.220.0.map",
                "San Perlita/maps/7.6.0/233.223.0.map",
                "San Perlita/maps/7.6.0/235.222.0.map",
                "San Perlita/maps/7.6.0/238.220.0.map",
                "San Perlita/maps/7.6.0/236.217.0.map",
                "San Perlita/maps/7.6.0/234.221.0.map",
                "San Perlita/maps/7.6.0/233.219.0.map",
                "San Perlita/maps/7.6.0/238.222.0.map",
                "San Perlita/maps/7.6.0/234.223.0.map",
                "San Perlita/maps/7.6.0/235.220.0.map",
                "San Perlita/maps/7.6.0/232.218.0.map",
                "San Perlita/maps/7.6.0/232.221.0.map",
                "San Perlita/maps/7.6.0/235.219.0.map",
                "San Perlita/maps/7.6.0/233.222.0.map",
                "San Perlita/maps/7.6.0/233.220.0.map",
                "San Perlita/maps/7.6.0/238.219.0.map",
                "San Perlita/maps/7.6.0/234.218.0.map",
                "San Perlita/maps/7.6.0/232.223.0.map",
                "San Perlita/maps/7.6.0/235.217.0.map",
                "San Perlita/maps/7.6.0/237.221.0.map",
                "San Perlita/maps/7.6.0/236.222.0.map",
                "San Perlita/maps/7.6.0/236.220.0.map",
                "San Perlita/maps/7.6.0/238.217.0.map",
                "San Perlita/maps/7.6.0/237.223.0.map",
                "San Perlita/maps/7.6.0/236.219.0.map",
                "San Perlita/maps/7.6.0/233.217.0.map",
                "San Perlita/maps/7.6.0/237.218.0.map",
                "San Perlita/maps/7.6.0/234.217.0.map",
                "San Perlita/maps/7.6.0/236.221.0.map",
                "San Perlita/maps/7.6.0/237.222.0.map",
                "San Perlita/maps/7.6.0/237.220.0.map",
                "San Perlita/maps/7.6.0/236.223.0.map",
                "San Perlita/maps/7.6.0/237.219.0.map",
                "San Perlita/maps/7.6.0/236.218.0.map",
                "San Perlita/#UmV0YSBIb3VzZQ==.seen.1.1",
                "San Perlita/#UmV0YSBIb3VzZQ==.seen.1.0",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/59.55.5.mmr",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/58.55.5.mmr",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/cache/58.55.5.mmr.1676140895000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/cache/59.55.5.mmr.1676139540000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/cache/58.55.5.mmr.1676139540000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/cache/59.55.5.mmr.1676140895000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.ano.json",
                "San Perlita/uistate.json",
                "San Perlita/#UmV0YSBIb3VzZQ==.log",
                "San Perlita/cache/maps/7.6.5/236.221.5.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.5/236.220.5.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.5/235.221.5.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.5/235.220.5.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/235.221.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/235.220.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/234.217.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/236.221.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/236.220.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.3/235.220.3.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.3/235.221.3.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.3/236.220.3.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.3/236.221.3.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.4/236.220.4.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.4/236.221.4.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.4/235.220.4.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.4/235.221.4.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/237.221.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/237.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/236.220.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/236.221.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/237.220.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/237.218.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/234.221.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/235.220.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/238.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/234.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/233.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/238.218.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/234.218.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/235.221.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/235.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/238.220.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/233.218.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.6/235.220.6.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/234.218.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/235.217.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/233.218.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/237.221.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/238.218.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/238.217.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/237.217.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.217.0.map.1676140896000.fb",
                "San Perlita/cache/o.1.1.1676140896000.fb",
                "San Perlita/cache/uistate.json.1676140896000.fb",
                "San Perlita/cache/o.1.0.1676140895000.fb",
                "San Perlita/cache/worldoptions.json.1676139273000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.seen.1.1.1676140895000.fb",
                "San Perlita/cache/master.gsav.1676140895000.fb",
                "San Perlita/cache/zones.json.1676140896000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.zones.json.1676140896000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.seen.1.0.1676140895000.fb",
                "San Perlita/cache/worldoptions.json.1676139272000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.seen.0.0.1676140895000.fb",
                "San Perlita/cache/mods.json.1676139273000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZV9kaWFyeQ==.json.1676140895000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.sfm.json.1676140896000.fb",
                "San Perlita/cache/o.0.0.1676140895000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.ano.json.1676140896000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.apu.json.1676140896000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.sav.1676140895000.fb",
                "San Perlita/cache/mods.json.1676139272000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.sfm.json",
                "San Perlita/mods.json",
                "San Perlita/o.1.0",
                "San Perlita/o.1.1",
                "San Perlita/#UmV0YSBIb3VzZQ==.zones.json",
                "San Perlita/zones.json",
                "San Perlita/worldoptions.json",
                "San Perlita/world_timestamp.json",
                "San Perlita/#UmV0YSBIb3VzZV9kaWFyeQ==.json",
                "San Perlita/master.gsav",
                "San Perlita/#UmV0YSBIb3VzZQ==.apu.json",
                "San Perlita/#UmV0YSBIb3VzZQ==.sav",
                "San Perlita/o.0.0",
                "San Perlita/#UmV0YSBIb3VzZQ==.seen.0.0"
            ));

            // trashed assertions -
            assertThat(MOCKED_TRASHED_SAVE_DIRECTORY).isEmptyDirectory();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void restore_backup_success_when_preexistent_save_to_trash_exists(@TempDir Path mockedSavePath, @TempDir Path mockedBackupPath, @TempDir Path mockedTrashedSavePath) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSavePath).thenReturn(mockedSavePath.resolve("saves/"));
            mockedPaths.when(Paths::getSaveBackupPath).thenReturn(mockedBackupPath);
            mockedPaths.when(Paths::getCustomTrashedSavePath).thenReturn(mockedTrashedSavePath);

            File MOCKED_CUSTOM_SAVE_DIRECTORY = Paths.getCustomSavePath().toFile();
            File MOCKED_SAVE_BACKUP_DIRECTORY = Paths.getSaveBackupPath().toFile();
            File MOCKED_TRASHED_SAVE_DIRECTORY = Paths.getCustomTrashedSavePath().toFile();

            assertThat(MOCKED_CUSTOM_SAVE_DIRECTORY.mkdirs()).isTrue(); // we are using a custom relative path, which we must create first...

            assertThat(MOCKED_CUSTOM_SAVE_DIRECTORY).exists();
            assertThat(MOCKED_SAVE_BACKUP_DIRECTORY).exists();
            assertThat(MOCKED_TRASHED_SAVE_DIRECTORY).exists();

            File MOCKED_BACKUP_1 = Paths.getSaveBackupPath().resolve("20230216_111637.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230216_111637.zip"),
                MOCKED_BACKUP_1
            );

            File MOCKED_BACKUP_2 = Paths.getSaveBackupPath().resolve("20230217_115559.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20230217_115559.zip"),
                MOCKED_BACKUP_2
            );

            File MOCKED_BACKUP_3 = Paths.getSaveBackupPath().resolve("20240808_205649.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205649.zip"),
                MOCKED_BACKUP_3
            );

            File MOCKED_BACKUP_4 = Paths.getSaveBackupPath().resolve("20240808_205736.zip").toFile();
            FileUtils.copyFile(
                TestUtils.getFromResource("save/backup/sample/20240808_205736.zip"),
                MOCKED_BACKUP_4
            );

            File MOCKED_EXISTENT_SAVE = Paths.getCustomSavePath().resolve("Braintree").toFile();
            FileUtils.copyDirectory(
                TestUtils.getFromResource("save/sample/Braintree"),
                MOCKED_EXISTENT_SAVE
            );

            AtomicInteger calledTimes = new AtomicInteger(0);
            AtomicReference<List<Integer>> calledWith = new AtomicReference<>(new ArrayList<>());
            Consumer<Integer> MOCKED_CALLBACK = value -> {
                calledTimes.incrementAndGet();
                calledWith.get().add(value);
            };

            // pre-test assertions ---
            assertThat(MOCKED_CUSTOM_SAVE_DIRECTORY).isNotEmptyDirectory();
            assertThat(MOCKED_TRASHED_SAVE_DIRECTORY).isEmptyDirectory();
            assertThat(MOCKED_SAVE_BACKUP_DIRECTORY).isNotEmptyDirectory();

            CustomFileAssertions.assertThat(
                MOCKED_SAVE_BACKUP_DIRECTORY
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "20230216_111637.zip",
                "20230217_115559.zip",
                "20240808_205649.zip",
                "20240808_205736.zip"
            ));

            CustomFileAssertions.assertThat(
                MOCKED_CUSTOM_SAVE_DIRECTORY
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "Braintree/cache/worldoptions.json.1676227889000.fb",
                "Braintree/cache/mods.json.1676227889000.fb",
                "Braintree/mods.json",
                "Braintree/worldoptions.json",
                "Braintree/world_timestamp.json"
            ));

            // execute test ---
            Optional<Thread> result = SaveManager.restoreBackup(MOCKED_BACKUP_4, MOCKED_CALLBACK);
            result.ifPresent(Thread::run);

            // verify assertions ---
            assertThat(result).isNotEmpty();

            // callback assertions -
            assertThat(calledTimes).hasPositiveValue();
            assertThat(calledWith).matches(ints -> !ints.get().isEmpty());

            // backup assertions -
            assertThat(MOCKED_CUSTOM_SAVE_DIRECTORY).isNotEmptyDirectory();

            CustomFileAssertions.assertThat(
                MOCKED_CUSTOM_SAVE_DIRECTORY
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
                "Stiles/#Q2hpbiBNaWxuZXI=.apu.json",
                "Braintree/cache/worldoptions.json.1676227889000.fb",
                "Braintree/cache/mods.json.1676227889000.fb",
                "Braintree/mods.json",
                "Braintree/worldoptions.json",
                "Braintree/world_timestamp.json",
                "San Perlita/maps/7.6.5/235.221.5.map",
                "San Perlita/maps/7.6.5/235.220.5.map",
                "San Perlita/maps/7.6.5/236.220.5.map",
                "San Perlita/maps/7.6.5/236.221.5.map",
                "San Perlita/maps/7.6.2/235.221.2.map",
                "San Perlita/maps/7.6.2/235.220.2.map",
                "San Perlita/maps/7.6.2/236.220.2.map",
                "San Perlita/maps/7.6.2/236.221.2.map",
                "San Perlita/maps/7.6.2/234.217.2.map",
                "San Perlita/maps/7.6.3/235.221.3.map",
                "San Perlita/maps/7.6.3/235.220.3.map",
                "San Perlita/maps/7.6.3/236.220.3.map",
                "San Perlita/maps/7.6.3/236.221.3.map",
                "San Perlita/maps/7.6.4/235.221.4.map",
                "San Perlita/maps/7.6.4/235.220.4.map",
                "San Perlita/maps/7.6.4/236.220.4.map",
                "San Perlita/maps/7.6.4/236.221.4.map",
                "San Perlita/maps/7.6.1/235.221.1.map",
                "San Perlita/maps/7.6.1/237.217.1.map",
                "San Perlita/maps/7.6.1/233.218.1.map",
                "San Perlita/maps/7.6.1/238.218.1.map",
                "San Perlita/maps/7.6.1/233.223.1.map",
                "San Perlita/maps/7.6.1/232.220.1.map",
                "San Perlita/maps/7.6.1/234.221.1.map",
                "San Perlita/maps/7.6.1/238.220.1.map",
                "San Perlita/maps/7.6.1/232.218.1.map",
                "San Perlita/maps/7.6.1/235.220.1.map",
                "San Perlita/maps/7.6.1/234.223.1.map",
                "San Perlita/maps/7.6.1/232.221.1.map",
                "San Perlita/maps/7.6.1/232.223.1.map",
                "San Perlita/maps/7.6.1/234.218.1.map",
                "San Perlita/maps/7.6.1/237.221.1.map",
                "San Perlita/maps/7.6.1/235.217.1.map",
                "San Perlita/maps/7.6.1/237.223.1.map",
                "San Perlita/maps/7.6.1/238.217.1.map",
                "San Perlita/maps/7.6.1/236.220.1.map",
                "San Perlita/maps/7.6.1/233.217.1.map",
                "San Perlita/maps/7.6.1/237.218.1.map",
                "San Perlita/maps/7.6.1/236.221.1.map",
                "San Perlita/maps/7.6.1/234.217.1.map",
                "San Perlita/maps/7.6.1/236.223.1.map",
                "San Perlita/maps/7.6.1/237.220.1.map",
                "San Perlita/maps/7.6.6/235.220.6.map",
                "San Perlita/maps/7.6.-1/238.217.-1.map",
                "San Perlita/maps/7.6.-1/234.218.-1.map",
                "San Perlita/maps/7.6.-1/235.217.-1.map",
                "San Perlita/maps/7.6.-1/237.217.-1.map",
                "San Perlita/maps/7.6.-1/237.221.-1.map",
                "San Perlita/maps/7.6.-1/233.218.-1.map",
                "San Perlita/maps/7.6.-1/237.223.-1.map",
                "San Perlita/maps/7.6.-1/234.223.-1.map",
                "San Perlita/maps/7.6.-1/238.218.-1.map",
                "San Perlita/maps/7.6.-1/232.218.-1.map",
                "San Perlita/maps/7.6.-1/232.220.-1.map",
                "San Perlita/maps/7.6.-1/233.223.-1.map",
                "San Perlita/maps/7.6.0/234.222.0.map",
                "San Perlita/maps/7.6.0/238.223.0.map",
                "San Perlita/maps/7.6.0/232.219.0.map",
                "San Perlita/maps/7.6.0/237.217.0.map",
                "San Perlita/maps/7.6.0/235.221.0.map",
                "San Perlita/maps/7.6.0/235.223.0.map",
                "San Perlita/maps/7.6.0/233.218.0.map",
                "San Perlita/maps/7.6.0/234.220.0.map",
                "San Perlita/maps/7.6.0/238.221.0.map",
                "San Perlita/maps/7.6.0/234.219.0.map",
                "San Perlita/maps/7.6.0/238.218.0.map",
                "San Perlita/maps/7.6.0/233.221.0.map",
                "San Perlita/maps/7.6.0/232.222.0.map",
                "San Perlita/maps/7.6.0/235.218.0.map",
                "San Perlita/maps/7.6.0/232.220.0.map",
                "San Perlita/maps/7.6.0/233.223.0.map",
                "San Perlita/maps/7.6.0/235.222.0.map",
                "San Perlita/maps/7.6.0/238.220.0.map",
                "San Perlita/maps/7.6.0/236.217.0.map",
                "San Perlita/maps/7.6.0/234.221.0.map",
                "San Perlita/maps/7.6.0/233.219.0.map",
                "San Perlita/maps/7.6.0/238.222.0.map",
                "San Perlita/maps/7.6.0/234.223.0.map",
                "San Perlita/maps/7.6.0/235.220.0.map",
                "San Perlita/maps/7.6.0/232.218.0.map",
                "San Perlita/maps/7.6.0/232.221.0.map",
                "San Perlita/maps/7.6.0/235.219.0.map",
                "San Perlita/maps/7.6.0/233.222.0.map",
                "San Perlita/maps/7.6.0/233.220.0.map",
                "San Perlita/maps/7.6.0/238.219.0.map",
                "San Perlita/maps/7.6.0/234.218.0.map",
                "San Perlita/maps/7.6.0/232.223.0.map",
                "San Perlita/maps/7.6.0/235.217.0.map",
                "San Perlita/maps/7.6.0/237.221.0.map",
                "San Perlita/maps/7.6.0/236.222.0.map",
                "San Perlita/maps/7.6.0/236.220.0.map",
                "San Perlita/maps/7.6.0/238.217.0.map",
                "San Perlita/maps/7.6.0/237.223.0.map",
                "San Perlita/maps/7.6.0/236.219.0.map",
                "San Perlita/maps/7.6.0/233.217.0.map",
                "San Perlita/maps/7.6.0/237.218.0.map",
                "San Perlita/maps/7.6.0/234.217.0.map",
                "San Perlita/maps/7.6.0/236.221.0.map",
                "San Perlita/maps/7.6.0/237.222.0.map",
                "San Perlita/maps/7.6.0/237.220.0.map",
                "San Perlita/maps/7.6.0/236.223.0.map",
                "San Perlita/maps/7.6.0/237.219.0.map",
                "San Perlita/maps/7.6.0/236.218.0.map",
                "San Perlita/#UmV0YSBIb3VzZQ==.seen.1.1",
                "San Perlita/#UmV0YSBIb3VzZQ==.seen.1.0",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/59.55.5.mmr",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/58.55.5.mmr",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/cache/58.55.5.mmr.1676140895000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/cache/59.55.5.mmr.1676139540000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/cache/58.55.5.mmr.1676139540000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.mm1/cache/59.55.5.mmr.1676140895000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.ano.json",
                "San Perlita/uistate.json",
                "San Perlita/#UmV0YSBIb3VzZQ==.log",
                "San Perlita/cache/maps/7.6.5/236.221.5.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.5/236.220.5.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.5/235.221.5.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.5/235.220.5.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/235.221.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/235.220.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/234.217.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/236.221.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.2/236.220.2.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.3/235.220.3.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.3/235.221.3.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.3/236.220.3.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.3/236.221.3.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.4/236.220.4.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.4/236.221.4.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.4/235.220.4.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.4/235.221.4.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/237.221.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/237.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/236.220.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/236.221.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/237.220.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/237.218.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/234.221.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/235.220.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/238.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/234.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/233.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/238.218.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/234.218.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/235.221.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/235.217.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/238.220.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.1/233.218.1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.6/235.220.6.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/234.218.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/235.217.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/233.218.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/237.221.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/238.218.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/238.217.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.-1/237.217.-1.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/236.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/237.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.217.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.220.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.219.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.221.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/234.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/238.222.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/235.218.0.map.1676140896000.fb",
                "San Perlita/cache/maps/7.6.0/233.217.0.map.1676140896000.fb",
                "San Perlita/cache/o.1.1.1676140896000.fb",
                "San Perlita/cache/uistate.json.1676140896000.fb",
                "San Perlita/cache/o.1.0.1676140895000.fb",
                "San Perlita/cache/worldoptions.json.1676139273000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.seen.1.1.1676140895000.fb",
                "San Perlita/cache/master.gsav.1676140895000.fb",
                "San Perlita/cache/zones.json.1676140896000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.zones.json.1676140896000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.seen.1.0.1676140895000.fb",
                "San Perlita/cache/worldoptions.json.1676139272000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.seen.0.0.1676140895000.fb",
                "San Perlita/cache/mods.json.1676139273000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZV9kaWFyeQ==.json.1676140895000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.sfm.json.1676140896000.fb",
                "San Perlita/cache/o.0.0.1676140895000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.ano.json.1676140896000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.apu.json.1676140896000.fb",
                "San Perlita/cache/#UmV0YSBIb3VzZQ==.sav.1676140895000.fb",
                "San Perlita/cache/mods.json.1676139272000.fb",
                "San Perlita/#UmV0YSBIb3VzZQ==.sfm.json",
                "San Perlita/mods.json",
                "San Perlita/o.1.0",
                "San Perlita/o.1.1",
                "San Perlita/#UmV0YSBIb3VzZQ==.zones.json",
                "San Perlita/zones.json",
                "San Perlita/worldoptions.json",
                "San Perlita/world_timestamp.json",
                "San Perlita/#UmV0YSBIb3VzZV9kaWFyeQ==.json",
                "San Perlita/master.gsav",
                "San Perlita/#UmV0YSBIb3VzZQ==.apu.json",
                "San Perlita/#UmV0YSBIb3VzZQ==.sav",
                "San Perlita/o.0.0",
                "San Perlita/#UmV0YSBIb3VzZQ==.seen.0.0"
            ));

            // trashed assertions -
            assertThat(MOCKED_TRASHED_SAVE_DIRECTORY).isNotEmptyDirectory();
            assertThat(MOCKED_TRASHED_SAVE_DIRECTORY.listFiles()).hasSize(1);

            File MOCKED_ACTUAL_TRASHED_SAVE = Objects.requireNonNull(MOCKED_TRASHED_SAVE_DIRECTORY.listFiles())[0];
            assertThat(MOCKED_ACTUAL_TRASHED_SAVE).exists();
            assertThat(MOCKED_ACTUAL_TRASHED_SAVE.getName()).contains( // assert that trashed mod is timestamp-ed
                new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
            );

            CustomFileAssertions.assertThat(
                MOCKED_ACTUAL_TRASHED_SAVE
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "Braintree/cache/worldoptions.json.1676227889000.fb",
                "Braintree/cache/mods.json.1676227889000.fb",
                "Braintree/mods.json",
                "Braintree/worldoptions.json",
                "Braintree/world_timestamp.json"
            ));


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}