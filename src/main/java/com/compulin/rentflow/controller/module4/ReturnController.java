package com.compulin.rentflow.controller.module4;

import com.compulin.rentflow.entity.module4.RentalReturn;
import com.compulin.rentflow.entity.module4.ReturnItem;
import com.compulin.rentflow.service.RentalReturnService;

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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReturn(@PathVariable Integer id) {
        returnService.deleteReturn(id);
    }
}
