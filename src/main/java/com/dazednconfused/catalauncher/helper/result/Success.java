package com.dazednconfused.catalauncher.helper.result;

import io.vavr.control.Either;

import java.util.Optional;

/**
 * Represents an operation's successful response.
 * */
public class Success<L extends Throwable,R> implements Result<L,R> {

    private final R result;

    /**
     * Protected constructor.
     * */
    protected Success(R result) {
        this.result = result;
    }

    /**
     * Protected constructor.
     * */
    protected Success() { // for those cases where the operation is a 'void'
        this.result = null;
    }

    public Optional<R> getResult() {
        return Optional.ofNullable(result);
    }

    @Override
    public Either<Failure<L,R>, Success<L,R>> toEither() {
        return Either.right(this);
    }
}
