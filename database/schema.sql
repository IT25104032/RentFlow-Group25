CREATE DATABASE IF NOT EXISTS rentflow_db;
USE rentflow_db;

# ============== MODULE 1 TABLES ================

#IT25104048
CREATE TABLE company (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    registration_no VARCHAR(60) NULL UNIQUE,
    email VARCHAR(120) NOT NULL,
    phone VARCHAR(25),
    address VARCHAR(255),
    registered_by INT NOT NULL,
    registration_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    company_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT chk_company_status
        CHECK (company_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
        
) AUTO_INCREMENT = 1001; 


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
    item_code VARCHAR(80) NULL,
    equ_description VARCHAR(500),
    rental_rate DECIMAL(12,2) NOT NULL,
    rate_period VARCHAR(20) NOT NULL,
    refundable_deposit_per_unit DECIMAL(12,2) NOT NULL DEFAULT 0,
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
        CHECK (refundable_deposit_per_unit >= 0),

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
    invoice_status varchar(25) NOT NULL CHECK (invoice_status IN ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')),
    PRIMARY KEY (invoice_id),
    CONSTRAINT FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
    );

#IT25104036
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
    CONSTRAINT FOREIGN KEY (rental_id) REFERENCES rental(rental_id),
    CONSTRAINT FOREIGN KEY (rental_item_id) REFERENCES rental_item(rental_item_id),
    CONSTRAINT FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id),
    CONSTRAINT FOREIGN KEY (created_by) REFERENCES sys_user(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
    );

#IT25104036
CREATE TABLE payment(
	payment_id INT AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date DATETIME NOT NULL,
    payment_method VARCHAR(30) NOT NULL CHECK (payment_method IN ('CASH', 'CARD', 'BANK_TRANSFER')),
    reference_no VARCHAR(100),
    received_by INT NOT NULL,
    payment_status VARCHAR(20) NOT NULL CHECK (payment_status IN ('COMPLETED', 'VOIDED')),
    PRIMARY KEY (payment_id),
    CONSTRAINT FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id),
    CONSTRAINT FOREIGN KEY (received_by) REFERENCES sys_user(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
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
    refund_date DATETIME NULL,
    received_by INT,
    deposit_status VARCHAR(25) NOT NULL CHECK (deposit_status IN ('PENDING', 'HELD', 'PARTIALLY_REFUNDED',
'REFUNDED', 'FORFEITED')),
	PRIMARY KEY(deposit_id),
    CONSTRAINT FOREIGN KEY (rental_id) REFERENCES rental(rental_id),
    CONSTRAINT FOREIGN KEY (received_by) REFERENCES sys_user(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
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

