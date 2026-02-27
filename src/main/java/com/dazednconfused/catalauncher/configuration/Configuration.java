package com.dazednconfused.catalauncher.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vavr.control.Try;

public class Configuration {

    private String cddaPath;
    private boolean backupOnExit;
    private boolean debug;
    private boolean shouldLookForUpdates;
    private String gameGithubRepoOwner;
    private String gameGithubRepoName;
    private boolean shouldCheckForGameUpdates;
    private String installedGameVersion;
    private boolean includePreReleaseBuilds;

    public Configuration() {
    }

    public String getCddaPath() {
        return cddaPath;
    }

    public void setCddaPath(String cddaPath) {
        this.cddaPath = cddaPath;
    }

    public boolean isBackupOnExit() {
        return backupOnExit;
    }

    public void setBackupOnExit(boolean backupOnExit) {
        this.backupOnExit = backupOnExit;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isShouldLookForUpdates() {
        return shouldLookForUpdates;
    }

    public void setShouldLookForUpdates(boolean shouldLookForUpdates) {
        this.shouldLookForUpdates = shouldLookForUpdates;
    }

    public String getGameGithubRepoOwner() {
        return gameGithubRepoOwner;
    }

    public void setGameGithubRepoOwner(String gameGithubRepoOwner) {
        this.gameGithubRepoOwner = gameGithubRepoOwner;
    }

    public String getGameGithubRepoName() {
        return gameGithubRepoName;
    }

    public void setGameGithubRepoName(String gameGithubRepoName) {
        this.gameGithubRepoName = gameGithubRepoName;
    }

    public boolean isShouldCheckForGameUpdates() {
        return shouldCheckForGameUpdates;
    }

    public void setShouldCheckForGameUpdates(boolean shouldCheckForGameUpdates) {
        this.shouldCheckForGameUpdates = shouldCheckForGameUpdates;
    }

    public String getInstalledGameVersion() {
        return installedGameVersion;
    }

    public void setInstalledGameVersion(String installedGameVersion) {
        this.installedGameVersion = installedGameVersion;
    }

    public boolean isIncludePreReleaseBuilds() {
        return includePreReleaseBuilds;
    }

    public void setIncludePreReleaseBuilds(boolean includePreReleaseBuilds) {
        this.includePreReleaseBuilds = includePreReleaseBuilds;
    }

    @Override
    public String toString() {
        return Try.of(() -> new ObjectMapper().writeValueAsString(this)).getOrElse(super::toString);
    }
}
