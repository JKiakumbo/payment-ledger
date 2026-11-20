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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceTest {

    private ConcurrentMap<UUID, Account> accounts;
    private TransferService transferService;
    
    private final UUID accountOneId = UUID.randomUUID();
    private final UUID accountTwoId = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        accounts = new ConcurrentHashMap<>();
        Account accountOne = new Account(accountOneId, BigDecimal.valueOf(100), Currency.GBP);
        Account accountTwo = new Account(accountTwoId, BigDecimal.valueOf(50),Currency.GBP);

        accounts.put(accountOneId, accountOne);
        accounts.put(accountTwoId, accountTwo);

        transferService = new TransferService(accounts);
    }


    @Test
    public void shouldTransferMoneyBetweenAccounts() {

        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = BigDecimal.valueOf(30);


        // WHEN
        transferService.transfer(fromAccountId,toAccountId, amount);

        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);

        // THEN
        assertEquals(BigDecimal.valueOf(70), actualA.balance());
        assertEquals(BigDecimal.valueOf(80), actualB.balance());
        assertEquals(BigDecimal.valueOf(150), actualA.balance().add(actualB.balance()));

    }

    @Test
    public void shouldRejectTransferWhenInsufficientBalance() {
        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = BigDecimal.valueOf(120);

        // WHEN
        assertThrowsExactly(InsufficientBalanceException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));

        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);

        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.balance());
        assertEquals(BigDecimal.valueOf(50), actualB.balance());
        assertEquals(BigDecimal.valueOf(150), actualA.balance().add(actualB.balance()));

    }

    @Test
    public void shouldRejectNegativeTransferAmount() {
        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = BigDecimal.valueOf(-50);

        // WHEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.balance());
        assertEquals(BigDecimal.valueOf(50), actualB.balance());
        assertEquals(BigDecimal.valueOf(150), actualA.balance().add(actualB.balance()));

    }

    @Test
    public void shouldRejectZeroTransferAmount() {
        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = BigDecimal.ZERO;

        // WHEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.balance());
        assertEquals(BigDecimal.valueOf(50), actualB.balance());
        assertEquals(BigDecimal.valueOf(150), actualA.balance().add(actualB.balance()));

    }

    @Test
    public void shouldNotRoundTransferAmount() {
        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = new BigDecimal("1.001");

        // WHEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.balance());
        assertEquals(BigDecimal.valueOf(50), actualB.balance());
        assertEquals(BigDecimal.valueOf(150), actualA.balance().add(actualB.balance()));

    }

    @Test
    public void shouldRejectTransferToAnUnknownDestinationAccount() {
        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(30);

        // WHEN
        assertThrowsExactly(AccountNotFoundException.class, () ->transferService.transfer(fromAccountId, toAccountId, amount));
        Account actual = accounts.get(fromAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actual.balance());

    }

    @Test
    public void shouldRejectTransferToAnUnknownSourceAccount() {
        // GIVEN
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = accountTwoId;
        BigDecimal amount = BigDecimal.valueOf(30);

        // WHEN
        assertThrowsExactly(AccountNotFoundException.class, () ->transferService.transfer(fromAccountId, toAccountId, amount));
        Account actual = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(50), actual.balance());

    }

    @Test
    public void shouldRejectTransferToSameAccount() {
        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountOneId;
        BigDecimal amount = BigDecimal.valueOf(30);

        // WHEN
        assertThrowsExactly(SameAccountTransferException.class, () ->transferService.transfer(fromAccountId, toAccountId, amount));
        Account actual = accounts.get(fromAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actual.balance());

    }

    @Test
    void shouldTransferEntireAvailableBalance() {

        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = BigDecimal.valueOf(100);

        // WHEN
        transferService.transfer(fromAccountId, toAccountId, amount);
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);

        // THEN
        assertEquals(BigDecimal.ZERO, actualA.balance());
        assertEquals(BigDecimal.valueOf(150), actualB.balance());
        assertEquals(BigDecimal.valueOf(150), actualA.balance().add(actualB.balance()));

    }

    @Test
    public void shouldRejectNullTransferAmount() {
        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = null;

        // WHEN
        assertThrowsExactly(InvalidAmountException.class, () -> transferService.transfer(fromAccountId, toAccountId, amount));
        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);
        // THEN
        assertEquals(BigDecimal.valueOf(100), actualA.balance());
        assertEquals(BigDecimal.valueOf(50), actualB.balance());
        assertEquals(BigDecimal.valueOf(150), actualA.balance().add(actualB.balance()));
    }

    @Test
    public void concurrentWithdrawalsShouldExposeRaceCondition() {
        UUID accountThreeId = UUID.randomUUID();
        for (int i = 0; i < 100; i++) {

            // GIVEN
            Account accountOne = new Account(accountOneId, BigDecimal.valueOf(100),Currency.GBP);
            Account accountTwo = new Account(accountTwoId, BigDecimal.ZERO, Currency.GBP);
            Account accountThree = new Account(accountThreeId, BigDecimal.ZERO, Currency.GBP);

            accounts.put(accountOneId, accountOne);
            accounts.put(accountTwoId, accountTwo);
            accounts.put(accountThreeId, accountThree);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            CountDownLatch startGate = new CountDownLatch(1);
            AtomicInteger expectedExceptions = new AtomicInteger();
            AtomicInteger unexpectedExceptions = new AtomicInteger();
            List<Future<Void>> futures = new ArrayList<>(2);
            try {
                Callable<Void> firstTransfer = () -> {
                    startGate.await();
                    transferService.transfer(accountOneId, accountTwoId, BigDecimal.valueOf(80));
                    return null;
                };

                Callable<Void> secondTransfer = () -> {
                    startGate.await();
                    transferService.transfer(accountOneId, accountThreeId, BigDecimal.valueOf(80));
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

            BigDecimal balanceA = accounts.get(accountOneId).balance();
            BigDecimal balanceB = accounts.get(accountTwoId).balance();
            BigDecimal balanceC = accounts.get(accountThreeId).balance();
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
            Account accountOne = new Account(accountOneId,BigDecimal.valueOf(100),Currency.GBP);
            Account accountTwo = new Account(accountTwoId, BigDecimal.valueOf(50),Currency.GBP);

            accounts.put(accountOneId, accountOne);
            accounts.put(accountTwoId, accountTwo);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            CountDownLatch startGate = new CountDownLatch(1);
            AtomicInteger expectedExceptions = new AtomicInteger();
            AtomicInteger unexpectedExceptions = new AtomicInteger();
            List<Future<Void>> futures = new ArrayList<>(2);
            try {
                Callable<Void> firstTransfer = () -> {
                    startGate.await();
                    transferService.transfer(accountOneId, accountTwoId, BigDecimal.valueOf(30));
                    return null;
                };

                Callable<Void> secondTransfer = () -> {
                    startGate.await();
                    transferService.transfer(accountTwoId, accountOneId, BigDecimal.valueOf(20));
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

            BigDecimal balanceA = accounts.get(accountOneId).balance();
            BigDecimal balanceB = accounts.get(accountTwoId).balance();
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
        Account accountOne = new Account(accountOneId, BigDecimal.valueOf(100), Currency.GBP);

        Account accountTwo = new Account(accountTwoId, BigDecimal.ZERO, Currency.GBP);

        accounts.put(accountOneId, accountOne);
        accounts.put(accountTwoId, accountTwo);

        try {
            for (int i = 0; i < requestCount; i++) {

                Future<Void> future = executor.submit(() -> {
                    transferService.transfer(accountOneId, accountTwoId, BigDecimal.ONE);
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

        BigDecimal balanceA = accounts.get(accountOneId).balance();
        BigDecimal balanceB = accounts.get(accountTwoId).balance();

        assertEquals(BigDecimal.ZERO, balanceA);
        assertEquals(BigDecimal.valueOf(100), balanceB);
    }

}