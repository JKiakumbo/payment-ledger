package me.jkiakumbo.payment_ledger.domain;

import me.jkiakumbo.payment_ledger.exception.InvalidAmountException;
import me.jkiakumbo.payment_ledger.exception.InvalidCurrencyException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldRejectAccountWithFractionalPennies() {
        assertThrowsExactly(InvalidAmountException.class, () -> new Account(
                UUID.randomUUID(),
                BigDecimal.valueOf(100.001),
                Currency.GBP)
        );
    }

    @Test
    void shouldRejectAccountWithNullCurrency() {
        assertThrowsExactly(InvalidCurrencyException.class, () -> new Account(
                BigDecimal.valueOf(100),
                null)
        );
    }

    @Test
    void shouldRejectAccountWithNullBalance() {
        assertThrowsExactly(InvalidAmountException.class, () -> new Account(
                UUID.randomUUID(),
                null,
                Currency.GBP,
                LocalDateTime.now())
        );
    }

    @Test
    void shouldRejectAccountWithNegativeBalance() {
        assertThrowsExactly(InvalidAmountException.class, () -> new Account(
                UUID.randomUUID(),
                BigDecimal.valueOf(-100),
                Currency.GBP,
                LocalDateTime.now())
        );
    }


}