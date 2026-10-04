package com.compulin.rentflow.controller.module4;

import com.compulin.rentflow.entity.module4.RentalReturn;
import com.compulin.rentflow.entity.module4.ReturnItem;
import com.compulin.rentflow.service.module4.RentalReturnService;

import com.compulin.rentflow.dto.module4.ProcessReturnRequest;
import com.compulin.rentflow.dto.module4.ProcessReturnResponse;
import com.compulin.rentflow.dto.module4.RentalSearchResultDTO;
import com.compulin.rentflow.dto.module4.RentalReturnDetailsDTO;

import org.springframework.http.HttpStatus;
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

    @GetMapping("/{id}")
    public RentalReturn getReturn(@PathVariable Integer id) {
        return returnService.getReturnById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalReturn createReturn(
            @RequestBody RentalReturn rentalReturn) {

        return returnService.createReturn(rentalReturn);
    }

    @GetMapping("/{id}/items")
    public List<ReturnItem> getReturnItems(
            @PathVariable Integer id) {

        return returnService.getReturnItems(id);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnItem addReturnItem(
            @RequestBody ReturnItem returnItem) {

        return returnService.addReturnItem(returnItem);
    }

    @PostMapping("/process")
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessReturnResponse processReturn(
            @RequestBody ProcessReturnRequest request) {

        return returnService.processReturn(request);
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

}
