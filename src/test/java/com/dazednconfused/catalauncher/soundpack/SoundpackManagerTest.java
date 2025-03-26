package com.dazednconfused.catalauncher.soundpack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.dazednconfused.catalauncher.assertions.CustomFileAssertions;
import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.helper.result.Result;
import com.dazednconfused.catalauncher.soundpack.dto.SoundpackDTO;
import com.dazednconfused.catalauncher.utils.TestUtils;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

class SoundpackManagerTest {

    @Test
    void install_soundpack_success_with_directory(@TempDir Path mockedDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedDirectory);
            File MOCKED_SOUNDPACK_DIR = TestUtils.getFromResource("soundpack/sample/unzipped/CC-Sounds-sfx-sample-for-tests");

            AtomicBoolean called = new AtomicBoolean(false);
            AtomicReference<SoundpackDTO> calledWith = new AtomicReference<>();
            Consumer<SoundpackDTO> MOCKED_CALLBACK = result -> {
                called.set(true);
                calledWith.set(result);
            };

            // pre-test assertions ---
            assertThat(MOCKED_SOUNDPACK_DIR).isNotNull();
            assertThat(MOCKED_SOUNDPACK_DIR).isDirectory();

            // execute test ---
            Result<Throwable, SoundpackDTO> result = SoundpackManager.installSoundpack(MOCKED_SOUNDPACK_DIR, MOCKED_CALLBACK);

            // verify assertions ---
            assertThat(result).isNotNull(); // assert non-null result
            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            // assert on result -
            SoundpackDTO ACTUAL_RESULT = result.getOrElseThrowUnchecked();

            assertThat(called.get()).isTrue();
            assertThat(calledWith.get()).isEqualTo(ACTUAL_RESULT);

            // assert on filesystem changes -
            File MOCKED_INSTALLED_SOUNDPACK = Paths.getCustomSoundpacksDir().resolve("CC-Sounds-sfx-sample-for-tests").toFile();

            CustomFileAssertions.assertThat(
                    MOCKED_INSTALLED_SOUNDPACK
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "soundpack.txt",
                    "explosion/default/credits.md",
                    "explosion/default/explosion_default.json",
                    "explosion/default/explosion_default_1.ogg",
                    "explosion/default/explosion_default_2.ogg",
                    "explosion/huge/credits.md",
                    "explosion/huge/explosion_huge.json",
                    "explosion/huge/explosion_huge_1.ogg",
                    "explosion/huge/explosion_huge_2.ogg",
                    "explosion/small/credits.md",
                    "explosion/small/explosion_small.json",
                    "explosion/small/explosion_small.ogg"
            ));

            // assert on registered changes -
            assertThat(SoundpackManager.listAllSoundpacks()).containsExactly(ACTUAL_RESULT);
        }
    }

    @Test
    void get_Soundpacks_folder_success(@TempDir Path mockedDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedDirectory);

            // execute test ---
            File result = SoundpackManager.getSoundpacksFolder();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getCustomSoundpacksDir().toFile());
        }
    }

    @Test
    void get_soundpacks_folder_folder_not_yet_created_success(@TempDir Path mockedDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedDirectory.resolve("a/missing/folder"));

            // pre-test assertions ---
            assertThat(Paths.getCustomSoundpacksDir().toFile()).doesNotExist();

            // execute test ---
            File result = SoundpackManager.getSoundpacksFolder();

            // verify assertions ---
            assertThat(result).isEqualTo(Paths.getCustomSoundpacksDir().toFile());
        }
    }

    @Test
    void uninstall_soundpack_success(@TempDir Path mockedSoundpacksDirectory, @TempDir Path mockedTrashedSoundpacksDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedSoundpacksDirectory);
            mockedPaths.when(Paths::getCustomTrashedSoundpacksPath).thenReturn(mockedTrashedSoundpacksDirectory);

            File MOCKED_SOUNDPACK_ZIP = TestUtils.getFromResource("soundpack/sample/unzipped/CC-Sounds-sfx-sample-for-tests");

            SoundpackDTO MOCKED_SOUNDPACK = SoundpackManager.installSoundpack(MOCKED_SOUNDPACK_ZIP, unused -> { }).getOrElseThrowUnchecked();

            AtomicBoolean called = new AtomicBoolean(false);
            AtomicReference<SoundpackDTO> calledWith = new AtomicReference<>();
            Consumer<SoundpackDTO> MOCKED_CALLBACK = SoundpackDTO -> {
                called.set(true);
                calledWith.set(SoundpackDTO);
            };

            // pre-test assertions ---
            assertThat(MOCKED_SOUNDPACK).isNotNull();

            assertThat(SoundpackManager.listAllSoundpacks()).containsExactly(MOCKED_SOUNDPACK);

            CustomFileAssertions.assertThat(
                    Paths.getCustomSoundpacksDir().resolve("CC-Sounds-sfx-sample-for-tests").toFile()
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "soundpack.txt",
                "explosion/default/credits.md",
                "explosion/default/explosion_default.json",
                "explosion/default/explosion_default_1.ogg",
                "explosion/default/explosion_default_2.ogg",
                "explosion/huge/credits.md",
                "explosion/huge/explosion_huge.json",
                "explosion/huge/explosion_huge_1.ogg",
                "explosion/huge/explosion_huge_2.ogg",
                "explosion/small/credits.md",
                "explosion/small/explosion_small.json",
                "explosion/small/explosion_small.ogg"
            ));

            assertThat(Paths.getCustomTrashedSoundpacksPath().toFile()).isEmptyDirectory();

            // execute test ---
            Result<Throwable, SoundpackDTO> result = SoundpackManager.uninstallSoundpack(MOCKED_SOUNDPACK, MOCKED_CALLBACK);

            // verify assertions ---

            // assert on DTO result -
            assertThat(result).isNotNull(); // assert non-null result
            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            SoundpackDTO ACTUAL_RESULT = result.getOrElseThrowUnchecked();
            assertThat(ACTUAL_RESULT).isEqualTo(MOCKED_SOUNDPACK);

            assertThat(called.get()).isTrue();
            assertThat(calledWith.get()).isEqualTo(ACTUAL_RESULT);

            // assert on filesystem changes -
            assertThat(Paths.getCustomSoundpacksDir().resolve("CC-Sounds-sfx-sample-for-tests").toFile()).doesNotExist();

            assertThat(Paths.getCustomTrashedSoundpacksPath().toFile()).isNotEmptyDirectory();
            CustomFileAssertions.assertThat(
                    Objects.requireNonNull(Paths.getCustomTrashedSoundpacksPath().toFile().listFiles())[0]
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "CC-Sounds-sfx-sample-for-tests/soundpack.txt",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/credits.md",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/explosion_default.json",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/explosion_default_1.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/explosion_default_2.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/credits.md",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/explosion_huge.json",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/explosion_huge_1.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/explosion_huge_2.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/small/credits.md",
                "CC-Sounds-sfx-sample-for-tests/explosion/small/explosion_small.json",
                "CC-Sounds-sfx-sample-for-tests/explosion/small/explosion_small.ogg"
            ));

            // assert on remaining filesystem -
            assertThat(SoundpackManager.listAllSoundpacks()).isEmpty();
        }
    }

    @Test
    void trash_soundpack_from_soundpacks_folder_success(@TempDir Path mockedSoundpacksDirectory, @TempDir Path mockedTrashedSoundpacksDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedSoundpacksDirectory);
            mockedPaths.when(Paths::getCustomTrashedSoundpacksPath).thenReturn(mockedTrashedSoundpacksDirectory);

            File MOCKED_SOUNDPACK_ZIP = TestUtils.getFromResource("soundpack/sample/unzipped/CC-Sounds-sfx-sample-for-tests");

            Result<Throwable, SoundpackDTO> installResult = SoundpackManager.installSoundpack(MOCKED_SOUNDPACK_ZIP, unused -> { });

            // pre-test assertions ---
            assertThat(installResult).isNotNull();
            assertThat(installResult.toEither().isRight()).isTrue();

            SoundpackDTO registeredSoundpack = installResult.getOrElseThrowUnchecked();
            assertThat(registeredSoundpack).isNotNull();

            File MOCKED_INSTALLED_SOUNDPACK = Paths.getCustomSoundpacksDir().resolve("CC-Sounds-sfx-sample-for-tests").toFile();

            CustomFileAssertions.assertThat( // assert that soundpack is installed
                MOCKED_INSTALLED_SOUNDPACK
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "soundpack.txt",
                "explosion/default/credits.md",
                "explosion/default/explosion_default.json",
                "explosion/default/explosion_default_1.ogg",
                "explosion/default/explosion_default_2.ogg",
                "explosion/huge/credits.md",
                "explosion/huge/explosion_huge.json",
                "explosion/huge/explosion_huge_1.ogg",
                "explosion/huge/explosion_huge_2.ogg",
                "explosion/small/credits.md",
                "explosion/small/explosion_small.json",
                "explosion/small/explosion_small.ogg"
            ));

            File MOCKED_TRASHED_SOUNDPACKS_FOLDER = Paths.getCustomTrashedSoundpacksPath().toFile();
            assertThat(MOCKED_TRASHED_SOUNDPACKS_FOLDER).isEmptyDirectory(); // assert that trash folder is empty

            // execute test ---
            Result<Throwable, Void> result = SoundpackManager.trashSoundpackFromSoundsFolder(registeredSoundpack);

            // verify assertions ---
            assertThat(result).isNotNull(); // assert non-null result

            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            assertThat(MOCKED_INSTALLED_SOUNDPACK).doesNotExist(); // assert that soundpack no longer exists

            // trashed soundpack assertions -
            assertThat(MOCKED_TRASHED_SOUNDPACKS_FOLDER.listFiles()).hasSize(1);
            File MOCKED_TRASHED_SOUNDPACK = Objects.requireNonNull(MOCKED_TRASHED_SOUNDPACKS_FOLDER.listFiles())[0];
            assertThat(MOCKED_TRASHED_SOUNDPACK).exists();

            assertThat(MOCKED_TRASHED_SOUNDPACK.getName()).contains( // assert that trashed soundpack is timestamp-ed
                new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
            );

            CustomFileAssertions.assertThat( // assert that soundpack is available in trash folder
                MOCKED_TRASHED_SOUNDPACK
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "CC-Sounds-sfx-sample-for-tests/soundpack.txt",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/credits.md",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/explosion_default.json",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/explosion_default_1.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/explosion_default_2.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/credits.md",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/explosion_huge.json",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/explosion_huge_1.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/explosion_huge_2.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/small/credits.md",
                "CC-Sounds-sfx-sample-for-tests/explosion/small/explosion_small.json",
                "CC-Sounds-sfx-sample-for-tests/explosion/small/explosion_small.ogg"
            ));
        }
    }

    @Test
    void trash_soundpack_from_soundpacks_folder_generating_trash_folder_success(@TempDir Path mockedSoundpacksDirectory, @TempDir Path mockedTrashedSoundpacksDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedSoundpacksDirectory);
            mockedPaths.when(Paths::getCustomTrashedSoundpacksPath).thenReturn(mockedTrashedSoundpacksDirectory.resolve("a/missing/folder"));

            File MOCKED_SOUNDPACK_ZIP = TestUtils.getFromResource("soundpack/sample/unzipped/CC-Sounds-sfx-sample-for-tests");

            Result<Throwable, SoundpackDTO> installResult = SoundpackManager.installSoundpack(MOCKED_SOUNDPACK_ZIP, unused -> { });

            // pre-test assertions ---
            assertThat(installResult).isNotNull();
            assertThat(installResult.toEither().isRight()).isTrue();

            SoundpackDTO registeredSoundpack = installResult.getOrElseThrowUnchecked();
            assertThat(registeredSoundpack).isNotNull();

            File MOCKED_INSTALLED_SOUNDPACK = Paths.getCustomSoundpacksDir().resolve("CC-Sounds-sfx-sample-for-tests").toFile();

            CustomFileAssertions.assertThat( // assert that soundpack is installed
                MOCKED_INSTALLED_SOUNDPACK
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "soundpack.txt",
                "explosion/default/credits.md",
                "explosion/default/explosion_default.json",
                "explosion/default/explosion_default_1.ogg",
                "explosion/default/explosion_default_2.ogg",
                "explosion/huge/credits.md",
                "explosion/huge/explosion_huge.json",
                "explosion/huge/explosion_huge_1.ogg",
                "explosion/huge/explosion_huge_2.ogg",
                "explosion/small/credits.md",
                "explosion/small/explosion_small.json",
                "explosion/small/explosion_small.ogg"
            ));

            File MOCKED_TRASHED_SOUNDPACKS_FOLDER = Paths.getCustomTrashedSoundpacksPath().toFile();
            assertThat(MOCKED_TRASHED_SOUNDPACKS_FOLDER).doesNotExist(); // assert that trash folder does not exist yet

            // execute test ---
            Result<Throwable, Void> result = SoundpackManager.trashSoundpackFromSoundsFolder(registeredSoundpack);

            // verify assertions ---
            assertThat(result).isNotNull(); // assert non-null result

            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            assertThat(MOCKED_INSTALLED_SOUNDPACK).doesNotExist(); // assert that soundpack no longer exists

            // trashed soundpack assertions -
            assertThat(MOCKED_TRASHED_SOUNDPACKS_FOLDER.listFiles()).hasSize(1);
            File MOCKED_TRASHED_SOUNDPACK = Objects.requireNonNull(MOCKED_TRASHED_SOUNDPACKS_FOLDER.listFiles())[0];
            assertThat(MOCKED_TRASHED_SOUNDPACK).exists();

            assertThat(MOCKED_TRASHED_SOUNDPACK.getName()).contains( // assert that trashed soundpack is timestamp-ed
                new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
            );

            CustomFileAssertions.assertThat( // assert that soundpack is available in trash folder
                MOCKED_TRASHED_SOUNDPACK
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "CC-Sounds-sfx-sample-for-tests/soundpack.txt",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/credits.md",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/explosion_default.json",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/explosion_default_1.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/default/explosion_default_2.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/credits.md",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/explosion_huge.json",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/explosion_huge_1.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/huge/explosion_huge_2.ogg",
                "CC-Sounds-sfx-sample-for-tests/explosion/small/credits.md",
                "CC-Sounds-sfx-sample-for-tests/explosion/small/explosion_small.json",
                "CC-Sounds-sfx-sample-for-tests/explosion/small/explosion_small.ogg"
            ));
        }
    }

    @Test
    void trash_soundpack_from_soundpacks_folder_failure_when_to_be_trashed_soundpack_does_not_exist(@TempDir Path mockedSoundpacksDirectory, @TempDir Path mockedTrashedSoundpacksDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedSoundpacksDirectory);
            mockedPaths.when(Paths::getCustomTrashedSoundpacksPath).thenReturn(mockedTrashedSoundpacksDirectory);

            SoundpackDTO badSoundpack = SoundpackDTO.builder().name("aNonExistingSoundpack").build();

            // pre-test assertions ---
            File MOCKED_TRASHED_SOUNDPACKS_FOLDER = Paths.getCustomTrashedSoundpacksPath().toFile();
            assertThat(MOCKED_TRASHED_SOUNDPACKS_FOLDER).isEmptyDirectory(); // assert that trash folder is empty

            // execute test ---
            Result<Throwable, Void> result = SoundpackManager.trashSoundpackFromSoundsFolder(badSoundpack);

            // verify assertions ---
            assertThat(result).isNotNull(); // assert non-null result

            assertThat(result.toEither().isLeft()).isTrue(); // assert that Result is Failure

            // trashed soundpack assertions -
            assertThat(MOCKED_TRASHED_SOUNDPACKS_FOLDER).isEmptyDirectory(); // assert that trash folder is empty
        }
    }
}