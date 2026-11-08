package me.jkiakumbo.payment_ledger.exceptions;

public class TransferNotCompletedException extends RuntimeException {
    public TransferNotCompletedException(String message) {
        super(message);
    }
}
