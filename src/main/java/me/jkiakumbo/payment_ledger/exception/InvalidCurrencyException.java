package me.jkiakumbo.payment_ledger.exception;

public class InvalidCurrencyException extends IllegalArgumentException {
    public InvalidCurrencyException(String message) {
        super(message);
    }
}
