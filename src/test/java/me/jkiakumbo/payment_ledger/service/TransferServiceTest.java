package me.jkiakumbo.payment_ledger.service;

import me.jkiakumbo.payment_ledger.domain.Account;
import me.jkiakumbo.payment_ledger.domain.Currency;
import me.jkiakumbo.payment_ledger.exception.AccountNotFoundException;
import me.jkiakumbo.payment_ledger.exception.InsufficientBalanceException;
import me.jkiakumbo.payment_ledger.exception.InvalidAmountException;
import me.jkiakumbo.payment_ledger.exception.SameAccountTransferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceTest {

    private Map<Long, Account> accounts;
    private TransferService transferService;

    @BeforeEach
    public void setup() {
        accounts = new HashMap<>();
        Account accountA = Account.builder()
                .id(1)
                .balance(BigDecimal.valueOf(100))
                .currency(Currency.GBP)
                .build();

        Account accountB = Account.builder()
                .id(2)
                .balance(BigDecimal.valueOf(50))
                .currency(Currency.GBP)
                .build();

        accounts.put(1L, accountA);
        accounts.put(2L, accountB);

        transferService = new TransferService(accounts);
    }


    @Test
    public void shouldTransferMoneyBetweenAccounts() {

        // GIVEN
        long fromAccountId = 1;
        long toAccountId = 2;
        BigDecimal amount = BigDecimal.valueOf(30);


        // WHEN
        transferService.transfer(fromAccountId,toAccountId, amount);

        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);

        // THEN
        assertEquals(BigDecimal.valueOf(70), actualA.getBalance());
        assertEquals(BigDecimal.valueOf(80), actualB.getBalance());
        assertEquals(BigDecimal.valueOf(150), actualA.getBalance().add(actualB.getBalance()));

    }

    @Test
    public void shouldRejectTransferWhenInsufficientBalance() {
        // GIVEN
        long fromAccountId = 1;
        long toAccountId = 2;
        BigDecimal amount = BigDecimal.valueOf(120);

        // WHEN
        assertThrowsExactly(InsufficientBalanceException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));

        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);

        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.getBalance());
        assertEquals(BigDecimal.valueOf(50), actualB.getBalance());
        assertEquals(BigDecimal.valueOf(150), actualA.getBalance().add(actualB.getBalance()));

    }

    @Test
    public void shouldRejectNegativeTransferAmount() {
        // GIVEN
        long fromAccountId = 1;
        long toAccountId = 2;
        BigDecimal amount = BigDecimal.valueOf(-50);

        // WHEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.getBalance());
        assertEquals(BigDecimal.valueOf(50), actualB.getBalance());
        assertEquals(BigDecimal.valueOf(150), actualA.getBalance().add(actualB.getBalance()));

    }

    @Test
    public void shouldRejectZeroTransferAmount() {
        // GIVEN
        long fromAccountId = 1;
        long toAccountId = 2;
        BigDecimal amount = BigDecimal.ZERO;

        // WHEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.getBalance());
        assertEquals(BigDecimal.valueOf(50), actualB.getBalance());
        assertEquals(BigDecimal.valueOf(150), actualA.getBalance().add(actualB.getBalance()));

    }

    @Test
    public void shouldNotRoundTransferAmount() {
        // GIVEN
        long fromAccountId = 1;
        long toAccountId = 2;
        BigDecimal amount = new BigDecimal("1.001");

        // WHEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.getBalance());
        assertEquals(BigDecimal.valueOf(50), actualB.getBalance());
        assertEquals(BigDecimal.valueOf(150), actualA.getBalance().add(actualB.getBalance()));

    }

    @Test
    public void shouldRejectTransferToAnUnknownDestinationAccount() {
        // GIVEN
        long fromAccountId = 1;
        long toAccountId = 3;
        BigDecimal amount = BigDecimal.valueOf(30);

        // WHEN
        assertThrowsExactly(AccountNotFoundException.class, () ->transferService.transfer(fromAccountId, toAccountId, amount));
        Account actual = accounts.get(fromAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actual.getBalance());

    }

    @Test
    public void shouldRejectTransferToAnUnknownSourceAccount() {
        // GIVEN
        long fromAccountId = 3;
        long toAccountId = 2;
        BigDecimal amount = BigDecimal.valueOf(30);

        // WHEN
        assertThrowsExactly(AccountNotFoundException.class, () ->transferService.transfer(fromAccountId, toAccountId, amount));
        Account actual = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(50), actual.getBalance());

    }

    @Test
    public void shouldRejectTransferToSameAccount() {
        // GIVEN
        long fromAccountId = 1;
        long toAccountId = 1;
        BigDecimal amount = BigDecimal.valueOf(30);

        // WHEN
        assertThrowsExactly(SameAccountTransferException.class, () ->transferService.transfer(fromAccountId, toAccountId, amount));
        Account actual = accounts.get(fromAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actual.getBalance());

    }

    @Test
    void shouldTransferEntireAvailableBalance() {

        // GIVEN
        long fromAccountId = 1;
        long toAccountId = 2;
        BigDecimal amount = BigDecimal.valueOf(100);

        // WHEN
        transferService.transfer(fromAccountId, toAccountId, amount);
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);

        // THEN
        assertEquals(BigDecimal.ZERO, actualA.getBalance());
        assertEquals(BigDecimal.valueOf(150), actualB.getBalance());
        assertEquals(BigDecimal.valueOf(150), actualA.getBalance().add(actualB.getBalance()));

    }

    @Test
    public void shouldRejectNullTransferAmount() {
        // GIVEN
        long fromAccountId = 1;
        long toAccountId = 2;
        BigDecimal amount = null;

        // WHEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.getBalance());
        assertEquals(BigDecimal.valueOf(50), actualB.getBalance());
        assertEquals(BigDecimal.valueOf(150), actualA.getBalance().add(actualB.getBalance()));
    }

}