package com.dazednconfused.catalauncher.helper;

import io.vavr.control.Try;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that reads build-time information from {@link #GIT_PROPERTIES_BUILD_TIME_FILE}, which is a file generated
 * by {@code git-commit-id-maven-plugin} during Maven's build stage.
 * <br/><br/>
 * Usage of this class during development will default to latest build information, if available. If {@link #GIT_PROPERTIES_BUILD_TIME_FILE}
 * is not present in the {@code target/classes} folder, all properties returned will be blank (because there is no build-time
 * information from which to retrieve these properties).
 * */
public class GitInfoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitInfoManager.class);

    private static final String GIT_PROPERTIES_BUILD_TIME_FILE = "git.properties";

    private static GitInfoManager instance;
    private static final Object lock = new Object(); //thread-safety singleton lock

    @Nullable
    private final Properties properties;

    /**
     * Singleton.
     * */
    public static GitInfoManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new GitInfoManager();
                }
            }
        }
        return instance;
    }

    /**
     * Constructor.
     * */
    private GitInfoManager() {
        this.properties = loadBuildProperties();
    }

    /**
     * Returns the {@code git.build.version} property.
     * */
    public String getBuildVersion() {
        return Optional.ofNullable(this.properties).map(p -> p.getProperty("git.build.version")).orElse("");
    }

    /**
     * Returns the {@code git.commit.id.full} property.
     * */
    public String getCommitIdFull() {
        return Optional.ofNullable(this.properties).map(p -> p.getProperty("git.commit.id.full")).orElse("");
    }

    /**
     * Returns the {@code git.build.time} property.
     * */
    public String getBuildTime() {
        return Optional.ofNullable(this.properties).map(p -> p.getProperty("git.build.time")).orElse("");
    }

    /**
     * Loads the build properties from {@link #GIT_PROPERTIES_BUILD_TIME_FILE}.
     * */
    @Nullable
    private Properties loadBuildProperties() {
        ClassLoader classLoader = this.getClass().getClassLoader();

        try (InputStream input = classLoader.getResourceAsStream(GIT_PROPERTIES_BUILD_TIME_FILE)) {
            return Try.of(() -> {
                Properties p = new Properties();
                p.load(input);
                return p;
            }).onFailure(t -> LOGGER.error("Could not load git information from [{}]", GIT_PROPERTIES_BUILD_TIME_FILE, t)).getOrNull();
        } catch (IOException e) {
            LOGGER.error("There was an error while loading build properties", e);
            throw new RuntimeException(e);
        }
    }
}
