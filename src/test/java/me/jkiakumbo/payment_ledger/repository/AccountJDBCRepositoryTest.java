package me.jkiakumbo.payment_ledger.repository;


import me.jkiakumbo.payment_ledger.config.PostgresTestConfig;
import me.jkiakumbo.payment_ledger.domain.Account;
import me.jkiakumbo.payment_ledger.domain.Currency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(PostgresTestConfig.class)
class AccountJDBCRepositoryTest {

    @Autowired
    private AccountJDBCRepository repository;

    @Test
    void shouldSaveAndFindAccount() {
        // GIVEN
        Account account = new Account(BigDecimal.TEN, Currency.GBP);

        // WHEN
        int affectedRow = repository.save(account);
        Optional<Account> savedAccount = repository.findById(account.id());

        // THEN
        assertEquals(1, affectedRow);
        assertTrue(savedAccount.isPresent());
        assertEquals(new BigDecimal("10.00"), savedAccount.get().balance());
        assertEquals(Currency.GBP, savedAccount.get().currency());
    }

    @Test
    void shouldUpdateBalance() {
        // GIVEN
        Account account = new Account(new BigDecimal("12.00"), Currency.GBP);
        repository.save(account);
        Optional<Account> savedAccount = repository.findById(account.id());
        assertEquals(new BigDecimal("12.00"), savedAccount.orElseThrow().balance());

        // WHEN
        int affectedRow = repository.updateBalance(account.id(), new BigDecimal("15.00"));
        Optional<Account> updatedAccount = repository.findById(account.id());

        // THEN
        assertEquals(1, affectedRow);
        assertTrue(updatedAccount.isPresent());
        assertEquals(new BigDecimal("15.00"), updatedAccount.get().balance());
        assertEquals(Currency.GBP, updatedAccount.get().currency());
    }

    @Test
    void shouldNotAffectRow() {
        // GIVEN
        Account account = new Account(new BigDecimal("12.00"), Currency.GBP);

        // WHEN
        int affectedRow = repository.updateBalance(account.id(), new BigDecimal("15.00"));
        Optional<Account> updatedAccount = repository.findById(account.id());

        // THEN
        assertEquals(0, affectedRow);
        assertFalse(updatedAccount.isPresent());
    }

    @Test
    void shouldNotFindAccount() {
        // GIVEN
        UUID accountId = UUID.randomUUID();
        // WHEN
        Optional<Account> updatedAccount = repository.findById(accountId);

        // THEN
        assertFalse(updatedAccount.isPresent());
    }
}