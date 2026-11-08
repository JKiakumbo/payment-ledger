package me.jkiakumbo.payment_ledger.command;

public record TransferCommand(
        long sourceAccountId,
        long destinationAccountId,
        long amount
) { }
