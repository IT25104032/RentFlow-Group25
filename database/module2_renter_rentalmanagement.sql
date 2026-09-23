# =====================================================
# COMPULIN  - RentFlow - EQUIPMENT HIRE & RENTAL MANAGEMENT SYSTEM
# MODULE 2
# Renter and Rental Management
# =====================================================


# 1. CUSTOMER

CREATE TABLE customer (
customer_id INT AUTO_INCREMENT,
company_id INT NOT NULL,
customer_name VARCHAR(150) NOT NULL,
email VARCHAR(120),
phone VARCHAR(25) NOT NULL,
address VARCHAR(255),
customer_type VARCHAR(20) NOT NULL,
customer_status VARCHAR(20) NOT NULL,
created_by INT,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

PRIMARY KEY(customer_id),

CONSTRAINT fk_customers_company
FOREIGN KEY (company_id)
REFERENCES company(company_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT fk_customers_created_by
FOREIGN KEY (created_by)
REFERENCES sys_user(user_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT check_customers_type
CHECK (customer_type IN ('INDIVIDUAL', 'BUSINESS')),

CONSTRAINT check_customers_status
CHECK (customer_status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))

);


#2. CUSTOMER_DOCUMENT

CREATE TABLE customer_document (
document_id INT AUTO_INCREMENT,
customer_id INT,
document_type VARCHAR(30) NOT NULL,
document_number VARCHAR(80) NOT NULL,
document_copy_path VARCHAR(500) NOT NULL,
expiry_date DATE,
checked_by INT NOT NULL,
checked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
notes VARCHAR(255),

PRIMARY KEY(document_id),

CONSTRAINT fk_customer_documents_customer
FOREIGN KEY (customer_id)
REFERENCES customer(customer_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT fk_customer_documents_checked_by
FOREIGN KEY (checked_by)
REFERENCES sys_user(user_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT check_customer_documents_type
CHECK (document_type IN ('NIC_ID', 'DRIVING_LICENCE','PASSPORT','OTHER'))
);


#3. CUSTOMER_SECONDARY_CONTACT

CREATE TABLE customer_secondary_contact (
secondary_contact_id INT AUTO_INCREMENT,
customer_id INT NOT NULL,
contact_name VARCHAR(100) NOT NULL,
relationship VARCHAR(50),
phone_number VARCHAR(20) NOT NULL,
alternate_phone VARCHAR(20),
email VARCHAR(150),
address VARCHAR(255),
notes VARCHAR(255),

PRIMARY KEY (secondary_contact_id),

CONSTRAINT uq_secondary_contact_customer
UNIQUE (customer_id),

CONSTRAINT fk_secondary_contacts_customer
FOREIGN KEY (customer_id)
REFERENCES customer(customer_id)
ON UPDATE CASCADE ON DELETE RESTRICT
);


#4. RENTAL

CREATE TABLE RENTAL (
rental_id INT AUTO_INCREMENT,
company_id INT NOT NULL,
customer_id INT NOT NULL,
created_by INT NOT NULL,
rental_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
start_date DATE NOT NULL,
due_date DATE NOT NULL,
rental_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
notes VARCHAR(500),
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

PRIMARY KEY (rental_id),

CONSTRAINT fk_rentals_company FOREIGN KEY (company_id) REFERENCES company(company_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT fk_rentals_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT fk_rentals_created_by FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT check_rentals_dates CHECK (due_date >= start_date),

CONSTRAINT check_rentals_status
CHECK (rental_status IN ('DRAFT', 'ACTIVE', 'OVERDUE', 'PARTIALLY_RETURNED', 'RETURNED', 'CLOSED', 'CANCELLED')));


# 5. RENTAL ITEM

CREATE TABLE rental_item (
rental_item_id INT AUTO_INCREMENT,
rental_id INT NOT NULL,
equipment_id INT NOT NULL,
quantity INT NOT NULL,
rate_per_unit DECIMAL(12,2) NOT NULL,
rate_period VARCHAR(20) NOT NULL,
deposit_per_unit DECIMAL(12,2) DEFAULT 0,
line_deposit DECIMAL(12,2) NOT NULL,
item_status VARCHAR(30) NOT NULL DEFAULT 'SELECTED',
issued_at DATETIME,

PRIMARY KEY (rental_item_id),

CONSTRAINT fk_rental_items_rental FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT fk_rental_items_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT check_rental_items_quantity CHECK (quantity > 0),

CONSTRAINT check_rental_items_rate CHECK (rate_per_unit >= 0),

CONSTRAINT check_rental_items_deposit CHECK (deposit_per_unit >= 0),

CONSTRAINT check_rental_items_line_deposit CHECK (line_deposit >= 0),

CONSTRAINT check_rental_items_status
CHECK (item_status IN ('SELECTED', 'ISSUED', 'PARTIALLY_RETURNED', 'RETURNED', 'LOST', 'CLOSED')));


#RENTAL EXTENSION

CREATE TABLE rental_extension (
extension_id INT AUTO_INCREMENT PRIMARY KEY,
rental_id INT NOT NULL,
old_due_date DATE NOT NULL,
new_due_date DATE NOT NULL,
extension_charge DECIMAL(12,2) DEFAULT 0,
approved_by INT NOT NULL,
reason VARCHAR(255),
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

CONSTRAINT fk_rental_extensions_rental FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT fk_rental_extensions_approved_by FOREIGN KEY (approved_by) REFERENCES sys_user(user_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT check_rental_extensions_dates CHECK (new_due_date > old_due_date),

CONSTRAINT check_rental_extensions_charge CHECK (extension_charge >= 0)


);


#POPULATING THE DATABASE

#1. Register a new customer
INSERT INTO customer (company_id, customer_name, email, phone, address, customer_type, customer_status, created_by, created_at)
VALUES (1001, 'John Perera', 'john.perera@email.com', '0771234567', '25 Main Street, Colombo', 'INDIVIDUAL', 'ACTIVE', 5, NOW());

#2. Add identification document
INSERT INTO customer_document (customer_id, document_type, document_number, document_copy_path, expiry_date, checked_by, checked_at, notes)
VALUES (1, 'NIC_ID', '199512345678', '/documents/customers/1/nic.pdf', NULL, 5, NOW(), 'Identification checked and verified');


#3. Create rental
INSERT INTO rental (rental_id, company_id, customer_id, created_by, rental_date, start_date, due_date, rental_status, notes, created_at)
VALUES (1001, 1001, 1, 5, NOW(), '2026-09-20', '2026-09-25', 'DRAFT', 'New equipment rental', NOW());

#4. Add equipment to rental
INSERT INTO rental_item (rental_id, equipment_id, quantity, rate_per_unit, rate_period, deposit_per_unit, line_deposit, item_status, issued_at)
SELECT 1001, equipment_id, 2, rental_rate, rate_period, refundable_deposit_per_unit, 2 * refundable_deposit_per_unit, 'SELECTED', NULL
FROM equipment
WHERE equipment_id = 10 AND company_id = 1001 AND item_status = 'ACTIVE' AND available_quantity >= 2;
  
  
#5. Record rental extension
INSERT INTO rental_extension (rental_id, old_due_date, new_due_date, extension_charge, approved_by, reason, created_at)
SELECT rental_id, due_date, '2026-09-30', 5000.00, 5, 'Customer requested rental extension', NOW()
FROM rental
WHERE rental_id = 1001 AND company_id = 1001;



#SQL QUERIES
#1. Search customer by name
SELECT customer_id, customer_name, email, phone, address, customer_type, customer_status, created_at
FROM customer c
WHERE company_id = 1001 AND (c.customer_name LIKE '%John%' OR phone = '0771234567')
ORDER BY customer_name;


#2. View customer profile
SELECT
    c.customer_id,
    c.customer_name,
    c.email,
    c.phone,
    c.address,
    c.customer_type,
    c.customer_status,
    c.created_at,
    u.full_name AS created_by
FROM customer c
LEFT JOIN sys_user u
    ON c.created_by = u.user_id
WHERE c.customer_id = 1
  AND c.company_id = 1001;


#3. Update Customer
UPDATE customer
SET
    customer_name = 'John Perera',
    email = 'john.new@gmail.com',
    phone = '0779999999',
    address = '50 Main Street, Colombo',
    customer_type = 'INDIVIDUAL'
WHERE customer_id = 1 AND company_id = 1001;
  
  
#4. View customer identification documents
SELECT
    cd.document_id,
    cd.customer_id,
    cd.document_type,
    cd.document_number,
    cd.document_copy_path,
    cd.expiry_date,
    cd.checked_at,
    u.full_name AS checked_by,
    cd.notes
FROM customer_document cd
JOIN sys_user u
    ON cd.checked_by = u.user_id
WHERE cd.customer_id = 1
ORDER BY cd.checked_at DESC;


#5. Update identification document
UPDATE customer_document
SET
    document_type = 'DRIVING_LICENCE',
    document_number = 'B1234567',
    document_copy_path = '/documents/customers/1/license.pdf',
    expiry_date = '2030-08-15',
    checked_by = 5,
    checked_at = NOW(),
    notes = 'Updated identification document'
WHERE document_id = 10
  AND customer_id = 1;
  

#6. View customer's rental history
SELECT
    r.rental_id,
    r.rental_date,
    r.start_date,
    r.due_date,
    r.rental_status,
    r.notes
FROM rental r
WHERE r.customer_id = 1 AND r.company_id = 1001
ORDER BY r.rental_date DESC;


#7. View customer's rental history with equipment
SELECT
    r.rental_id,
    r.start_date,
    r.due_date,
    r.rental_status,
    e.item_name,
    e.item_code,
    ri.quantity,
    ri.rate_per_unit,
    ri.rate_period,
    ri.item_status
FROM rental r
JOIN rental_item ri
    ON r.rental_id = ri.rental_id
JOIN equipment e
    ON ri.equipment_id = e.equipment_id
WHERE r.customer_id = 1 AND r.company_id = 1001
ORDER BY r.start_date DESC;

  
#8. View customer's current active rentals
SELECT
    r.rental_id,
    r.start_date,
    r.due_date,
    r.rental_status,
    e.item_name,
    ri.quantity,
    ri.item_status
FROM rental r
JOIN rental_item ri
    ON r.rental_id = ri.rental_id
JOIN equipment e
    ON ri.equipment_id = e.equipment_id
WHERE r.customer_id = 1
  AND r.company_id = 1001
  AND r.rental_status IN ('ACTIVE', 'OVERDUE','PARTIALLY_RETURNED')
ORDER BY r.due_date;

  
#9. View available equipment
SELECT
    e.equipment_id,
    e.item_name,
    e.item_code,
    ec.category_name,
    e.charge_description,
    e.rental_rate,
    e.rate_period,
    e.refundable_deposit_per_unit,
    e.available_quantity
FROM equipment e
JOIN equipment_category ec
    ON e.category_id = ec.category_id
WHERE e.company_id = 1001 AND e.equ_status = 'ACTIVE' AND e.available_quantity > 0
ORDER BY ec.category_name, e.item_name;


#10. Search available equipment
SELECT
    e.equipment_id,
    e.item_name,
    e.item_code,
    ec.category_name,
    e.rental_rate,
    e.rate_period,
    e.available_quantity
FROM equipment e
JOIN equipment_category ec
    ON e.category_id = ec.category_id
WHERE e.company_id = 1001
  AND e.equ_status = 'ACTIVE'
  AND e.available_quantity > 0
  AND (e.item_name LIKE '%camera%' OR e.item_code LIKE '%camera%' OR ec.category_name LIKE '%camera%')
ORDER BY e.item_name;

#11. Validate requested quantity
SELECT equipment_id, item_name, available_quantity,
    CASE
        WHEN available_quantity >= 3
            THEN 'AVAILABLE'
        ELSE 'INSUFFICIENT_QUANTITY'
    END AS availability_result
FROM equipment
WHERE equipment_id = 10 AND company_id = 1001 AND equ_status = 'ACTIVE';
  

#12. View rental details
SELECT
    r.rental_id,
    r.rental_date,
    r.start_date,
    r.due_date,
    r.rental_status,
    r.notes,
    c.customer_id,
    c.customer_name,
    c.phone,
    c.email,
    u.full_name AS created_by
FROM rental r
JOIN customer c
    ON r.customer_id = c.customer_id
JOIN sys_user u
    ON r.created_by = u.user_id
WHERE r.rental_id = 1001 AND r.company_id = 1001;
  
SELECT
    ri.rental_item_id,
    e.item_name,
    e.item_code,
    ri.quantity,
    ri.rate_per_unit,
    ri.rate_period,
    ri.deposit_per_unit,
    ri.line_deposit,
    ri.item_status
FROM rental_item ri
JOIN equipment e
    ON ri.equipment_id = e.equipment_id
WHERE ri.rental_id = 1001;


#13. Calculate security deposit
SELECT
    rental_id,
    SUM(line_deposit) AS calculated_deposit
FROM rental_item
WHERE rental_id = 1001
GROUP BY rental_id;


#14. Record equipment issue
UPDATE rental_item
SET
    item_status = 'ISSUED',
    issued_at = NOW()
WHERE rental_item_id = 1;

UPDATE rental
SET rental_status = 'ACTIVE'
WHERE rental_id = 1001 AND company_id = 1001;
  

#15. Reduce equipment availability
UPDATE equipment
SET available_quantity = available_quantity - 2
WHERE equipment_id = 10 AND company_id = 1001 AND available_quantity >= 2;
  
#14 and 15 should be one part in springboot
  
#16. Update rental due date
UPDATE rental
SET due_date = '2026-09-30'
WHERE rental_id = 1001 AND company_id = 1001;
  
  
#17. View rental extension history
SELECT
    re.extension_id,
    re.rental_id,
    re.old_due_date,
    re.new_due_date,
    re.extension_charge,
    re.reason,
    re.created_at,
    u.full_name AS approved_by
FROM rental_extension re
JOIN sys_user u
    ON re.approved_by = u.user_id
WHERE re.rental_id = 1001
ORDER BY re.created_at DESC;


#18. View all active rentals
SELECT
    r.rental_id,
    c.customer_name,
    r.start_date,
    r.due_date,
    r.rental_status
FROM rental r
JOIN customer c
    ON r.customer_id = c.customer_id
WHERE r.company_id = 1001
  AND r.rental_status IN (
      'ACTIVE',
      'PARTIALLY_RETURNED'
  )
ORDER BY r.due_date;


#19. Find overdue rentals
SELECT
    r.rental_id,
    c.customer_id,
    c.customer_name,
    c.phone,
    r.start_date,
    r.due_date,
    DATEDIFF(CURDATE(), r.due_date) AS days_overdue,
    r.rental_status
FROM rental r
JOIN customer c
    ON r.customer_id = c.customer_id
WHERE r.company_id = 1001 AND r.due_date < CURDATE() AND r.rental_status IN ('ACTIVE', 'PARTIALLY_RETURNED', 'OVERDUE')
ORDER BY r.due_date;


#20. Rentals due soon
SELECT
    r.rental_id,
    c.customer_name,
    c.phone,
    r.start_date,
    r.due_date,
    r.rental_status
FROM rental r
JOIN customer c
    ON r.customer_id = c.customer_id
WHERE r.company_id = 1001
  AND r.due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 3 DAY) AND r.rental_status IN ('ACTIVE', 'PARTIALLY_RETURNED')
ORDER BY r.due_date;