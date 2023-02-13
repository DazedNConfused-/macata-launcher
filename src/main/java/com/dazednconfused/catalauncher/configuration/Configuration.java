package com.dazednconfused.catalauncher.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vavr.control.Try;

public class Configuration {

    private String cddaPath;
    private boolean backupOnExit;
    private boolean debug;

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

    @Override
    public String toString() {
        return Try.of(() -> new ObjectMapper().writeValueAsString(this)).getOrElse(super::toString);
    }
}
