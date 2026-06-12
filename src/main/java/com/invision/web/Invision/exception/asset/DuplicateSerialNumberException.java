package com.invision.web.Invision.exception.asset;

public class DuplicateSerialNumberException extends RuntimeException {
    private final String serialNumber;

    public DuplicateSerialNumberException(String serialNumber) {
        super("Asset with serial number '" + serialNumber + "' already exists");
        this.serialNumber = serialNumber;
    }

    public DuplicateSerialNumberException(String serialNumber, String message) {
        super(message);
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}