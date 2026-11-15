package me.jkiakumbo.payment_ledger.exception;

public class InsufficientBalanceException extends IllegalStateException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
