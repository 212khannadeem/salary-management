package com.salarymanagement.exception;

public class ConflictException extends ApiException {
    public ConflictException(String c, String m) {
        super(c, m);
    }
}
