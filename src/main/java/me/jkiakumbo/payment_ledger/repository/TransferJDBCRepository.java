package me.jkiakumbo.payment_ledger.repository;

import me.jkiakumbo.payment_ledger.domain.Transfer;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TransferJDBCRepository implements TransferRepository {
    private final JdbcClient jdbcClient;

    public TransferJDBCRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public int save(Transfer transfer) {
        return jdbcClient.sql("""
            INSERT INTO transfers (id, source_account_id, destination_account_id, amount, currency, created_at)
            VALUES (:id, :sourceId, :destinationId, :amount, :currency, :createdAt)""")
                .param("id", transfer.id())
                .param("sourceId", transfer.sourceAccountId())
                .param("destinationId", transfer.destinationAccountId())
                .param("amount", transfer.amount())
                .param("currency", transfer.currency().name())
                .param("createdAt", transfer.createdAt())
                .update();
    }

    @Override
    public Optional<Transfer> findById(UUID transferId) {
        return jdbcClient.sql("""
            SELECT id, source_account_id, destination_account_id, amount, currency, created_at 
            FROM transfers 
            WHERE id = :id""")
                .param("id", transferId)
                .query(Transfer.class)
                .optional();
    }
}
