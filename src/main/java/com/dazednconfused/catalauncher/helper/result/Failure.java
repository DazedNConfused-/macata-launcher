package com.dazednconfused.catalauncher.helper.result;

import io.vavr.control.Either;

/**
 * Represents an operation's failure.
 * */
public class Failure<L extends Throwable,R> implements Result<L,R> {

    private final L error;

    protected Failure(L error) {
        this.error = error;
    }

    public L getError() {
        return error;
    }

    @Override
    public Either<Failure<L,R>, Success<L,R>> toEither() {
        return Either.left(this);
    }
}