package me.jkiakumbo.payment_ledger.repository;

import me.jkiakumbo.payment_ledger.domain.Account;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class Accounts {
    private final Map<Long, Account> accounts;

    public Accounts() {
        this.accounts = new HashMap<>();
    }

    public Optional<Account> getById(long id) {
        Account account = accounts.get(id);

        if (account != null) {
            return Optional.of(account);
        }

        return Optional.empty();
    }

    public Account create(Account account) {
        return accounts.put(account.getId(), account);
    }

    public void update(Account account) {
        accounts.replace(account.getId(), account);
    }


}
