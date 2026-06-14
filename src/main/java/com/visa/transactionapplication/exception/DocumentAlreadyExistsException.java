package com.visa.transactionapplication.exception;

public class DocumentAlreadyExistsException extends RuntimeException {

    public DocumentAlreadyExistsException(String documentNumber) {
        super("Account already exists with document number: " + documentNumber);
    }
}
