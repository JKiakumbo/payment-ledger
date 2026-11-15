package me.jkiakumbo.payment_ledger.domain;

import lombok.*;
import me.jkiakumbo.payment_ledger.exception.InvalidAmountException;
import me.jkiakumbo.payment_ledger.exception.InvalidCurrencyException;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Builder
public class Account {
    private final long id;
    private final BigDecimal balance;
    private final Currency currency;

    public Account( long id, BigDecimal balance, Currency currency) {
        validate(balance, currency);

        this.id = id;
        this.balance = balance;
        this.currency = currency;
    }

    private static void validate(BigDecimal balance, Currency currency) {
        if (Objects.isNull(balance) || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Balance cannot be null or negative");
        }

        if (currency == null) {
            throw new InvalidCurrencyException("Currency must be provided");
        }

        if (balance.scale() > 2) {
            throw new InvalidAmountException("Invalid number of decimal places");
        }
    }

    public Account copyWithBalance(BigDecimal balance) {
        return new Account(this.getId(), balance, this.currency);
    }
}
