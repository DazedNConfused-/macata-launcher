package com.dazednconfused.catalauncher.configuration;

import static com.dazednconfused.catalauncher.helper.Constants.LAUNCHER_FILES;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vavr.control.Try;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);

    private static final String CONFIG_FILEPATH = LAUNCHER_FILES + "/configuration.json";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static ConfigurationManager instance;

    private final Configuration configuration;

    /**
     * Singleton.
     * */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    /**
     * Constructor.
     * */
    private ConfigurationManager() {
        LOGGER.info("Loading global configuration...");
        Optional<Configuration> loadedConfiguration = load();

        if (loadedConfiguration.isPresent()) {
            this.configuration = loadedConfiguration.get();
            LOGGER.info("Configuration file loaded: [{}]", configuration);
        } else {
            this.configuration = save(new Configuration());
            LOGGER.info("Configuration file not present. Setting up defaults: [{}]", configuration);
        }
    }

    public String getCddaPath() {
        return this.configuration.getCddaPath();
    }

    public void setCddaPath(String cddaPath) {
        this.configuration.setCddaPath(cddaPath);
        save(this.configuration);
    }

    public boolean isBackupOnExit() {
        return this.configuration.isBackupOnExit();
    }

    public void setBackupOnExit(boolean backupOnExit) {
        this.configuration.setBackupOnExit(backupOnExit);
        save(this.configuration);
    }

    public boolean isDebug() {
        return this.configuration.isDebug();
    }

    public void setDebug(boolean debug) {
        this.configuration.setDebug(debug);
        save(this.configuration);
    }

    public boolean isShouldLookForUpdates() {
        return this.configuration.isShouldLookForUpdates();
    }

    public void setShouldLookForUpdates(boolean shouldLookForUpdates) {
        this.configuration.setShouldLookForUpdates(shouldLookForUpdates);
        save(this.configuration);
    }

    /**
     * Saves the given {@link Configuration} to disk.
     * */
    private static Configuration save(Configuration configuration) {
        File file = new File(CONFIG_FILEPATH);

        if (!file.getParentFile().exists()) {
            LOGGER.debug("[{}] doesn't exist. Creating folder structure...", file.getParentFile());
            Try.of(() -> file.getParentFile().mkdirs()).onFailure(throwable ->
                    LOGGER.error("There was an error creating configuration folder structure [{}]", file.getParentFile(), throwable)
            );
        }
        if (!file.exists()) {
            LOGGER.debug("[{}] doesn't exist. Creating file...", file.getName());
            Try.of(file::createNewFile).onFailure(throwable ->
                    LOGGER.error("There was an error creating configuration file [{}]", file.getName(), throwable)
            );
        }

        try (FileWriter fw = new FileWriter(file)) {
            LOGGER.debug("Writing configuration [{}] into [{}]...", configuration, file);
            IOUtils.write(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(configuration), fw);
            return configuration;
        } catch (IOException e) {
            LOGGER.error("There was an error saving configuration to [{}]", file, e);
            LOGGER.debug("Configuration: [{}]", configuration);
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the currently saved {@link Configuration}; or {@link Optional#empty()} if no {@link Configuration} exists.
     * */
    private static Optional<Configuration> load() {
        File file = new File(CONFIG_FILEPATH);

        if (!file.exists()) {
            LOGGER.debug("No configuration file exists in [{}]", file);
            return Optional.empty();
        }

        try (FileReader fr = new FileReader(file)) {
            return Optional.of(OBJECT_MAPPER.readValue(fr, Configuration.class));
        } catch (IOException e) {
            LOGGER.error("There was an error reading configuration from [{}]", CONFIG_FILEPATH, e);
            throw new RuntimeException(e);
        }
    }
}
