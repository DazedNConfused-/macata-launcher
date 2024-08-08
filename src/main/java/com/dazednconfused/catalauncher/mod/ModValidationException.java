package com.dazednconfused.catalauncher.mod;

public class ModValidationException extends RuntimeException {

    public ModValidationException(String message) {
        super(message);
    }

    public ModValidationException() {
        super();
    }

    public ModValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModValidationException(Throwable cause) {
        super(cause);
    }

    protected ModValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
