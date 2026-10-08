#!/usr/bin/env bash
# Module 4 API check: issue a rental, return it (good + damaged + lost, late),
# settle with the deposit, collect the rest, rental closes.
# Needs: backend on :8081, a fresh rentflow_db, curl, jq, mysql client.
set -e
B=${BASE:-http://localhost:8081/api}
J='Content-Type: application/json'
q(){ mysql -u"${DB_USERNAME:-root}" -p"${DB_PASSWORD:-root}" rentflow_db -N -e "$1" 2>/dev/null; }

echo "1) no login on this branch: Module 4 works as the seed rental officer (user 3)"

echo "2) rental: 3 drills + 1 grinder, started 6 days ago, due 2 days ago"
echo "   (Modules 1-3 are not on this branch, so the issued rental is created with SQL)"
CID=$(q "INSERT INTO customer (company_id, customer_name, email, phone, address, customer_type, customer_status, created_by)
         VALUES (1000, 'M4 Test Renter', 'm4@test.lk', '0770000004', 'Colombo', 'INDIVIDUAL', 'ACTIVE', 3); SELECT LAST_INSERT_ID();")
RID=$(q "INSERT INTO rental (company_id, customer_id, created_by, start_date, due_date, rental_status)
         VALUES (1000, $CID, 3, CURDATE() - INTERVAL 6 DAY, CURDATE() - INTERVAL 2 DAY, 'ACTIVE'); SELECT LAST_INSERT_ID();")
DRILL=$(q "INSERT INTO rental_item (rental_id, equipment_id, quantity, rate_per_unit, rate_period, deposit_per_unit, line_deposit, item_status, issued_at)
           VALUES ($RID, 1, 3, 1500, 'DAY', 5000, 15000, 'ISSUED', NOW()); SELECT LAST_INSERT_ID();")
GRINDER=$(q "INSERT INTO rental_item (rental_id, equipment_id, quantity, rate_per_unit, rate_period, deposit_per_unit, line_deposit, item_status, issued_at)
             VALUES ($RID, 2, 1, 1200, 'DAY', 4000, 4000, 'ISSUED', NOW()); SELECT LAST_INSERT_ID();")
q "UPDATE equipment SET available_quantity = available_quantity - 3 WHERE equipment_id = 1;
   UPDATE equipment SET available_quantity = available_quantity - 1 WHERE equipment_id = 2;
   INSERT INTO charge (rental_id, rental_item_id, charge_type, charge_description, amount, charge_date, created_by)
   VALUES ($RID, $DRILL, 'RENTAL', 'Drill x3', 18000, NOW(), 3), ($RID, $GRINDER, 'RENTAL', 'Grinder x1', 4800, NOW(), 3)"
q "INSERT INTO security_deposit (rental_id, calculated_deposit, deposit_amount_received, received_date, received_by, deposit_status) VALUES ($RID, 19000, 19000, NOW(), 3, 'HELD')"
echo "   rental $RID, drill stock $(q 'select available_quantity from equipment where equipment_id=1')"

echo "3) return screen shows what is out and the late charge per unit"
curl -s "$B/returns/rental/$RID" | jq -c '{daysLate, items: [.items[] | {equipmentName, remainingQuantity, lateChargePerUnit}]}'

echo "4) too many units is refused"
curl -s -H "$J" -d "{\"rentalId\":$RID,\"items\":[{\"rentalItemId\":$DRILL,\"quantityReturned\":4,\"conditionStatus\":\"GOOD\"}]}" $B/returns/process | jq -c .

echo "5) return 2 good + 1 damaged drill, grinder stolen"
curl -s -H "$J" -d "{\"rentalId\":$RID,\"notes\":\"api test\",
  \"items\":[{\"rentalItemId\":$DRILL,\"quantityReturned\":2,\"conditionStatus\":\"GOOD\"},
            {\"rentalItemId\":$DRILL,\"quantityReturned\":1,\"conditionStatus\":\"DAMAGED\",
             \"damage\":{\"damageLevel\":\"MINOR\",\"damageDescription\":\"Chuck cracked\",\"estimatedCost\":3000,\"finalCharge\":2500}}],
  \"lostItems\":[{\"rentalItemId\":$GRINDER,\"quantityLost\":1,\"lossType\":\"STOLEN\",\"replacementCostPerUnit\":40000,\"reason\":\"stolen at site\"}]}" \
  $B/returns/process | jq -c .
echo "   drill stock $(q 'select available_quantity from equipment where equipment_id=1') (2 back, damaged one held)"
echo "   grinder total $(q 'select total_quantity from equipment where equipment_id=2') (lost unit written off)"

echo "6) settlement preview"
curl -s $B/settlements/$RID | jq -c '{totalCharges,alreadyPaid,depositHeld,depositToUse,depositToRefund,balanceAfterDeposit}'
echo "7) settle: deposit used, balance still owed -> PENDING"
curl -s -X POST $B/settlements/$RID | jq -c '{settlementStatus,outstanding,rentalStatus}'
OWED=$(curl -s $B/settlements/$RID | jq .outstanding)
echo "8) collect $OWED -> SETTLED, rental CLOSED"
curl -s -H "$J" -d "{\"amount\":$OWED,\"paymentMethod\":\"CASH\"}" $B/settlements/$RID/payments | jq -c '{settlementStatus,outstanding,rentalStatus,settledDepositUsed}'

echo "9) damaged drill repaired -> back in stock"
DID=$(curl -s "$B/damages?rentalId=$RID" | jq '.[0].damageId')
curl -s -X POST $B/damages/$DID/repaired | jq -c '{damageId,status}'
echo "   drill stock $(q 'select available_quantity from equipment where equipment_id=1')"
echo "10) overview numbers"; curl -s $B/returns/overview | jq -c .
