package com.compulin.rentflow.controller.module4;

import com.compulin.rentflow.dto.module4.ProcessReturnRequest;
import com.compulin.rentflow.dto.module4.ProcessReturnResponse;
import com.compulin.rentflow.dto.module4.RentalReturnDetailsDTO;
import com.compulin.rentflow.dto.module4.RentalSearchResultDTO;
import com.compulin.rentflow.entity.module4.RentalReturn;
import com.compulin.rentflow.entity.module4.ReturnItem;
import com.compulin.rentflow.service.module4.RentalReturnService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@CrossOrigin(origins = "http://localhost:5173")
public class ReturnController {

    private final RentalReturnService returnService;

    public ReturnController(RentalReturnService returnService) {
        this.returnService = returnService;
    }

    @GetMapping
    public List<RentalReturn> getAllReturns() {
        return returnService.getAllReturns();
    }

    @GetMapping("/{returnId}")
    public RentalReturn getReturnById(
            @PathVariable Integer returnId) {

        return returnService.getReturnById(returnId);
    }

    @GetMapping("/{returnId}/items")
    public List<ReturnItem> getReturnItems(
            @PathVariable Integer returnId) {

        return returnService.getReturnItems(returnId);
    }

    @GetMapping("/search")
    public List<RentalSearchResultDTO> searchRentals(
            @RequestParam String search) {

        return returnService.searchRentals(search);
    }

    @GetMapping("/rental/{rentalId}")
    public RentalReturnDetailsDTO getRentalForReturn(
            @PathVariable Integer rentalId) {

        return returnService.getRentalForReturn(rentalId);
    }

    @PostMapping("/process")
    public ProcessReturnResponse processReturn(
            @RequestBody ProcessReturnRequest request) {

        return returnService.processReturn(request);
    }
}

