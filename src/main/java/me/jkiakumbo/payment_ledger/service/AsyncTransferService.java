package me.jkiakumbo.payment_ledger.service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class AsyncTransferService {

    private final ExecutorService executor;
    private final TransferService transferService;

    public AsyncTransferService(TransferService transferService, ExecutorService executor) {
        this.executor = executor;
        this.transferService = transferService;
    }

    public CompletableFuture<Void> transferAsync(long fromAccountId, long toAccountId, BigDecimal amount) {
        Supplier<Void> transfer = () -> {
            transferService.transfer(fromAccountId, toAccountId, amount);
            return null;
        };
        return CompletableFuture.supplyAsync(transfer, executor);
    }
}
