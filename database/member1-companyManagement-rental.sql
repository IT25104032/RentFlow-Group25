drop database module1_test;
CREATE DATABASE module1_test;
USE module1_test;

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
)auto_increment = 1001;


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


ALTER TABLE company
ADD CONSTRAINT fk_companies_registered_by
    FOREIGN KEY (registered_by)
    REFERENCES sys_user(user_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;
        
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

SHOW TABLES;
 
#=========Inserting data to tables============

-- 1. Create Compulin Admin
INSERT INTO sys_user
(company_id, full_name, email, password_hash, phone,
 user_role, user_status)
VALUES
(NULL,
 'Compulin Admin',
 'admin@compulin.com',
 'test_hash_1',
 '0771111111',
 'COMPULIN_ADMIN',
 'ACTIVE');
 
 SELECT * FROM sys_user;

INSERT INTO company
(company_name, registration_no, email, phone, address, registered_by)
VALUES
('ABC Equipment Rentals', 'REG-001', 'info@abcrentals.com',
 '0112345678', 'Colombo', 1);
 
SELECT * from company;
 
 
INSERT INTO sys_user
(company_id, full_name, email, password_hash, phone, user_role)
VALUES

(1001, 'Nimal Perera', 'nimal@abcrentals.com',
 'test_hash_2', '0772222222', 'COMPANY_ADMIN'),

(1001, 'Kamal Silva', 'kamal@abcrentals.com',
 'test_hash_3', '0773333333', 'RENTAL_OFFICER'),

(1001, 'Amal Fernando', 'amal@abcrentals.com',
 'test_hash_4', '0774444444', 'RENTAL_OFFICER');
 
 
INSERT INTO equipment_category
(company_id, category_name, cat_description)
VALUES
(1001, 'Power Tools', 'Electric and battery powered tools'),
(1001, 'Construction Equipment', 'Equipment used for construction work'),
(1001, 'Cleaning Equipment', 'Commercial cleaning machines');


INSERT INTO equipment
(company_id, category_id, item_name, item_code,
 equ_description, rental_rate, rate_period,
 refundable_deposit_per_unit,
 total_quantity, available_quantity)
VALUES

(1001, 1, 'Electric Drill', 'DRILL001',
 'Heavy duty electric drill',
 1500.00, 'DAY',
 5000.00, 10, 8),
  
(1001, 1, 'Angle Grinder', 'GRIND001',
 'Professional angle grinder',
 1200.00, 'DAY',
 4000.00, 8, 6),

(1001, 2, 'Concrete Mixer', 'MIX001',
 'Portable concrete mixer',
 5000.00, 'DAY',
 15000.00, 5, 4),

(1001, 3, 'Pressure Washer', 'WASH001',
 'High pressure cleaning machine',
 2500.00, 'DAY',
 8000.00, 6, 5);
