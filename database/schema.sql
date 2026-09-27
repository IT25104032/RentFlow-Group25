CREATE DATABASE IF NOT EXISTS rentflow_db;
USE rentflow_db;

# ============== MODULE 1 TABLES ================

#IT25104048
CREATE TABLE company (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    registration_no VARCHAR(60) UNIQUE,
    email VARCHAR(120) NOT NULL,
    phone VARCHAR(25),
    address VARCHAR(255),
    registered_by INT NOT NULL,
    registration_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    company_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
        
    CONSTRAINT chk_company_status
        CHECK (company_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
        
) AUTO_INCREMENT = 1000; 


#IT25104048
CREATE TABLE sys_user (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NULL,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(25),
    user_role VARCHAR(30) NOT NULL,
    user_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_company
        FOREIGN KEY (company_id)
        REFERENCES company(company_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_user_role
        CHECK (
            user_role IN (
                'COMPULIN_ADMIN',
                'COMPANY_ADMIN',
                'RENTAL_OFFICER'
            )
        ),

    CONSTRAINT chk_user_status
        CHECK (
            user_status IN (
                'ACTIVE',
                'INACTIVE'
            )
        )
);


#IT25104048
ALTER TABLE company
ADD CONSTRAINT fk_companies_registered_by
    FOREIGN KEY (registered_by)
    REFERENCES sys_user(user_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;
        
#IT25104048
CREATE TABLE equipment_category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    cat_description VARCHAR(255),
    cat_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_categories_company
        FOREIGN KEY (company_id)
        REFERENCES company(company_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_category_status
        CHECK (
            cat_status IN (
                'ACTIVE',
                'INACTIVE'
            )
        )
);

#IT25104048
CREATE TABLE equipment (
    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    category_id INT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    item_code VARCHAR(80),
    equ_description VARCHAR(500),
    rental_rate DECIMAL(12,2) NOT NULL,
    rate_period VARCHAR(20) NOT NULL,
    security_deposit_per_unit DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    equ_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_equipment_company
        FOREIGN KEY (company_id)
        REFERENCES company(company_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_equipment_category
        FOREIGN KEY (category_id)
        REFERENCES equipment_category(category_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT uq_equipment_item_code
        UNIQUE (company_id, item_code),

    CONSTRAINT chk_equipment_rate
        CHECK (rental_rate >= 0),

    CONSTRAINT chk_equipment_deposit
        CHECK (security_deposit_per_unit >= 0),

    CONSTRAINT chk_equipment_total_quantity
        CHECK (total_quantity >= 0),

    CONSTRAINT chk_equipment_available_quantity
        CHECK (available_quantity >= 0),

    CONSTRAINT chk_available_not_greater_than_total
        CHECK (available_quantity <= total_quantity),

    CONSTRAINT chk_equipment_status
        CHECK (
            equ_status IN (
                'ACTIVE',
                'INACTIVE',
                'OUT_OF_SERVICE'
            )
        )
);


# ============== MODULE 2 TABLES ================

# 1. CUSTOMER
#IT25104032
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

CONSTRAINT fk_customers_company FOREIGN KEY (company_id) REFERENCES company(company_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT fk_customers_created_by FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT check_customers_type CHECK (customer_type IN ('INDIVIDUAL', 'BUSINESS')),

CONSTRAINT check_customers_status CHECK (customer_status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))

);


#2. CUSTOMER_DOCUMENT
#IT25104032
CREATE TABLE customer_document (
document_id INT AUTO_INCREMENT,
customer_id INT NOT NULL,
document_type VARCHAR(30) NOT NULL,
document_number VARCHAR(80) NOT NULL,
document_copy_path VARCHAR(500) NOT NULL,
expiry_date DATE,
checked_by INT NOT NULL,
checked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
notes VARCHAR(255),

PRIMARY KEY(document_id),

CONSTRAINT fk_customer_documents_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT fk_customer_documents_checked_by FOREIGN KEY (checked_by) REFERENCES sys_user(user_id)
ON UPDATE CASCADE ON DELETE RESTRICT,

CONSTRAINT check_customer_documents_type CHECK (document_type IN ('NIC_ID', 'DRIVING_LICENCE','PASSPORT','OTHER'))
);


#3. CUSTOMER_SECONDARY_CONTACT
#IT25104032
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

CONSTRAINT uq_secondary_contact_customer UNIQUE (customer_id),

CONSTRAINT fk_secondary_contacts_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
ON UPDATE CASCADE ON DELETE RESTRICT
);


#4. RENTAL
#IT25104032
CREATE TABLE rental (
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
#IT25104032
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
#IT25104032
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


# ============== MODULE 3 TABLES ================
#IT25104036
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
    invoice_status varchar(25) NOT NULL, 
    PRIMARY KEY (invoice_id),
    CONSTRAINT FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT check_invoice_status CHECK (invoice_status IN ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'CANCELLED'))
    );

#IT25104036
CREATE TABLE charge(
	charge_id INT AUTO_INCREMENT,
    rental_id INT NOT NULL,
    rental_item_id INT NULL,
    invoice_id INT NULL,
    charge_type VARCHAR(30) NOT NULL CHECK (charge_type IN ('RENTAL', 'EXTENSION', 'LATE', 'DAMAGE', 'LOST_ITEM', 'OTHER')),
    charge_description VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    charge_date DATETIME NOT NULL,
    created_by INT NOT NULL,
    PRIMARY KEY (charge_id),
    CONSTRAINT FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FOREIGN KEY (rental_item_id) REFERENCES rental_item(rental_item_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
    );

#IT25104036
CREATE TABLE payment(
	payment_id INT AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date DATETIME NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    reference_no VARCHAR(100),
    received_by INT NOT NULL,
    payment_status VARCHAR(20)NOT NULL,
    PRIMARY KEY (payment_id),
    CONSTRAINT FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FOREIGN KEY (received_by) REFERENCES sys_user(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
	CONSTRAINT check_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'BANK_TRANSFER')),
	CONSTRAINT check_paymen_status CHECK (payment_status IN ('COMPLETED', 'VOIDED'))
    );
    
#IT25104036
CREATE TABLE security_deposit(
	deposit_id INT AUTO_INCREMENT,
    rental_id INT UNIQUE,
    calculated_deposit DECIMAL(12,2) NOT NULL,
    deposit_amount_received DECIMAL(12,2) NOT NULL,
    amount_deducted DECIMAL(12,2) DEFAULT 0,
    amount_refunded DECIMAL(12,2) DEFAULT 0,
    received_date DATETIME,
    refund_date DATETIME,
    received_by INT,
    deposit_status VARCHAR(25) NOT NULL,
	PRIMARY KEY(deposit_id),
    CONSTRAINT FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FOREIGN KEY (received_by) REFERENCES sys_user(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
	CONSTRAINT check_deposit_status CHECK (deposit_status IN ('PENDING', 'HELD', 'PARTIALLY_REFUNDED', 'REFUNDED', 'FORFEITED'))
);


# ============== MODULE 4 TABLES ================
#TABLE 1 : rental_return
#IT25104066
CREATE TABLE rental_return (
    return_id INT AUTO_INCREMENT PRIMARY KEY,
    rental_id INT NOT NULL,
    return_date DATETIME NOT NULL,
    processed_by INT NOT NULL,
    return_type VARCHAR(20) NOT NULL CHECK (return_type IN ('FULL', 'PARTIAL')),
    notes VARCHAR(500),
    
    FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    
    FOREIGN KEY (processed_by) REFERENCES sys_user(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
);


#TABLE 2 : return_item
#IT25104066
CREATE TABLE return_item (
    return_item_id INT AUTO_INCREMENT PRIMARY KEY,
    return_id INT NOT NULL,
    rental_item_id INT NOT NULL,
    qty_returned INT NOT NULL CHECK (qty_returned > 0),
    condition_status VARCHAR(30) NOT NULL CHECK (condition_status IN ('GOOD', 'DAMAGED', 'MISSING PARTS', 'NEEDS MAINTENANCE')),
    inspection_notes VARCHAR(500),
    returned_at DATETIME NOT NULL,

	FOREIGN KEY (return_id) REFERENCES rental_return(return_id)
	ON UPDATE CASCADE ON DELETE RESTRICT,
    
    FOREIGN KEY (rental_item_id) REFERENCES rental_item(rental_item_id)
	ON UPDATE CASCADE ON DELETE RESTRICT
);


#TABLE 3 : damage_record
#IT25104066
CREATE TABLE damage_record (
    damage_id INT AUTO_INCREMENT PRIMARY KEY,
    return_item_id INT NOT NULL,
    damaged_quantity INT NOT NULL CHECK (damaged_quantity > 0),
    damage_description VARCHAR(500) NOT NULL,
    damage_level VARCHAR(20) NOT NULL CHECK (damage_level IN ('MINOR', 'MODERATE', 'SEVERE')),
    estimated_cost DECIMAL(12,2) DEFAULT 0 CHECK (estimated_cost >= 0),
    final_charge DECIMAL(12,2) DEFAULT 0 CHECK (final_charge >= 0),
    assessed_by INT NOT NULL,
    assessment_date DATETIME NOT NULL,
    damage_status VARCHAR(30) NOT NULL CHECK (damage_status IN ('ASSESSED', 'CHARGED', 'REPAIRED', 'WAIVED')),


	FOREIGN KEY (return_item_id) REFERENCES return_item(return_item_id)
	ON UPDATE CASCADE ON DELETE RESTRICT,
    
    FOREIGN KEY (assessed_by) REFERENCES sys_user(user_id)
	ON UPDATE CASCADE ON DELETE RESTRICT

);

#TABLE 4 : lost_item 
#IT25104066
CREATE TABLE lost_item(
    lost_item_id INT AUTO_INCREMENT PRIMARY KEY,
    rental_item_id INT NOT NULL,
    quantity_lost INT NOT NULL CHECK (quantity_lost > 0),
    loss_type VARCHAR(30) NOT NULL CHECK (loss_type IN ('LOST', 'STOLEN', 'NON_RETURNED')),
    reported_date DATETIME NOT NULL,
    reason VARCHAR(500),
    replacement_cost_per_unit DECIMAL(12,2) DEFAULT 0 CHECK (replacement_cost_per_unit >= 0),
    charge_amount DECIMAL(12,2) DEFAULT 0 CHECK (charge_amount >= 0),
    reported_by INT NOT NULL,
    lost_status VARCHAR(30) NOT NULL CHECK (lost_status IN ('PENDING', 'RECOVERED', 'CHARGED', 'SETTLED')),
    notes VARCHAR(500),
    
    FOREIGN KEY (rental_item_id) REFERENCES rental_item(rental_item_id)
	ON UPDATE CASCADE ON DELETE RESTRICT,
    
    FOREIGN KEY (reported_by) REFERENCES sys_user(user_id)
	ON UPDATE CASCADE ON DELETE RESTRICT

);


#TABLE 5 : settlement
#IT25104066
CREATE TABLE settlement (
    settlement_id INT AUTO_INCREMENT PRIMARY KEY,
    rental_id INT NOT NULL UNIQUE,
    rental_charges DECIMAL(12,2) DEFAULT 0 CHECK (rental_charges >= 0),
    late_charges DECIMAL(12,2) DEFAULT 0 CHECK (late_charges >= 0),
    damage_charges DECIMAL(12,2) DEFAULT 0 CHECK (damage_charges >= 0),
    lost_item_charges DECIMAL(12,2) DEFAULT 0 CHECK (lost_item_charges >= 0),
    total_charges DECIMAL(12,2) NOT NULL CHECK (total_charges >= 0),
    deposit_used DECIMAL(12,2) DEFAULT 0 CHECK (deposit_used >= 0),
    deposit_refunded DECIMAL(12,2) DEFAULT 0 CHECK (deposit_refunded >= 0),
    final_balance DECIMAL(12,2) NOT NULL,
    settled_at DATETIME,
    settled_by INT NOT NULL,
    settlement_status VARCHAR(20) NOT NULL CHECK (settlement_status IN ('PENDING', 'SETTLED')),


	FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
	ON UPDATE CASCADE ON DELETE RESTRICT,
    
    FOREIGN KEY (settled_by) REFERENCES sys_user(user_id)
	ON UPDATE CASCADE ON DELETE RESTRICT

);

# =================== INSERT Statements =============================

# 1. CREATE COMPULIN ADMIN (IT25104048)
INSERT INTO sys_user
(user_id, company_id, full_name, email, password_hash, phone, user_role, user_status) VALUES
(1, NULL, 'Compulin Admin', 'admin@compulin.com', 'test_hash_1', '0771111111', 'COMPULIN_ADMIN', 'ACTIVE');

# 2. CREATE COMPANY (IT25104048)
INSERT INTO company
(company_id, company_name, registration_no, email, phone, address, registered_by, company_status) VALUES
(1000, 'ABC Equipment Rentals', 'BR123456', 'info@abcrentals.com', '0112345678', 'Colombo', 1, 'ACTIVE');

# 3. CREATE COMPANY USERS (IT25104048)
INSERT INTO sys_user (user_id, company_id, full_name, email, password_hash, phone, user_role, user_status) VALUES
(2, 1000, 'Kamal Silva', 'kamal@abc.lk', 'test_hash_2', '0773333333', 'COMPANY_ADMIN', 'ACTIVE'),
(3, 1000, 'Nimal Perera', 'nimal@abcrentals.com', 'test_hash_3', '0772222222', 'RENTAL_OFFICER', 'ACTIVE'),
(4, 1000, 'Amal Fernando', 'amal@abcrentals.com', 'test_hash_4', '0774444444', 'RENTAL_OFFICER', 'ACTIVE'),
(5, 1000, 'Sahan Fernando', 'sahan@abcrentals.com', 'test_hash_5', '0775555555', 'COMPANY_ADMIN', 'ACTIVE');

# 4. CREATE EQUIPMENT CATEGORIES (IT25104048)
INSERT INTO equipment_category (category_id, company_id, category_name, cat_description, cat_status) VALUES
(1, 1000, 'Power Tools', 'Electric and battery powered tools', 'ACTIVE'),
(2, 1000, 'Construction Equipment', 'Equipment used for construction work', 'ACTIVE'),
(3, 1000, 'Cleaning Equipment', 'Commercial cleaning machines', 'ACTIVE'),
(4, 1000, 'Photography Equipment', 'Cameras and related equipment', 'ACTIVE');

# 5. CREATE EQUIPMENT (IT25104048)
INSERT INTO equipment (equipment_id, company_id, category_id, item_name, item_code, equ_description, rental_rate, rate_period,
security_deposit_per_unit, total_quantity, available_quantity, equ_status) VALUES

(1, 1000, 1, 'Electric Drill', 'DRILL001', 'Heavy duty electric drill', 1500.00, 'DAY', 5000.00, 10, 10, 'ACTIVE'),
(2, 1000, 1, 'Angle Grinder', 'GRIND001', 'Professional angle grinder', 1200.00, 'DAY', 4000.00, 8, 7, 'ACTIVE'),
(3, 1000, 2, 'Concrete Mixer', 'MIX001', 'Portable concrete mixer', 5000.00, 'DAY', 15000.00, 5, 4, 'ACTIVE'),
(4, 1000, 3, 'Pressure Washer', 'WASH001', 'High pressure cleaning machine', 2500.00, 'DAY', 8000.00, 6, 5, 'ACTIVE'),
(10, 1000, 4, 'Digital Camera', 'CAMERA001', 'Digital camera available for rental', 2000.00, 'DAY', 6000.00, 5, 4, 'ACTIVE');

# 6. CREATE CUSTOMERS (IT25104032)
INSERT INTO customer (customer_id, company_id, customer_name, email, phone, address, customer_type, customer_status, created_by) VALUES
(1, 1000, 'Kasun Jayasinghe', 'kasun@email.com', '0771234567', 'Colombo', 'INDIVIDUAL', 'ACTIVE', 3),
(2, 1000, 'Sunrise Construction Pvt Ltd', 'info@sunrise.lk', '0114567890', 'Kandy', 'BUSINESS', 'ACTIVE', 3),
(3, 1000, 'Dilshan Perera', 'dilshan@email.com', '0769876543', 'Gampaha', 'INDIVIDUAL', 'ACTIVE', 4);

# 7. CREATE CUSTOMER DOCUMENTS (IT25104032)
INSERT INTO customer_document
(document_id, customer_id, document_type, document_number, document_copy_path, expiry_date, checked_by, checked_at, notes) VALUES
(1, 1, 'NIC_ID', '200012345678', '/documents/kasun_nic.pdf', NULL, 3, '2026-09-20 09:00:00', 'NIC checked'),
(2, 2, 'OTHER', 'BR-2025-001', '/documents/sunrise_registration.pdf', NULL, 3, '2026-09-10 08:30:00', 'Business registration checked'),
(3, 3, 'DRIVING_LICENCE', 'B1234567', '/documents/dilshan_licence.pdf', '2028-05-15', 4, '2026-09-23 13:00:00', 'Licence checked');

# 8. CREATE CUSTOMER SECONDARY CONTACTS (IT25104032)
INSERT INTO customer_secondary_contact
(secondary_contact_id, customer_id, contact_name, relationship, phone_number, alternate_phone, email, address, notes) VALUES
(1, 1, 'Sunil Jayasinghe', 'Father', '0775551111', NULL, 'sunil@email.com', 'Colombo', 'Emergency secondary contact'),
(2, 2, 'Ruwan Silva', 'Manager', '0775552222', '0715552222', 'ruwan@sunrise.lk', 'Kandy', 'Company contact person'),
(3, 3, 'Nimal Perera', 'Brother', '0775553333', NULL, 'nimalp@email.com', 'Gampaha', 'Emergency secondary contact');

# 9. CREATE RENTALS (IT25104032)
INSERT INTO rental (rental_id, company_id, customer_id, created_by, rental_date, start_date, due_date, rental_status, notes) VALUES
(1, 1000, 1, 3, '2026-09-20', '2026-09-20', '2026-09-25', 'PARTIALLY_RETURNED', 'Customer rented power tools'),
(2, 1000, 2, 3, '2026-09-10', '2026-09-10', '2026-09-15', 'OVERDUE', 'Construction equipment rental'),
(3, 1000, 3, 4, '2026-09-23', '2026-09-23', '2026-09-28', 'ACTIVE', 'Cleaning equipment rental'),
(4, 1000, 1, 4, '2026-09-25', '2026-09-25', '2026-09-27', 'ACTIVE', 'Short camera rental');

# 10. CREATE RENTAL ITEMS (IT25104032)
INSERT INTO rental_item
(rental_item_id, rental_id, equipment_id, quantity, rate_per_unit, rate_period, deposit_per_unit, line_deposit, item_status, issued_at) VALUES
# Rental 1
(1, 1, 1, 2, 1500.00, 'DAY', 5000.00, 10000.00, 'SELECTED', NULL),
# Rental 2
(2, 1, 2, 1, 1200.00, 'DAY', 4000.00, 4000.00, 'PARTIALLY_RETURNED','2026-09-20 10:00:00'),
# Rental 3
(3, 2, 3, 1, 5000.00, 'DAY', 15000.00, 15000.00, 'LOST', '2026-09-10 09:00:00'),
# Rental 4
(4, 3, 4, 1, 2500.00, 'DAY', 8000.00, 8000.00, 'ISSUED', '2026-09-23 14:00:00'),
# Rental 5
(5, 4, 10, 1, 2000.00, 'DAY', 6000.00, 6000.00, 'ISSUED', '2026-09-25 11:00:00');

# 11. CREATE RENTAL EXTENSIONS (IT25104032)
INSERT INTO rental_extension (extension_id, rental_id, old_due_date, new_due_date, extension_charge, approved_by, reason) VALUES
(1, 1, '2026-09-25', '2026-09-28', 1500.00, 3, 'Customer requested three additional days'),
(2, 3, '2026-09-28', '2026-09-30', 1500.00, 3, 'Customer requested additional days');

# 12. CREATE INVOICES (IT25104036)
INSERT INTO invoice
(invoice_id, rental_id, invoice_date, due_date, subtotal, additional_charges, total_amount, amount_paid, balance_due, invoice_status) VALUES
(1, 1,'2026-09-20','2026-09-28',15000.00,2000.00,17000.00,10000.00,7000.00,'PARTIALLY_PAID'),
(2, 2,'2026-09-10','2026-09-15',25000.00,5000.00,30000.00,0.00,30000.00,'UNPAID'),
(3, 3,'2026-09-23','2026-09-28',12500.00,0.00,12500.00,12500.00,0.00,'PAID');

# 13. CREATE CHARGES (IT25104036)
INSERT INTO charge (rental_id, rental_item_id, invoice_id, charge_type, charge_description, amount, charge_date, created_by) VALUES
# ----- Invoice 1 -----
(1, 1, 1,'RENTAL','Electric drill rental charge',7500.00,'2026-09-20 10:00:00',3),
(1, 2, 1,'RENTAL','Angle grinder rental charge',6000.00,'2026-09-20 10:00:00',3),
(1, NULL, 1,'EXTENSION','Rental extension charge',1500.00,'2026-09-24 10:00:00',3),
(1, 2, 1,'DAMAGE','Damage charge for returned angle grinder',2000.00,'2026-09-24 15:30:00',3),

# ----- Invoice 2 -----
(2, 3, 2, 'RENTAL','Concrete mixer rental charge',25000.00,'2026-09-10 09:00:00',3),
(2, 3, 2,'LATE','Late return charge',5000.00,'2026-09-20 10:00:00',3),

# ----- Invoice 3 -----
(3, 4, 3,'RENTAL','Pressure washer rental charge',12500.00,'2026-09-23 14:00:00',4);


# 14. CREATE PAYMENTS (IT25104036)
INSERT INTO payment (payment_id, invoice_id, amount, payment_date, payment_method, reference_no, received_by, payment_status)VALUES
(1, 1, 10000.00, '2026-09-20 10:30:00', 'CARD', 'PAY001', 3, 'COMPLETED'),
(2, 3, 12500.00, '2026-09-23 14:30:00', 'CASH', 'PAY002', 4, 'COMPLETED');

# 15. CREATE SECURITY DEPOSITS (IT25104036)

INSERT INTO security_deposit
(deposit_id, rental_id, calculated_deposit, deposit_amount_received, amount_deducted, amount_refunded, received_date, received_by,
 deposit_status) VALUES
(1, 1, 14000.00, 14000.00, 2000.00, 0.00, '2026-09-20 10:00:00', 3, 'HELD'),
(2, 2, 15000.00, 15000.00, 15000.00, 0.00, '2026-09-10 09:00:00', 3, 'FORFEITED'),
(3, 3, 8000.00, 8000.00, 0.00, 0.00, '2026-09-23 14:00:00', 4, 'HELD');

# 16. CREATE RENTAL RETURN (IT25104066)
INSERT INTO rental_return (return_id, rental_id, return_date, processed_by, return_type, notes)VALUES
(1, 1, '2026-09-24 15:00:00', 3, 'PARTIAL', 'Customer returned the angle grinder');

# 17. CREATE RETURN ITEM (IT25104066)
INSERT INTO return_item (return_item_id, return_id, rental_item_id, qty_returned, condition_status, inspection_notes, returned_at) VALUES
(1, 1, 2, 1, 'DAMAGED', 'Angle grinder casing has visible damage', '2026-09-24 15:00:00');

 # 18. CREATE DAMAGE RECORD (IT25104066)
INSERT INTO damage_record
(damage_id, return_item_id, 
damaged_quantity, damage_description, damage_level, estimated_cost, final_charge, assessed_by, assessment_date, damage_status) VALUES
(1, 1, 1, 'Outer casing damaged during rental', 'MODERATE', 2500.00, 2000.00, 3, '2026-09-24 15:30:00', 'CHARGED');

# 19. CREATE LOST ITEM (IT25104066)
INSERT INTO lost_item
(lost_item_id, rental_item_id, quantity_lost, loss_type, reported_date, reason, replacement_cost_per_unit, charge_amount, 
reported_by, lost_status, notes) VALUES
(1, 3, 1, 'NON_RETURNED', '2026-09-20 10:00:00', 'Customer has not returned the equipment', 75000.00, 75000.00, 3, 'CHARGED', 
'Follow-up required');

# 20. CREATE SETTLEMENT (IT25104066)
INSERT INTO settlement
(settlement_id, rental_id, rental_charges, late_charges,
damage_charges, lost_item_charges, total_charges, deposit_used, deposit_refunded, final_balance, settled_at, settled_by, settlement_status)
VALUES
(1, 1, 13500.00, 0.00, 2000.00, 0.00, 15500.00, 2000.00, 12000.00, 13500.00, NULL, 3, 'PENDING');


# ==================== SQL queries =============================

# ============== MODULE 1 ===================

#IT25104048
#1. Check all companies
SELECT * FROM company;

#IT25104048
#2. Check all users 
SELECT * FROM sys_user;

#IT25104048
#3. company — UPDATE queries 
UPDATE company
SET
    company_name = 'ABC Equipment Rental Services',
    email = 'contact@abc.lk',
    phone = '0711111111',
    address = 'Colombo 03'
WHERE company_id = 1000;

#IT25104048
#4. update registration number 
UPDATE company SET registration_no = 'BR789456' WHERE company_id = 1000;

#IT25104048
#5. Deactivate a company 
UPDATE company SET company_status = 'INACTIVE' WHERE company_id = 1000;


#IT25104048
#6. Search company by name 
SELECT * FROM company WHERE company_name LIKE '%ABC%';

#IT25104048
#7. Search by registration number 
SELECT * FROM company WHERE registration_no = 'BR789456';

#IT25104048
#8. Show company with the admin who registered it 
SELECT * FROM company JOIN sys_user ON company.registered_by = sys_user.user_id;
 
#IT25104048
#9. Update user's name and phone 
UPDATE sys_user SET full_name = 'Kamal Fernando', phone = '0779999999' WHERE user_id = 2;

#IT25104048
#10. Change email 
UPDATE sys_user SET email = 'kamal.new@abc.lk' WHERE user_id = 2;

#IT25104048
#11. Change user role 
UPDATE sys_user SET user_role = 'RENTAL_OFFICER' WHERE user_id = 2;

#IT25104048
#12. Activate user 
UPDATE sys_user SET user_status = 'ACTIVE' WHERE user_id = 2;

#IT25104048
#13. Deactivate user
UPDATE sys_user SET user_status = 'INACTIVE' WHERE user_id = 2;

#IT25104048
#14. Find a user by email 
SELECT * FROM sys_user WHERE email = 'kamal@abc.lk';

#IT25104048
#15. Show all users belonging to company with company_id = 1000
SELECT user_id, full_name, email, phone, user_role, user_status, created_at 
FROM sys_user 
WHERE company_id = 1000;

#IT25104048
#16. Show active users 
SELECT user_id, full_name, email, phone, user_role
FROM sys_user
WHERE company_id = 1000 AND user_status = 'ACTIVE';
  
#IT25104048
#17. Show Company Admins 
SELECT user_id, full_name, email, phone
FROM sys_user
WHERE company_id = 1000 AND user_role = 'COMPANY_ADMIN' AND user_status = 'ACTIVE';
  
#IT25104048
#18. Show Rental Officers 
SELECT user_id, full_name, email, phone 
FROM sys_user
WHERE company_id = 1000 AND user_role = 'RENTAL_OFFICER' AND user_status = 'ACTIVE';
  
#IT25104048
#19. Count staff by role 
SELECT user_role, COUNT(*) AS user_count
FROM sys_user
WHERE company_id = 1000 AND user_status = 'ACTIVE' GROUP BY user_role;

#IT25104048
#20. Update category name 
UPDATE equipment_category SET category_name = 'Professional Power Tools' WHERE category_id = 1;

#IT25104048
#21. Update description 
UPDATE equipment_category SET cat_description = 'Professional drills, grinders and related power tools' WHERE category_id = 1;

#IT25104048
#22. Update both 
UPDATE equipment_category
SET category_name = 'Professional Power Tools', cat_description = 'Professional drills, grinders and related power tools'
WHERE category_id = 1;


# ========= MODULE 2 ===============

#IT25104032
#1. Search customer by name/phone
SELECT customer_id, customer_name, email, phone, address, customer_type, customer_status, created_at
FROM customer c
WHERE company_id = 1000 AND (c.customer_name LIKE '%Kasun%' OR phone = '0771234567')
ORDER BY customer_name;


#IT25104032
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
  AND c.company_id = 1000;


#IT25104032
#3. Update Customer
UPDATE customer
SET
    customer_name = 'John Perera',
    email = 'john.new@gmail.com',
    phone = '0779999999',
    address = '50 Main Street, Colombo',
    customer_type = 'INDIVIDUAL'
WHERE customer_id = 1 AND company_id = 1000;
  
  
#IT25104032
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
JOIN customer c
    ON cd.customer_id = c.customer_id
WHERE cd.customer_id = 1
  AND c.company_id = 1000
ORDER BY cd.checked_at DESC;


#IT25104032
#5. Update identification document
UPDATE customer_document cd
JOIN customer c
    ON cd.customer_id = c.customer_id
SET
    cd.document_type = 'DRIVING_LICENCE',
    cd.document_number = 'B1234567',
    cd.document_copy_path = '/documents/customers/1/license.pdf',
    cd.expiry_date = '2030-08-15',
    cd.checked_by = 5,
    cd.checked_at = NOW(),
    cd.notes = 'Updated identification document'
WHERE cd.document_id = 1
  AND cd.customer_id = 1
  AND c.company_id = 1000;
  
  
#IT25104032
#6. View customer's secondary contact
SELECT
    s.secondary_contact_id,
    s.customer_id,
    s.contact_name,
    s.relationship,
    s.phone_number,
    s.alternate_phone,
    s.email,
    s.address,
    s.notes
FROM customer_secondary_contact s
JOIN customer c
    ON s.customer_id = c.customer_id
WHERE s.customer_id = 1
  AND c.company_id = 1000;


#IT25104032
#7. Update customer's secondary contact
UPDATE customer_secondary_contact s
JOIN customer c
    ON s.customer_id = c.customer_id
SET
    s.contact_name = 'Sunil Jayasinghe',
    s.relationship = 'Father',
    s.phone_number = '0775551111',
    s.email = 'sunil.new@email.com',
    s.address = 'Colombo',
    s.notes = 'Updated secondary contact'
WHERE s.customer_id = 1
  AND c.company_id = 1000;


#IT25104032
#8. View customer's rental history
SELECT
    r.rental_id,
    r.rental_date,
    r.start_date,
    r.due_date,
    r.rental_status,
    r.notes
FROM rental r
WHERE r.customer_id = 1 AND r.company_id = 1000
ORDER BY r.rental_date DESC;


#IT25104032
#9. View customer's rental history with equipment
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
WHERE r.customer_id = 1 AND r.company_id = 1000 AND e.company_id = r.company_id
ORDER BY r.start_date DESC;

  
#IT25104032
#10. View customer's current active rentals
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
  AND r.company_id = 1000
  AND e.company_id = r.company_id
  AND r.rental_status IN ('ACTIVE', 'OVERDUE','PARTIALLY_RETURNED')
ORDER BY r.due_date;

  
#IT25104032
#11. View available equipment
SELECT
    e.equipment_id,
    e.item_name,
    e.item_code,
    ec.category_name,
    e.equ_description,
    e.rental_rate,
    e.rate_period,
    e.security_deposit_per_unit,
    e.available_quantity
FROM equipment e
JOIN equipment_category ec
    ON e.category_id = ec.category_id
WHERE e.company_id = 1000 AND e.equ_status = 'ACTIVE' AND e.available_quantity > 0
ORDER BY ec.category_name, e.item_name;


#IT25104032
#12. Search available equipment
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
WHERE e.company_id = 1000
  AND e.equ_status = 'ACTIVE'
  AND e.available_quantity > 0
  AND (e.item_name LIKE '%camera%' OR e.item_code LIKE '%camera%' OR ec.category_name LIKE '%camera%')
ORDER BY e.item_name;

#IT25104032
#13. Validate requested quantity
#Example : customer requests 3 units of equipment 10
SELECT equipment_id, item_name, available_quantity,
    CASE
        WHEN available_quantity >= 3
            THEN 'AVAILABLE'
        ELSE 'INSUFFICIENT_QUANTITY'
    END AS availability_result
FROM equipment
WHERE equipment_id = 10 AND company_id = 1000 AND equ_status = 'ACTIVE';
  

#IT25104032
#14. View rental details
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
WHERE r.rental_id = 1 AND r.company_id = 1000 AND c.company_id = r.company_id;
  
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
JOIN rental r
    ON ri.rental_id = r.rental_id
JOIN equipment e
    ON ri.equipment_id = e.equipment_id
WHERE ri.rental_id = 1
  AND r.company_id = 1000
  AND e.company_id = r.company_id;
  
  
#IT25104032
#15. Calculate security deposit
# Module 2 calculates the security (refundable) deposit from rental_items.
# The deposits table belongs to Module 3, which records the calculated and collected deposit.
SELECT
    rental_id,
    SUM(line_deposit) AS calculated_deposit
FROM rental_item
WHERE rental_id = 1
GROUP BY rental_id;


#IT25104032
#16. Record equipment issue
START TRANSACTION;

# 16.1 Lock the rental item and obtain the actual issue quantity.
SELECT
    ri.rental_item_id,
    ri.rental_id,
    ri.equipment_id,
    ri.quantity,
    ri.item_status
FROM rental_item ri
JOIN rental r
    ON ri.rental_id = r.rental_id
WHERE ri.rental_item_id = 1
  AND r.company_id = 1000
  AND ri.item_status = 'SELECTED'
FOR UPDATE;


# 16.2 Check equipment availability before issuing.
SELECT
    e.equipment_id,
    e.item_name,
    e.available_quantity,
    ri.quantity AS requested_quantity,
    CASE
        WHEN e.available_quantity >= ri.quantity
            THEN 'AVAILABLE'
        ELSE 'INSUFFICIENT_QUANTITY'
    END AS availability_result
FROM equipment e
JOIN rental_item ri
    ON e.equipment_id = ri.equipment_id
JOIN rental r
    ON ri.rental_id = r.rental_id
WHERE ri.rental_item_id = 1
  AND r.company_id = 1000
  AND e.company_id = r.company_id
  AND ri.item_status = 'SELECTED';


# 16.3 Reduce equipment availability using the actual rental quantity.
UPDATE equipment e
JOIN rental_item ri
    ON e.equipment_id = ri.equipment_id
JOIN rental r
    ON ri.rental_id = r.rental_id
SET
    e.available_quantity = e.available_quantity - ri.quantity
WHERE ri.rental_item_id = 1
  AND r.company_id = 1000
  AND e.company_id = r.company_id
  AND ri.item_status = 'SELECTED'
  AND e.equ_status = 'ACTIVE'
  AND e.available_quantity >= ri.quantity;


# 16.4 Mark the rental item as issued.
UPDATE rental_item ri
JOIN rental r
    ON ri.rental_id = r.rental_id
SET
    ri.item_status = 'ISSUED',
    ri.issued_at = NOW()
WHERE ri.rental_item_id = 1
  AND r.company_id = 1000
  AND ri.item_status = 'SELECTED';


# 16.5 Mark the rental as active.
UPDATE rental
SET rental_status = 'ACTIVE'
WHERE rental_id = (
    SELECT rental_id
    FROM (
        SELECT rental_id
        FROM rental_item
        WHERE rental_item_id = 1
    ) AS x
)
AND company_id = 1000;


COMMIT;

#IT25104032
#16.6 Check equipment availability after issue
SELECT
    e.equipment_id,
    e.item_name,
    e.total_quantity,
    e.available_quantity,
    e.equ_status
FROM equipment e
WHERE e.equipment_id = 1
  AND e.company_id = 1000;

#IT25104032
#17. Extend rental
START TRANSACTION;

#Example: extend rental 3 from 2026-09-28 to 2026-09-30.
#In the application, the new date and charge should come from user input.

#IT25104032
SELECT
    rental_id,
    due_date,
    '2026-09-30',
    1500.00,
    3,
    'Customer requested additional days'
FROM rental
WHERE rental_id = 3
  AND company_id = 1000
  AND '2026-09-30' > due_date;

#IT25104032
-- Update the rental's current due date.
UPDATE rental
SET due_date = '2026-09-30'
WHERE rental_id = 3
  AND company_id = 1000
  AND '2026-09-30' > due_date;

#IT25104032
COMMIT;
  
#IT25104032  
#18. View rental extension history
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
JOIN rental r
    ON re.rental_id = r.rental_id
JOIN sys_user u
    ON re.approved_by = u.user_id
WHERE re.rental_id = 3
  AND r.company_id = 1000
ORDER BY re.created_at DESC;


#IT25104032
#19. View all active rentals
SELECT
    r.rental_id,
    c.customer_name,
    r.start_date,
    r.due_date,
    r.rental_status
FROM rental r
JOIN customer c
    ON r.customer_id = c.customer_id
WHERE r.company_id = 1000 AND (c.company_id = r.company_id)
  AND r.rental_status IN (
      'ACTIVE',
      'PARTIALLY_RETURNED'
  )
ORDER BY r.due_date;


#IT25104032
#20. Find overdue rentals
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
WHERE r.company_id = 1000
	AND c.company_id = r.company_id
    AND r.due_date < CURDATE()
    AND r.rental_status IN ('ACTIVE', 'PARTIALLY_RETURNED', 'OVERDUE')
ORDER BY r.due_date;


#IT25104032
#21. Rentals due soon
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
WHERE r.company_id = 1000
  AND c.company_id = r.company_id
  AND r.due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 3 DAY) AND r.rental_status IN ('ACTIVE', 'PARTIALLY_RETURNED')
ORDER BY r.due_date;


# =========== MODULE 3 =============

#IT25104036
#1. View All Invoices and Their Balances
SELECT  invoice_id, rental_id, total_amount, amount_paid, balance_due, invoice_status FROM invoice;


#IT25104036
#2. Identify Unpaid or Partially Paid Invoices
SELECT invoice_id, rental_id, due_date, total_amount, balance_due, invoice_status FROM invoice 
WHERE invoice_status IN ('UNPAID', 'PARTIALLY_PAID')
ORDER BY due_date ASC;

#IT25104036
#3. Summarize Charges by Charge Type
SELECT charge_type, COUNT(charge_id) AS total_incidents, SUM(amount) AS total_value FROM charge 
GROUP BY charge_type ORDER BY total_value DESC;

#IT25104036
#4. Itemized Charge Breakdown for a Specific Invoice
SELECT charge_id, charge_type, charge_description, amount, charge_date FROM charge 
WHERE invoice_id = 1 
ORDER BY charge_date ASC;

#IT25104036
#5. Check Invoices with Additional Non-Rental Charges
SELECT invoice_id, rental_id, subtotal, additional_charges, total_amount FROM invoice 
WHERE additional_charges > 0;


#IT25104036
#6. Payment History by Payment Method
SELECT payment_method, COUNT(payment_id) AS transaction_count, SUM(amount) AS total_collected FROM payment 
WHERE payment_status = 'COMPLETED' GROUP BY payment_method;


#IT25104036
#7. View All Payments for a Specific Invoice
SELECT payment_id, payment_method, reference_no, amount, payment_date FROM payment 
WHERE invoice_id = 1;

#IT25104036
#8. Total Revenue vs. Outstanding Receivables
SELECT SUM(amount_paid) AS total_revenue_collected, SUM(balance_due) AS total_outstanding_receivables FROM invoice;

#IT25104036
#9. Current Status of All Security Deposits
SELECT rental_id, deposit_amount_received, amount_deducted, amount_refunded, deposit_status FROM security_deposit;

#IT25104036
#10. Identify Forfeited Security Deposits
SELECT deposit_id, rental_id, deposit_amount_received, amount_deducted, deposit_status FROM security_deposit 
WHERE deposit_status = 'FORFEITED';

#IT25104036
#11. Calculate Net Held Deposit Liability
SELECT COUNT(deposit_id) AS active_deposits, SUM(deposit_amount_received - amount_deducted - amount_refunded) AS total_held_liability 
FROM security_deposit WHERE deposit_status = 'HELD';


#IT25104036
#12. Compare Total Invoice Charges vs Calculated Charges
SELECT i.invoice_id, i.total_amount AS invoice_header_total, SUM(c.amount) AS sum_of_line_charges,
(i.total_amount - SUM(c.amount)) AS discrepancy FROM invoice i
LEFT JOIN charge c ON i.invoice_id = c.invoice_id
GROUP BY i.invoice_id, i.total_amount;

#IT25104036
#13. Compare Invoice Amount Paid vs Actual Payments Recorded
SELECT i.invoice_id, i.amount_paid AS recorded_amount_paid, 
COALESCE(SUM(p.amount), 0) AS actual_receipts_total 
FROM invoice i
LEFT JOIN payment p ON i.invoice_id = p.invoice_id AND p.payment_status = 'COMPLETED'
GROUP BY i.invoice_id, i.amount_paid;


#IT25104036
#14. Find Overdue Invoices
SELECT invoice_id, rental_id, due_date, balance_due, DATEDIFF(CURRENT_DATE, due_date) AS days_overdue 
FROM invoice 
WHERE invoice_status IN ('UNPAID', 'PARTIALLY_PAID') AND due_date < CURRENT_DATE;
  
#IT25104036
#15. Staff Collection Performance
SELECT received_by AS staff_user_id, COUNT(payment_id) AS payments_processed, SUM(amount) AS total_value_collected FROM payment 
WHERE payment_status = 'COMPLETED' GROUP BY received_by;

UPDATE invoice
SET invoice_status = CASE
	WHEN amount_paid = 0 THEN 'UNPAID'
    WHEN amount_paid > 0 AND amount_paid < total_amount THEN 'PARTIALLY_PAID'
    WHEN amount_paid >= total_amount THEN 'PAID'
    ELSE invoice_status
END
WHERE invoice_id = 1;

# =========== MODULE 4 =============
#IT25104066
#1. Check all rental returns
SELECT * FROM rental_return;

#IT25104066
#2. Useful for rental_return history screen 
SELECT * FROM rental_return
JOIN return_item ON rental_return.return_id = return_item.return_id;

#IT25104066
#3. To check damaged items
SELECT return_item_id, rental_item_id, qty_returned, condition_status, inspection_notes
FROM return_item WHERE condition_status = 'DAMAGED';

#IT25104066
#4. Damage record details screen
SELECT * FROM return_item
JOIN damage_record ON return_item.return_item_id = damage_record.return_item_id;

#IT25104066
#5. Total damage charges
SELECT SUM(final_charge) AS total_damage_charges FROM damage_record;

#IT25104066
#6. Total lost-item charges
SELECT SUM(charge_amount) AS total_lost_item_charges FROM lost_item;

#IT25104066
#7. Count lost items by type
SELECT loss_type, SUM(quantity_lost) AS total_quantity FROM lost_item GROUP BY loss_type;

#IT25104066
#8. Pending settlements
SELECT settlement_id, rental_id, total_charges, deposit_used, deposit_refunded, final_balance, settlement_status
FROM settlement WHERE settlement_status = 'PENDING';

#IT25104066
#9. Settlement summary
SELECT * FROM settlement ORDER BY settlement_id DESC;

#IT25104066
#10. Total quantity returned for each rental item
SELECT rental_item_id, SUM(qty_returned) AS total_returned FROM return_item GROUP BY rental_item_id;

#IT25104066
#11. Severe damage cases 
SELECT damage_id, return_item_id, damaged_quantity, damage_description, estimated_cost, final_charge, damage_status
FROM damage_record WHERE damage_level = 'MODERATE';

#IT25104066
#12. Unresolved lost/non-returned items
SELECT lost_item_id, rental_item_id, quantity_lost, loss_type, reported_date, reason, charge_amount, lost_status
FROM lost_item WHERE lost_status = 'CHARGED';

#IT25104066
#13. View returns with customer details
SELECT rr.return_id, r.rental_id, c.customer_name, rr.return_date, rr.return_type FROM rental_return rr
JOIN rental r ON rr.rental_id = r.rental_id JOIN customer c ON r.customer_id = c.customer_id;


#IT25104066
#14. View equipment that was returned
SELECT c.customer_name, e.item_name, ri.qty_returned, ri.condition_status, ri.inspection_notes, rr.return_date
FROM rental_return rr
JOIN rental r ON rr.rental_id = r.rental_id
JOIN customer c ON r.customer_id = c.customer_id
JOIN return_item ri ON rr.return_id = ri.return_id
JOIN rental_item ritem ON ri.rental_item_id = ritem.rental_item_id
JOIN equipment e ON ritem.equipment_id = e.equipment_id;

#IT25104066
#15. Complete damage report
SELECT c.customer_name, e.item_name, dr.damaged_quantity, dr.damage_description, dr.damage_level, dr.estimated_cost,
dr.final_charge, dr.damage_status
FROM damage_record dr
JOIN return_item ri ON dr.return_item_id = ri.return_item_id
JOIN rental_item ritem ON ri.rental_item_id = ritem.rental_item_id
JOIN rental r ON ritem.rental_id = r.rental_id
JOIN customer c ON r.customer_id = c.customer_id
JOIN equipment e ON ritem.equipment_id = e.equipment_id;
    
#IT25104066
#16. View lost/non-returned equipment with customer
SELECT
    c.customer_name,
    e.item_name,
    li.quantity_lost,
    li.loss_type,
    li.reason,
    li.replacement_cost_per_unit,
    li.charge_amount,
    li.lost_status
FROM lost_item li
JOIN rental_item ri ON li.rental_item_id = ri.rental_item_id
JOIN rental r ON ri.rental_id = r.rental_id
JOIN customer c ON r.customer_id = c.customer_id
JOIN equipment e ON ri.equipment_id = e.equipment_id;
    
    
#IT25104066
#17. View staff member who processed each return
SELECT rr.return_id, rr.rental_id, rr.return_date, u.full_name AS processed_by FROM rental_return rr
JOIN sys_user u ON rr.processed_by = u.user_id;
    
    
#IT25104066
#18. View staff member who assessed damage
SELECT dr.damage_id, dr.damage_description, dr.damage_level, dr.final_charge, u.full_name AS assessed_by
FROM damage_record dr
JOIN sys_user u ON dr.assessed_by = u.user_id;
    

#IT25104066
#19. Settlement with customer details
SELECT
    s.settlement_id,
    c.customer_name,
    s.rental_charges,
    s.late_charges,
    s.damage_charges,
    s.lost_item_charges,
    s.total_charges,
    s.deposit_used,
    s.deposit_refunded,
    s.final_balance,
    s.settlement_status
FROM settlement s
JOIN rental r ON s.rental_id = r.rental_id
JOIN customer c ON r.customer_id = c.customer_id;
    

#IT25104066
#20. Compare settlement with security deposit
SELECT
    s.rental_id,
    sd.deposit_amount_received,
    sd.amount_deducted,
    sd.amount_refunded,
    sd.deposit_status,
    s.deposit_used,
    s.deposit_refunded,
    s.final_balance,
    s.settlement_status
FROM settlement s
JOIN security_deposit sd ON s.rental_id = sd.rental_id;
    

#IT25104066
#21. Test recovering a lost item
UPDATE lost_item SET lost_status = 'RECOVERED' WHERE lost_item_id = 1;

#IT25104066
#22.Check all lost items with lost_item_id = 1
SELECT * FROM lost_item WHERE lost_item_id = 1;

