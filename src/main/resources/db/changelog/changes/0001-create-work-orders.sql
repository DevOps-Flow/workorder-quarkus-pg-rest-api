--liquibase formatted sql

--changeset labsafer:0001-create-work-orders
CREATE TABLE IF NOT EXISTS work_orders (
    id               UUID PRIMARY KEY,
    customer_id      UUID NOT NULL,
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    status           VARCHAR(40) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_work_orders_customer_id ON work_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_work_orders_status ON work_orders(status);
--rollback DROP TABLE IF EXISTS work_orders;
