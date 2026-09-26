package com.compulin.rentflow.repository.module4;

import com.compulin.rentflow.entity.module4.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnItemRepository
        extends JpaRepository<ReturnItem, Integer> {

    List<ReturnItem> findByRentalReturnReturnId(Integer returnId);

    List<ReturnItem> findByRentalItemRentalItemId(Integer rentalItemId);
}