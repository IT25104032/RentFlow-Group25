package com.compulin.rentflow.service.module2;


import com.compulin.rentflow.dto.module2.RentalExtensionRequest;
import com.compulin.rentflow.dto.module2.RentalExtensionResponse;
import com.compulin.rentflow.entity.module2.Rental;
import com.compulin.rentflow.entity.module2.RentalExtension;
import com.compulin.rentflow.repository.module2.RentalExtensionRepository;
import com.compulin.rentflow.repository.module2.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RentalExtensionService {

    private final RentalRepository rentalRepository;
    private final RentalExtensionRepository rentalExtensionRepository;

    public RentalExtensionService(
            RentalRepository rentalRepository,
            RentalExtensionRepository rentalExtensionRepository) {

        this.rentalRepository = rentalRepository;
        this.rentalExtensionRepository =
                rentalExtensionRepository;
    }

    @Transactional
    public RentalExtensionResponse extendRental(
            RentalExtensionRequest request) {

        if (request.getRentalId() == null) {
            throw new IllegalArgumentException(
                    "Rental ID is required."
            );
        }

        if (request.getCompanyId() == null) {
            throw new IllegalArgumentException(
                    "Company ID is required."
            );
        }

        if (request.getNewDueDate() == null) {
            throw new IllegalArgumentException(
                    "New due date is required."
            );
        }

        if (request.getApprovedBy() == null) {
            throw new IllegalArgumentException(
                    "Approving user is required."
            );
        }

        Rental rental =
                rentalRepository.findByRentalIdAndCompanyId(
                        request.getRentalId(),
                        request.getCompanyId()
                ).orElse(null);

        if (rental == null) {
            return null;
        }

        String rentalStatus = rental.getRentalStatus();

        if (!"ACTIVE".equalsIgnoreCase(rentalStatus)
                && !"OVERDUE".equalsIgnoreCase(rentalStatus)
                && !"PARTIALLY_RETURNED".equalsIgnoreCase(rentalStatus)) {
            throw new IllegalArgumentException(
                    "Only ACTIVE, OVERDUE, or PARTIALLY_RETURNED rentals can be extended."
            );
        }

        if (rental.getDueDate() == null) {
            throw new IllegalArgumentException(
                    "Current rental due date is missing."
            );
        }

        if (!request.getNewDueDate().isAfter(
                rental.getDueDate()
        )) {
            throw new IllegalArgumentException(
                    "New due date must be after the current due date."
            );
        }

        BigDecimal extensionCharge =
                request.getExtensionCharge();

        if (extensionCharge == null) {
            extensionCharge = BigDecimal.ZERO;
        }

        if (extensionCharge.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Extension charge cannot be negative."
            );
        }

        RentalExtension extension =
                new RentalExtension();

        extension.setRentalId(
                rental.getRentalId()
        );

        extension.setOldDueDate(
                rental.getDueDate()
        );

        extension.setNewDueDate(
                request.getNewDueDate()
        );

        extension.setExtensionCharge(
                extensionCharge
        );

        extension.setApprovedBy(
                request.getApprovedBy()
        );

        extension.setReason(
                request.getReason()
        );

        RentalExtension savedExtension =
                rentalExtensionRepository.save(
                        extension
                );

        rental.setDueDate(
                request.getNewDueDate()
        );

        rentalRepository.save(rental);

        return mapToResponse(savedExtension);
    }

    public List<RentalExtensionResponse>
    getExtensionHistory(Integer rentalId) {

        List<RentalExtension> extensions =
                rentalExtensionRepository
                        .findByRentalIdOrderByCreatedAtDesc(
                                rentalId
                        );

        return extensions.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private RentalExtensionResponse mapToResponse(
            RentalExtension extension) {

        RentalExtensionResponse response =
                new RentalExtensionResponse();

        response.setExtensionId(
                extension.getExtensionId()
        );

        response.setRentalId(
                extension.getRentalId()
        );

        response.setOldDueDate(
                extension.getOldDueDate()
        );

        response.setNewDueDate(
                extension.getNewDueDate()
        );

        response.setExtensionCharge(
                extension.getExtensionCharge()
        );

        response.setApprovedBy(
                extension.getApprovedBy()
        );

        response.setReason(
                extension.getReason()
        );

        response.setCreatedAt(
                extension.getCreatedAt()
        );

        return response;
    }
}