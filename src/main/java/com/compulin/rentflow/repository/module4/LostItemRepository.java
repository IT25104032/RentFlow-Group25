package com.compulin.rentflow.repository.module4;

import com.compulin.rentflow.entity.module4.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LostItemRepository
        extends JpaRepository<LostItem, Integer> {

    List<LostItem> findByRentalItemIdIn(List<Integer> rentalItemIds);
}
