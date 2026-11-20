package me.jkiakumbo.payment_ledger.service;

import me.jkiakumbo.payment_ledger.domain.Account;
import me.jkiakumbo.payment_ledger.exception.AccountNotFoundException;
import me.jkiakumbo.payment_ledger.exception.InsufficientBalanceException;
import me.jkiakumbo.payment_ledger.exception.InvalidAmountException;
import me.jkiakumbo.payment_ledger.exception.SameAccountTransferException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class TransferService {
    private final ConcurrentMap<UUID, Account> accounts;

    public TransferService(ConcurrentMap<UUID, Account> accounts) {
        this.accounts = accounts;
    }

    public synchronized void transfer(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount) {

        if (Objects.isNull(amount) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        if (amount.scale() > 2) {
            throw new InvalidAmountException("Transfer amount must have maximum two decimal places");
        }

        if (sourceAccountId == destinationAccountId) {
            throw new SameAccountTransferException("Transfer to the same account is not allowed");
        }

        Account from = accounts.get(sourceAccountId);
        Account to = accounts.get(destinationAccountId);

        transfer(from, to, amount);

    }

    private void transfer(Account from, Account to, BigDecimal amount) {
        if (Objects.isNull(from) || Objects.isNull(to)){
            throw new AccountNotFoundException("One of the account not found");
        }

        if (from.balance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        BigDecimal fromBalance = from.balance().subtract(amount);
        BigDecimal toBalance = to.balance().add(amount);

        this.accounts.put(from.id(), from.copyWithBalance(fromBalance));
        this.accounts.put(to.id(), to.copyWithBalance(toBalance));

    }
}
