ALTER TABLE sales DROP CONSTRAINT chk_sales_payment_method;
ALTER TABLE sales ADD CONSTRAINT chk_sales_payment_method
    CHECK (payment_method IN ('CASH','CARD','TRANSFER','OTHER','MP_NATHALY'));
