package com.compulin.rentflow.service.module2;


import com.compulin.rentflow.dto.module2.RentalRequest;
import com.compulin.rentflow.dto.module2.RentalResponse;
import com.compulin.rentflow.entity.module2.Rental;
import com.compulin.rentflow.repository.module2.RentalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;

    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
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

    public List<RentalResponse> getCustomerRentalHistory(
            Integer customerId,
            Integer companyId) {

        List<Rental> rentals =
                rentalRepository.findByCustomerIdAndCompanyId(
                        customerId,
                        companyId
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
}