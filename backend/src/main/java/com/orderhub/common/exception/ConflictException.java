package com.orderhub.common.exception;

/** The request conflicts with the current state of a resource (e.g. a duplicate unique key). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
