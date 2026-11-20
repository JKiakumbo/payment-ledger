package me.jkiakumbo.payment_ledger.domain;

import me.jkiakumbo.payment_ledger.exception.InvalidAmountException;
import me.jkiakumbo.payment_ledger.exception.InvalidCurrencyException;
import me.jkiakumbo.payment_ledger.exception.SameAccountTransferException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public record Transfer(
        UUID id,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        Currency currency,
        LocalDateTime createdAt
) {
    public Transfer {
        validate(sourceAccountId, destinationAccountId, amount, currency);
    }

    public Transfer (UUID id, UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount, Currency currency) {
        this(id, sourceAccountId, destinationAccountId, amount, currency, LocalDateTime.now());
    }

    public Transfer (UUID id, UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount) {
        this(id, sourceAccountId, destinationAccountId, amount, Currency.GBP);
    }
    public Transfer (UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount) {
        this(UUID.randomUUID(), sourceAccountId, destinationAccountId, amount);
    }


    private static void validate(UUID source, UUID destination, BigDecimal amount, Currency currency) {
        if (Objects.isNull(amount) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount cannot be null or negative");
        }

        if (currency == null) {
            throw new InvalidCurrencyException("Currency must be provided");
        }

        if (amount.scale() > 2) {
            throw new InvalidAmountException("Invalid number of decimal places");
        }

        if (Objects.isNull(source) || Objects.isNull(destination)) {
            throw new IllegalArgumentException("Source account and destination accounts required");
        }

        if ( source.equals(destination)) {
            throw new SameAccountTransferException("Transfer to same account is not allowed");
        }
    }
}
