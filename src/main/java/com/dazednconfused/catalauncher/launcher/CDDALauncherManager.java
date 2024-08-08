package com.dazednconfused.catalauncher.launcher;

import com.dazednconfused.catalauncher.helper.Paths;

import io.vavr.control.Try;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDDALauncherManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDDALauncherManager.class);

    private static final String OSX_LAUNCHER_WRAPPER_SCRIPT = "osx_launcher_wrapper.sh";
    private static final String OSX_LAUNCHER_WRAPPER_SCRIPT_FILEPATH = Paths.getLauncherFiles() + "/" + OSX_LAUNCHER_WRAPPER_SCRIPT;

    /**
     * Executes {@code CDDA}'s application with the given arguments.
     * */
    public static Process executeCddaApplication(String appPath, String ...args) {
        return executeCddaApplication(ArrayUtils.addAll(new String[]{ "sh", loadLauncherWrapper().getPath(), appPath }, args));
    }

    /**
     * Executes {@code CDDA}'s application with the given arguments.
     * */
    private static Process executeCddaApplication(String[] cmdarray) {
        LOGGER.info("Executing command [{}]", (Object) cmdarray);

        return Try.of(() -> Runtime.getRuntime().exec(cmdarray)).onFailure(t -> LOGGER.error("There was an error executing [{}]", cmdarray, t)).getOrNull();
    }

    /**
     * Creates a new thread to monitor the {@code CDDA} application after it has been run, and waits for it to exit. When it does,
     * it immediately executes the given {@link Runnable} object.
     * */
    public static void monitorCddaProcess(Process process, Runnable onExit) {
        new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                LOGGER.info("CDDA process exited with code {}", exitCode);
                onExit.run();
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting for CDDA process to exit", e);
                // no further cleanup needed - just go ahead and proceed with the interruption
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Loads a customized {@code cataclysm-tiles} wrapper launcher, closely resembling the one the official {@code Cataclysm.app}
     * comes bundled with, but with the distinct difference that this one allows arguments to be passed to the main binary (which
     * is something that can normally be done, but not from macOS' ".app" application).
     *
     * @apiNote {@link #OSX_LAUNCHER_WRAPPER_SCRIPT_FILEPATH} will be overwritten on this method's invocation. Therefore, custom
     *          user modifications to the launcher's wrapper will not last.
     * */
    private static File loadLauncherWrapper() {
        File file = new File(OSX_LAUNCHER_WRAPPER_SCRIPT_FILEPATH);

        LOGGER.debug("Extracting OSX launcher wrapper into [{}]...", file);

        try (InputStream launcherWrapperInputStream = CDDALauncherManager.class.getClassLoader().getResourceAsStream(OSX_LAUNCHER_WRAPPER_SCRIPT)) {
            FileUtils.copyInputStreamToFile(Objects.requireNonNull(launcherWrapperInputStream), file);
        } catch (IOException e) {
            LOGGER.error("There was an error while extracting launcher wrapper from [{}]", file, e);
            throw new RuntimeException(e);
        }

        return file;
    }
}
