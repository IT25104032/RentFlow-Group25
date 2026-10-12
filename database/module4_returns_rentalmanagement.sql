# =====================================================
# COMPULIN EQUIPMENT HIRE & RENTAL MANAGEMENT SYSTEM
# MODULE 4
# Return, Damage, Lost Items & Settlement Management
# =====================================================


# ============== TABLE 1 : rental_return =================
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


# ============== TABLE 2 : return_item =================
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


# ============== TABLE 3 : damage_record =================
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

# ============== TABLE 4 : lost_item =================
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


# ============== TABLE 5 : settlement =================
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

# ============== INSERT statements ====================

#TABLE: rental_return
 INSERT INTO rental_return
(rental_id, return_date, processed_by,
 return_type, notes)
VALUES

(1, '2026-09-24 15:00:00',
 3, 'PARTIAL',
 'Customer returned one drill');
 
#TABLE: return_item
INSERT INTO return_item
(return_id, rental_item_id,
 qty_returned, condition_status,
 inspection_notes, returned_at)
VALUES

(1, 1, 1,
 'DAMAGED',
 'Drill casing has visible damage',
 '2026-09-24 15:00:00');

#TABLE: damage_record 
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

#TABLE: lost_item 
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
 
 
 #TABLE: settlement
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
  
 
 
# ============== SQL QUERIES =================
#1.
SELECT * FROM rental_return;

#2. Useful for rental_return history screen 
SELECT * FROM rental_return
JOIN return_item ON rental_return.return_id = return_item.return_id;

#3. To check damaged items
SELECT return_item_id, rental_item_id, qty_returned, condition_status, inspection_notes
FROM return_item WHERE condition_status = 'DAMAGED';

#4. Damage record details screen
SELECT * FROM return_item
JOIN damage_record ON return_item.return_item_id = damage_record.return_item_id;

#5. Total damage charges
SELECT SUM(final_charge) AS total_damage_charges FROM damage_record;

#6. Total lost-item charges
SELECT SUM(charge_amount) AS total_lost_item_charges FROM lost_item;

#7. Count lost items by type
SELECT loss_type, SUM(quantity_lost) AS total_quantity FROM lost_item GROUP BY loss_type;

#8. Pending settlements
SELECT settlement_id, rental_id, total_charges, deposit_used, deposit_refunded, final_balance, settlement_status
FROM settlement WHERE settlement_status = 'PENDING';

#9. Settlement summary
SELECT * FROM settlement ORDER BY settlement_id DESC;

#10. Total quantity returned for each rental item
SELECT rental_item_id, SUM(qty_returned) AS total_returned FROM return_item GROUP BY rental_item_id;

#11. Severe damage cases 
SELECT damage_id, return_item_id, damaged_quantity, damage_description, estimated_cost, final_charge, damage_status
FROM damage_record WHERE damage_level = 'MODERATE';

#12. Unresolved lost/non-returned items
SELECT lost_item_id, rental_item_id, quantity_lost, loss_type, reported_date, reason, charge_amount, lost_status
FROM lost_item WHERE lost_status = 'CHARGED';

#13. View returns with customer details
SELECT rr.return_id, r.rental_id, c.customer_name, rr.return_date, rr.return_type FROM rental_return rr
JOIN rental r ON rr.rental_id = r.rental_id JOIN customer c ON r.customer_id = c.customer_id;

#14. View equipment that was returned
SELECT
    c.customer_name,
    e.item_name,
    ri.qty_returned,
    ri.condition_status,
    ri.inspection_notes,
    rr.return_date
FROM rental_return rr
JOIN rental r
    ON rr.rental_id = r.rental_id
JOIN customer c
    ON r.customer_id = c.customer_id
JOIN return_item ri
    ON rr.return_id = ri.return_id
JOIN rental_item ritem
    ON ri.rental_item_id = ritem.rental_item_id
JOIN equipment e
    ON ritem.equipment_id = e.equipment_id;


#15. Complete damage report
SELECT
    c.customer_name,
    e.item_name,
    dr.damaged_quantity,
    dr.damage_description,
    dr.damage_level,
    dr.estimated_cost,
    dr.final_charge,
    dr.damage_status
FROM damage_record dr
JOIN return_item ri
    ON dr.return_item_id = ri.return_item_id
JOIN rental_item ritem
    ON ri.rental_item_id = ritem.rental_item_id
JOIN rental r
    ON ritem.rental_id = r.rental_id
JOIN customer c
    ON r.customer_id = c.customer_id
JOIN equipment e
    ON ritem.equipment_id = e.equipment_id;
    
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
JOIN rental_item ri
    ON li.rental_item_id = ri.rental_item_id
JOIN rental r
    ON ri.rental_id = r.rental_id
JOIN customer c
    ON r.customer_id = c.customer_id
JOIN equipment e
    ON ri.equipment_id = e.equipment_id;
    
    
#17. View staff member who processed each return
SELECT
    rr.return_id,
    rr.rental_id,
    rr.return_date,
    u.full_name AS processed_by
FROM rental_return rr
JOIN sys_user u
    ON rr.processed_by = u.user_id;
    
#18. View staff member who assessed damage
SELECT
    dr.damage_id,
    dr.damage_description,
    dr.damage_level,
    dr.final_charge,
    u.full_name AS assessed_by
FROM damage_record dr
JOIN sys_user u
    ON dr.assessed_by = u.user_id;
    
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
JOIN rental r
    ON s.rental_id = r.rental_id
JOIN customer c
    ON r.customer_id = c.customer_id;
    
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
JOIN security_deposit sd
    ON s.rental_id = sd.rental_id;
    
#21. Test recovering a lost item
UPDATE lost_item
SET lost_status = 'RECOVERED'
WHERE lost_item_id = 1;

SELECT *
FROM lost_item
WHERE lost_item_id = 1;

