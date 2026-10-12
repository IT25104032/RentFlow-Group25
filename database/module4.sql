# =====================================================
# COMPULIN EQUIPMENT HIRE & RENTAL MANAGEMENT SYSTEM
# MODULE 4
# Return, Damage, Lost Items & Settlement Management
# =====================================================

# This script depends on:
# user
# rental
# rental_item

# ===========================================================================================

# ============== TABLE 1 : rental_return =================
CREATE TABLE rental_return (
    return_id INT AUTO_INCREMENT PRIMARY KEY,
    rental_id INT NOT NULL,
    return_date DATETIME NOT NULL,
    processed_by INT NOT NULL,
    return_type VARCHAR(20) NOT NULL CHECK (return_type IN ('FULL', 'PARTIAL')),
    notes VARCHAR(500),
    
    FOREIGN KEY (rental_id) REFERENCES rental_return(rental_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    
    FOREIGN KEY (processed_by) REFERENCES sys_user(user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
);


# ============== TABLE 2 : return_item =================
CREATE TABLE return_item (
    return_item_id INT AUTO_INCREMENT PRIMARY KEY,
    return_id INT NOT NULL,
    rental_item_id INT NOT NULL,
    qty_returned INT NOT NULL CHECK (quantity_returned > 0),
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


	FOREIGN KEY (rental_id) REFERENCES rental_return(rental_id)
	ON UPDATE CASCADE ON DELETE RESTRICT,
    
    FOREIGN KEY (settled_by) REFERENCES sys_user(user_id)
	ON UPDATE CASCADE ON DELETE RESTRICT

);

# ============== SQL QUERIES =================
#1.
SELECT * FROM rental_return;

#2. Useful for rental_return history screen 
SELECT * FROM rental_return
JOIN return_item ON rental_return.return_id = return_item.return_id;

#3. To check damaged items
SELECT return_item_id, rental_item_id, quantity_returned, condition_status, inspection_notes
FROM return_item WHERE condition_status = 'DAMAGED';

#4. Useful for damage records screen
SELECT * FROM return_item
JOIN damage_record ON return_item.return_item_id = damage_record.return_item_id;

#5. Useful for severe damage cases 
SELECT damage_id, return_item_id, damaged_quantity, damage_description, estimated_cost, final_charge, damage_status
FROM damage_record WHERE damage_level = 'SEVERE';

#6. Unresolved lost/non-returned items
SELECT lost_item_id, rental_item_id, quantity_lost, loss_type, reported_date, reason, charge_amount, lost_status
FROM lost_item WHERE status = 'PENDING';

#7. Total damage charges
SELECT SUM(final_charge) AS total_damage_charges FROM damage_record;

#8. Total lost item charges
SELECT SUM(charge_amount) AS total_lost_item_charges FROM lost_item;

#9. Count lost items by type
SELECT loss_type, SUM(quantity_lost) AS total_quantity FROM lost_item GROUP BY loss_type;

#10. View pending settlements
SELECT settlement_id, rental_id, total_charges, deposit_used, deposit_refunded, final_balance, settlement_status
FROM settlement WHERE settlement_status = 'PENDING';

#11. settlement summary
SELECT rental_id, rental_charges, late_charges, damage_charges, lost_item_charges, total_charges, deposit_used, deposit_refunded, final_balance, settlement_status
FROM settlement ORDER BY settlement_id DESC;

#12. Find late returns
SELECT r.return_id, r.rental_id, rt.due_date, r.return_date FROM rental_return r
JOIN rental rt ON r.rental_id = rt.rental_id
WHERE DATE(r.return_date) > rt.due_date;

#13. No. of units of a rental item returned so far
SELECT rental_item_id, SUM(quantity_returned) AS total_returned FROM return_item
GROUP BY rental_item_id;

#14. Update lost item when recovered
UPDATE lost_item SET status = 'RECOVERED' WHERE lost_item_id = 5;
SELECT * FROM lost_item  WHERE lost_item_id = 5;  #Check updated status

#15. Complete a settlement
UPDATE settlement 
SET settlement_status = 'SETTLED', settled_at = NOW()
WHERE settlement_id = 3;

