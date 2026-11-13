package me.jkiakumbo.payment_ledger.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    long balance;
    Currency currency;

    public Account(long balance, Currency currency) {
        this.balance = balance;
        this.currency = currency;
    }
}
