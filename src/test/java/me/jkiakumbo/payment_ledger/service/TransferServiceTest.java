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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceTest {

    private ConcurrentMap<Long, Account> accounts;
    private TransferService transferService;

    @BeforeEach
    public void setup() {
        accounts = new ConcurrentHashMap<>();
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

    @Test
    public void concurrentWithdrawalsShouldExposeRaceCondition() {
        for (int i = 0; i < 100; i++) {

            // GIVEN
            Account accountA = Account.builder()
                    .id(1)
                    .balance(BigDecimal.valueOf(100))
                    .currency(Currency.GBP)
                    .build();

            Account accountB = Account.builder()
                    .id(2)
                    .balance(BigDecimal.ZERO)
                    .currency(Currency.GBP)
                    .build();

            Account accountC = Account.builder()
                    .id(3)
                    .balance(BigDecimal.ZERO)
                    .currency(Currency.GBP)
                    .build();

            accounts.put(1L, accountA);
            accounts.put(2L, accountB);
            accounts.put(3L, accountC);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            CountDownLatch startGate = new CountDownLatch(1);
            AtomicInteger expectedExceptions = new AtomicInteger();
            AtomicInteger unexpectedExceptions = new AtomicInteger();
            List<Future<Void>> futures = new ArrayList<>(2);
            try {
                Callable<Void> firstTransfer = () -> {
                    startGate.await();
                    transferService.transfer(1L, 2L, BigDecimal.valueOf(80));
                    return null;
                };

                Callable<Void> secondTransfer = () -> {
                    startGate.await();
                    transferService.transfer(1L, 3L, BigDecimal.valueOf(80));
                    return null;
                };


                // WHEN
                Future<Void> first = executor.submit(firstTransfer);
                Future<Void> second = executor.submit(secondTransfer);
                futures.addAll(List.of(first, second));

                startGate.countDown();

                for (Future<Void> future : futures) {
                    try {
                        future.get(5, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        Throwable throwable = e.getCause();
                        if (throwable instanceof InsufficientBalanceException) {
                            expectedExceptions.incrementAndGet();
                        }
                        else {
                            unexpectedExceptions.incrementAndGet();
                        }
                    } catch (InterruptedException | TimeoutException e) {
                        unexpectedExceptions.incrementAndGet();
                    }
                }


            } finally {
                startGate.countDown();
                futures.stream()
                        .filter(f -> !f.isDone())
                        .forEach(f -> f.cancel(true));
                executor.shutdownNow();
            }

            // THEN

            BigDecimal balanceA = accounts.get(1L).getBalance();
            BigDecimal balanceB = accounts.get(2L).getBalance();
            BigDecimal balanceC = accounts.get(3L).getBalance();
            BigDecimal totalBalance = balanceA.add(balanceB).add(balanceC);
            BigDecimal expectedTotal = BigDecimal.valueOf(100);

            boolean fundsConserved = totalBalance.compareTo(expectedTotal) == 0;
            boolean sourceNonNegative = balanceA.signum() >= 0;
            boolean overspendingDetected = !fundsConserved || !sourceNonNegative;
            boolean validRecipients = (balanceB.compareTo(BigDecimal.valueOf(80)) == 0 && balanceC.signum() == 0)
                            || (balanceC.compareTo(BigDecimal.valueOf(80)) == 0 && balanceB.signum() == 0);


            System.out.printf(
                    "A=%s, B=%s, C=%s, total=%s, expected rejection=%d, unexpected rejection=%d, violation=%s%n",
                    balanceA,
                    balanceB,
                    balanceC,
                    totalBalance,
                    expectedExceptions.get(),
                    unexpectedExceptions.get(),
                    overspendingDetected
            );


            assertEquals(BigDecimal.valueOf(20), balanceA);
            assertTrue(validRecipients);
            assertEquals(BigDecimal.valueOf(100), totalBalance);
            assertEquals(1, expectedExceptions.get());
            assertEquals(0, unexpectedExceptions.get());

        }
    }

    @Test
    void concurrentTransfersMustNotOverspend() {
        for (int i = 0; i < 100; i++) {

            // GIVEN
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

            ExecutorService executor = Executors.newFixedThreadPool(2);

            CountDownLatch startGate = new CountDownLatch(1);
            AtomicInteger expectedExceptions = new AtomicInteger();
            AtomicInteger unexpectedExceptions = new AtomicInteger();
            List<Future<Void>> futures = new ArrayList<>(2);
            try {
                Callable<Void> firstTransfer = () -> {
                    startGate.await();
                    transferService.transfer(1L, 2L, BigDecimal.valueOf(30));
                    return null;
                };

                Callable<Void> secondTransfer = () -> {
                    startGate.await();
                    transferService.transfer(2L, 1L, BigDecimal.valueOf(20));
                    return null;
                };


                // WHEN
                Future<Void> first = executor.submit(firstTransfer);
                Future<Void> second = executor.submit(secondTransfer);
                futures.addAll(List.of(first, second));

                startGate.countDown();

                for (Future<Void> future : futures) {
                    try {
                        future.get(5, TimeUnit.SECONDS);
                    } catch (ExecutionException | InterruptedException | TimeoutException e) {
                            unexpectedExceptions.incrementAndGet();

                    }
                }

            } finally {
                startGate.countDown();
                futures.stream()
                        .filter(f -> !f.isDone())
                        .forEach(f -> f.cancel(true));
                executor.shutdownNow();
            }

            // THEN

            BigDecimal balanceA = accounts.get(1L).getBalance();
            BigDecimal balanceB = accounts.get(2L).getBalance();
            BigDecimal totalBalance = balanceA.add(balanceB);
            BigDecimal expectedTotal = BigDecimal.valueOf(150);

            boolean fundsConserved = totalBalance.compareTo(expectedTotal) == 0;
            boolean sourceNonNegative = balanceA.signum() >= 0;
            boolean overspendingDetected = !fundsConserved || !sourceNonNegative;

            System.out.printf(
                    "A=%s, B=%s, total=%s, expected rejection=%d, unexpected rejection=%d, violation=%s%n",
                    balanceA,
                    balanceB,
                    totalBalance,
                    expectedExceptions.get(),
                    unexpectedExceptions.get(),
                    overspendingDetected
            );


            assertEquals(BigDecimal.valueOf(90), balanceA);
            assertEquals(BigDecimal.valueOf(60), balanceB);
            assertEquals(BigDecimal.valueOf(150), totalBalance);
            assertEquals(0, unexpectedExceptions.get());

        }
    }

    @Test
    void shouldHandle100ConcurrentTransfers() throws Exception {

        int requestCount = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(10);

        List<Future<Void>> futures = new ArrayList<>();

        // GIVEN
        Account accountA = Account.builder()
                .id(1)
                .balance(BigDecimal.valueOf(100))
                .currency(Currency.GBP)
                .build();

        Account accountB = Account.builder()
                .id(2)
                .balance(BigDecimal.ZERO)
                .currency(Currency.GBP)
                .build();

        accounts.put(1L, accountA);
        accounts.put(2L, accountB);

        try {
            for (int i = 0; i < requestCount; i++) {

                Future<Void> future = executor.submit(() -> {
                    transferService.transfer(1L, 2L, BigDecimal.ONE);
                    return null;
                });

                futures.add(future);
            }

            futures.forEach(f -> {
                try {
                    f.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
            });

        } finally {
            futures.stream()
                    .filter(f -> !f.isDone())
                    .forEach(f -> f.cancel(true));
            executor.shutdownNow();
        }

        BigDecimal balanceA = accounts.get(1L).getBalance();
        BigDecimal balanceB = accounts.get(2L).getBalance();

        assertEquals(BigDecimal.ZERO, balanceA);
        assertEquals(BigDecimal.valueOf(100), balanceB);
    }

}