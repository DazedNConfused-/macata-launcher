package com.dazednconfused.catalauncher.mod;

import com.dazednconfused.catalauncher.assertions.CustomFileAssertions;
import com.dazednconfused.catalauncher.database.base.DisposableDatabase;
import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.helper.result.Result;
import com.dazednconfused.catalauncher.mod.dto.ModDTO;
import com.dazednconfused.catalauncher.mod.dto.ModfileDTO;
import com.dazednconfused.catalauncher.utils.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ModManagerTest {

    private static ModManager instance;

    @BeforeAll
    public static void setup() {
        instance = ModManager.getInstance();
    }

    @AfterEach
    public void teardown() {
        ((DisposableDatabase) instance.modRepository).reset();
    }
    
    @AfterAll
    public static void cleanup() {
        ((DisposableDatabase) instance.modRepository).destroy();
    }

    @Test
    void register_mod_success() {

        // prepare mock data ---
        ModfileDTO MOCKED_MODFILE_1 = ModfileDTO.builder()
                .path("/a/mocked/1.path")
                .hash("aMockedHash1")
                .build();

        ModfileDTO MOCKED_MODFILE_2 = ModfileDTO.builder()
                .path("/a/mocked/2.path")
                .hash("aMockedHash2")
                .build();

        ModfileDTO MOCKED_MODFILE_3 = ModfileDTO.builder()
                .path("/a/mocked/3.path")
                .hash("aMockedHash3")
                .build();

        ModDTO MOCKED_MOD = ModDTO.builder()
                .name("mockedMod")
                .modinfo("mockedModInfo")
                .modfiles(Arrays.asList(
                        MOCKED_MODFILE_1,
                        MOCKED_MODFILE_2,
                        MOCKED_MODFILE_3
                ))
                .build();

        // execute test ---
        Result<Throwable, ModDTO> result = instance.registerMod(MOCKED_MOD);

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result
        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

        assertThat(result.getOrElseThrowUnchecked()).isNotNull();
        ModDTO ACTUAL_RESULT = result.getOrElseThrowUnchecked();

        assertThat(ACTUAL_RESULT).isNotNull();
        assertThat(ACTUAL_RESULT.getName()).isEqualTo("mockedMod");
        assertThat(ACTUAL_RESULT.getModinfo()).isEqualTo("mockedModInfo");
        assertThat(ACTUAL_RESULT.getModfiles()).isNotNull();
        assertThat(ACTUAL_RESULT.getModfiles()).hasSize(3);


        ModfileDTO EXPECTED_MODFILE_1 = ModfileDTO.builder()
                .modId(ACTUAL_RESULT.getId())
                .path("/a/mocked/1.path")
                .hash("aMockedHash1")
                .build();

        ModfileDTO EXPECTED_MODFILE_2 = ModfileDTO.builder()
                .modId(ACTUAL_RESULT.getId())
                .path("/a/mocked/2.path")
                .hash("aMockedHash2")
                .build();

        ModfileDTO EXPECTED_MODFILE_3 = ModfileDTO.builder()
                .modId(ACTUAL_RESULT.getId())
                .path("/a/mocked/3.path")
                .hash("aMockedHash3")
                .build();

        assertThat(ACTUAL_RESULT.getModfiles()).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                "id", "createdDate", "updatedDate"
        ).containsExactlyInAnyOrder(
                EXPECTED_MODFILE_1,
                EXPECTED_MODFILE_2,
                EXPECTED_MODFILE_3
        );

        assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getCreatedDate).allSatisfy(createdDate -> assertThat(createdDate).isNotNull());
        assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getUpdatedDate).allSatisfy(updatedDate -> assertThat(updatedDate).isNotNull());
        assertThat(ACTUAL_RESULT.getModfiles()).allSatisfy(dto -> assertThat(dto.getCreatedDate()).isEqualTo(dto.getUpdatedDate()));
    }

    @Test
    void list_all_registered_mods_success() {

        // prepare mock data ---
        ModfileDTO MOCKED_MODFILE_1_1 = ModfileDTO.builder()
                .path("/a/mocked/1/1.path")
                .hash("aMockedHash1_1")
                .build();

        ModfileDTO MOCKED_MODFILE_1_2 = ModfileDTO.builder()
                .path("/a/mocked/1/2.path")
                .hash("aMockedHash1_2")
                .build();

        ModfileDTO MOCKED_MODFILE_1_3 = ModfileDTO.builder()
                .path("/a/mocked/1/3.path")
                .hash("aMockedHash1_3")
                .build();

        ModDTO MOCKED_MOD_1 = ModDTO.builder()
                .name("mockedMod1")
                .modinfo("mockedModInfo1")
                .modfiles(Arrays.asList(
                        MOCKED_MODFILE_1_1,
                        MOCKED_MODFILE_1_2,
                        MOCKED_MODFILE_1_3
                ))
                .build();


        ModfileDTO MOCKED_MODFILE_2_1 = ModfileDTO.builder()
                .path("/a/mocked/2/1.path")
                .hash("aMockedHash2_1")
                .build();

        ModfileDTO MOCKED_MODFILE_2_2 = ModfileDTO.builder()
                .path("/a/mocked/2/2.path")
                .hash("aMockedHash2_2")
                .build();

        ModfileDTO MOCKED_MODFILE_2_3 = ModfileDTO.builder()
                .path("/a/mocked/2/3.path")
                .hash("aMockedHash2_3")
                .build();

        ModDTO MOCKED_MOD_2 = ModDTO.builder()
                .name("mockedMod2")
                .modinfo("mockedModInfo2")
                .modfiles(Arrays.asList(
                        MOCKED_MODFILE_2_1,
                        MOCKED_MODFILE_2_2,
                        MOCKED_MODFILE_2_3
                ))
                .build();


        ModfileDTO MOCKED_MODFILE_3_1 = ModfileDTO.builder()
                .path("/a/mocked/3/1.path")
                .hash("aMockedHash3_1")
                .build();

        ModfileDTO MOCKED_MODFILE_3_2 = ModfileDTO.builder()
                .path("/a/mocked/3/2.path")
                .hash("aMockedHash3_2")
                .build();

        ModfileDTO MOCKED_MODFILE_3_3 = ModfileDTO.builder()
                .path("/a/mocked/3/3.path")
                .hash("aMockedHash3_3")
                .build();

        ModDTO MOCKED_MOD_3 = ModDTO.builder()
                .name("mockedMod3")
                .modinfo("mockedModInfo3")
                .modfiles(Arrays.asList(
                        MOCKED_MODFILE_3_1,
                        MOCKED_MODFILE_3_2,
                        MOCKED_MODFILE_3_3
                ))
                .build();

        ModDTO EXPECTED_RESULT_1 = instance.registerMod(MOCKED_MOD_1).getOrElseThrowUnchecked();
        ModDTO EXPECTED_RESULT_2 = instance.registerMod(MOCKED_MOD_2).getOrElseThrowUnchecked();
        ModDTO EXPECTED_RESULT_3 = instance.registerMod(MOCKED_MOD_3).getOrElseThrowUnchecked();

        // execute test ---
        List<ModDTO> result = instance.listAllRegisteredMods();

        // verify assertions ---
        assertThat(result).containsExactlyInAnyOrder(
                EXPECTED_RESULT_1,
                EXPECTED_RESULT_2,
                EXPECTED_RESULT_3
        );
    }

    @Test
    void unregister_mod_success() {

        // prepare mock data ---
        ModfileDTO MOCKED_MODFILE_1_1 = ModfileDTO.builder()
                .path("/a/mocked/1/1.path")
                .hash("aMockedHash1_1")
                .build();

        ModfileDTO MOCKED_MODFILE_1_2 = ModfileDTO.builder()
                .path("/a/mocked/1/2.path")
                .hash("aMockedHash1_2")
                .build();

        ModfileDTO MOCKED_MODFILE_1_3 = ModfileDTO.builder()
                .path("/a/mocked/1/3.path")
                .hash("aMockedHash1_3")
                .build();

        ModDTO MOCKED_MOD_1 = ModDTO.builder()
                .name("mockedMod1")
                .modinfo("mockedModInfo1")
                .modfiles(Arrays.asList(
                        MOCKED_MODFILE_1_1,
                        MOCKED_MODFILE_1_2,
                        MOCKED_MODFILE_1_3
                ))
                .build();


        ModfileDTO MOCKED_MODFILE_2_1 = ModfileDTO.builder()
                .path("/a/mocked/2/1.path")
                .hash("aMockedHash2_1")
                .build();

        ModfileDTO MOCKED_MODFILE_2_2 = ModfileDTO.builder()
                .path("/a/mocked/2/2.path")
                .hash("aMockedHash2_2")
                .build();

        ModfileDTO MOCKED_MODFILE_2_3 = ModfileDTO.builder()
                .path("/a/mocked/2/3.path")
                .hash("aMockedHash2_3")
                .build();

        ModDTO MOCKED_MOD_2 = ModDTO.builder()
                .name("mockedMod2")
                .modinfo("mockedModInfo2")
                .modfiles(Arrays.asList(
                        MOCKED_MODFILE_2_1,
                        MOCKED_MODFILE_2_2,
                        MOCKED_MODFILE_2_3
                ))
                .build();


        ModfileDTO MOCKED_MODFILE_3_1 = ModfileDTO.builder()
                .path("/a/mocked/3/1.path")
                .hash("aMockedHash3_1")
                .build();

        ModfileDTO MOCKED_MODFILE_3_2 = ModfileDTO.builder()
                .path("/a/mocked/3/2.path")
                .hash("aMockedHash3_2")
                .build();

        ModfileDTO MOCKED_MODFILE_3_3 = ModfileDTO.builder()
                .path("/a/mocked/3/3.path")
                .hash("aMockedHash3_3")
                .build();

        ModDTO MOCKED_MOD_3 = ModDTO.builder()
                .name("mockedMod3")
                .modinfo("mockedModInfo3")
                .modfiles(Arrays.asList(
                        MOCKED_MODFILE_3_1,
                        MOCKED_MODFILE_3_2,
                        MOCKED_MODFILE_3_3
                ))
                .build();

        ModDTO EXPECTED_TO_BE_UNREGISTERED = instance.registerMod(MOCKED_MOD_1).getOrElseThrowUnchecked();
        ModDTO EXPECTED_TO_REMAIN_1 = instance.registerMod(MOCKED_MOD_2).getOrElseThrowUnchecked();
        ModDTO EXPECTED_TO_REMAIN_2 = instance.registerMod(MOCKED_MOD_3).getOrElseThrowUnchecked();

        // pre-test assertions ---
        assertThat(instance.listAllRegisteredMods()).containsExactlyInAnyOrder(
                EXPECTED_TO_BE_UNREGISTERED,
                EXPECTED_TO_REMAIN_1,
                EXPECTED_TO_REMAIN_2
        );

        // execute test ---
        Result<Throwable, Void> result = instance.unregisterMod(EXPECTED_TO_BE_UNREGISTERED);

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

        assertThat(result.toEither().get().getResult().isEmpty()).isTrue(); // assert that Result's Success is empty

        assertThat(instance.listAllRegisteredMods()).containsExactlyInAnyOrder(
                EXPECTED_TO_REMAIN_1,
                EXPECTED_TO_REMAIN_2
        );
    }

    @Test
    void get_mods_folder_success(@TempDir Path mockedDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomModsDir).thenReturn(mockedDirectory.toString());

            // execute test ---
            File result = instance.getModsFolder();

            // verify assertions ---
            assertThat(result).isEqualTo(new File(Paths.getCustomModsDir()));
        }
    }

    @Test
    void validate_mod_for_zip_success() {

        // prepare mock data ---
        File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/zipped/cdda_mutation_rebalance_mod.zip");

        // execute test ---
        Result<Throwable, File> result = instance.validateMod(MOCKED_MOD_ZIP);

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result
        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

        assertThat(result.getOrElseThrowUnchecked()).isNotNull();
        File ACTUAL_RESULT = result.getOrElseThrowUnchecked();

        CustomFileAssertions.assertThat(ACTUAL_RESULT).containsExactlyFilesWithRelativePaths(Arrays.asList(
                "modinfo.json",
                "README.md",
                "items/armor/integrated.json"
        ));
    }

    @Test
    void validate_mod_for_directory_success() {

        // prepare mock data ---
        File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/unzipped/cdda_mutation_rebalance_mod");

        // execute test ---
        Result<Throwable, File> result = instance.validateMod(MOCKED_MOD_ZIP);

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result
        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

        assertThat(result.getOrElseThrowUnchecked()).isNotNull();
        File ACTUAL_RESULT = result.getOrElseThrowUnchecked();

        CustomFileAssertions.assertThat(ACTUAL_RESULT).containsExactlyFilesWithRelativePaths(Arrays.asList(
            "modinfo.json",
            "README.md",
            "items/armor/integrated.json"
        ));
    }

    @Test
    void validate_mod_failure_when_path_does_not_exist() {

        // prepare mock data ---
        File MOCKED_MOD_ZIP = mock(File.class);
        when(MOCKED_MOD_ZIP.exists()).thenReturn(false);

        // execute test ---
        Result<Throwable, File> result = instance.validateMod(MOCKED_MOD_ZIP);

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        assertThat(result.toEither().isRight()).isFalse(); // assert that Result is not a Success
    }

    @Test
    void validate_mod_failure_when_path_cannot_be_read() {

        // prepare mock data ---
        File MOCKED_MOD_ZIP = mock(File.class);
        when(MOCKED_MOD_ZIP.exists()).thenReturn(true);
        when(MOCKED_MOD_ZIP.canRead()).thenReturn(false);

        // execute test ---
        Result<Throwable, File> result = instance.validateMod(MOCKED_MOD_ZIP);

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        assertThat(result.toEither().isRight()).isFalse(); // assert that Result is not a Success
    }

    @Test
    void validate_mod_failure_when_path_is_not_a_directory_nor_a_zip_file() {

        // prepare mock data ---
        File MOCKED_MOD_ZIP = mock(File.class);
        when(MOCKED_MOD_ZIP.exists()).thenReturn(true);
        when(MOCKED_MOD_ZIP.canRead()).thenReturn(true);

        when(MOCKED_MOD_ZIP.isDirectory()).thenReturn(false);
        when(MOCKED_MOD_ZIP.getName()).thenReturn("mock");

        // execute test ---
        Result<Throwable, File> result = instance.validateMod(MOCKED_MOD_ZIP);

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        assertThat(result.toEither().isRight()).isFalse(); // assert that Result is not a Success
    }

    @Test
    void validate_mod_failure_when_modinfo_json_does_not_exist() {

        // prepare mock data ---
        File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/invalid");

        // execute test ---
        Result<Throwable, File> result = instance.validateMod(MOCKED_MOD_ZIP);

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        assertThat(result.toEither().isRight()).isFalse(); // assert that Result is not a Success
    }

    @Test
    void copy_mod_to_mods_folder_success(@TempDir Path mockedDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomModsDir).thenReturn(mockedDirectory.toString());
            File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/unzipped/cdda_mutation_rebalance_mod");

            // execute test ---
            Result<Throwable, File> result = instance.copyModToModsFolder(MOCKED_MOD_ZIP);

            // verify assertions ---
            assertThat(result).isNotNull(); // assert non-null result
            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            File ACTUAL_RESULT = result.getOrElseThrowUnchecked();
            assertThat(ACTUAL_RESULT).isNotNull();

            Path EXPECTED_PATH = Path.of(Paths.getCustomModsDir(), "cdda_mutation_rebalance_mod");
            assertThat(ACTUAL_RESULT.getPath()).isEqualTo(EXPECTED_PATH.toString());

            CustomFileAssertions.assertThat(
                    new File(EXPECTED_PATH.toString())
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "modinfo.json",
                    "README.md",
                    "items/armor/integrated.json"
            ));
        }
    }

    @Test
    void trash_mod_from_mods_folder_success(@TempDir Path mockedModsDirectory, @TempDir Path mockedTrashedModsDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomModsDir).thenReturn(mockedModsDirectory.toString());
            mockedPaths.when(Paths::getCustomTrashedModsPath).thenReturn(mockedTrashedModsDirectory.toString());

            File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/unzipped/cdda_mutation_rebalance_mod");

            Result<Throwable, ModDTO> installResult = instance.installMod(MOCKED_MOD_ZIP, unused -> { });

            // pre-test assertions ---
            assertThat(installResult).isNotNull();
            assertThat(installResult.toEither().isRight()).isTrue();

            ModDTO registeredMod = installResult.getOrElseThrowUnchecked();
            assertThat(registeredMod).isNotNull();

            File MOCKED_INSTALLED_MOD = new File(Path.of(Paths.getCustomModsDir(), "cdda_mutation_rebalance_mod").toString());

            CustomFileAssertions.assertThat( // assert that mod is installed
                    MOCKED_INSTALLED_MOD
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "modinfo.json",
                    "README.md",
                    "items/armor/integrated.json"
            ));

            File MOCKED_TRASHED_MODS_FOLDER = new File(Paths.getCustomTrashedModsPath());
            assertThat(MOCKED_TRASHED_MODS_FOLDER).isEmptyDirectory(); // assert that trash folder is empty

            // execute test ---
            Result<Throwable, Void> result = instance.trashModFromModsFolder(registeredMod);

            // verify assertions ---
            assertThat(result).isNotNull(); // assert non-null result

            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            assertThat(MOCKED_INSTALLED_MOD).doesNotExist(); // assert that mod no longer exists

            // trashed mod assertions -
            assertThat(MOCKED_TRASHED_MODS_FOLDER.listFiles()).hasSize(1);
            File MOCKED_TRASHED_MOD = Objects.requireNonNull(MOCKED_TRASHED_MODS_FOLDER.listFiles())[0];
            assertThat(MOCKED_TRASHED_MOD).exists();

            assertThat(MOCKED_TRASHED_MOD.getName()).contains( // assert that trashed mod is timestamp-ed
                    new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
            );

            CustomFileAssertions.assertThat( // assert that mod is available in trash folder
                    MOCKED_TRASHED_MOD
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "cdda_mutation_rebalance_mod/modinfo.json",
                    "cdda_mutation_rebalance_mod/README.md",
                    "cdda_mutation_rebalance_mod/items/armor/integrated.json"
            ));
        }
    }

    @Test
    void trash_mod_from_mods_folder_with_foreign_files_success(@TempDir Path mockedModsDirectory, @TempDir Path mockedTrashedModsDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomModsDir).thenReturn(mockedModsDirectory.toString());
            mockedPaths.when(Paths::getCustomTrashedModsPath).thenReturn(mockedTrashedModsDirectory.toString());

            File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/unzipped/cdda_mutation_rebalance_mod");

            Result<Throwable, ModDTO> installResult = instance.installMod(MOCKED_MOD_ZIP, unused -> { });

            File MOCKED_FOREIGN_FILE = TestUtils.getFromResource("mod/sample/zipped/cdda_mutation_rebalance_mod.zip");
            FileUtils.copyFile(MOCKED_FOREIGN_FILE, new File(Path.of(
                    Paths.getCustomModsDir(),
                    "cdda_mutation_rebalance_mod",
                    "foreign_folder",
                    "foreign_file.zip"
            ).toString()));

            // pre-test assertions ---
            assertThat(installResult).isNotNull();
            assertThat(installResult.toEither().isRight()).isTrue();

            ModDTO registeredMod = installResult.getOrElseThrowUnchecked();
            assertThat(registeredMod).isNotNull();

            File MOCKED_INSTALLED_MOD = new File(Path.of(Paths.getCustomModsDir(), "cdda_mutation_rebalance_mod").toString());

            CustomFileAssertions.assertThat( // assert that mod is installed
                    MOCKED_INSTALLED_MOD
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "modinfo.json",
                    "README.md",
                    "items/armor/integrated.json",
                    "foreign_folder/foreign_file.zip"
            ));

            File MOCKED_TRASHED_MODS_FOLDER = new File(Paths.getCustomTrashedModsPath());
            assertThat(MOCKED_TRASHED_MODS_FOLDER).isEmptyDirectory(); // assert that trash folder is empty

            // execute test ---
            Result<Throwable, Void> result = instance.trashModFromModsFolder(registeredMod);

            // verify assertions ---
            assertThat(result).isNotNull(); // assert non-null result

            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            CustomFileAssertions.assertThat( // assert that mod folder still contains foreign/untracked file
                    MOCKED_INSTALLED_MOD
            ).containsExactlyFilesWithRelativePaths(List.of(
                    "foreign_folder/foreign_file.zip"
            ));

            // trashed mod assertions -
            assertThat(MOCKED_TRASHED_MODS_FOLDER.listFiles()).hasSize(1);
            File MOCKED_TRASHED_MOD = Objects.requireNonNull(MOCKED_TRASHED_MODS_FOLDER.listFiles())[0];
            assertThat(MOCKED_TRASHED_MOD).exists();

            assertThat(MOCKED_TRASHED_MOD.getName()).contains( // assert that trashed mod is timestamp-ed
                    new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
            );

            CustomFileAssertions.assertThat( // assert that mod is available in trash folder
                    MOCKED_TRASHED_MOD
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "cdda_mutation_rebalance_mod/modinfo.json",
                    "cdda_mutation_rebalance_mod/README.md",
                    "cdda_mutation_rebalance_mod/items/armor/integrated.json"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void parse_file_to_dto_success() {

        // prepare mock data ---
        File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/unzipped/cdda_mutation_rebalance_mod");

        // execute test ---
        ModDTO result = instance.parse(MOCKED_MOD_ZIP);

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        ModDTO EXPECTED_RESULT = getExpectedModDtoForTests(
                TestUtils.getFromResource("mod/sample/unzipped").toPath()
        );

        assertThat(result).isEqualTo(EXPECTED_RESULT);
    }

    @Test
    void install_mod_success_with_directory(@TempDir Path mockedDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomModsDir).thenReturn(mockedDirectory.toString());
            File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/unzipped/cdda_mutation_rebalance_mod");

            AtomicBoolean called = new AtomicBoolean(false);
            AtomicReference<ModDTO> calledWith = new AtomicReference<>();
            Consumer<ModDTO> MOCKED_CALLBACK = modDTO -> {
                called.set(true);
                calledWith.set(modDTO);
            };

            // pre-test assertions ---
            assertThat(MOCKED_MOD_ZIP).isNotNull();
            assertThat(MOCKED_MOD_ZIP).isDirectory();

            // execute test ---
            Result<Throwable, ModDTO> result = instance.installMod(MOCKED_MOD_ZIP, MOCKED_CALLBACK);

            // verify assertions ---
            assertThat(result).isNotNull(); // assert non-null result
            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            // assert on DTO result -
            ModDTO ACTUAL_RESULT = result.getOrElseThrowUnchecked();

            assertThat(called.get()).isTrue();
            assertThat(calledWith.get()).isEqualTo(ACTUAL_RESULT);

            ModDTO EXPECTED_RESULT = getExpectedModDtoForTests(
                    Path.of(Paths.getCustomModsDir())
            );

            assertThat(ACTUAL_RESULT.getId()).isNotNull();
            assertThat(ACTUAL_RESULT.getCreatedDate()).isNotNull();
            assertThat(ACTUAL_RESULT.getUpdatedDate()).isNotNull();
            assertThat(ACTUAL_RESULT.getCreatedDate()).isEqualTo(ACTUAL_RESULT.getUpdatedDate());
            assertThat(ACTUAL_RESULT.getModfiles()).isNotNull();
            assertThat(ACTUAL_RESULT).usingRecursiveComparison().ignoringFields(
                    "id", "createdDate", "updatedDate",
                    "modfiles" // this one will be asserted on individually next
            ).isEqualTo(EXPECTED_RESULT);

            assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getId).isNotNull();
            assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getModId).isNotNull();
            assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getCreatedDate).isNotNull();
            assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getUpdatedDate).isNotNull();
            assertThat(ACTUAL_RESULT.getModfiles()).allSatisfy(dto -> assertThat(dto.getCreatedDate()).isEqualTo(dto.getUpdatedDate()));
            assertThat(ACTUAL_RESULT.getModfiles()).usingRecursiveComparison().ignoringFields(
                    "id", "modId", "createdDate", "updatedDate"
            ).isEqualTo(EXPECTED_RESULT.getModfiles());

            // assert on filesystem changes -
            File MOCKED_INSTALLED_MOD = new File(Path.of(Paths.getCustomModsDir(), "cdda_mutation_rebalance_mod").toString());

            CustomFileAssertions.assertThat(
                    MOCKED_INSTALLED_MOD
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "modinfo.json",
                    "README.md",
                    "items/armor/integrated.json"
            ));

            // assert on database changes -
            assertThat(instance.listAllRegisteredMods()).containsExactly(ACTUAL_RESULT);

        }
    }

    @Test
    void install_mod_success_with_zip(@TempDir Path mockedDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomModsDir).thenReturn(mockedDirectory.toString());
            File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/zipped/cdda_mutation_rebalance_mod.zip");

            AtomicBoolean called = new AtomicBoolean(false);
            AtomicReference<ModDTO> calledWith = new AtomicReference<>();
            Consumer<ModDTO> MOCKED_CALLBACK = modDTO -> {
                called.set(true);
                calledWith.set(modDTO);
            };

            // pre-test assertions ---
            assertThat(MOCKED_MOD_ZIP).isNotNull();
            assertThat(MOCKED_MOD_ZIP).isFile();

            // execute test ---
            Result<Throwable, ModDTO> result = instance.installMod(MOCKED_MOD_ZIP, MOCKED_CALLBACK);

            // verify assertions ---
            assertThat(result).isNotNull(); // assert non-null result
            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            // assert on DTO result -
            ModDTO ACTUAL_RESULT = result.getOrElseThrowUnchecked();

            assertThat(called.get()).isTrue();
            assertThat(calledWith.get()).isEqualTo(ACTUAL_RESULT);

            ModDTO EXPECTED_RESULT = getExpectedModDtoForTests(
                    Path.of(Paths.getCustomModsDir())
            );

            assertThat(ACTUAL_RESULT.getId()).isNotNull();
            assertThat(ACTUAL_RESULT.getCreatedDate()).isNotNull();
            assertThat(ACTUAL_RESULT.getUpdatedDate()).isNotNull();
            assertThat(ACTUAL_RESULT.getCreatedDate()).isEqualTo(ACTUAL_RESULT.getUpdatedDate());
            assertThat(ACTUAL_RESULT.getModfiles()).isNotNull();
            assertThat(ACTUAL_RESULT).usingRecursiveComparison().ignoringFields(
                    "id", "createdDate", "updatedDate",
                    "modfiles" // this one will be asserted on individually next
            ).isEqualTo(EXPECTED_RESULT);

            assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getId).isNotNull();
            assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getModId).isNotNull();
            assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getCreatedDate).isNotNull();
            assertThat(ACTUAL_RESULT.getModfiles()).extracting(ModfileDTO::getUpdatedDate).isNotNull();
            assertThat(ACTUAL_RESULT.getModfiles()).allSatisfy(dto -> assertThat(dto.getCreatedDate()).isEqualTo(dto.getUpdatedDate()));
            assertThat(ACTUAL_RESULT.getModfiles()).usingRecursiveComparison().ignoringFields(
                    "id", "modId", "createdDate", "updatedDate"
            ).isEqualTo(EXPECTED_RESULT.getModfiles());

            // assert on filesystem changes -
            File MOCKED_INSTALLED_MOD = new File(Path.of(Paths.getCustomModsDir(), "cdda_mutation_rebalance_mod").toString());

            CustomFileAssertions.assertThat(
                    MOCKED_INSTALLED_MOD
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "modinfo.json",
                    "README.md",
                    "items/armor/integrated.json"
            ));

            // assert on database changes -
            assertThat(instance.listAllRegisteredMods()).containsExactly(ACTUAL_RESULT);

        }
    }

    @Test
    void uninstall_mod_success(@TempDir Path mockedModsDirectory, @TempDir Path mockedTrashedModsDirectory) {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            // prepare mock data ---
            mockedPaths.when(Paths::getCustomModsDir).thenReturn(mockedModsDirectory.toString());
            mockedPaths.when(Paths::getCustomTrashedModsPath).thenReturn(mockedTrashedModsDirectory.toString());

            File MOCKED_MOD_ZIP = TestUtils.getFromResource("mod/sample/zipped/cdda_mutation_rebalance_mod.zip");

            ModDTO MOCKED_DTO = instance.installMod(MOCKED_MOD_ZIP, unused -> { }).getOrElseThrowUnchecked();

            AtomicBoolean called = new AtomicBoolean(false);
            AtomicReference<ModDTO> calledWith = new AtomicReference<>();
            Consumer<ModDTO> MOCKED_CALLBACK = modDTO -> {
                called.set(true);
                calledWith.set(modDTO);
            };

            // pre-test assertions ---
            assertThat(MOCKED_DTO).isNotNull();

            assertThat(instance.listAllRegisteredMods()).containsExactly(MOCKED_DTO);

            CustomFileAssertions.assertThat(
                    new File(Paths.getCustomModsDir(), "cdda_mutation_rebalance_mod")
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "modinfo.json",
                    "README.md",
                    "items/armor/integrated.json"
            ));

            assertThat(new File(Paths.getCustomTrashedModsPath())).isEmptyDirectory();

            // execute test ---
            Result<Throwable, ModDTO> result = instance.uninstallMod(MOCKED_DTO, MOCKED_CALLBACK);

            // verify assertions ---

            // assert on DTO result -
            assertThat(result).isNotNull(); // assert non-null result
            assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

            ModDTO ACTUAL_RESULT = result.getOrElseThrowUnchecked();
            assertThat(ACTUAL_RESULT).isEqualTo(MOCKED_DTO);

            assertThat(called.get()).isTrue();
            assertThat(calledWith.get()).isEqualTo(ACTUAL_RESULT);

            // assert on filesystem changes -
            assertThat(new File(Paths.getCustomModsDir(), "cdda_mutation_rebalance_mod")).doesNotExist();

            assertThat(new File(Paths.getCustomTrashedModsPath())).isNotEmptyDirectory();
            CustomFileAssertions.assertThat(
                    Objects.requireNonNull(new File(Paths.getCustomTrashedModsPath()).listFiles())[0]
            ).containsExactlyFilesWithRelativePaths(Arrays.asList(
                    "cdda_mutation_rebalance_mod/modinfo.json",
                    "cdda_mutation_rebalance_mod/README.md",
                    "cdda_mutation_rebalance_mod/items/armor/integrated.json"
            ));

            // assert on database changes -
            assertThat(instance.listAllRegisteredMods()).isEmpty();
        }
    }

    /**
     * Expected DTO for some tests in {@link ModManagerTest}'s suite.
     * */
    private static ModDTO getExpectedModDtoForTests(Path rootPath) {
        return ModDTO.builder()
                .name("cdda_mutation_rebalance_mod")
                .modinfo("[\n" +
                        "  {\n" +
                        "    \"type\": \"MOD_INFO\",\n" +
                        "    \"id\": \"mutation_rebalance_mod\",\n" +
                        "    \"name\": \"Mutation Rebalance Mod\",\n" +
                        "    \"authors\": [ \"DazedNConfused-\" ],\n" +
                        "    \"maintainers\": [ \"DazedNConfused-\" ],\n" +
                        "    \"description\": \"Rebalances certain mutations with crippling nerfs.\",\n" +
                        "    \"category\": \"rebalance\",\n" +
                        "    \"dependencies\": [ \"dda\" ]\n" +
                        "  }\n" +
                        "]")
                .modfiles(Arrays.asList(
                        ModfileDTO.builder()
                                .path(new File(rootPath.toString(), "cdda_mutation_rebalance_mod/modinfo.json").getPath())
                                .hash("e4d6ee5815bf4d4d6a0454f97e1eba89")
                                .build(),
                        ModfileDTO.builder()
                                .path(new File(rootPath.toString(), "cdda_mutation_rebalance_mod/README.md").getPath())
                                .hash("cbd11359de789778523e307a7bdf419f")
                                .build(),
                        ModfileDTO.builder()
                                .path(new File(rootPath.toString(), "cdda_mutation_rebalance_mod/items/armor/integrated.json").getPath())
                                .hash("caeeb7c03d3aaf942221328f288e3924")
                                .build()
                ))
                .build();
    }
}