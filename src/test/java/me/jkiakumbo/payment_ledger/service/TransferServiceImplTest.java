package me.jkiakumbo.payment_ledger.service;

import me.jkiakumbo.payment_ledger.exceptions.*;
import me.jkiakumbo.payment_ledger.command.TransferCommand;
import me.jkiakumbo.payment_ledger.domain.Account;
import me.jkiakumbo.payment_ledger.domain.Currency;
import me.jkiakumbo.payment_ledger.repository.Accounts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransferServiceImplTest {

    private Accounts accounts;
    private  TransferService transferService;


    @BeforeEach
    public void setup() {
        accounts = new Accounts();
        transferService = new TransferServiceImpl(accounts);
    }

    @Test
    public void shouldTransferMoney() {

        // GIVEN
        Account accountA = Account.builder()
                .id(1)
                .balance(10000)
                .currency(Currency.GBP)
                .build();

        accounts.create(accountA);

        Account accountB = Account.builder()
                .id(2)
                .balance(5000)
                .currency(Currency.GBP)
                .build();

        accounts.create(accountB);

        TransferCommand command = new TransferCommand(1, 2, 3000);

        // WHEN
        transferService.transfer(command);
        Account resultA = accounts.getById(1).orElseThrow();
        Account resultB = accounts.getById(2).orElseThrow();

        // THEN
        assertEquals(7000, resultA.getBalance());
        assertEquals(8000, resultB.getBalance());

    }

    @Test
    public void shouldRejectTransferWhenInsufficientFunds() {

        // GIVEN
        Account accountA = Account.builder()
                .id(1)
                .balance(10000)
                .currency(Currency.GBP)
                .build();

        accounts.create(accountA);

        Account accountB = Account.builder()
                .id(2)
                .balance(5000)
                .currency(Currency.GBP)
                .build();

        accounts.create(accountB);

        TransferCommand command = new TransferCommand(1, 2, 15000);

        // WHEN
        assertThrowsExactly(InsufficientAmountException.class, () -> transferService.transfer(command));
        Account resultA = accounts.getById(1).orElseThrow();
        Account resultB = accounts.getById(2).orElseThrow();

        // THEN
        assertEquals(10000, resultA.getBalance());
        assertEquals(5000, resultB.getBalance());

    }

    @Test
    public void shouldNotAcceptZeroAmount() {
        // WHEN
        long amount = 0;
        TransferCommand command = new TransferCommand(1, 2, amount);

        // THEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(command));
    }

    @Test
    public void shouldNotAcceptNegativeAmount() {
        // WHEN
        long amount = -50;
        TransferCommand command = new TransferCommand(1, 2, amount);

        // THEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(command));
    }

    @Test
    public void shouldRejectTransferForNoExistingSource() {

        // GIVEN
        Account accountB = Account.builder()
                .id(2)
                .balance(5000)
                .currency(Currency.GBP)
                .build();

        accounts.create(accountB);

        TransferCommand command = new TransferCommand(1, 2, 15000);

        // WHEN
        assertThrowsExactly(SourceAccountNotFoundException.class, () -> transferService.transfer(command));
        Account resultB = accounts.getById(2).orElseThrow();

        // THEN
        assertEquals(5000, resultB.getBalance());

    }

    @Test
    public void shouldRejectTransferForNoExistingDestination() {

        // GIVEN
        Account accountA = Account.builder()
                .id(1)
                .balance(5000)
                .currency(Currency.GBP)
                .build();

        accounts.create(accountA);

        TransferCommand command = new TransferCommand(1, 2, 15000);

        // WHEN
        assertThrowsExactly(DestinationAccountNotFoundException.class, () -> transferService.transfer(command));
        Account resultA = accounts.getById(1).orElseThrow();

        // THEN
        assertEquals(5000, resultA.getBalance());

    }

    @Test
    public void shouldRejectTransferToSameAccount() {
        // WHEN
        long amount = 50;
        TransferCommand command = new TransferCommand(1, 1, amount);

        // THEN
        assertThrowsExactly(TransferToSameAccountException.class, () -> transferService.transfer(command));
    }


    @Test
    public void shouldPreserveBalancesWhenDestinationOverflows() {

        // GIVEN
        Account accountA = Account.builder()
                .id(1)
                .balance(10000)
                .currency(Currency.GBP)
                .build();

        accounts.create(accountA);

        Account accountB = Account.builder()
                .id(2)
                .balance(Long.MAX_VALUE - 10)
                .currency(Currency.GBP)
                .build();

        accounts.create(accountB);

        TransferCommand command = new TransferCommand(1, 2, 3000);

        // WHEN
        assertThrowsExactly(TransferNotCompletedException.class, () -> transferService.transfer(command));
        Account resultA = accounts.getById(1).orElseThrow();
        Account resultB = accounts.getById(2).orElseThrow();

        // THEN
        assertEquals(10000, resultA.getBalance());
        assertEquals(Long.MAX_VALUE - 10, resultB.getBalance());

    }

    @Test
    public void shouldRejectTransferBetweenDifferentCurrencies() {

        // GIVEN
        Account accountA = Account.builder()
                .id(1)
                .balance(10000)
                .currency(Currency.GBP)
                .build();

        accounts.create(accountA);

        Account accountB = Account.builder()
                .id(2)
                .balance(5000)
                .currency(Currency.EUR)
                .build();

        accounts.create(accountB);

        TransferCommand command = new TransferCommand(1, 2, 3000);

        // WHEN
        assertThrowsExactly(UnsupportedCurrencyException.class, () -> transferService.transfer(command));
        Account resultA = accounts.getById(1).orElseThrow();
        Account resultB = accounts.getById(2).orElseThrow();

        // THEN
        assertEquals(10000, resultA.getBalance());
        assertEquals(5000, resultB.getBalance());

    }

    @Test
    public void shouldRejectTransfersInUnsupportedCurrency() {

        // GIVEN
        Account accountA = Account.builder()
                .id(1)
                .balance(10000)
                .currency(Currency.EUR)
                .build();

        accounts.create(accountA);

        Account accountB = Account.builder()
                .id(2)
                .balance(5000)
                .currency(Currency.EUR)
                .build();

        accounts.create(accountB);

        TransferCommand command = new TransferCommand(1, 2, 3000);

        // WHEN
        assertThrowsExactly(UnsupportedCurrencyException.class, () -> transferService.transfer(command));
        Account resultA = accounts.getById(1).orElseThrow();
        Account resultB = accounts.getById(2).orElseThrow();

        // THEN
        assertEquals(10000, resultA.getBalance());
        assertEquals(5000, resultB.getBalance());

    }

}
