CREATE DATABASE module3;
USE module3;

CREATE TABLE invoice(
	invoice_id INT AUTO_INCREMENT, 
    rental_id INT NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE,
    subtotal DECIMAL(12,2) NOT NULL,
    additional_charges DECIMAL(12,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    amount_paid DECIMAL(12,2) DEFAULT 0,
    balance_due DECIMAL(12,2) NOT NULL,
    invoice_status varchar(25) NOT NULL CHECK (stat IN ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')),
    PRIMARY KEY (invoice_id),
    CONSTRAINT FOREIGN KEY (rental_id) REFERENCES rentals(rental_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
    );
    
UPDATE invoice
SET invoice_status = CASE 
    WHEN amount_paid = 0 THEN 'UNPAID'
    WHEN amount_paid > 0 AND amount_paid < total_amount THEN 'PARTIALLY_PAID'
    WHEN amount_paid >= total_amount THEN 'PAID'
    ELSE invoice_status 
END
WHERE invoice_id = 101;


CREATE TABLE charge(
	charge_id INT AUTO_INCREMENT,
    rental_id INT NOT NULL,
    rental_item_id INT NULL,
    invoice_id INT NULL,
    charge_type VARCHAR(30) NOT NULL CHECK (charge_type IN ('RENTAL', 'EXTENSION', 'LATE', 'DAMAGE',
'LOST_ITEM', 'OTHER')),
    charge_description VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    charge_date DATETIME NOT NULL,
    created_by INT NOT NULL,
    PRIMARY KEY (charge_id),
    CONSTRAINT FOREIGN KEY (rental_id) REFERENCES rentals(rental_id),
    CONSTRAINT FOREIGN KEY (rental_item_id) REFERENCES rental_items(rental_item_id),
    CONSTRAINT FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id),
    CONSTRAINT FOREIGN KEY (created_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
    );

CREATE TABLE payment(
	payment_id INT AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date DATETIME NOT NULL,
    payment_method VARCHAR(30) NOT NULL CHECK (payment_method IN ('CASH', 'CARD', 'BANK_TRANSFER')),
    reference_no VARCHAR(100),
    received_by INT NOT NULL,
    payment_status VARCHAR(20) NOT NULL CHECK (stat IN ('COMPLETED', 'VOIDED')),
    PRIMARY KEY (payment_id),
    CONSTRAINT FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id),
    CONSTRAINT FOREIGN KEY (recieved_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
    );
    
CREATE TABLE security_deposit(
	deposit_id INT AUTO_INCREMENT,
    rental_id INT UNIQUE,
    calculated_deposit DECIMAL(12,2) NOT NULL,
    deposit_amount_received DECIMAL(12,2) NOT NULL,
    amount_deducted DECIMAL(12,2) DEFAULT 0,
    amount_refunded DECIMAL(12,2) DEFAULT 0,
    received_date DATETIME,
    refund_date DATETIME NULL,
    received_by INT,
    deposit_status VARCHAR(25) NOT NULL CHECK (stat IN ('PENDING', 'HELD', 'PARTIALLY_REFUNDED',
'REFUNDED', 'FORFEITED')),
	PRIMARY KEY(deposit_id),
    CONSTRAINT FOREIGN KEY (rental_id) REFERENCES rental(rental_id),
    CONSTRAINT FOREIGN KEY (recieved_by) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 1. Insert Deposit Record
INSERT INTO security_deposit (deposit_id, rental_id, calculated_deposit, deposit_amount_received, amount_deducted, amount_refunded, received_date, received_by, deposit_status)
VALUES (1, 101, 200.00, 200.00, 0.00, 0.00, NOW(), 1, 'HELD');

-- 2. Insert Invoice Header (Initial state: Unpaid)
INSERT INTO invoice (invoice_id, rental_id, invoice_date, due_date, subtotal, additional_charges, total_amount, amount_paid, balance_due, invoice_status)
VALUES (1001, 101, '2026-09-14', '2026-09-21', 400.00, 75.00, 475.00, 0.00, 475.00, 'UNPAID');

-- 3. Insert Base Rental Charge
INSERT INTO charge (rental_id, rental_item_id, invoice_id, charge_type, charge_description, amount, charge_date, created_by)
VALUES (101, 501, 1001, 'RENTAL', 'Base rental fee for 2 units x 4 days', 400.00, NOW(), 1);

-- 4. Insert Damage Penalty Charge (Passed from Module 4)
INSERT INTO charge (rental_id, rental_item_id, invoice_id, charge_type, charge_description, amount, charge_date, created_by)
VALUES (101, 501, 1001, 'DAMAGE', 'Minor equipment damage assessment', 75.00, NOW(), 1);


-- Record the payment entry
INSERT INTO payment (invoice_id, amount, payment_date, payment_method, reference_no, received_by, payment_status)
VALUES (1001, 200.00, NOW(), 'BANK_TRANSFER', 'TXN-982312', 1, 'COMPLETED');

-- Update the invoice totals and recalculate status dynamically
UPDATE invoice
SET 
    amount_paid = amount_paid + 200.00,
    balance_due = total_amount - (amount_paid + 200.00),
    invoice_status = CASE 
        WHEN (amount_paid + 200.00) = 0 THEN 'UNPAID'
        WHEN (amount_paid + 200.00) > 0 AND (amount_paid + 200.00) < total_amount THEN 'PARTIALLY_PAID'
        WHEN (amount_paid + 200.00) >= total_amount THEN 'PAID'
        ELSE invoice_status 
    END
WHERE invoice_id = 1001;

-- Verify the updated invoice status
SELECT invoice_id, total_amount, amount_paid, balance_due, invoice_status 
FROM invoice
WHERE invoice_id = 1001;




SELECT 
    i.invoice_id,
    i.total_amount AS header_total_amount,
    SUM(c.amount) AS calculated_charges_sum,
    (i.total_amount - SUM(c.amount)) AS discrepancy
FROM invoice i
JOIN charge c ON i.invoice_id = c.invoice_id
WHERE i.invoice_id = 1001
GROUP BY i.invoice_id, i.total_amount;


SELECT 
    c.charge_id,
    c.charge_type,
    c.charge_description,
    c.amount,
    c.charge_date,
    u.full_name AS added_by_staff
FROM charge c
JOIN user u ON c.created_by = u.user_id
WHERE c.invoice_id = 1001
ORDER BY c.charge_date ASC;


SELECT 
    COALESCE(SUM(i.amount_paid), 0.00) AS total_revenue_collected,
    COALESCE(SUM(i.balance_due), 0.00) AS total_outstanding_balance,
    (SELECT COALESCE(SUM(deposit_amount_received - amount_refunded), 0.00) 
     FROM security_deposit
     WHERE deposit_status = 'HELD') AS total_deposits_held
FROM invoice i
WHERE i.invoice_status != 'CANCELLED';


-- Step A: Find all unbilled charges for a rental
SELECT charge_id, charge_type, charge_description, amount 
FROM charge 
WHERE rental_id = 101 AND invoice_id IS NULL;

-- Step B: Attach unbilled charges to Invoice #1001
UPDATE charge 
SET invoice_id = 1001 
WHERE rental_id = 101 AND invoice_id IS NULL;

-- Step C: Recalculate invoice total_amount and balance_due
UPDATE invoice i
SET 
    additional_charges = (
        SELECT COALESCE(SUM(amount), 0.00) 
        FROM charge
        WHERE invoice_id = i.invoice_id AND charge_type != 'RENTAL'
    ),
    total_amount = subtotal + additional_charges,
    balance_due = total_amount - amount_paid
WHERE invoice_id = 1001;



-- Step A: Record final payment matching balance_due
INSERT INTO payment (invoice_id, amount, payment_date, payment_method, reference_no, received_by, payment_status)
VALUES (1001, 275.00, NOW(), 'CARD', 'TXN-998811', 1, 'COMPLETED');

-- Step B: Recalculate invoice status dynamically
UPDATE invoice
SET 
    amount_paid = amount_paid + 275.00,
    balance_due = total_amount - (amount_paid + 275.00),
    invoice_status = CASE 
        WHEN (amount_paid + 275.00) >= total_amount THEN 'PAID'
        WHEN (amount_paid + 275.00) > 0 THEN 'PARTIALLY_PAID'
        ELSE 'UNPAID'
    END
WHERE invoice_id = 1001;

-- Step C: Verify invoice is fully paid
SELECT invoice_id, total_amount, amount_paid, balance_due, invoice_status 
FROM invoice
WHERE invoice_id = 1001;




-- Process deposit deduction and mark as PARTIALLY_REFUNDED
UPDATE security_deposit
SET 
    amount_deducted = 75.00,
    amount_refunded = deposit_amount_received - 75.00,
    refund_date = NOW(),
    deposit_status = 'PARTIALLY_REFUNDED'
WHERE rental_id = 101;

-- Verify updated deposit state
SELECT rental_id, calculated_deposit, deposit_amount_received, amount_deducted, amount_refunded, deposit_status
FROM security_deposit
WHERE rental_id = 101;




INSERT INTO settlement (
    rental_id, rental_charges, late_charges, damage_charges, lost_item_charges, 
    total_charges, deposit_used, deposit_refunded, final_balance, settled_at, settled_by, status
)
SELECT 
    r.rental_id,
    COALESCE(SUM(CASE WHEN c.charge_type = 'RENTAL' THEN c.amount ELSE 0 END), 0.00) AS rental_charges,
    COALESCE(SUM(CASE WHEN c.charge_type = 'LATE' THEN c.amount ELSE 0 END), 0.00) AS late_charges,
    COALESCE(SUM(CASE WHEN c.charge_type = 'DAMAGE' THEN c.amount ELSE 0 END), 0.00) AS damage_charges,
    COALESCE(SUM(CASE WHEN c.charge_type = 'LOST_ITEM' THEN c.amount ELSE 0 END), 0.00) AS lost_item_charges,
    COALESCE(SUM(c.amount), 0.00) AS total_charges,
    s.amount_deducted AS deposit_used,
    s.amount_refunded AS deposit_refunded,
    i.balance_due AS final_balance,
    NOW(),
    1,
    'SETTLED'
FROM rental r
LEFT JOIN charges c ON r.rental_id = c.rental_id
LEFT JOIN security_deposits s ON r.rental_id = s.rental_id
LEFT JOIN invoices i ON r.rental_id = i.rental_id
WHERE r.rental_id = 101
GROUP BY r.rental_id, s.amount_deducted, s.amount_refunded, i.balance_due;

-- View created settlement record
SELECT * FROM settlement WHERE rental_id = 101;





-- Step A: Mark payment as VOIDED
UPDATE payment
SET payment_status = 'VOIDED' 
WHERE payment_id = 1;

-- Step B: Recalculate amount_paid and status using only COMPLETED payments
UPDATE invoice i
LEFT JOIN (
    SELECT invoice_id, COALESCE(SUM(amount), 0.00) AS valid_paid 
    FROM payment
    WHERE payment_status = 'COMPLETED' AND invoice_id = 1001
) p ON i.invoice_id = p.invoice_id
SET 
    i.amount_paid = COALESCE(p.valid_paid, 0.00),
    i.balance_due = i.total_amount - COALESCE(p.valid_paid, 0.00),
    i.invoice_status = CASE 
        WHEN COALESCE(p.valid_paid, 0.00) = 0 THEN 'UNPAID'
        WHEN COALESCE(p.valid_paid, 0.00) > 0 AND COALESCE(p.valid_paid, 0.00) < i.total_amount THEN 'PARTIALLY_PAID'
        ELSE 'PAID'
    END
WHERE i.invoice_id = 1001;





-- Retrieve invoices strictly belonging to Company ID #1
SELECT 
    i.invoice_id, 
    i.invoice_date, 
    i.total_amount, 
    i.amount_paid, 
    i.balance_due, 
    i.invoice_status,
    r.company_id
FROM invoice i
JOIN rental r ON i.rental_id = r.rental_id
WHERE r.company_id = 1;






SELECT 
    i.invoice_id,
    i.rental_id,
    i.due_date,
    i.total_amount,
    i.balance_due,
    i.invoice_status,
    DATEDIFF(CURRENT_DATE, i.due_date) AS days_overdue
FROM invoice i
WHERE i.invoice_status IN ('UNPAID', 'PARTIALLY_PAID') 
  AND i.due_date < CURRENT_DATE
ORDER BY days_overdue DESC;

