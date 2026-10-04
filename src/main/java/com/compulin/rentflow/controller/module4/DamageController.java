package com.compulin.rentflow.controller.module4;

import com.compulin.rentflow.entity.module4.DamageRecord;
import com.compulin.rentflow.service.module4.DamageService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/damages")
@CrossOrigin(origins = "http://localhost:5173")
public class DamageController {

    private final DamageService damageService;

    public DamageController(DamageService damageService) {
        this.damageService = damageService;
    }

    @GetMapping
    public List<DamageRecord> getAllDamageRecords() {
        return damageService.getAllDamageRecords();
    }

    @GetMapping("/{damageId}")
    public DamageRecord getDamageRecord(
            @PathVariable Integer damageId) {

        return damageService.getDamageRecord(damageId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DamageRecord createDamageRecord(
            @RequestBody DamageRecord damageRecord) {

        return damageService.createDamageRecord(damageRecord);
    }

    @PutMapping("/{damageId}")
    public DamageRecord updateDamageRecord(
            @PathVariable Integer damageId,
            @RequestBody DamageRecord damageRecord) {

        return damageService.updateDamageRecord(
                damageId,
                damageRecord
        );
    }
}
