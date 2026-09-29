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
    public List<DamageRecord> getAll() {
        return damageService.getAllDamageRecords();
    }

    @GetMapping("/{id}")
    public DamageRecord getOne(
            @PathVariable Integer id) {

        return damageService.getDamageRecord(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DamageRecord create(
            @RequestBody DamageRecord damageRecord) {

        return damageService.createDamageRecord(damageRecord);
    }

    @PutMapping("/{id}")
    public DamageRecord update(
            @PathVariable Integer id,
            @RequestBody DamageRecord damageRecord) {

        return damageService.updateDamageRecord(
                id,
                damageRecord);
    }
}