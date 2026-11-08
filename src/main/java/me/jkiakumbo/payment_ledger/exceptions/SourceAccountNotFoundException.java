package me.jkiakumbo.payment_ledger.exceptions;

public class SourceAccountNotFoundException extends RuntimeException {
    public SourceAccountNotFoundException(String message) {
        super(message);
    }
}
