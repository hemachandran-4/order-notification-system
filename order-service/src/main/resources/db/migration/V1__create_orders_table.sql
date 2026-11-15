CREATE TABLE orders (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    customer_email VARCHAR(255)   NOT NULL,
    total          DECIMAL(10, 2) NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_orders_customer_email ON orders (customer_email);