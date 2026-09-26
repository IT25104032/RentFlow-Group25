#=========Inserting data to tables============
INSERT INTO company
(company_name, registration_no, email, phone, address, registered_by)
VALUES
('ABC Equipment Rentals', 'REG-001', 'info@abcrentals.com',
 '0112345678', 'Colombo', 1);
 
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
 

 INSERT INTO customer
(company_id, customer_name, email, phone,
 address, customer_type, customer_status, created_by)
VALUES

(1001, 'Kasun Jayasinghe', 'kasun@email.com',
 '0711234567', 'Colombo',
 'INDIVIDUAL', 'ACTIVE', 3),

(1001, 'Sunrise Construction Pvt Ltd', 'info@sunrise.lk',
 '0114567890', 'Kandy',
 'BUSINESS', 'ACTIVE', 3),

(1001, 'Dilshan Perera', 'dilshan@email.com',
 '0769876543', 'Gampaha',
 'INDIVIDUAL', 'ACTIVE', 4);
 
 INSERT INTO customer_document
(customer_id, document_type, document_number,
 document_copy_path, expiry_date, checked_by, notes)
VALUES

(1, 'NIC_ID', '200012345678',
 '/documents/kasun_nic.pdf',
 NULL, 3, 'NIC checked'),

(2, 'OTHER', 'BR-2025-001',
 '/documents/sunrise_registration.pdf',
 NULL, 3, 'Business registration checked'),

(3, 'DRIVING_LICENCE', 'B1234567',
 '/documents/dilshan_licence.pdf',
 '2028-05-15', 4, 'Licence checked');
 
 INSERT INTO customer_secondary_contact
(customer_id, contact_name, relationship,
 phone_number, alternate_phone, email, address)
VALUES

(1, 'Sunil Jayasinghe', 'Father',
 '0775551111', NULL,
 'sunil@email.com', 'Colombo'),

(2, 'Ruwan Silva', 'Manager',
 '0775552222', '0715552222',
 'ruwan@sunrise.lk', 'Kandy'),

(3, 'Nimal Perera', 'Brother',
 '0775553333', NULL,
 'nimalp@email.com', 'Gampaha');
 
 INSERT INTO rental
(company_id, customer_id, created_by,
 start_date, due_date, rental_status, notes)
VALUES

(1001, 1, 3,
 '2026-09-20', '2026-09-25',
 'PARTIALLY_RETURNED',
 'Customer rented power tools'),

(1001, 2, 3,
 '2026-09-10', '2026-09-15',
 'OVERDUE',
 'Construction equipment rental'),

(1001, 3, 4,
 '2026-09-23', '2026-09-28',
 'ACTIVE',
 'Cleaning equipment rental');
 
 INSERT INTO rental_item
(rental_id, equipment_id, quantity,
 rate_per_unit, rate_period,
 deposit_per_unit, line_deposit,
 item_status, issued_at)
VALUES

#Rental 1
(1, 1, 2,
 1500.00, 'DAY',
 5000.00, 10000.00,
 'PARTIALLY_RETURNED', '2026-09-20 10:00:00'),

(1, 2, 1,
 1200.00, 'DAY',
 4000.00, 4000.00,
 'ISSUED', '2026-09-20 10:00:00'),

#Rental 2
(2, 3, 1,
 5000.00, 'DAY',
 15000.00, 15000.00,
 'LOST', '2026-09-10 09:00:00'),

#Rental 3
(3, 4, 1,
 2500.00, 'DAY',
 8000.00, 8000.00,
 'ISSUED', '2026-09-23 14:00:00');
 
 
 INSERT INTO rental_extension
(rental_id, old_due_date, new_due_date,
 extension_charge, approved_by, reason)
VALUES

(1, '2026-09-25', '2026-09-28',
 1500.00, 3,
 'Customer requested three additional days');
 
 
 INSERT INTO invoice
(rental_id, invoice_date, due_date,
 subtotal, additional_charges,
 total_amount, amount_paid,
 balance_due, invoice_status)
VALUES

(1, '2026-09-20', '2026-09-28',
 15000.00, 2000.00,
 17000.00, 10000.00,
 7000.00, 'PARTIALLY_PAID'),

(2, '2026-09-10', '2026-09-15',
 25000.00, 5000.00,
 30000.00, 0,
 30000.00, 'UNPAID'),

(3, '2026-09-23', '2026-09-28',
 12500.00, 0,
 12500.00, 12500.00,
 0.00, 'PAID');
 
 
 INSERT INTO charge
(rental_id, rental_item_id, invoice_id,
 charge_type, charge_description,
 amount, charge_date, created_by)
VALUES

(1, 1, 1,
 'RENTAL', 'Electric drill rental charge',
 7500.00, '2026-09-20 10:00:00', 3),

(1, 2, 1,
 'RENTAL', 'Angle grinder rental charge',
 6000.00, '2026-09-20 10:00:00', 3),

(1, NULL, 1,
 'EXTENSION', 'Rental extension charge',
 1500.00, '2026-09-24 10:00:00', 3),

(2, 3, 2,
 'LATE', 'Late return charge',
 5000.00, '2026-09-20 10:00:00', 3);
 
 INSERT INTO payment
(invoice_id, amount, payment_date,
 payment_method, reference_no,
 received_by, payment_status)
VALUES

(1, 10000.00,
 '2026-09-20 10:30:00',
 'CARD', 'PAY001',
 3, 'COMPLETED'),

(3, 12500.00,
 '2026-09-23 14:30:00',
 'CASH', 'PAY002',
 4, 'COMPLETED');
 
 INSERT INTO security_deposit
(rental_id, calculated_deposit,
 deposit_amount_received,
 amount_deducted, amount_refunded,
 received_date, received_by,
 deposit_status)
VALUES

(1, 14000.00, 14000.00,
 2000.00, 0.00,
 '2026-09-20 10:00:00',
 3, 'HELD'),

(2, 15000.00, 15000.00,
 15000.00, 0.00,
 '2026-09-10 09:00:00',
 3, 'FORFEITED'),

(3, 8000.00, 8000.00,
 0.00, 0.00,
 '2026-09-23 14:00:00',
 4, 'HELD');
 
 INSERT INTO rental_return
(rental_id, return_date, processed_by,
 return_type, notes)
VALUES

(1, '2026-09-24 15:00:00',
 3, 'PARTIAL',
 'Customer returned one drill');
 
 INSERT INTO return_item
(return_id, rental_item_id,
 qty_returned, condition_status,
 inspection_notes, returned_at)
VALUES

(1, 1, 1,
 'DAMAGED',
 'Drill casing has visible damage',
 '2026-09-24 15:00:00');
 
 INSERT INTO damage_record
(return_item_id, damaged_quantity,
 damage_description, damage_level,
 estimated_cost, final_charge,
 assessed_by, assessment_date,
 damage_status)
VALUES

(1, 1,
 'Outer casing damaged during rental',
 'MODERATE',
 2500.00,
 2000.00,
 3,
 '2026-09-24 15:30:00',
 'CHARGED');
 
 INSERT INTO lost_item
(rental_item_id, quantity_lost,
 loss_type, reported_date, reason,
 replacement_cost_per_unit,
 charge_amount, reported_by,
 lost_status, notes)
VALUES

(3, 1,
 'NON_RETURNED',
 '2026-09-20 10:00:00',
 'Customer has not returned the equipment',
 75000.00,
 75000.00,
 3,
 'CHARGED',
 'Follow-up required');
 
 INSERT INTO settlement
(rental_id,
 rental_charges,
 late_charges,
 damage_charges,
 lost_item_charges,
 total_charges,
 deposit_used,
 deposit_refunded,
 final_balance,
 settled_at,
 settled_by,
 settlement_status)
VALUES

(1,
 13500.00,
 0.00,
 2000.00,
 0.00,
 15500.00,
 2000.00,
 12000.00,
 13500.00,
 NULL,
 3,
 'PENDING');
  