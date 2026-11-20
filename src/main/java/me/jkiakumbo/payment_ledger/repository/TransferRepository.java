package me.jkiakumbo.payment_ledger.repository;

import me.jkiakumbo.payment_ledger.domain.Account;
import me.jkiakumbo.payment_ledger.domain.Transfer;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {
    int save(Transfer transfer);
    Optional<Transfer> findById(UUID transferId);
}
