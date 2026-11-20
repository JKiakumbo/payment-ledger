package me.jkiakumbo.payment_ledger.service;

import me.jkiakumbo.payment_ledger.domain.Account;
import me.jkiakumbo.payment_ledger.domain.Currency;
import me.jkiakumbo.payment_ledger.exception.InsufficientBalanceException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AsyncTransferServiceTest {
    private ConcurrentMap<UUID, Account> accounts;
    private AsyncTransferService asyncTransferService;
    private ExecutorService executor;
    private final UUID accountOneId = UUID.randomUUID();
    private final UUID accountTwoId = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        accounts = new ConcurrentHashMap<>();
        executor = Executors.newFixedThreadPool(5);
        Account accountOne = new Account(accountOneId, BigDecimal.valueOf(100), Currency.GBP);
        Account accountTwo = new Account(accountTwoId, BigDecimal.valueOf(50), Currency.GBP);

        accounts.put(accountOneId, accountOne);
        accounts.put(accountTwoId, accountTwo);

        asyncTransferService = new AsyncTransferService(new TransferService(accounts), executor);
    }


    @Test
    public void shouldTransferMoneyBetweenAccounts() {

        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = BigDecimal.valueOf(30);


        // WHEN
        try {
            asyncTransferService.transferAsync(fromAccountId, toAccountId, amount).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        Account actualAccountOne = accounts.get(fromAccountId);
        Account actualAccountTwo = accounts.get(toAccountId);

        // THEN
        assertEquals(BigDecimal.valueOf(70), actualAccountOne.balance());
        assertEquals(BigDecimal.valueOf(80), actualAccountTwo.balance());
        assertEquals(BigDecimal.valueOf(150), actualAccountOne.balance().add(actualAccountTwo.balance()));

    }

    @Test
    public void shouldRejectTransferWhenInsufficientBalance() {
        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = BigDecimal.valueOf(120);

        // WHEN


        ExecutionException exception = assertThrows(ExecutionException.class, () -> asyncTransferService
                .transferAsync(fromAccountId, toAccountId, amount)
                .get(5, TimeUnit.SECONDS));

        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);

        // THEN
        assertInstanceOf(InsufficientBalanceException.class, exception.getCause());
        assertEquals(BigDecimal.valueOf(100), actualA.balance());
        assertEquals(BigDecimal.valueOf(50), actualB.balance());
        assertEquals(BigDecimal.valueOf(150), actualA.balance().add(actualB.balance()));

    }

    @Test
    void shouldHandle100ConcurrentTransfers() {

        int requestCount = 100;

        List<Future<Void>> futures = new ArrayList<>();

        // GIVEN
        Account accountOne = new Account(accountOneId, BigDecimal.valueOf(100), Currency.GBP);
        Account accountTwo = new Account(accountTwoId, BigDecimal.ZERO, Currency.GBP);

        accounts.put(accountOneId, accountOne);
        accounts.put(accountTwoId, accountTwo);

        try {
            for (int i = 0; i < requestCount; i++) {

                CompletableFuture<Void> future = asyncTransferService.transferAsync(accountOneId, accountTwoId, BigDecimal.ONE);

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

    @Test
    public void concurrentWithdrawalsShouldSucceed() {
        UUID accountThreeId = UUID.randomUUID();
        for (int i = 0; i < 100; i++) {

            // GIVEN
            Account accountOne = new Account(accountOneId, BigDecimal.valueOf(100), Currency.GBP);
            Account accountTwo = new Account(accountTwoId, BigDecimal.ZERO, Currency.GBP);
            Account accountThree = new Account(accountThreeId, BigDecimal.ZERO, Currency.GBP);

            accounts.put(accountOneId, accountOne);
            accounts.put(accountTwoId, accountTwo);
            accounts.put(accountThreeId, accountThree);

            AtomicInteger expectedExceptions = new AtomicInteger();
            AtomicInteger unexpectedExceptions = new AtomicInteger();
            List<Future<Void>> futures = new ArrayList<>(2);
            try {
                //WHEN
                Future<Void> first = asyncTransferService.transferAsync(accountOneId, accountTwoId, BigDecimal.valueOf(80));
                Future<Void> second = asyncTransferService.transferAsync(accountOneId, accountThreeId, BigDecimal.valueOf(80));

                futures.addAll(List.of(first, second));

                for (Future<Void> future : futures) {
                    try {
                        future.get(5, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        Throwable throwable = e.getCause();
                        if (throwable instanceof InsufficientBalanceException) {
                            expectedExceptions.incrementAndGet();
                        } else {
                            unexpectedExceptions.incrementAndGet();
                        }
                    } catch (InterruptedException | TimeoutException e) {
                        unexpectedExceptions.incrementAndGet();
                    }
                }


            } finally {
                futures.stream()
                        .filter(f -> !f.isDone())
                        .forEach(f -> f.cancel(true));
            }

            // THEN
            BigDecimal balanceA = accounts.get(accountOneId).balance();
            BigDecimal balanceB = accounts.get(accountTwoId).balance();
            BigDecimal balanceC = accounts.get(accountThreeId).balance();
            BigDecimal totalBalance = balanceA.add(balanceB).add(balanceC);
            boolean validRecipients = (balanceB.compareTo(BigDecimal.valueOf(80)) == 0 && balanceC.signum() == 0)
                    || (balanceC.compareTo(BigDecimal.valueOf(80)) == 0 && balanceB.signum() == 0);


            assertEquals(BigDecimal.valueOf(20), balanceA);
            assertTrue(validRecipients);
            assertEquals(BigDecimal.valueOf(100), totalBalance);
            assertEquals(1, expectedExceptions.get());
            assertEquals(0, unexpectedExceptions.get());

        }
    }

    @Test
    void concurrentTransfersMustNotOverspend() {

        // GIVEN
        Account accountOne = new Account(accountOneId, BigDecimal.valueOf(100), Currency.GBP);
        Account accountTwo = new Account(accountTwoId, BigDecimal.valueOf(50), Currency.GBP);

        accounts.put(accountOneId, accountOne);
        accounts.put(accountTwoId, accountTwo);

        AtomicInteger unexpectedExceptions = new AtomicInteger();
        List<Future<Void>> futures = new ArrayList<>(2);

        //WHEN
        try {
            Future<Void> first = asyncTransferService.transferAsync(accountOneId, accountTwoId, BigDecimal.valueOf(30));
            Future<Void> second = asyncTransferService.transferAsync(accountTwoId, accountOneId, BigDecimal.valueOf(20));
            futures.addAll(List.of(first, second));

            for (Future<Void> future : futures) {
                try {
                    future.get(5, TimeUnit.SECONDS);
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    unexpectedExceptions.incrementAndGet();
                }
            }

        } finally {
            futures.stream()
                    .filter(f -> !f.isDone())
                    .forEach(f -> f.cancel(true));
            executor.shutdownNow();
        }

        // THEN

        BigDecimal balanceA = accounts.get(accountOneId).balance();
        BigDecimal balanceB = accounts.get(accountTwoId).balance();
        BigDecimal totalBalance = balanceA.add(balanceB);

        assertEquals(BigDecimal.valueOf(90), balanceA);
        assertEquals(BigDecimal.valueOf(60), balanceB);
        assertEquals(BigDecimal.valueOf(150), totalBalance);
        assertEquals(0, unexpectedExceptions.get());

    }

    @Test
    public void shouldTransferMoneyBetweenAccountsAsynchronously() {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Thread> executingThread = new AtomicReference<>();
        AsyncTransferService service = getService(executingThread, started, release);

        // GIVEN
        UUID fromAccountId = accountOneId;
        UUID toAccountId = accountTwoId;
        BigDecimal amount = BigDecimal.valueOf(30);

        // WHEN
        Future<Void> future = service.transferAsync(fromAccountId, toAccountId, amount);

        // THEN
        try {
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertFalse(future.isDone());
            assertNotEquals(Thread.currentThread(), executingThread.get());
            release.countDown();
            future.get(5, TimeUnit.SECONDS);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            release.countDown();
        }

        Account actualA = accounts.get(fromAccountId);
        Account actualB = accounts.get(toAccountId);
        assertEquals(BigDecimal.valueOf(70), actualA.balance());
        assertEquals(BigDecimal.valueOf(80), actualB.balance());
        assertEquals(BigDecimal.valueOf(150), actualA.balance().add(actualB.balance()));


    }
    @Test
    public void shouldRejectSubmissionsAfterExecutorShutdown() {
        // WHEN
        executor.shutdownNow();
        assertThrowsExactly(RejectedExecutionException.class, () ->  asyncTransferService.transferAsync(accountOneId, accountTwoId, BigDecimal.ONE));

        BigDecimal balanceA = accounts.get(accountOneId).balance();
        BigDecimal balanceB = accounts.get(accountTwoId).balance();
        BigDecimal totalBalance = balanceA.add(balanceB);

        // THEN
        assertEquals(BigDecimal.valueOf(100), balanceA);
        assertEquals(BigDecimal.valueOf(50), balanceB);
        assertEquals(BigDecimal.valueOf(150), totalBalance);
    }

    private @NonNull AsyncTransferService getService(AtomicReference<Thread> executingThread, CountDownLatch started, CountDownLatch release) {
        TransferService controlledService = new TransferService(accounts) {

            @Override
            public synchronized void transfer(
                    UUID fromId, UUID toId, BigDecimal amount) {
                executingThread.set(Thread.currentThread());
                started.countDown();

                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                super.transfer(fromId, toId, amount);
            }
        };

        return new AsyncTransferService(controlledService, executor);
    }

    @AfterEach
    public void cleanup() {
        executor.shutdownNow();
    }

}