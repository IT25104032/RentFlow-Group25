package com.compulin.rentflow.service.module4;

import com.compulin.rentflow.dto.module4.*;
import com.compulin.rentflow.entity.module4.RentalReturn;
import com.compulin.rentflow.entity.module4.ReturnItem;
import com.compulin.rentflow.repository.module4.*;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentalReturnService {

    private final RentalReturnRepository
            rentalReturnRepository;

    private final ReturnItemRepository
            returnItemRepository;

    private final RentalLookupRepository
            rentalLookupRepository;

    public RentalReturnService(
            RentalReturnRepository rentalReturnRepository,
            ReturnItemRepository returnItemRepository,
            RentalLookupRepository rentalLookupRepository) {

        this.rentalReturnRepository =
                rentalReturnRepository;

        this.returnItemRepository =
                returnItemRepository;

        this.rentalLookupRepository =
                rentalLookupRepository;


    }

    public List<RentalSearchResultDTO> searchRentals(
            String search) {

        return rentalLookupRepository
                .searchRentals(search);
    }

    public RentalReturnDetailsDTO getRentalForReturn(
            Integer rentalId) {

        return rentalLookupRepository
                .getRental(rentalId);
    }

    @Transactional
    public ProcessReturnResponse processReturn(
            ProcessReturnRequest request) {

        if (request.getItems() == null ||
                request.getItems().isEmpty()) {

            throw new RuntimeException(
                    "At least one returned item must be selected."
            );
        }

        /*
         * First validate quantities BEFORE saving.
         */
        for (ReturnItemRequest item :
                request.getItems()) {

            if (item.getQuantityReturned() == null ||
                    item.getQuantityReturned() <= 0) {

                throw new RuntimeException(
                        "Return quantity must be greater than zero."
                );
            }

            Integer remainingQuantity =
                    rentalLookupRepository
                            .getRemainingQuantity(
                                    item.getRentalItemId()
                            );

            if (remainingQuantity == null) {

                throw new RuntimeException(
                        "Rental item not found."
                );
            }

            if (item.getQuantityReturned()
                    > remainingQuantity) {

                throw new RuntimeException(
                        "Returned quantity cannot exceed " +
                                "the outstanding quantity."
                );
            }

            validateCondition(
                    item.getConditionStatus()
            );
        }

        /*
         * Create the overall return record.
         */
        RentalReturn rentalReturn =
                new RentalReturn();

        rentalReturn.setRentalId(
                request.getRentalId());

        rentalReturn.setProcessedBy(
                request.getProcessedBy());

        rentalReturn.setReturnDate(
                LocalDateTime.now());

        rentalReturn.setNotes(
                request.getNotes());

        /*
         * Temporarily set PARTIAL.
         * We calculate the final value after saving items.
         */
        rentalReturn.setReturnType("PARTIAL");

        rentalReturn =
                rentalReturnRepository.save(
                        rentalReturn);

        List<Integer> damagedReturnItemIds =
                new ArrayList<>();

        /*
         * Save every selected returned item.
         */
        for (ReturnItemRequest itemRequest :
                request.getItems()) {

            ReturnItem returnItem =
                    new ReturnItem();

            returnItem.setRentalReturn(
                    rentalReturn);

            returnItem.setRentalItemId(
                    itemRequest.getRentalItemId());

            returnItem.setQuantityReturned(
                    itemRequest.getQuantityReturned());

            returnItem.setConditionStatus(
                    itemRequest.getConditionStatus());

            returnItem.setInspectionNotes(
                    itemRequest.getInspectionNotes());

            returnItem.setReturnedAt(
                    LocalDateTime.now());

            ReturnItem savedItem =
                    returnItemRepository.save(
                            returnItem);

            /*
             * If damaged, remember it.
             * React can then redirect to Damage Assessment.
             */
            if ("DAMAGED".equalsIgnoreCase(
                    itemRequest.getConditionStatus())) {

                damagedReturnItemIds.add(
                        savedItem.getReturnItemId()
                );
            }
        }

        /*
         * Now reload ALL rental items and calculate
         * remaining quantities after this return.
         */
        RentalReturnDetailsDTO updatedRental =
                rentalLookupRepository
                        .getRental(
                                request.getRentalId()
                        );

        boolean everythingReturned = true;

        for (RentalItemReturnDTO item :
                updatedRental.getItems()) {

            if (item.getRemainingQuantity() == 0) {

                rentalLookupRepository
                        .updateRentalItemStatus(
                                item.getRentalItemId(),
                                "RETURNED"
                        );

            } else if (
                    item.getAlreadyReturnedQuantity() > 0) {

                everythingReturned = false;

                rentalLookupRepository
                        .updateRentalItemStatus(
                                item.getRentalItemId(),
                                "PARTIALLY_RETURNED"
                        );

            } else {

                everythingReturned = false;

                rentalLookupRepository
                        .updateRentalItemStatus(
                                item.getRentalItemId(),
                                "ISSUED"
                        );
            }
        }

        String returnType;
        String rentalStatus;

        if (everythingReturned) {

            returnType = "FULL";
            rentalStatus = "RETURNED";

        } else {

            returnType = "PARTIAL";
            rentalStatus =
                    "PARTIALLY_RETURNED";
        }

        /*
         * Update return header.
         */
        rentalReturn.setReturnType(returnType);

        rentalReturnRepository.save(
                rentalReturn);

        /*
         * Update original rental.
         */
        rentalLookupRepository
                .updateRentalStatus(
                        request.getRentalId(),
                        rentalStatus
                );

        return new ProcessReturnResponse(
                rentalReturn.getReturnId(),
                request.getRentalId(),
                returnType,
                rentalStatus,
                damagedReturnItemIds
        );
    }

    private void validateCondition(
            String condition) {

        if (condition == null) {
            throw new RuntimeException(
                    "Item condition is required."
            );
        }

        List<String> allowed =
                List.of(
                        "GOOD",
                        "DAMAGED",
                        "MISSING PARTS",
                        "NEEDS MAINTENANCE"
                );

        if (!allowed.contains(
                condition.toUpperCase())) {

            throw new RuntimeException(
                    "Invalid item condition."
            );
        }
    }

    public List<RentalReturn> getAllReturns() {
        return rentalReturnRepository.findAll();
    }

    public RentalReturn getReturnById(Integer id) {

        return rentalReturnRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Return not found with ID: " + id
                        )
                );
    }

    public RentalReturn createReturn(
            RentalReturn rentalReturn) {

        return rentalReturnRepository.save(
                rentalReturn
        );
    }

    public List<ReturnItem> getReturnItems(
            Integer returnId) {

        return returnItemRepository
                .findByRentalReturn_ReturnId(
                        returnId
                );
    }

    public ReturnItem addReturnItem(
            ReturnItem returnItem) {

        return returnItemRepository.save(
                returnItem
        );
    }
}