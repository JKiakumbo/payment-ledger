package me.jkiakumbo.payment_ledger.exceptions;

public class TransferToSameAccountException extends RuntimeException {
    public TransferToSameAccountException(String message) {
        super(message);
    }
}
