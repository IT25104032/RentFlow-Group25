package com.compulin.rentflow.service.module2;

import com.compulin.rentflow.dto.module2.RentalHistoryResponse;
import com.compulin.rentflow.dto.module2.RentalItemResponse;
import com.compulin.rentflow.repository.module2.RentalItemRepository;
import com.compulin.rentflow.dto.module2.RentalRequest;
import com.compulin.rentflow.dto.module2.RentalResponse;
import com.compulin.rentflow.entity.module2.Rental;
import com.compulin.rentflow.entity.module2.RentalItem;
import com.compulin.rentflow.repository.module2.RentalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;

    private final RentalItemRepository rentalItemRepository;

    public RentalService(
            RentalRepository rentalRepository,
            RentalItemRepository rentalItemRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.rentalItemRepository = rentalItemRepository;
    }

    public RentalResponse createRental(RentalRequest request) {

        if (request.getStartDate() == null) {
            throw new IllegalArgumentException(
                    "Rental start date is required"
            );
        }

        if (request.getDueDate() == null) {
            throw new IllegalArgumentException(
                    "Rental due date is required"
            );
        }

        if (request.getDueDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException(
                    "Due date cannot be before start date"
            );
        }

        Rental rental = new Rental();

        rental.setCompanyId(request.getCompanyId());
        rental.setCustomerId(request.getCustomerId());
        rental.setCreatedBy(request.getCreatedBy());
        rental.setStartDate(request.getStartDate());
        rental.setDueDate(request.getDueDate());
        rental.setRentalStatus(request.getRentalStatus());
        rental.setNotes(request.getNotes());

        Rental savedRental = rentalRepository.save(rental);

        return convertToResponse(savedRental);
    }

    public RentalResponse getRental(
            Integer rentalId,
            Integer companyId) {

        Optional<Rental> optionalRental =
                rentalRepository.findByRentalIdAndCompanyId(
                        rentalId,
                        companyId
                );

        if (optionalRental.isEmpty()) {
            return null;
        }

        return convertToResponse(optionalRental.get());
    }

    public List<RentalHistoryResponse> getCustomerRentalHistory(
            Integer customerId,
            Integer companyId
    ) {

        List<Rental> rentals =
                rentalRepository.findByCustomerIdAndCompanyId(
                        customerId,
                        companyId
                );

        return rentals.stream()
                .map(rental -> {

                    RentalHistoryResponse history =
                            new RentalHistoryResponse();

                    history.setRental(
                            convertToResponse(rental)
                    );

                    List<RentalItemResponse> rentalItems =
                            rentalItemRepository
                                    .findByRentalId(
                                            rental.getRentalId()
                                    )
                                    .stream()
                                    .map(this::convertRentalItemToResponse)
                                    .toList();

                    history.setRentalItems(rentalItems);

                    return history;
                })
                .toList();
    }

    public List<RentalResponse> getActiveRentals(
            Integer companyId) {

        List<Rental> rentals =
                rentalRepository.findActiveRentals(
                        companyId
                );

        return rentals.stream()
                .map(this::convertToResponse)
                .toList();
    }


    public List<RentalResponse> getOverdueRentals(
            Integer companyId) {

        List<Rental> rentals =
                rentalRepository.findOverdueRentals(
                        companyId
                );

        return rentals.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<RentalResponse> getRentalsDueSoon(
            Integer companyId) {

        LocalDate today = LocalDate.now();

        LocalDate dueSoonDate =
                today.plusDays(3);

        List<Rental> rentals =
                rentalRepository.findRentalsDueSoon(
                        companyId,
                        today,
                        dueSoonDate
                );

        return rentals.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private RentalResponse convertToResponse(Rental rental) {

        RentalResponse response = new RentalResponse();

        response.setRentalId(rental.getRentalId());
        response.setCompanyId(rental.getCompanyId());
        response.setCustomerId(rental.getCustomerId());
        response.setCreatedBy(rental.getCreatedBy());
        response.setRentalDate(rental.getRentalDate());
        response.setStartDate(rental.getStartDate());
        response.setDueDate(rental.getDueDate());
        response.setRentalStatus(rental.getRentalStatus());
        response.setNotes(rental.getNotes());
        response.setCreatedAt(rental.getCreatedAt());

        return response;
    }

    private RentalItemResponse convertRentalItemToResponse(
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