package com.dazednconfused.catalauncher.mod;

import com.dazednconfused.catalauncher.database.mod.dao.ModH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.repository.ModH2RepositoryImpl;
import com.dazednconfused.catalauncher.database.mod.repository.ModRepository;
import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.helper.result.Result;
import com.dazednconfused.catalauncher.mod.dto.ModDTO;
import com.dazednconfused.catalauncher.mod.mapper.ModMapper;

import io.vavr.control.Try;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
     * Unregisters and deletes the given {@code toBeUninstalled} mod.
     * */
    public Result<Throwable, Void> uninstallMod(ModDTO toBeUninstalled) {
        LOGGER.info("Uninstalling mod [{}]...", toBeUninstalled);

        throw new RuntimeException("Not implemented yet");
        //Try.run(() -> FileUtils.deleteDirectory(toBeUninstalled)).onFailure(t -> LOGGER.error("There was an error deleting mod [{}]", toBeUninstalled, t));
    }

    /**
     * Installs given {@code toBeInstalled} mod inside {@link Paths#getCustomModsDir()}.
     * */
    public void installMod(File toBeInstalled, Consumer<Path> onDoneCallback) {
        LOGGER.info("Installing mod [{}]...", toBeInstalled);

        throw new RuntimeException("Not implemented yet");

        /*
        File installInto = new File(getModsFolder().getPath() + "/" + toBeInstalled.getName());

        Try.run(() -> {
            LOGGER.debug("Copying [{}] into [{}]...", toBeInstalled, installInto);
            FileUtils.copyDirectory(toBeInstalled, installInto);
        }).onFailure(t -> LOGGER.error("There was an error installing mod [{}]", toBeInstalled, t)).andThen(() -> onDoneCallback.accept(installInto.toPath()));
        */
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
     * Retrieves the {@link Paths#getCustomModsDir()} as a {@link File}.
     * */
    private static File getModsFolder() {
        File modsPath = new File(Paths.getCustomModsDir());
        if (!modsPath.exists()) {
            LOGGER.debug("Mods folder [{}] not found. Creating...", modsPath);
            Try.of(modsPath::mkdirs).onFailure(t -> LOGGER.error("Could not create mods destination folder [{}]", modsPath, t));
        }

        return modsPath;
    }
}
