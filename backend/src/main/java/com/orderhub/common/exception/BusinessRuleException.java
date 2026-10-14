package com.orderhub.common.exception;

/** A well-formed request that breaks a domain rule (insufficient stock, illegal status change...). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
