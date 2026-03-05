package org.newsanalyzer.service;

/**
 * Thrown when a Federal Register API response cannot be deserialized.
 * Allows callers to distinguish parse failures from "not found" responses.
 */
public class FederalRegisterParseException extends RuntimeException {

    public FederalRegisterParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
