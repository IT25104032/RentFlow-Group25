package com.compulin.rentflow.service.module4;

import com.compulin.rentflow.entity.module4.RentalReturn;
import com.compulin.rentflow.entity.module4.ReturnItem;
import com.compulin.rentflow.repository.module4.RentalReturnRepository;
import com.compulin.rentflow.repository.module4.ReturnItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RentalReturnService {

    private final RentalReturnRepository rentalReturnRepository;
    private final ReturnItemRepository returnItemRepository;

    public RentalReturnService(
            RentalReturnRepository rentalReturnRepository,
            ReturnItemRepository returnItemRepository) {

        this.rentalReturnRepository = rentalReturnRepository;
        this.returnItemRepository = returnItemRepository;
    }

    public List<RentalReturn> getAllReturns() {
        return rentalReturnRepository.findAll();
    }

    public RentalReturn getReturnById(Integer id) {
        return rentalReturnRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Return not found"));
    }

    public List<RentalReturn> getReturnsByRental(Integer rentalId) {
        return rentalReturnRepository.findByRentalId(rentalId);
    }

    public RentalReturn createReturn(RentalReturn rentalReturn) {

        if (rentalReturn.getReturnDate() == null) {
            rentalReturn.setReturnDate(LocalDateTime.now());
        }

        if (rentalReturn.getReturnType() == null ||
                (!rentalReturn.getReturnType().equals("FULL") &&
                        !rentalReturn.getReturnType().equals("PARTIAL"))) {

            throw new IllegalArgumentException(
                    "Return type must be FULL or PARTIAL");
        }

        return rentalReturnRepository.save(rentalReturn);
    }

    public List<ReturnItem> getReturnItems(Integer returnId) {
        return returnItemRepository.findByReturnId(returnId);
    }

    public ReturnItem addReturnItem(ReturnItem returnItem) {

        if (returnItem.getQuantityReturned() == null ||
                returnItem.getQuantityReturned() <= 0) {

            throw new IllegalArgumentException(
                    "Returned quantity must be greater than zero");
        }

        if (returnItem.getReturnedAt() == null) {
            returnItem.setReturnedAt(LocalDateTime.now());
        }

        return returnItemRepository.save(returnItem);
    }
}