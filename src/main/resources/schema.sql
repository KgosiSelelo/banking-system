CREATE TABLE IF NOT EXISTS customer (
                                        customer_id   VARCHAR(10)  PRIMARY KEY,
    first_name    VARCHAR(50)  NOT NULL,
    surname       VARCHAR(50)  NOT NULL,
    address       VARCHAR(150) NOT NULL,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    salt          VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(128) NOT NULL
    );

CREATE TABLE IF NOT EXISTS employer (
                                        employer_id     INT AUTO_INCREMENT PRIMARY KEY,
                                        company_name    VARCHAR(100) NOT NULL,
    company_address VARCHAR(150) NOT NULL
    );

CREATE TABLE IF NOT EXISTS account (
                                       account_number VARCHAR(12)   PRIMARY KEY,
    account_type   VARCHAR(12)   NOT NULL CHECK (account_type IN ('Savings', 'Investment', 'Cheque')),
    branch         VARCHAR(50)   NOT NULL,
    date_opened    DATE          NOT NULL,
    balance        DECIMAL(15,2) NOT NULL,
    customer_id    VARCHAR(10)   NOT NULL,
    employer_id    INT,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (employer_id) REFERENCES employer(employer_id)
    );

CREATE TABLE IF NOT EXISTS bank_transaction (
                                                transaction_id   VARCHAR(40)   PRIMARY KEY,
    account_number   VARCHAR(12)   NOT NULL,
    transaction_type VARCHAR(12)   NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'INTEREST')),
    amount           DECIMAL(15,2) NOT NULL,
    balance_after    DECIMAL(15,2) NOT NULL,
    created_at       TIMESTAMP     NOT NULL,
    note             VARCHAR(100),
    FOREIGN KEY (account_number) REFERENCES account(account_number) ON DELETE CASCADE
    );