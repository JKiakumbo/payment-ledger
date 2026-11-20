CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    currency VARCHAR(3) NOT NULL ,
    balance NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ( balance >= 0 )
);

CREATE TABLE transfers (
    id UUID PRIMARY KEY,
    source_account_id UUID NOT NULL,
    destination_account_id UUID NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_transfer_source_account
        FOREIGN KEY (source_account_id)
        REFERENCES accounts(id),
    CONSTRAINT FK_transfer_destination_account
        FOREIGN KEY (destination_account_id)
        REFERENCES accounts(id),
    CHECK ( amount > 0 ),
    CHECK ( source_account_id <> destination_account_id )
);

