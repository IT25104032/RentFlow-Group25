DROP DATABASE IF EXISTS compulin_rental_db;
CREATE DATABASE compulin_rental_db;
USE compulin_rental_db;

CREATE TABLE companies (
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
);

CREATE TABLE users (
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
        REFERENCES companies(company_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_user_role
        CHECK (
            role IN (
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

RENAME TABLE users TO sys_user;

ALTER TABLE sys_user
CHANGE COLUMN role user_role VARCHAR(30) NOT NULL;

ALTER TABLE companies
ADD CONSTRAINT fk_companies_registered_by
    FOREIGN KEY (registered_by)
    REFERENCES users(user_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;
    
RENAME TABLE companies TO company;
    
    CREATE TABLE equipment_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    cat_description VARCHAR(255),
    cat_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_categories_company
        FOREIGN KEY (company_id)
        REFERENCES companies(company_id)
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

RENAME TABLE equipment_categories TO equipment_category;

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
        REFERENCES companies(company_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_equipment_category
        FOREIGN KEY (category_id)
        REFERENCES equipment_categories(category_id)
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

## Select the database ##
USE compulin_rental_db;

## 2. Check whether the Compulin Admin already exists ##
SELECT *
FROM sys_user;

## 3. Insert the Compulin Admin first ##
INSERT INTO sys_user
(
    company_id,full_name,email,password_hash,phone,user_role,user_status
)
VALUES
(
    NULL,'Compulin Admin','admin@compulin.lk','TEMPORARY_HASH','0771234567','COMPULIN_ADMIN','ACTIVE'
);

SELECT *
FROM sys_user;

## 4. Insert the company ##
INSERT INTO company
(
    company_name,registration_no,email,phone,address,registered_by,company_status
)
VALUES
(
    'ABC Equipment Rentals','BR123456','info@abc.lk','0712345678','Colombo',1,'ACTIVE'
);

SELECT *
FROM company;

## 5. Check all users ##
SELECT *
FROM sys_user;

## 6. company — UPDATE queries ##
UPDATE company
SET
    company_name = 'ABC Equipment Rental Services',
    email = 'contact@abc.lk',
    phone = '0711111111',
    address = 'Colombo 03'
WHERE company_id = 1;

## update registration number ##
UPDATE company
SET registration_no = 'BR789456'
WHERE company_id = 1;

## Suspend a company ##
UPDATE company
SET company_status = 'SUSPENDED'
WHERE company_id = 1;

## Reactivate a company ##
UPDATE company
SET company_status = 'ACTIVE'
WHERE company_id = 1;

## Make company inactive ##
UPDATE company
SET company_status = 'INACTIVE'
WHERE company_id = 1;

##__2. company — SELECT/search queries__##

## Show all companies ##
SELECT *
FROM company;

## Show active companies ##
SELECT *
FROM company
WHERE company_status = 'ACTIVE';

## Search company by name ##
SELECT *
FROM company
WHERE company_name LIKE '%ABC%';

## Search by registration number ##
SELECT *
FROM company
WHERE registration_no = 'BR123456';

## Show company with the admin who registered it ##
SELECT
    c.company_id,
    c.company_name,
    c.registration_no,
    c.email,
    c.phone,
    c.address,
    u.full_name AS registered_by_user,
    c.registration_date,
    c.company_status
FROM company c
JOIN sys_user u
    ON c.registered_by = u.user_id;
 
 ##__3. sys_user — UPDATE queries__##
 
## Update user's name and phone ##
UPDATE sys_user
SET
    full_name = 'Kamal Fernando',
    phone = '0779999999'
WHERE user_id = 2;

 ## Change email ##
 UPDATE sys_user
SET email = 'kamal.new@abc.lk'
WHERE user_id = 2;

## Change user role ##
UPDATE sys_user
SET user_role = 'RENTAL_OFFICER'
WHERE user_id = 2;

## Activate user ##
UPDATE sys_user
SET user_status = 'ACTIVE'
WHERE user_id = 2;

## Deactivate user ##
UPDATE sys_user
SET user_status = 'INACTIVE'
WHERE user_id = 2;

##___4. sys_user — SEARCH queries__##

## Find a user by email ##
SELECT *
FROM sys_user
WHERE email = 'kamal@abc.lk';

## Show all users ##
SELECT *
FROM sys_user;

## Show all users belonging to company 1 ##
SELECT
    user_id,
    full_name,
    email,
    phone,
    user_role,
    user_status,
    created_at
FROM sys_user
WHERE company_id = 1;

## Show active users ##
SELECT
    user_id,
    full_name,
    email,
    phone,
    user_role
FROM sys_user
WHERE company_id = 1
  AND user_status = 'ACTIVE';
  
## Show Company Admins ##
SELECT
    user_id,
    full_name,
    email,
    phone
FROM sys_user
WHERE company_id = 1
  AND user_role = 'COMPANY_ADMIN'
  AND user_status = 'ACTIVE';
  
## Show Rental Officers ##
SELECT
    user_id,
    full_name,
    email,
    phone
FROM sys_user
WHERE company_id = 1
  AND user_role = 'RENTAL_OFFICER'
  AND user_status = 'ACTIVE';
  
## Count staff by role ##
SELECT
    user_role,
    COUNT(*) AS user_count
FROM sys_user
WHERE company_id = 1
  AND user_status = 'ACTIVE'
GROUP BY user_role;

##__5. equipment_category — UPDATE queries__##

## Update category name ##
UPDATE equipment_category
SET category_name = 'Professional Power Tools'
WHERE category_id = 1;

## Update description ##
UPDATE equipment_category
SET cat_description =
    'Professional drills, grinders and related power tools'
WHERE category_id = 1;

## Update both ##
UPDATE equipment_category
SET
    category_name = 'Professional Power Tools',
    cat_description =
        'Professional drills, grinders and related power tools'
WHERE category_id = 1;

## Deactivate category ##
UPDATE equipment_category
SET cat_status = 'INACTIVE'
WHERE category_id = 1;

## Activate category ##
UPDATE equipment_category
SET cat_status = 'ACTIVE'
WHERE category_id = 1;




 
 














    

