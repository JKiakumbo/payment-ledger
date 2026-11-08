package me.jkiakumbo.payment_ledger.exceptions;

public class DestinationAccountNotFoundException extends RuntimeException {
    public DestinationAccountNotFoundException(String message) {
        super(message);
    }
}
