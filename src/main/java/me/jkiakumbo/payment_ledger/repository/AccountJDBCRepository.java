package me.jkiakumbo.payment_ledger.repository;

import me.jkiakumbo.payment_ledger.domain.Account;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountJDBCRepository implements AccountRepository {
    private final JdbcClient jdbcClient;

    public AccountJDBCRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public int save(Account account) {
        return jdbcClient.sql("INSERT INTO accounts (id, balance, currency, created_at) VALUES (:id, :balance, :currency, :createdAt)")
                .param("id", account.id())
                .param("balance", account.balance())
                .param("currency", account.currency().name())
                .param("createdAt", account.createdAt())
                .update();
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return jdbcClient.sql("SELECT id, balance, currency, created_at FROM accounts WHERE id = :id")
                .param("id", accountId)
                .query(Account.class)
                .optional();
    }

    @Override
    public int updateBalance(UUID accountId, BigDecimal balance) {
        return jdbcClient.sql("UPDATE accounts SET balance = :balance WHERE id = :id")
                .param("balance", balance)
                .param("id", accountId)
                .update();
    }
}
