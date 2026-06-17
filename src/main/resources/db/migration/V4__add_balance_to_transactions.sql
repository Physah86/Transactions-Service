ALTER TABLE transactions
    ADD COLUMN balance DECIMAL(19, 2) NOT NULL DEFAULT 0.0;

UPDATE transactions SET balance = amount;