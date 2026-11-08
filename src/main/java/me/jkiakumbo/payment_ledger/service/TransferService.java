package me.jkiakumbo.payment_ledger.service;

import me.jkiakumbo.payment_ledger.command.TransferCommand;

public interface TransferService {
    void transfer(TransferCommand command);
}
