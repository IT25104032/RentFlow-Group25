package com.compulin.rentflow.service.module4;

import com.compulin.rentflow.entity.module4.RentalReturn;
import com.compulin.rentflow.entity.module4.ReturnItem;
import com.compulin.rentflow.repository.module4.RentalReturnRepository;
import com.compulin.rentflow.repository.module4.ReturnItemRepository;
import org.springframework.stereotype.Service;

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

    public RentalReturn createReturn(RentalReturn rentalReturn) {
        return rentalReturnRepository.save(rentalReturn);
    }

    public List<ReturnItem> getReturnItems(Integer returnId) {
        return returnItemRepository
                .findByRentalReturnReturnId(returnId);
    }

    public ReturnItem addReturnItem(ReturnItem returnItem) {

        if (returnItem.getQuantityReturned() <= 0) {
            throw new IllegalArgumentException(
                    "Returned quantity must be greater than zero");
        }

        return returnItemRepository.save(returnItem);
    }

    public void deleteReturn(Integer id) {
        rentalReturnRepository.deleteById(id);
    }
}
