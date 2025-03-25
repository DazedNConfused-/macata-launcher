package com.dazednconfused.catalauncher.soundpack;

import com.dazednconfused.catalauncher.assertions.CustomFileAssertions;
import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.helper.result.Result;
import com.dazednconfused.catalauncher.soundpack.dto.SoundpackDTO;
import com.dazednconfused.catalauncher.utils.TestUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

//        // prepare mock data ---
//        SoundpackfileDTO MOCKED_SoundpackFILE_1 = SoundpackfileDTO.builder()
//                .path("/a/mocked/1.path")
//                .hash("aMockedHash1")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_2 = SoundpackfileDTO.builder()
//                .path("/a/mocked/2.path")
//                .hash("aMockedHash2")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_3 = SoundpackfileDTO.builder()
//                .path("/a/mocked/3.path")
//                .hash("aMockedHash3")
//                .build();
//
//        SoundpackDTO MOCKED_Soundpack = SoundpackDTO.builder()
//                .name("mockedSoundpack")
//                .Soundpackinfo("mockedSoundpackInfo")
//                .Soundpackfiles(Arrays.asList(
//                        MOCKED_SoundpackFILE_1,
//                        MOCKED_SoundpackFILE_2,
//                        MOCKED_SoundpackFILE_3
//                ))
//                .build();
//
//        // execute test ---
//        Result<Throwable, SoundpackDTO> result = instance.registerSoundpack(MOCKED_Soundpack);
//
//        // verify assertions ---
//        assertThat(result).isNotNull(); // assert non-null result
//        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success
//
//        assertThat(result.getOrElseThrowUnchecked()).isNotNull();
//        SoundpackDTO ACTUAL_RESULT = result.getOrElseThrowUnchecked();
//
//        assertThat(ACTUAL_RESULT).isNotNull();
//        assertThat(ACTUAL_RESULT.getName()).isEqualTo("mockedSoundpack");
//        assertThat(ACTUAL_RESULT.getSoundpackinfo()).isEqualTo("mockedSoundpackInfo");
//        assertThat(ACTUAL_RESULT.getSoundpackfiles()).isNotNull();
//        assertThat(ACTUAL_RESULT.getSoundpackfiles()).hasSize(3);
//
//
//        SoundpackfileDTO EXPECTED_SoundpackFILE_1 = SoundpackfileDTO.builder()
//                .SoundpackId(ACTUAL_RESULT.getId())
//                .path("/a/mocked/1.path")
//                .hash("aMockedHash1")
//                .build();
//
//        SoundpackfileDTO EXPECTED_SoundpackFILE_2 = SoundpackfileDTO.builder()
//                .SoundpackId(ACTUAL_RESULT.getId())
//                .path("/a/mocked/2.path")
//                .hash("aMockedHash2")
//                .build();
//
//        SoundpackfileDTO EXPECTED_SoundpackFILE_3 = SoundpackfileDTO.builder()
//                .SoundpackId(ACTUAL_RESULT.getId())
//                .path("/a/mocked/3.path")
//                .hash("aMockedHash3")
//                .build();
//
//        assertThat(ACTUAL_RESULT.getSoundpackfiles()).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
//                "id", "createdDate", "updatedDate"
//        ).containsExactlyInAnyOrder(
//                EXPECTED_SoundpackFILE_1,
//                EXPECTED_SoundpackFILE_2,
//                EXPECTED_SoundpackFILE_3
//        );
//
//        assertThat(ACTUAL_RESULT.getSoundpackfiles()).extracting(SoundpackfileDTO::getCreatedDate).allSatisfy(createdDate -> assertThat(createdDate).isNotNull());
//        assertThat(ACTUAL_RESULT.getSoundpackfiles()).extracting(SoundpackfileDTO::getUpdatedDate).allSatisfy(updatedDate -> assertThat(updatedDate).isNotNull());
//        assertThat(ACTUAL_RESULT.getSoundpackfiles()).allSatisfy(dto -> assertThat(dto.getCreatedDate()).isEqualTo(dto.getUpdatedDate()));

    @Test
    void list_all_registered_Soundpacks_success() {

//        // prepare mock data ---
//        SoundpackfileDTO MOCKED_SoundpackFILE_1_1 = SoundpackfileDTO.builder()
//                .path("/a/mocked/1/1.path")
//                .hash("aMockedHash1_1")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_1_2 = SoundpackfileDTO.builder()
//                .path("/a/mocked/1/2.path")
//                .hash("aMockedHash1_2")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_1_3 = SoundpackfileDTO.builder()
//                .path("/a/mocked/1/3.path")
//                .hash("aMockedHash1_3")
//                .build();
//
//        SoundpackDTO MOCKED_Soundpack_1 = SoundpackDTO.builder()
//                .name("mockedSoundpack1")
//                .Soundpackinfo("mockedSoundpackInfo1")
//                .Soundpackfiles(Arrays.asList(
//                        MOCKED_SoundpackFILE_1_1,
//                        MOCKED_SoundpackFILE_1_2,
//                        MOCKED_SoundpackFILE_1_3
//                ))
//                .build();
//
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_2_1 = SoundpackfileDTO.builder()
//                .path("/a/mocked/2/1.path")
//                .hash("aMockedHash2_1")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_2_2 = SoundpackfileDTO.builder()
//                .path("/a/mocked/2/2.path")
//                .hash("aMockedHash2_2")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_2_3 = SoundpackfileDTO.builder()
//                .path("/a/mocked/2/3.path")
//                .hash("aMockedHash2_3")
//                .build();
//
//        SoundpackDTO MOCKED_Soundpack_2 = SoundpackDTO.builder()
//                .name("mockedSoundpack2")
//                .Soundpackinfo("mockedSoundpackInfo2")
//                .Soundpackfiles(Arrays.asList(
//                        MOCKED_SoundpackFILE_2_1,
//                        MOCKED_SoundpackFILE_2_2,
//                        MOCKED_SoundpackFILE_2_3
//                ))
//                .build();
//
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_3_1 = SoundpackfileDTO.builder()
//                .path("/a/mocked/3/1.path")
//                .hash("aMockedHash3_1")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_3_2 = SoundpackfileDTO.builder()
//                .path("/a/mocked/3/2.path")
//                .hash("aMockedHash3_2")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_3_3 = SoundpackfileDTO.builder()
//                .path("/a/mocked/3/3.path")
//                .hash("aMockedHash3_3")
//                .build();
//
//        SoundpackDTO MOCKED_Soundpack_3 = SoundpackDTO.builder()
//                .name("mockedSoundpack3")
//                .Soundpackinfo("mockedSoundpackInfo3")
//                .Soundpackfiles(Arrays.asList(
//                        MOCKED_SoundpackFILE_3_1,
//                        MOCKED_SoundpackFILE_3_2,
//                        MOCKED_SoundpackFILE_3_3
//                ))
//                .build();
//
//        SoundpackDTO EXPECTED_RESULT_1 = instance.registerSoundpack(MOCKED_Soundpack_1).getOrElseThrowUnchecked();
//        SoundpackDTO EXPECTED_RESULT_2 = instance.registerSoundpack(MOCKED_Soundpack_2).getOrElseThrowUnchecked();
//        SoundpackDTO EXPECTED_RESULT_3 = instance.registerSoundpack(MOCKED_Soundpack_3).getOrElseThrowUnchecked();
//
//        // execute test ---
//        List<SoundpackDTO> result = instance.listAllRegisteredSoundpacks();
//
//        // verify assertions ---
//        assertThat(result).containsExactlyInAnyOrder(
//                EXPECTED_RESULT_1,
//                EXPECTED_RESULT_2,
//                EXPECTED_RESULT_3
//        );
    }

    @Test
    void unregister_Soundpack_success() {

//        // prepare mock data ---
//        SoundpackfileDTO MOCKED_SoundpackFILE_1_1 = SoundpackfileDTO.builder()
//                .path("/a/mocked/1/1.path")
//                .hash("aMockedHash1_1")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_1_2 = SoundpackfileDTO.builder()
//                .path("/a/mocked/1/2.path")
//                .hash("aMockedHash1_2")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_1_3 = SoundpackfileDTO.builder()
//                .path("/a/mocked/1/3.path")
//                .hash("aMockedHash1_3")
//                .build();
//
//        SoundpackDTO MOCKED_Soundpack_1 = SoundpackDTO.builder()
//                .name("mockedSoundpack1")
//                .Soundpackinfo("mockedSoundpackInfo1")
//                .Soundpackfiles(Arrays.asList(
//                        MOCKED_SoundpackFILE_1_1,
//                        MOCKED_SoundpackFILE_1_2,
//                        MOCKED_SoundpackFILE_1_3
//                ))
//                .build();
//
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_2_1 = SoundpackfileDTO.builder()
//                .path("/a/mocked/2/1.path")
//                .hash("aMockedHash2_1")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_2_2 = SoundpackfileDTO.builder()
//                .path("/a/mocked/2/2.path")
//                .hash("aMockedHash2_2")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_2_3 = SoundpackfileDTO.builder()
//                .path("/a/mocked/2/3.path")
//                .hash("aMockedHash2_3")
//                .build();
//
//        SoundpackDTO MOCKED_Soundpack_2 = SoundpackDTO.builder()
//                .name("mockedSoundpack2")
//                .Soundpackinfo("mockedSoundpackInfo2")
//                .Soundpackfiles(Arrays.asList(
//                        MOCKED_SoundpackFILE_2_1,
//                        MOCKED_SoundpackFILE_2_2,
//                        MOCKED_SoundpackFILE_2_3
//                ))
//                .build();
//
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_3_1 = SoundpackfileDTO.builder()
//                .path("/a/mocked/3/1.path")
//                .hash("aMockedHash3_1")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_3_2 = SoundpackfileDTO.builder()
//                .path("/a/mocked/3/2.path")
//                .hash("aMockedHash3_2")
//                .build();
//
//        SoundpackfileDTO MOCKED_SoundpackFILE_3_3 = SoundpackfileDTO.builder()
//                .path("/a/mocked/3/3.path")
//                .hash("aMockedHash3_3")
//                .build();
//
//        SoundpackDTO MOCKED_Soundpack_3 = SoundpackDTO.builder()
//                .name("mockedSoundpack3")
//                .Soundpackinfo("mockedSoundpackInfo3")
//                .Soundpackfiles(Arrays.asList(
//                        MOCKED_SoundpackFILE_3_1,
//                        MOCKED_SoundpackFILE_3_2,
//                        MOCKED_SoundpackFILE_3_3
//                ))
//                .build();
//
//        SoundpackDTO EXPECTED_TO_BE_UNREGISTERED = instance.registerSoundpack(MOCKED_Soundpack_1).getOrElseThrowUnchecked();
//        SoundpackDTO EXPECTED_TO_REMAIN_1 = instance.registerSoundpack(MOCKED_Soundpack_2).getOrElseThrowUnchecked();
//        SoundpackDTO EXPECTED_TO_REMAIN_2 = instance.registerSoundpack(MOCKED_Soundpack_3).getOrElseThrowUnchecked();
//
//        // pre-test assertions ---
//        assertThat(instance.listAllRegisteredSoundpacks()).containsExactlyInAnyOrder(
//                EXPECTED_TO_BE_UNREGISTERED,
//                EXPECTED_TO_REMAIN_1,
//                EXPECTED_TO_REMAIN_2
//        );
//
//        // execute test ---
//        Result<Throwable, Void> result = instance.unregisterSoundpack(EXPECTED_TO_BE_UNREGISTERED);
//
//        // verify assertions ---
//        assertThat(result).isNotNull(); // assert non-null result
//
//        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success
//
//        assertThat(result.toEither().get().getResult().isEmpty()).isTrue(); // assert that Result's Success is empty
//
//        assertThat(instance.listAllRegisteredSoundpacks()).containsExactlyInAnyOrder(
//                EXPECTED_TO_REMAIN_1,
//                EXPECTED_TO_REMAIN_2
//        );
    }



//    @Test
//    void validate_Soundpack_for_zip_success() {
//
//        // prepare mock data ---
//        File MOCKED_Soundpack_ZIP = TestUtils.getFromResource("Soundpack/sample/zipped/cdda_mutation_rebalance_Soundpack.zip");
//
//        // execute test ---
//        Result<Throwable, File> result = instance.validateSoundpack(MOCKED_Soundpack_ZIP);
//
//        // verify assertions ---
//        assertThat(result).isNotNull(); // assert non-null result
//        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success
//
//        assertThat(result.getOrElseThrowUnchecked()).isNotNull();
//        File ACTUAL_RESULT = result.getOrElseThrowUnchecked();
//
//        CustomFileAssertions.assertThat(ACTUAL_RESULT).containsExactlyFilesWithRelativePaths(Arrays.asList(
//                "Soundpackinfo.json",
//                "README.md",
//                "items/armor/integrated.json"
//        ));
//    }

//    @Test
//    void validate_Soundpack_for_directory_success() {
//
//        // prepare mock data ---
//        File MOCKED_Soundpack_ZIP = TestUtils.getFromResource("Soundpack/sample/unzipped/cdda_mutation_rebalance_Soundpack");
//
//        // execute test ---
//        Result<Throwable, File> result = instance.validateSoundpack(MOCKED_Soundpack_ZIP);
//
//        // verify assertions ---
//        assertThat(result).isNotNull(); // assert non-null result
//        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success
//
//        assertThat(result.getOrElseThrowUnchecked()).isNotNull();
//        File ACTUAL_RESULT = result.getOrElseThrowUnchecked();
//
//        CustomFileAssertions.assertThat(ACTUAL_RESULT).containsExactlyFilesWithRelativePaths(Arrays.asList(
//            "Soundpackinfo.json",
//            "README.md",
//            "items/armor/integrated.json"
//        ));
//    }

//    @Test
//    void validate_Soundpack_failure_when_path_does_not_exist() {
//
//        // prepare mock data ---
//        File MOCKED_Soundpack_ZIP = mock(File.class);
//        when(MOCKED_Soundpack_ZIP.exists()).thenReturn(false);
//
//        // execute test ---
//        Result<Throwable, File> result = instance.validateSoundpack(MOCKED_Soundpack_ZIP);
//
//        // verify assertions ---
//        assertThat(result).isNotNull(); // assert non-null result
//
//        assertThat(result.toEither().isRight()).isFalse(); // assert that Result is not a Success
//    }
//
//    @Test
//    void validate_Soundpack_failure_when_path_cannot_be_read() {
//
//        // prepare mock data ---
//        File MOCKED_Soundpack_ZIP = mock(File.class);
//        when(MOCKED_Soundpack_ZIP.exists()).thenReturn(true);
//        when(MOCKED_Soundpack_ZIP.canRead()).thenReturn(false);
//
//        // execute test ---
//        Result<Throwable, File> result = instance.validateSoundpack(MOCKED_Soundpack_ZIP);
//
//        // verify assertions ---
//        assertThat(result).isNotNull(); // assert non-null result
//
//        assertThat(result.toEither().isRight()).isFalse(); // assert that Result is not a Success
//    }
//
//    @Test
//    void validate_Soundpack_failure_when_path_is_not_a_directory_nor_a_zip_file() {
//
//        // prepare mock data ---
//        File MOCKED_Soundpack_ZIP = mock(File.class);
//        when(MOCKED_Soundpack_ZIP.exists()).thenReturn(true);
//        when(MOCKED_Soundpack_ZIP.canRead()).thenReturn(true);
//
//        when(MOCKED_Soundpack_ZIP.isDirectory()).thenReturn(false);
//        when(MOCKED_Soundpack_ZIP.getName()).thenReturn("mock");
//
//        // execute test ---
//        Result<Throwable, File> result = instance.validateSoundpack(MOCKED_Soundpack_ZIP);
//
//        // verify assertions ---
//        assertThat(result).isNotNull(); // assert non-null result
//
//        assertThat(result.toEither().isRight()).isFalse(); // assert that Result is not a Success
//    }
//
//    @Test
//    void validate_Soundpack_failure_when_Soundpackinfo_json_does_not_exist() {
//
//        // prepare mock data ---
//        File MOCKED_Soundpack_ZIP = TestUtils.getFromResource("Soundpack/sample/invalid");
//
//        // execute test ---
//        Result<Throwable, File> result = instance.validateSoundpack(MOCKED_Soundpack_ZIP);
//
//        // verify assertions ---
//        assertThat(result).isNotNull(); // assert non-null result
//
//        assertThat(result.toEither().isRight()).isFalse(); // assert that Result is not a Success
//    }
//
//    @Test
//    void copy_Soundpack_to_Soundpacks_folder_success(@TempDir Path mockedDirectory) {
//        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
//
//            // prepare mock data ---
//            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedDirectory);
//            File MOCKED_Soundpack_ZIP = TestUtils.getFromResource("Soundpack/sample/unzipped/cdda_mutation_rebalance_Soundpack");
//
//            // execute test ---
//            Result<Throwable, File> result = instance.copySoundpackToSoundpacksFolder(MOCKED_Soundpack_ZIP);
//
//            // verify assertions ---
//            assertThat(result).isNotNull(); // assert non-null result
//            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success
//
//            File ACTUAL_RESULT = result.getOrElseThrowUnchecked();
//            assertThat(ACTUAL_RESULT).isNotNull();
//
//            Path EXPECTED_PATH = Paths.getCustomSoundpacksDir().resolve("cdda_mutation_rebalance_Soundpack");
//            assertThat(ACTUAL_RESULT.getPath()).isEqualTo(EXPECTED_PATH.toString());
//
//            CustomFileAssertions.assertThat(
//                    new File(EXPECTED_PATH.toString())
//            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
//                    "Soundpackinfo.json",
//                    "README.md",
//                    "items/armor/integrated.json"
//            ));
//        }
//    }
//
//    @Test
//    void trash_Soundpack_from_Soundpacks_folder_success(@TempDir Path mockedSoundpacksDirectory, @TempDir Path mockedTrashedSoundpacksDirectory) {
//        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
//
//            // prepare mock data ---
//            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedSoundpacksDirectory);
//            mockedPaths.when(Paths::getCustomTrashedSoundpacksPath).thenReturn(mockedTrashedSoundpacksDirectory);
//
//            File MOCKED_Soundpack_ZIP = TestUtils.getFromResource("Soundpack/sample/unzipped/cdda_mutation_rebalance_Soundpack");
//
//            Result<Throwable, SoundpackDTO> installResult = instance.installSoundpack(MOCKED_Soundpack_ZIP, unused -> { });
//
//            // pre-test assertions ---
//            assertThat(installResult).isNotNull();
//            assertThat(installResult.toEither().isRight()).isTrue();
//
//            SoundpackDTO registeredSoundpack = installResult.getOrElseThrowUnchecked();
//            assertThat(registeredSoundpack).isNotNull();
//
//            File MOCKED_INSTALLED_Soundpack = Paths.getCustomSoundpacksDir().resolve("cdda_mutation_rebalance_Soundpack").toFile();
//
//            CustomFileAssertions.assertThat( // assert that Soundpack is installed
//                    MOCKED_INSTALLED_Soundpack
//            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
//                    "Soundpackinfo.json",
//                    "README.md",
//                    "items/armor/integrated.json"
//            ));
//
//            File MOCKED_TRASHED_SoundpackS_FOLDER = Paths.getCustomTrashedSoundpacksPath().toFile();
//            assertThat(MOCKED_TRASHED_SoundpackS_FOLDER).isEmptyDirectory(); // assert that trash folder is empty
//
//            // execute test ---
//            Result<Throwable, Void> result = instance.trashSoundpackFromSoundpacksFolder(registeredSoundpack);
//
//            // verify assertions ---
//            assertThat(result).isNotNull(); // assert non-null result
//
//            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success
//
//            assertThat(MOCKED_INSTALLED_Soundpack).doesNotExist(); // assert that Soundpack no longer exists
//
//            // trashed Soundpack assertions -
//            assertThat(MOCKED_TRASHED_SoundpackS_FOLDER.listFiles()).hasSize(1);
//            File MOCKED_TRASHED_Soundpack = Objects.requireNonNull(MOCKED_TRASHED_SoundpackS_FOLDER.listFiles())[0];
//            assertThat(MOCKED_TRASHED_Soundpack).exists();
//
//            assertThat(MOCKED_TRASHED_Soundpack.getName()).contains( // assert that trashed Soundpack is timestamp-ed
//                    new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
//            );
//
//            CustomFileAssertions.assertThat( // assert that Soundpack is available in trash folder
//                    MOCKED_TRASHED_Soundpack
//            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
//                    "cdda_mutation_rebalance_Soundpack/Soundpackinfo.json",
//                    "cdda_mutation_rebalance_Soundpack/README.md",
//                    "cdda_mutation_rebalance_Soundpack/items/armor/integrated.json"
//            ));
//        }
//    }
//
//    @Test
//    void trash_Soundpack_from_Soundpacks_folder_generating_trash_folder_success(@TempDir Path mockedSoundpacksDirectory, @TempDir Path mockedTrashedSoundpacksDirectory) {
//        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
//
//            // prepare mock data ---
//            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedSoundpacksDirectory);
//            mockedPaths.when(Paths::getCustomTrashedSoundpacksPath).thenReturn(mockedTrashedSoundpacksDirectory.resolve("a/missing/folder"));
//
//            File MOCKED_Soundpack_ZIP = TestUtils.getFromResource("Soundpack/sample/unzipped/cdda_mutation_rebalance_Soundpack");
//
//            Result<Throwable, SoundpackDTO> installResult = instance.installSoundpack(MOCKED_Soundpack_ZIP, unused -> { });
//
//            // pre-test assertions ---
//            assertThat(installResult).isNotNull();
//            assertThat(installResult.toEither().isRight()).isTrue();
//
//            SoundpackDTO registeredSoundpack = installResult.getOrElseThrowUnchecked();
//            assertThat(registeredSoundpack).isNotNull();
//
//            File MOCKED_INSTALLED_Soundpack = Paths.getCustomSoundpacksDir().resolve("cdda_mutation_rebalance_Soundpack").toFile();
//
//            CustomFileAssertions.assertThat( // assert that Soundpack is installed
//                    MOCKED_INSTALLED_Soundpack
//            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
//                    "Soundpackinfo.json",
//                    "README.md",
//                    "items/armor/integrated.json"
//            ));
//
//            File MOCKED_TRASHED_SoundpackS_FOLDER = Paths.getCustomTrashedSoundpacksPath().toFile();
//            assertThat(MOCKED_TRASHED_SoundpackS_FOLDER).doesNotExist(); // assert that trash folder does not exist yet
//
//            // execute test ---
//            Result<Throwable, Void> result = instance.trashSoundpackFromSoundpacksFolder(registeredSoundpack);
//
//            // verify assertions ---
//            assertThat(result).isNotNull(); // assert non-null result
//
//            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success
//
//            assertThat(MOCKED_INSTALLED_Soundpack).doesNotExist(); // assert that Soundpack no longer exists
//
//            // trashed Soundpack assertions -
//            assertThat(MOCKED_TRASHED_SoundpackS_FOLDER.listFiles()).hasSize(1);
//            File MOCKED_TRASHED_Soundpack = Objects.requireNonNull(MOCKED_TRASHED_SoundpackS_FOLDER.listFiles())[0];
//            assertThat(MOCKED_TRASHED_Soundpack).exists();
//
//            assertThat(MOCKED_TRASHED_Soundpack.getName()).contains( // assert that trashed Soundpack is timestamp-ed
//                    new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
//            );
//
//            CustomFileAssertions.assertThat( // assert that Soundpack is available in trash folder
//                    MOCKED_TRASHED_Soundpack
//            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
//                    "cdda_mutation_rebalance_Soundpack/Soundpackinfo.json",
//                    "cdda_mutation_rebalance_Soundpack/README.md",
//                    "cdda_mutation_rebalance_Soundpack/items/armor/integrated.json"
//            ));
//        }
//    }
//
//    @Test
//    void trash_Soundpack_from_Soundpacks_folder_with_foreign_files_success(@TempDir Path mockedSoundpacksDirectory, @TempDir Path mockedTrashedSoundpacksDirectory) {
//        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
//
//            // prepare mock data ---
//            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedSoundpacksDirectory);
//            mockedPaths.when(Paths::getCustomTrashedSoundpacksPath).thenReturn(mockedTrashedSoundpacksDirectory);
//
//            File MOCKED_Soundpack_ZIP = TestUtils.getFromResource("Soundpack/sample/unzipped/cdda_mutation_rebalance_Soundpack");
//
//            Result<Throwable, SoundpackDTO> installResult = instance.installSoundpack(MOCKED_Soundpack_ZIP, unused -> { });
//
//            File MOCKED_FOREIGN_FILE = TestUtils.getFromResource("Soundpack/sample/zipped/cdda_mutation_rebalance_Soundpack.zip");
//            FileUtils.copyFile(MOCKED_FOREIGN_FILE, Paths.getCustomSoundpacksDir()
//                    .resolve("cdda_mutation_rebalance_Soundpack")
//                    .resolve("foreign_folder")
//                    .resolve("foreign_file.zip")
//                    .toFile()
//            );
//
//
//            // pre-test assertions ---
//            assertThat(installResult).isNotNull();
//            assertThat(installResult.toEither().isRight()).isTrue();
//
//            SoundpackDTO registeredSoundpack = installResult.getOrElseThrowUnchecked();
//            assertThat(registeredSoundpack).isNotNull();
//
//            File MOCKED_INSTALLED_Soundpack = Paths.getCustomSoundpacksDir().resolve("cdda_mutation_rebalance_Soundpack").toFile();
//
//            CustomFileAssertions.assertThat( // assert that Soundpack is installed
//                    MOCKED_INSTALLED_Soundpack
//            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
//                    "Soundpackinfo.json",
//                    "README.md",
//                    "items/armor/integrated.json",
//                    "foreign_folder/foreign_file.zip"
//            ));
//
//            File MOCKED_TRASHED_SoundpackS_FOLDER = Paths.getCustomTrashedSoundpacksPath().toFile();
//            assertThat(MOCKED_TRASHED_SoundpackS_FOLDER).isEmptyDirectory(); // assert that trash folder is empty
//
//            // execute test ---
//            Result<Throwable, Void> result = instance.trashSoundpackFromSoundpacksFolder(registeredSoundpack);
//
//            // verify assertions ---
//            assertThat(result).isNotNull(); // assert non-null result
//
//            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success
//
//            CustomFileAssertions.assertThat( // assert that Soundpack folder still contains foreign/untracked file
//                    MOCKED_INSTALLED_Soundpack
//            ).containsExactlyFilesWithRelativePaths(List.of(
//                    "foreign_folder/foreign_file.zip"
//            ));
//
//            // trashed Soundpack assertions -
//            assertThat(MOCKED_TRASHED_SoundpackS_FOLDER.listFiles()).hasSize(1);
//            File MOCKED_TRASHED_Soundpack = Objects.requireNonNull(MOCKED_TRASHED_SoundpackS_FOLDER.listFiles())[0];
//            assertThat(MOCKED_TRASHED_Soundpack).exists();
//
//            assertThat(MOCKED_TRASHED_Soundpack.getName()).contains( // assert that trashed Soundpack is timestamp-ed
//                    new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
//            );
//
//            CustomFileAssertions.assertThat( // assert that Soundpack is available in trash folder
//                    MOCKED_TRASHED_Soundpack
//            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
//                    "cdda_mutation_rebalance_Soundpack/Soundpackinfo.json",
//                    "cdda_mutation_rebalance_Soundpack/README.md",
//                    "cdda_mutation_rebalance_Soundpack/items/armor/integrated.json"
//            ));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    void parse_file_to_dto_success() {
//
//        // prepare mock data ---
//        File MOCKED_Soundpack_ZIP = TestUtils.getFromResource("Soundpack/sample/unzipped/cdda_mutation_rebalance_Soundpack");
//
//        // execute test ---
//        SoundpackDTO result = instance.parse(MOCKED_Soundpack_ZIP);
//
//        // verify assertions ---
//        assertThat(result).isNotNull(); // assert non-null result
//
//        SoundpackDTO EXPECTED_RESULT = getExpectedSoundpackDtoForTests(
//                TestUtils.getFromResource("Soundpack/sample/unzipped").toPath()
//        );
//
//        assertThat(result).usingRecursiveComparison().ignoringFields(
//                "Soundpackfiles" // this one will be asserted on individually next
//        ).isEqualTo(EXPECTED_RESULT);
//        assertThat(result.getSoundpackfiles()).containsExactlyInAnyOrderElementsOf(EXPECTED_RESULT.getSoundpackfiles());
//    }
//
//    @Test
//    void install_Soundpack_success_with_zip(@TempDir Path mockedDirectory) {
//        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
//
//            // prepare mock data ---
//            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedDirectory);
//            File MOCKED_Soundpack_ZIP = TestUtils.getFromResource("Soundpack/sample/zipped/cdda_mutation_rebalance_Soundpack.zip");
//
//            AtomicBoolean called = new AtomicBoolean(false);
//            AtomicReference<SoundpackDTO> calledWith = new AtomicReference<>();
//            Consumer<SoundpackDTO> MOCKED_CALLBACK = SoundpackDTO -> {
//                called.set(true);
//                calledWith.set(SoundpackDTO);
//            };
//
//            // pre-test assertions ---
//            assertThat(MOCKED_Soundpack_ZIP).isNotNull();
//            assertThat(MOCKED_Soundpack_ZIP).isFile();
//
//            // execute test ---
//            Result<Throwable, SoundpackDTO> result = instance.installSoundpack(MOCKED_Soundpack_ZIP, MOCKED_CALLBACK);
//
//            // verify assertions ---
//            assertThat(result).isNotNull(); // assert non-null result
//            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success
//
//            // assert on DTO result -
//            SoundpackDTO ACTUAL_RESULT = result.getOrElseThrowUnchecked();
//
//            assertThat(called.get()).isTrue();
//            assertThat(calledWith.get()).isEqualTo(ACTUAL_RESULT);
//
//            SoundpackDTO EXPECTED_RESULT = getExpectedSoundpackDtoForTests(Paths.getCustomSoundpacksDir());
//
//            assertThat(ACTUAL_RESULT.getId()).isNotNull();
//            assertThat(ACTUAL_RESULT.getCreatedDate()).isNotNull();
//            assertThat(ACTUAL_RESULT.getUpdatedDate()).isNotNull();
//            assertThat(ACTUAL_RESULT.getCreatedDate()).isEqualTo(ACTUAL_RESULT.getUpdatedDate());
//            assertThat(ACTUAL_RESULT.getSoundpackfiles()).isNotNull();
//            assertThat(ACTUAL_RESULT).usingRecursiveComparison().ignoringFields(
//                    "id", "createdDate", "updatedDate",
//                    "Soundpackfiles" // this one will be asserted on individually next
//            ).isEqualTo(EXPECTED_RESULT);
//
//            assertThat(ACTUAL_RESULT.getSoundpackfiles()).extracting(SoundpackfileDTO::getId).isNotNull();
//            assertThat(ACTUAL_RESULT.getSoundpackfiles()).extracting(SoundpackfileDTO::getSoundpackId).isNotNull();
//            assertThat(ACTUAL_RESULT.getSoundpackfiles()).extracting(SoundpackfileDTO::getCreatedDate).isNotNull();
//            assertThat(ACTUAL_RESULT.getSoundpackfiles()).extracting(SoundpackfileDTO::getUpdatedDate).isNotNull();
//            assertThat(ACTUAL_RESULT.getSoundpackfiles()).allSatisfy(dto -> assertThat(dto.getCreatedDate()).isEqualTo(dto.getUpdatedDate()));
//            assertThat(ACTUAL_RESULT.getSoundpackfiles()).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
//                    "id", "SoundpackId", "createdDate", "updatedDate"
//            ).containsExactlyInAnyOrderElementsOf(EXPECTED_RESULT.getSoundpackfiles());
//
//            // assert on filesystem changes -
//            File MOCKED_INSTALLED_Soundpack = Paths.getCustomSoundpacksDir().resolve("cdda_mutation_rebalance_Soundpack").toFile();
//
//            CustomFileAssertions.assertThat(
//                    MOCKED_INSTALLED_Soundpack
//            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
//                    "Soundpackinfo.json",
//                    "README.md",
//                    "items/armor/integrated.json"
//            ));
//
//            // assert on database changes -
//            assertThat(instance.listAllRegisteredSoundpacks()).containsExactly(ACTUAL_RESULT);
//
//        }
//    }
//
//
//    @Test
//    void get_path_for_success(@TempDir Path mockedDirectory) {
//        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
//
//            // prepare mock data ---
//            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedDirectory);
//            SoundpackDTO MOCKED_DTO = SoundpackDTO.builder().name("cdda_mutation_rebalance_Soundpack").build();
//
//            // execute test ---
//            instance.getPathFor(MOCKED_DTO);
//
//            // verify assertions ---
//            assertThat(instance.getPathFor(MOCKED_DTO)).isEqualTo(
//                    Paths.getCustomSoundpacksDir().resolve("cdda_mutation_rebalance_Soundpack")
//            );
//        }
//    }
//
//    @Test
//    void get_Soundpack_for_success(@TempDir Path mockedDirectory) {
//        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
//
//            // prepare mock data ---
//            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedDirectory);
//
//            SoundpackfileDTO MOCKED_SoundpackFILE_1_1 = SoundpackfileDTO.builder()
//                    .path("/a/mocked/1/1.path")
//                    .hash("aMockedHash1_1")
//                    .build();
//
//            SoundpackfileDTO MOCKED_SoundpackFILE_1_2 = SoundpackfileDTO.builder()
//                    .path("/a/mocked/1/2.path")
//                    .hash("aMockedHash1_2")
//                    .build();
//
//            SoundpackfileDTO MOCKED_SoundpackFILE_1_3 = SoundpackfileDTO.builder()
//                    .path("/a/mocked/1/3.path")
//                    .hash("aMockedHash1_3")
//                    .build();
//
//            SoundpackDTO MOCKED_Soundpack_1 = SoundpackDTO.builder()
//                    .name("mockedSoundpack1")
//                    .Soundpackinfo("mockedSoundpackInfo1")
//                    .Soundpackfiles(Arrays.asList(
//                            MOCKED_SoundpackFILE_1_1,
//                            MOCKED_SoundpackFILE_1_2,
//                            MOCKED_SoundpackFILE_1_3
//                    ))
//                    .build();
//
//
//            SoundpackfileDTO MOCKED_SoundpackFILE_2_1 = SoundpackfileDTO.builder()
//                    .path("/a/mocked/2/1.path")
//                    .hash("aMockedHash2_1")
//                    .build();
//
//            SoundpackfileDTO MOCKED_SoundpackFILE_2_2 = SoundpackfileDTO.builder()
//                    .path("/a/mocked/2/2.path")
//                    .hash("aMockedHash2_2")
//                    .build();
//
//            SoundpackfileDTO MOCKED_SoundpackFILE_2_3 = SoundpackfileDTO.builder()
//                    .path("/a/mocked/2/3.path")
//                    .hash("aMockedHash2_3")
//                    .build();
//
//            SoundpackDTO MOCKED_Soundpack_2 = SoundpackDTO.builder()
//                    .name("mockedSoundpack2")
//                    .Soundpackinfo("mockedSoundpackInfo2")
//                    .Soundpackfiles(Arrays.asList(
//                            MOCKED_SoundpackFILE_2_1,
//                            MOCKED_SoundpackFILE_2_2,
//                            MOCKED_SoundpackFILE_2_3
//                    ))
//                    .build();
//
//
//            SoundpackfileDTO MOCKED_SoundpackFILE_3_1 = SoundpackfileDTO.builder()
//                    .path("/a/mocked/3/1.path")
//                    .hash("aMockedHash3_1")
//                    .build();
//
//            SoundpackfileDTO MOCKED_SoundpackFILE_3_2 = SoundpackfileDTO.builder()
//                    .path("/a/mocked/3/2.path")
//                    .hash("aMockedHash3_2")
//                    .build();
//
//            SoundpackfileDTO MOCKED_SoundpackFILE_3_3 = SoundpackfileDTO.builder()
//                    .path("/a/mocked/3/3.path")
//                    .hash("aMockedHash3_3")
//                    .build();
//
//            SoundpackDTO MOCKED_Soundpack_3 = SoundpackDTO.builder()
//                    .name("mockedSoundpack3")
//                    .Soundpackinfo("mockedSoundpackInfo3")
//                    .Soundpackfiles(Arrays.asList(
//                            MOCKED_SoundpackFILE_3_1,
//                            MOCKED_SoundpackFILE_3_2,
//                            MOCKED_SoundpackFILE_3_3
//                    ))
//                    .build();
//
//            SoundpackDTO EXPECTED_RESULT_1 = instance.registerSoundpack(MOCKED_Soundpack_1).getOrElseThrowUnchecked();
//            SoundpackDTO EXPECTED_RESULT_2 = instance.registerSoundpack(MOCKED_Soundpack_2).getOrElseThrowUnchecked();
//            SoundpackDTO EXPECTED_RESULT_3 = instance.registerSoundpack(MOCKED_Soundpack_3).getOrElseThrowUnchecked();
//
//            File MOCKED_Soundpack = Paths.getCustomSoundpacksDir().resolve("mockedSoundpack1").toFile();
//
//            // pre-test assertions ---
//            assertThat(instance.listAllRegisteredSoundpacks()).hasSize(3);
//
//            // execute test ---
//            Optional<SoundpackDTO> result = instance.getSoundpackFor(MOCKED_Soundpack);
//
//            // verify assertions ---
//            assertThat(result).isNotNull();
//            assertThat(result.isPresent()).isTrue();
//
//            assertThat(result.get()).isEqualTo(EXPECTED_RESULT_1);
//        }
//    }
//
//    @Test
//    void get_Soundpack_for_not_found_success(@TempDir Path mockedDirectory) {
//        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
//
//            // prepare mock data ---
//            mockedPaths.when(Paths::getCustomSoundpacksDir).thenReturn(mockedDirectory);
//
//            File MOCKED_Soundpack =  Paths.getCustomSoundpacksDir().resolve("mockedSoundpack1").toFile();
//
//            // pre-test assertions ---
//            assertThat(instance.listAllRegisteredSoundpacks()).hasSize(0);
//
//            // execute test ---
//            Optional<SoundpackDTO> result = instance.getSoundpackFor(MOCKED_Soundpack);
//
//            // verify assertions ---
//            assertThat(result).isNotNull();
//            assertThat(result.isEmpty()).isTrue();
//        }
//    }
}