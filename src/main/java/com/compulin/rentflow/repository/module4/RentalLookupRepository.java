package com.compulin.rentflow.repository.module4;

import com.compulin.rentflow.dto.module4.RentalItemReturnDTO;
import com.compulin.rentflow.dto.module4.RentalReturnDetailsDTO;
import com.compulin.rentflow.dto.module4.RentalSearchResultDTO;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RentalLookupRepository {

    private final JdbcTemplate jdbcTemplate;

    public RentalLookupRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RentalSearchResultDTO> searchRentals(String search) {

        String sql = """
                SELECT
                    r.rental_id,
                    r.customer_id,
                    co.company_name,
                    c.customer_name,
                    r.start_date,
                    r.due_date,
                    r.rental_status
                FROM rental r
                JOIN customer c
                    ON r.customer_id = c.customer_id
                JOIN company co
                    ON r.company_id = co.company_id
                WHERE
                    r.rental_status IN (
                        'ACTIVE',
                        'OVERDUE',
                        'PARTIALLY_RETURNED'
                    )
                    AND (
                        CAST(r.rental_id AS CHAR) = ?
                        OR LOWER(c.customer_name) LIKE LOWER(?)
                        OR LOWER(co.company_name) LIKE LOWER(?)
                    )
                ORDER BY r.rental_id DESC
                """;

        String nameSearch = "%" + search + "%";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new RentalSearchResultDTO(
                                rs.getInt("rental_id"),
                                rs.getInt("customer_id"),
                                rs.getString("company_name"),
                                rs.getString("customer_name"),
                                rs.getString("start_date"),
                                rs.getString("due_date"),
                                rs.getString("rental_status")
                        ),
                search,
                nameSearch,
                nameSearch
        );
    }

    public RentalReturnDetailsDTO getRental(Integer rentalId) {

        String rentalSql = """
                SELECT
                    r.rental_id,
                    co.company_name,
                    c.customer_name,
                    c.phone,
                    r.start_date,
                    r.due_date,
                    r.rental_status
                FROM rental r
                JOIN customer c
                    ON r.customer_id = c.customer_id
                JOIN company co
                    ON r.company_id = co.company_id
                WHERE r.rental_id = ?
                """;

        RentalReturnDetailsDTO rental =
                jdbcTemplate.queryForObject(
                        rentalSql,
                        (rs, rowNum) -> {

                            RentalReturnDetailsDTO dto =
                                    new RentalReturnDetailsDTO();

                            dto.setRentalId(
                                    rs.getInt("rental_id"));

                            dto.setCompanyName(
                                    rs.getString("company_name"));

                            dto.setCustomerName(
                                    rs.getString("customer_name"));

                            dto.setCustomerPhone(
                                    rs.getString("phone"));

                            dto.setStartDate(
                                    rs.getString("start_date"));

                            dto.setDueDate(
                                    rs.getString("due_date"));

                            dto.setRentalStatus(
                                    rs.getString("rental_status"));

                            return dto;
                        },
                        rentalId
                );

        rental.setItems(
                getRentalItems(rentalId)
        );

        return rental;
    }

    public List<RentalItemReturnDTO> getRentalItems(
            Integer rentalId) {

        String sql = """
                SELECT
                    ri.rental_item_id,
                    ri.equipment_id,
                    e.item_name AS equipment_name,
                    e.item_code,
                    ri.quantity AS issued_quantity,
                    COALESCE(
                        SUM(ret.qty_returned), 0
                    ) AS already_returned,
                    ri.quantity -
                    COALESCE(
                        SUM(ret.qty_returned), 0
                    ) AS remaining_quantity,
                    ri.item_status
                FROM rental_item ri
                JOIN equipment e
                    ON ri.equipment_id = e.equipment_id
                LEFT JOIN return_item ret
                    ON ri.rental_item_id =
                       ret.rental_item_id
                WHERE ri.rental_id = ?
                GROUP BY
                    ri.rental_item_id,
                    ri.equipment_id,
                    e.item_name,
                    e.item_code,
                    ri.quantity,
                    ri.item_status
                ORDER BY ri.rental_item_id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    RentalItemReturnDTO dto =
                            new RentalItemReturnDTO();

                    dto.setRentalItemId(
                            rs.getInt("rental_item_id"));

                    dto.setEquipmentId(
                            rs.getInt("equipment_id"));

                    dto.setEquipmentName(
                            rs.getString("equipment_name"));

                    dto.setItemCode(
                            rs.getString("item_code"));

                    dto.setIssuedQuantity(
                            rs.getInt("issued_quantity"));

                    dto.setAlreadyReturnedQuantity(
                            rs.getInt("already_returned"));

                    dto.setRemainingQuantity(
                            rs.getInt("remaining_quantity"));

                    dto.setItemStatus(
                            rs.getString("item_status"));

                    return dto;
                },
                rentalId
        );
    }

    public Integer getRemainingQuantity(
            Integer rentalItemId) {

        String sql = """
                SELECT
                    ri.quantity -
                    COALESCE(
                        SUM(ret.qty_returned), 0
                    )
                FROM rental_item ri
                LEFT JOIN return_item ret
                    ON ri.rental_item_id =
                       ret.rental_item_id
                WHERE ri.rental_item_id = ?
                GROUP BY
                    ri.rental_item_id,
                    ri.quantity
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                rentalItemId
        );
    }

    public Integer getRentalIdForItem(
            Integer rentalItemId) {

        String sql = """
                SELECT rental_id
                FROM rental_item
                WHERE rental_item_id = ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                rentalItemId
        );
    }

    public void updateRentalItemStatus(
            Integer rentalItemId,
            String status) {

        jdbcTemplate.update(
                """
                UPDATE rental_item
                SET item_status = ?
                WHERE rental_item_id = ?
                """,
                status,
                rentalItemId
        );
    }

    public void updateRentalStatus(
            Integer rentalId,
            String status) {

        jdbcTemplate.update(
                """
                UPDATE rental
                SET rental_status = ?
                WHERE rental_id = ?
                """,
                status,
                rentalId
        );
    }
}
