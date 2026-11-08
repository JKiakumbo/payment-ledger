package me.jkiakumbo.payment_ledger.exceptions;

public class UnsupportedCurrencyException extends RuntimeException {
    public UnsupportedCurrencyException(String message) {
        super(message);
    }
}
