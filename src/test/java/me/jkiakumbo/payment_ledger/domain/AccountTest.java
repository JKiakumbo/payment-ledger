package me.jkiakumbo.payment_ledger.domain;

import me.jkiakumbo.payment_ledger.exception.InvalidAmountException;
import me.jkiakumbo.payment_ledger.exception.InvalidCurrencyException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldRejectAccountWithFractionalPennies() {
        assertThrowsExactly(InvalidAmountException.class, () -> new Account(
                3L,
                BigDecimal.valueOf(100.001),
                Currency.GBP)
        );
    }

    @Test
    void shouldRejectAccountWithNullCurrency() {
        assertThrowsExactly(InvalidCurrencyException.class, () -> new Account(
                3L,
                BigDecimal.valueOf(100),
                null)
        );
    }

    @Test
    void shouldRejectAccountWithNullBalance() {
        assertThrowsExactly(InvalidAmountException.class, () -> new Account(
                3L,
                null,
                Currency.GBP)
        );
    }

    @Test
    void shouldRejectAccountWithNegativeBalance() {
        assertThrowsExactly(InvalidAmountException.class, () -> new Account(
                3L,
                BigDecimal.valueOf(-100),
                Currency.GBP)
        );
    }


}