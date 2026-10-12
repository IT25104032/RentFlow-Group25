package com.compulin.rentflow.service.module2;

import com.compulin.rentflow.dto.module2.RentalConfirmationRequest;
import com.compulin.rentflow.dto.module2.RentalConfirmationResponse;
import com.compulin.rentflow.dto.module2.RentalItemRequest;
import com.compulin.rentflow.dto.module2.RentalItemResponse;
import com.compulin.rentflow.dto.module2.RentalResponse;
import com.compulin.rentflow.entity.module2.Rental;
import com.compulin.rentflow.entity.module2.RentalItem;
import com.compulin.rentflow.repository.module2.RentalItemRepository;
import com.compulin.rentflow.repository.module2.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class RentalConfirmationService {

    private final RentalRepository rentalRepository;

    private final RentalItemRepository rentalItemRepository;

    public RentalConfirmationService(
            RentalRepository rentalRepository,
            RentalItemRepository rentalItemRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.rentalItemRepository = rentalItemRepository;
    }


    /*
     * Confirm the complete rental.
     *
     * The rental and all rental items are
     * saved as one database transaction.
     */
    @Transactional
    public RentalConfirmationResponse confirmRental(
            RentalConfirmationRequest request
    ) {

        validateRequest(request);


        /*
         * Create the Rental entity.
         */
        Rental rental = new Rental();

        rental.setCompanyId(
                request.getRental().getCompanyId()
        );

        rental.setCustomerId(
                request.getRental().getCustomerId()
        );

        rental.setCreatedBy(
                request.getRental().getCreatedBy()
        );

        rental.setStartDate(
                request.getRental().getStartDate()
        );

        rental.setDueDate(
                request.getRental().getDueDate()
        );

        rental.setRentalStatus(
                "DRAFT"
        );

        rental.setNotes(
                appendFinalDepositMarker(
                        request.getRental().getNotes(),
                        request.getTotalDeposit()
                )
        );


        /*
         * Save the rental first.
         *
         * MySQL generates the rental ID.
         */
        Rental savedRental =
                rentalRepository.save(rental);


        /*
         * Create the rental items.
         */
        List<RentalItemResponse> itemResponses =
                new ArrayList<>();


        for (
                RentalItemRequest itemRequest :
                request.getRentalItems()
        ) {

            RentalItem rentalItem =
                    new RentalItem();


            /*
             * Attach the newly-created rental ID.
             */
            rentalItem.setRentalId(
                    savedRental.getRentalId()
            );


            rentalItem.setEquipmentId(
                    itemRequest.getEquipmentId()
            );

            rentalItem.setQuantity(
                    itemRequest.getQuantity()
            );

            rentalItem.setRatePerUnit(
                    itemRequest.getRatePerUnit()
            );

            rentalItem.setRatePeriod(
                    itemRequest.getRatePeriod()
            );

            rentalItem.setDepositPerUnit(
                    itemRequest.getDepositPerUnit()
            );


            /*
             * Calculate the line deposit
             * on the backend.
             */
            BigDecimal lineDeposit =
                    itemRequest
                            .getDepositPerUnit()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemRequest
                                                    .getQuantity()
                                    )
                            );


            rentalItem.setLineDeposit(
                    lineDeposit
            );


            /*
             * A newly confirmed rental item
             * starts as SELECTED.
             */
            rentalItem.setItemStatus(
                    "SELECTED"
            );


            RentalItem savedItem =
                    rentalItemRepository.save(
                            rentalItem
                    );


            itemResponses.add(
                    convertItemToResponse(
                            savedItem
                    )
            );
        }


        // The final agreed deposit is already stored in the rental notes marker.
        // Module 2 deliberately does not write to Module 3's security_deposit table.


        /*
         * Build the rental response.
         */
        RentalResponse rentalResponse =
                convertRentalToResponse(
                        savedRental
                );


        /*
         * Build the final confirmation response.
         */
        RentalConfirmationResponse response =
                new RentalConfirmationResponse();


        response.setRental(
                rentalResponse
        );

        response.setRentalItems(
                itemResponses
        );

        response.setMessage(
                "Rental confirmed successfully."
        );


        return response;
    }


    /**
     * Stores the final agreed deposit in the existing notes column without
     * changing the database schema. The marker is parsed by RentalService and
     * hidden from the ordinary Notes display in RentalHistory.jsx.
     */
    private String appendFinalDepositMarker(String notes, BigDecimal totalDeposit) {
        String cleanNotes = notes == null ? "" : notes;
        cleanNotes = cleanNotes.replaceAll("\\s*\\[FINAL_DEPOSIT=-?\\d+(?:\\.\\d+)?\\]", "").trim();
        String marker = "[FINAL_DEPOSIT=" + totalDeposit.toPlainString() + "]";
        String combined = cleanNotes.isEmpty() ? marker : cleanNotes + "\n" + marker;
        if (combined.length() > 500) {
            throw new IllegalArgumentException(
                    "Rental notes are too long to store the final deposit marker. Shorten the notes and try again."
            );
        }
        return combined;
    }


    /*
     * Validate the complete confirmation request.
     */
    private void validateRequest(
            RentalConfirmationRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Rental confirmation request is required."
            );
        }


        if (request.getRental() == null) {

            throw new IllegalArgumentException(
                    "Rental details are required."
            );
        }

        if (request.getTotalDeposit() == null) {
            throw new IllegalArgumentException(
                    "Total security deposit is required."
            );
        }

        if (request.getTotalDeposit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Total security deposit cannot be negative."
            );
        }



        if (
                request.getRental().getCompanyId()
                        == null
        ) {

            throw new IllegalArgumentException(
                    "Company ID is required."
            );
        }


        if (
                request.getRental().getCustomerId()
                        == null
        ) {

            throw new IllegalArgumentException(
                    "Customer ID is required."
            );
        }


        if (
                request.getRental().getCreatedBy()
                        == null
        ) {

            throw new IllegalArgumentException(
                    "Created by user ID is required."
            );
        }


        if (
                request.getRental().getStartDate()
                        == null
        ) {

            throw new IllegalArgumentException(
                    "Start date is required."
            );
        }


        if (
                request.getRental().getDueDate()
                        == null
        ) {

            throw new IllegalArgumentException(
                    "Due date is required."
            );
        }


        if (
                request.getRental().getDueDate()
                        .isBefore(
                                request.getRental()
                                        .getStartDate()
                        )
        ) {

            throw new IllegalArgumentException(
                    "Due date cannot be before start date."
            );
        }


        if (
                request.getRentalItems() == null
                        ||
                        request.getRentalItems().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "At least one rental item is required."
            );
        }


        for (
                RentalItemRequest item :
                request.getRentalItems()
        ) {

            validateRentalItem(item);
        }
    }


    /*
     * Validate an individual rental item.
     */
    private void validateRentalItem(
            RentalItemRequest item
    ) {

        if (item == null) {

            throw new IllegalArgumentException(
                    "Rental item cannot be null."
            );
        }


        if (
                item.getEquipmentId() == null
        ) {

            throw new IllegalArgumentException(
                    "Equipment ID is required."
            );
        }


        if (
                item.getQuantity() == null
                        ||
                        item.getQuantity() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Rental item quantity must be greater than zero."
            );
        }


        if (
                item.getRatePerUnit() == null
        ) {

            throw new IllegalArgumentException(
                    "Rate per unit is required."
            );
        }


        if (
                item.getRatePerUnit()
                        .compareTo(
                                BigDecimal.ZERO
                        ) < 0
        ) {

            throw new IllegalArgumentException(
                    "Rate per unit cannot be negative."
            );
        }


        if (
                item.getRatePeriod() == null
                        ||
                        item.getRatePeriod()
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Rate period is required."
            );
        }


        if (
                item.getDepositPerUnit() == null
        ) {

            throw new IllegalArgumentException(
                    "Deposit per unit is required."
            );
        }


        if (
                item.getDepositPerUnit()
                        .compareTo(
                                BigDecimal.ZERO
                        ) < 0
        ) {

            throw new IllegalArgumentException(
                    "Deposit per unit cannot be negative."
            );
        }
    }


    /*
     * Convert Rental entity to response DTO.
     */
    private RentalResponse convertRentalToResponse(
            Rental rental
    ) {

        RentalResponse response =
                new RentalResponse();


        response.setRentalId(
                rental.getRentalId()
        );

        response.setCompanyId(
                rental.getCompanyId()
        );

        response.setCustomerId(
                rental.getCustomerId()
        );

        response.setCreatedBy(
                rental.getCreatedBy()
        );

        response.setRentalDate(
                rental.getRentalDate()
        );

        response.setStartDate(
                rental.getStartDate()
        );

        response.setDueDate(
                rental.getDueDate()
        );

        response.setRentalStatus(
                rental.getRentalStatus()
        );

        response.setNotes(
                rental.getNotes()
        );

        response.setCreatedAt(
                rental.getCreatedAt()
        );


        return response;
    }


    /*
     * Convert RentalItem entity to response DTO.
     */
    private RentalItemResponse convertItemToResponse(
            RentalItem rentalItem
    ) {

        RentalItemResponse response =
                new RentalItemResponse();


        response.setRentalItemId(
                rentalItem.getRentalItemId()
        );

        response.setRentalId(
                rentalItem.getRentalId()
        );

        response.setEquipmentId(
                rentalItem.getEquipmentId()
        );

        response.setQuantity(
                rentalItem.getQuantity()
        );

        response.setRatePerUnit(
                rentalItem.getRatePerUnit()
        );

        response.setRatePeriod(
                rentalItem.getRatePeriod()
        );

        response.setDepositPerUnit(
                rentalItem.getDepositPerUnit()
        );

        response.setLineDeposit(
                rentalItem.getLineDeposit()
        );

        response.setItemStatus(
                rentalItem.getItemStatus()
        );

        response.setIssuedAt(
                rentalItem.getIssuedAt()
        );


        return response;
    }
}