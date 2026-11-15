package me.jkiakumbo.payment_ledger.exception;

public class SameAccountTransferException extends IllegalArgumentException {
    public SameAccountTransferException(String message) {
        super(message);
    }
}
