package com.compulin.rentflow.service.module2;


import com.compulin.rentflow.dto.module2.RentalItemRequest;
import com.compulin.rentflow.dto.module2.RentalItemResponse;
import com.compulin.rentflow.entity.module2.Rental;
import com.compulin.rentflow.entity.module2.RentalItem;
import com.compulin.rentflow.repository.module2.RentalItemRepository;
import com.compulin.rentflow.repository.module2.RentalRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;


@Service
public class RentalItemService {

    private final RentalItemRepository rentalItemRepository;
    private final RentalRepository rentalRepository;


    public RentalItemService(
            RentalItemRepository rentalItemRepository,
            RentalRepository rentalRepository
    ) {
        this.rentalItemRepository =
                rentalItemRepository;

        this.rentalRepository =
                rentalRepository;
    }


    /*
     * Create a rental item.
     */
    public RentalItemResponse createRentalItem(
            RentalItemRequest request
    ) {

        validateRequest(request);


        RentalItem rentalItem =
                new RentalItem();


        rentalItem.setRentalId(
                request.getRentalId()
        );

        rentalItem.setEquipmentId(
                request.getEquipmentId()
        );

        rentalItem.setQuantity(
                request.getQuantity()
        );

        rentalItem.setRatePerUnit(
                request.getRatePerUnit()
        );

        rentalItem.setRatePeriod(
                request.getRatePeriod()
        );

        rentalItem.setDepositPerUnit(
                request.getDepositPerUnit()
        );


        /*
         * Calculate line deposit on the backend.
         *
         * This prevents the frontend from being
         * the only place calculating the deposit.
         */
        BigDecimal lineDeposit =
                request.getDepositPerUnit()
                        .multiply(
                                BigDecimal.valueOf(
                                        request.getQuantity()
                                )
                        );


        rentalItem.setLineDeposit(
                lineDeposit
        );


        /*
         * New rental items start as SELECTED.
         */
        rentalItem.setItemStatus(
                "SELECTED"
        );


        RentalItem savedRentalItem =
                rentalItemRepository.save(
                        rentalItem
                );


        return convertToResponse(
                savedRentalItem
        );
    }


    /*
     * Get all items belonging to one rental.
     */
    public List<RentalItemResponse> getRentalItems(
            Integer rentalId
    ) {

        return rentalItemRepository
                .findByRentalId(rentalId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    /*
     * Get one rental item belonging to a
     * specific rental.
     */
    public RentalItemResponse getRentalItem(
            Integer rentalItemId,
            Integer rentalId
    ) {

        RentalItem rentalItem =
                rentalItemRepository
                        .findByRentalItemIdAndRentalId(
                                rentalItemId,
                                rentalId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Rental item not found"
                                )
                        );


        return convertToResponse(
                rentalItem
        );
    }

    /*
     * Issue a rental item.
     *
     * The complete requested quantity is issued.
     */
    public RentalItemResponse issueRentalItem(
            Integer rentalItemId,
            Integer rentalId
    ) {

        RentalItem rentalItem =
                rentalItemRepository
                        .findByRentalItemIdAndRentalId(
                                rentalItemId,
                                rentalId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Rental item not found."
                                )
                        );

        /*
         * Prevent issuing the same item twice.
         */
        if ("ISSUED".equalsIgnoreCase(
                rentalItem.getItemStatus()
        )) {

            throw new IllegalArgumentException(
                    "This rental item has already been issued."
            );
        }

        /*
         * Only SELECTED items can be issued.
         */
        if (!"SELECTED".equalsIgnoreCase(
                rentalItem.getItemStatus()
        )) {

            throw new IllegalArgumentException(
                    "Only selected rental items can be issued."
            );
        }

        /*
         * Quantity must be valid.
         */
        if (
                rentalItem.getQuantity() == null
                        ||
                        rentalItem.getQuantity() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Rental item quantity must be greater than zero."
            );
        }

        /*
         * Mark the complete requested quantity
         * as issued.
         */
        rentalItem.setItemStatus("ISSUED");

        rentalItem.setIssuedAt(
                LocalDateTime.now()
        );

        RentalItem savedRentalItem =
                rentalItemRepository.save(
                        rentalItem
                );


        /*
         * Change the rental status from DRAFT to ACTIVE
         * after the equipment has been successfully issued.
         */
        Rental rental =
                rentalRepository
                        .findById(rentalId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Rental not found."
                                )
                        );

        if ("DRAFT".equalsIgnoreCase(
                rental.getRentalStatus()
        )) {

            rental.setRentalStatus("ACTIVE");

            rentalRepository.save(rental);
        }


        return convertToResponse(
                savedRentalItem
        );
    }

    /*
     * Validate the request before saving.
     */
    private void validateRequest(
            RentalItemRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Rental item request is required."
            );
        }


        if (request.getRentalId() == null) {
            throw new IllegalArgumentException(
                    "Rental ID is required."
            );
        }


        if (request.getEquipmentId() == null) {
            throw new IllegalArgumentException(
                    "Equipment ID is required."
            );
        }


        if (
                request.getQuantity() == null
                        ||
                        request.getQuantity() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }


        if (request.getRatePerUnit() == null) {

            throw new IllegalArgumentException(
                    "Rate per unit is required."
            );
        }


        if (
                request.getRatePerUnit()
                        .compareTo(BigDecimal.ZERO) < 0
        ) {

            throw new IllegalArgumentException(
                    "Rate per unit cannot be negative."
            );
        }


        if (
                request.getDepositPerUnit() == null
        ) {

            throw new IllegalArgumentException(
                    "Deposit per unit is required."
            );
        }


        if (
                request.getDepositPerUnit()
                        .compareTo(BigDecimal.ZERO) < 0
        ) {

            throw new IllegalArgumentException(
                    "Deposit per unit cannot be negative."
            );
        }


        if (
                request.getRatePeriod() == null
                        ||
                        request.getRatePeriod()
                                .trim()
                                .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Rate period is required."
            );
        }
    }


    /*
     * Convert Entity → Response DTO.
     */
    private RentalItemResponse convertToResponse(
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