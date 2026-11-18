package me.jkiakumbo.payment_ledger.domain;

import lombok.*;
import me.jkiakumbo.payment_ledger.exception.InvalidAmountException;
import me.jkiakumbo.payment_ledger.exception.InvalidCurrencyException;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public record Account(UUID id, BigDecimal balance, Currency currency, ZonedDateTime createdAt) {

    public Account {
        validate(balance, currency);
    }

    public Account (UUID id, BigDecimal balance, Currency currency) {
        this(id, balance, currency, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    public Account (BigDecimal balance, Currency currency) {
        this(UUID.randomUUID(), balance, currency);
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
        return new Account(this.id, balance, this.currency, createdAt);
    }
}
