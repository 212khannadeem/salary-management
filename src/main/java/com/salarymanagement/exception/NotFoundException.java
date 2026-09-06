package com.salarymanagement.exception;

public class NotFoundException extends ApiException {
    public NotFoundException(String m) {
        super("NOT_FOUND", m);
    }
}
