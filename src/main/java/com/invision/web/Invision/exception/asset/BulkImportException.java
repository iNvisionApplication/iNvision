package com.invision.web.Invision.exception.asset;

public class BulkImportException extends RuntimeException {
    public BulkImportException(String message) {
        super(message);
    }

    public BulkImportException(String message, Throwable cause) {
        super(message, cause);
    }
}