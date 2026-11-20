package me.jkiakumbo.payment_ledger.repository;

import me.jkiakumbo.payment_ledger.domain.Account;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    int save(Account account);
    Optional<Account> findById(UUID accountId);
    int updateBalance(UUID accountId, BigDecimal balance);
}
