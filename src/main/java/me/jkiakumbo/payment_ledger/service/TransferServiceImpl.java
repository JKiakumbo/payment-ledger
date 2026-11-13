package me.jkiakumbo.payment_ledger.service;

import jakarta.transaction.Transactional;
import me.jkiakumbo.payment_ledger.domain.Currency;
import me.jkiakumbo.payment_ledger.exceptions.*;
import me.jkiakumbo.payment_ledger.command.TransferCommand;
import me.jkiakumbo.payment_ledger.domain.Account;
import me.jkiakumbo.payment_ledger.repository.Accounts;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransferServiceImpl implements TransferService {
    private final Accounts accounts;

    public TransferServiceImpl (Accounts accounts) {
        this.accounts = accounts;
    }

    @Override
    @Transactional
    public void transfer(TransferCommand command) {

        if (command.amount() <= 0) {
            throw new InvalidAmountException("The transfer amount must be greater than zero");
        }

        if (command.sourceAccountId() == command.destinationAccountId()) {
            throw new TransferToSameAccountException("Transfer to same account is not allowed");
        }

        long firstId = Math.min(command.sourceAccountId(), command.destinationAccountId());
        long secondId = Math.max(command.sourceAccountId(), command.destinationAccountId());


        Account first  = accounts.findByIdForUpdate(firstId)
                .orElseThrow(() -> new AccountNotFoundException("Account with id" + firstId + " not found"));

        Account second = accounts.findByIdForUpdate(secondId)
                .orElseThrow(() -> new AccountNotFoundException("Account with id" + secondId + " not found"));

        Account source = firstId == command.sourceAccountId() ? first : second;
        Account destination = firstId == command.destinationAccountId() ? first : second;

        transfer(source, destination, command.amount());

    }

    private void transfer(Account source, Account destination, long amount) {
        if (source.getBalance() < amount) {
            throw new InsufficientAmountException("The money is not enough to complete the transfer");
        }

        if (!Currency.GBP.equals(source.getCurrency()) || !Currency.GBP.equals(destination.getCurrency())) {
            throw new UnsupportedCurrencyException("Cannot transfer to account with different currency");
        }

        try {
            long sourceCurrentBalance = Math.subtractExact(source.getBalance(), amount);
            long destinationCurrentBalance = Math.addExact(destination.getBalance(), amount);
            source.setBalance(sourceCurrentBalance);
            destination.setBalance(destinationCurrentBalance);
            accounts.saveAllAndFlush(List.of(source, destination));
        } catch (ArithmeticException exception) {
            throw new TransferNotCompletedException("The transfer cannot be completed: " + exception.getMessage());
        }
        
    }
}
