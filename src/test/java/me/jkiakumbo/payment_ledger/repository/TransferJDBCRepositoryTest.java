package me.jkiakumbo.payment_ledger.repository;

import me.jkiakumbo.payment_ledger.config.PostgresTestConfig;
import me.jkiakumbo.payment_ledger.domain.Account;
import me.jkiakumbo.payment_ledger.domain.Currency;
import me.jkiakumbo.payment_ledger.domain.Transfer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Import(PostgresTestConfig.class)
class TransferJDBCRepositoryTest {

    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private AccountRepository accountRepository;

    private final UUID sourceId = UUID.randomUUID();
    private final UUID destinationId = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        accountRepository.save(new Account(sourceId, new BigDecimal("50.00"), Currency.GBP));
        accountRepository.save(new Account(destinationId, new BigDecimal("20.00"), Currency.GBP));
    }


    @Test
    void shouldSaveAndFindTransfer() {
        // GIVE
        Transfer transfer = new Transfer(sourceId, destinationId, new BigDecimal("20.50"));

        // WHEN
        int affectedRow = transferRepository.save(transfer);
        Optional<Transfer> transferOptional = transferRepository.findById(transfer.id());

        // THEN
        assertEquals(1, affectedRow);
        assertTrue(transferOptional.isPresent());
        assertEquals(new BigDecimal("20.50"), transferOptional.get().amount());
        assertEquals(transfer.sourceAccountId(), transferOptional.get().sourceAccountId());
        assertEquals(transfer.destinationAccountId(), transferOptional.get().destinationAccountId());
    }

    @Test
    void shouldNotFindTransfer() {
        // WHEN
        Optional<Transfer> transferOptional = transferRepository.findById(UUID.randomUUID());

        // THEN
        assertFalse(transferOptional.isPresent());
    }

    @Test
    void shouldRejectTransferForNoExistingSource() {
        // GIVE
        Transfer transfer = new Transfer(UUID.randomUUID(), destinationId, new BigDecimal("20.50"));

        // WHEN
        assertThrowsExactly(DataIntegrityViolationException.class, () -> transferRepository.save(transfer));
        Optional<Transfer> transferOptional = transferRepository.findById(transfer.id());

        // THEN
        assertFalse(transferOptional.isPresent());
    }

    @Test
    void shouldRejectTransferForNoExistingDestination() {
        // GIVE
        Transfer transfer = new Transfer(sourceId, UUID.randomUUID(), new BigDecimal("20.50"));

        // WHEN
        assertThrowsExactly(DataIntegrityViolationException.class, () -> transferRepository.save(transfer));
        Optional<Transfer> transferOptional = transferRepository.findById(transfer.id());

        // THEN
        assertFalse(transferOptional.isPresent());
    }

}