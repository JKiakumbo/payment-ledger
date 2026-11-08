package me.jkiakumbo.payment_ledger.service;

import me.jkiakumbo.payment_ledger.domain.Currency;
import me.jkiakumbo.payment_ledger.exceptions.*;
import me.jkiakumbo.payment_ledger.command.TransferCommand;
import me.jkiakumbo.payment_ledger.domain.Account;
import me.jkiakumbo.payment_ledger.repository.Accounts;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TransferServiceImpl implements TransferService {
    final private Accounts accounts;

    public TransferServiceImpl (Accounts accounts) {
        this.accounts = accounts;
    }

    @Override
    public void transfer(TransferCommand command) {

        if (command.amount() <= 0) {
            throw new InvalidAmountException("The transfer amount must be greater than zero");
        }

        if (command.sourceAccountId() == command.destinationAccountId()) {
            throw new TransferToSameAccountException("Transfer to same account is not allowed");
        }


        Optional<Account> sourceOptional  = accounts.getById(command.sourceAccountId());
        Account source = sourceOptional.orElseThrow(() -> new SourceAccountNotFoundException("Source account not found"));

        Optional<Account> destinationOptional = accounts.getById(command.destinationAccountId());
        Account destination = destinationOptional.orElseThrow(() -> new DestinationAccountNotFoundException("Destination account not found"));

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
        } catch (ArithmeticException exception) {
            throw new TransferNotCompletedException("The transfer cannot be completed: " + exception.getMessage());
        }
        
    }
}
