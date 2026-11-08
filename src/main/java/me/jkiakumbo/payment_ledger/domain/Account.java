package me.jkiakumbo.payment_ledger.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account {
    long id;
    long balance;
    Currency currency;

    public Account (long id, long balance, Currency currency) {
        this.id = id;
        this.balance = balance;
        this.currency = currency;
    }

}
