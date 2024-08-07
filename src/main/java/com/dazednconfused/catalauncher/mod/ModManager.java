package com.dazednconfused.catalauncher.mod;

import com.dazednconfused.catalauncher.database.mod.dao.ModH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.repository.ModH2RepositoryImpl;
import com.dazednconfused.catalauncher.database.mod.repository.ModRepository;
import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.helper.Zipper;
import com.dazednconfused.catalauncher.helper.result.Result;
import com.dazednconfused.catalauncher.mod.dto.ModDTO;
import com.dazednconfused.catalauncher.mod.dto.ModfileDTO;
import com.dazednconfused.catalauncher.mod.mapper.ModMapper;

import io.vavr.control.Try;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModManager.class);
    private static ModManager instance;

    protected final ModRepository modRepository;

    /**
     * Singleton.
     */
    public static synchronized ModManager getInstance() {
        if (instance == null) {
            instance = new ModManager();
        }
        return instance;
    }

    /**
     * Constructor.
     */
    private ModManager() {
        this.modRepository = new ModH2RepositoryImpl(
            new ModH2DAOImpl(),
            new ModfileH2DAOImpl()
        );
    }

    /**
     * Returns all mods currently registered.
     *
     * @implNote A mod is <i>registered</i> only when it is retrievable from the mods' database.
     * */
    public List<ModDTO> listAllRegisteredMods() {
        LOGGER.debug("Listing all mods...");
        return this.modRepository.findAll().stream()
            .map(ModMapper.INSTANCE::toDTO)
            .collect(Collectors.toList());

        /*
        return Arrays.stream(Objects.requireNonNull(getModsFolder().listFiles()))
                .filter(file -> !file.getName().equals(".DS_Store"))
                .collect(Collectors.toList());
        */
    }

    /**
     * Installs given {@code toBeInstalled} mod inside {@link Paths#getCustomModsDir()}.
     * */
    public Result<Throwable, ModDTO> installMod(File toBeInstalled, Consumer<ModDTO> onDoneCallback) {
        LOGGER.info("Installing mod [{}]...", toBeInstalled);
        throw new RuntimeException("Not implemented yet");
        /*
        return Try.of(() -> {
            // validate -

            // parse into DTO -

            // copy to mods folder -

            // register -

        }).map(dto -> {
            onDoneCallback.accept(dto);
            return Result.success(dto);
        }).onFailure(
            t -> LOGGER.error("There was an error installing mod [{}]", toBeInstalled.getPath(), t)
        ).recover(Result::failure).get();

        */
    }

    /**
     * Unregisters and deletes the given {@code toBeUninstalled} mod.
     * */
    public Result<Throwable, Void> uninstallMod(ModDTO toBeUninstalled) {
        LOGGER.info("Uninstalling mod [{}]...", toBeUninstalled);

        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Registers the given {@code toBeRegistered} mod.
     * */
    protected Result<Throwable, ModDTO> registerMod(ModDTO toBeRegistered) {
        LOGGER.debug("Registering mod [{}]...", toBeRegistered);
        return Try.of(() ->
            this.modRepository.insert(ModMapper.INSTANCE.toEntity(toBeRegistered))
        ).onFailure(
            t -> LOGGER.error("There was an error registering mod [{}]", toBeRegistered, t)
        ).map(ModMapper.INSTANCE::toDTO).map(Result::success).recover(Result::failure).get();
    }

    /**
     * Unregisters the given {@code toBeUnregistered} mod.
     * */
    protected Result<Throwable, Void> unregisterMod(ModDTO toBeUnregistered) {
        LOGGER.debug("Unregistering mod [{}]...", toBeUnregistered);
        return Try.run(() ->
            this.modRepository.delete(ModMapper.INSTANCE.toEntity(toBeUnregistered))
        ).onFailure(
            t -> LOGGER.error("There was an error unregistering mod [{}]", toBeUnregistered, t)
        ).map(Result::success).recover(Result::failure).get();
    }

    /**
     * Copies the given {@code toBeInstalled} mod into the {@link Paths#getCustomModsDir()} folder.
     * */
    protected Result<Throwable, Void> copyModToModsFolder(File toBeInstalled) {
        File installInto = new File(getModsFolder().getPath() + "/" + toBeInstalled.getName());

        return Try.run(() -> {
            LOGGER.debug("Copying [{}] into [{}]...", toBeInstalled, installInto);
            FileUtils.copyDirectory(toBeInstalled, installInto);
        }).onFailure(
            t -> LOGGER.error("There was an error installing mod [{}]", toBeInstalled, t)
        ).map(Result::success).recover(Result::failure).get();
    }

    /**
     * Deletes the given {@code toBeUninstalled} mod from the {@link Paths#getCustomModsDir()} folder.
     * */
    protected Result<Throwable, Void> deleteModFromModsFolder(File toBeUninstalled) {
        return Try.run(() -> {
            LOGGER.debug("Deleting [{}]...", toBeUninstalled);
            FileUtils.deleteDirectory(toBeUninstalled);
        }).onFailure(
            t -> LOGGER.error("There was an error deleting mod [{}]", toBeUninstalled, t)
        ).map(Result::success).recover(Result::failure).get();
    }

    /**
     * Retrieves the {@link Paths#getCustomModsDir()} as a {@link File}.
     * */
    protected File getModsFolder() {
        File modsPath = new File(Paths.getCustomModsDir());
        if (!modsPath.exists()) {
            LOGGER.debug("Mods folder [{}] not found. Creating...", modsPath);
            Try.of(modsPath::mkdirs).onFailure(t -> LOGGER.error("Could not create mods destination folder [{}]", modsPath, t));
        }

        return modsPath;
    }

    /**
     * Returns whether the given {@code toBeInstalled} mod is a valid installation candidate. If it is, it will return a parsed
     * {@link File} folder structure ready to be copied to the destination folder, wrapped inside a {@link Result#success()}.
     * Otherwise, it will return the validation error, wrapped inside a {@link Result#failure(Throwable)}.
     * */
    protected Result<Throwable, File> validateMod(File toBeInstalled) {
        LOGGER.debug("Validating mod [{}]...", toBeInstalled);

        return Try.of(() -> {
            if (!toBeInstalled.exists()) {
                LOGGER.trace("Mod file [{}] does not exist and thus is considered invalid...", toBeInstalled);
                throw new ModValidationException("Mod file [" + toBeInstalled + "] does not exist");
            }

            if (!toBeInstalled.canRead()) {
                LOGGER.trace("Mod file [{}] cannot be read and thus is considered invalid...", toBeInstalled);
                throw new ModValidationException("Mod file [" + toBeInstalled + "] cannot be read");
            }

            File result;
            if (toBeInstalled.isDirectory()) {
                LOGGER.trace("Mod file [{}] is a directory...", toBeInstalled);
                result = toBeInstalled;
            } else if (toBeInstalled.getName().endsWith(".zip")) {
                LOGGER.trace("Mod file [{}] is a .zip file...", toBeInstalled);
                result = unzipToTempFolder(toBeInstalled);
            } else {
                LOGGER.trace("Mod file [{}] is neither a directory nor a .zip file...", toBeInstalled);
                throw new ModValidationException("Mod file [" + toBeInstalled + "] is neither a directory nor a zip file");
            }

            File modInfoFile = new File(result, "modinfo.json");
            if (!modInfoFile.exists() || !modInfoFile.isFile()) {
                LOGGER.trace("Mod doesn't contain modinfo.json and thus is considered invalid...");
                throw new ModValidationException("modinfo.json not found in mod file [" + toBeInstalled + "]");
            }

            return result;
        }).map(Result::success).recover(Result::failure).get();
    }

    /**
     * Parses the given {@code mod} {@link File} into its {@link ModDTO} representation.
     * */
    protected ModDTO parse(File mod) {
        List<File> modfiles = new ArrayList<>();
        com.dazednconfused.catalauncher.utils.FileUtils.collectAllFilesFromInto(mod, modfiles);

        return ModDTO.builder()
            .name(mod.getName())
            .modinfo(getModInfoFor(mod))
            .modfiles(
                modfiles.stream().map(file -> ModfileDTO.builder()
                    .path(file.getPath())
                    .hash(com.dazednconfused.catalauncher.utils.FileUtils.getFileChecksum(file))
                    .build()
                ).collect(Collectors.toList())
            )
            .build();
    }

    /**
     * Retrieves the {@code modinfo.json} from the given {@code mod} {@link File}.
     * */
    private static String getModInfoFor(File mod) {
        File modInfoFile = new File(mod, "modinfo.json");

        try(BufferedReader reader = Files.newBufferedReader(modInfoFile.toPath())) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unzips the given {@code zipFile} into a temporary folder.
     * */
    private File unzipToTempFolder(File zipFile) throws IOException {
        Path tempDir = Files.createTempDirectory("macata_mod_unzip");

        Zipper.decompressAndCallback(
            zipFile, tempDir,
            unused -> { },
            1000
        );

        // derive the folder name from the zip file name (without extension)
        String zipFileName = zipFile.getName();
        String folderName = zipFileName.substring(0, zipFileName.lastIndexOf('.'));

        // locate the folder inside the temp directory
        Path specificFolderPath = tempDir.resolve(folderName);

        // Check if the specific folder exists
        if (Files.exists(specificFolderPath) && Files.isDirectory(specificFolderPath)) {
            return specificFolderPath.toFile();
        } else {
            throw new IOException("Expected folder not found: " + specificFolderPath);
        }
    }

}
