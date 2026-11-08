package me.jkiakumbo.payment_ledger.exceptions;

public class InsufficientAmountException extends RuntimeException {
    public InsufficientAmountException(String message) {
        super(message);
    }
}
