package com.dazednconfused.catalauncher.helper.result;

import io.vavr.control.Either;

/**
 * Represents the result of an operation that can either yield a {@link Success} or a {@link Failure} upon completion.
 * */
public interface Result<L extends Throwable, R> {

    /**
     * Represents a {@link Result} that has ended in {@link Success}.
     * */
    static <L extends Throwable, R> Result<L,R> success(R result) {
        return new Success<>(result);
    }

    /**
     * Represents a {@link Result} that has ended in {@link Success}.
     * */
    static <L extends Throwable, R> Result<L,R> success() {
        return new Success<>();
    }

    /**
     * Represents a {@link Result} that has ended in {@link Failure}.
     * */
    static <L extends Throwable, R> Result<L,R> failure(L error) {
        return new Failure<>(error);
    }

    Either<Failure<L,R>, Success<L,R>> toEither();
}
