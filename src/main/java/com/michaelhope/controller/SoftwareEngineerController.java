package com.michaelhope.controller;

import com.michaelhope.dto.SoftwareEngineerRequest;
import com.michaelhope.dto.SoftwareEngineerResponse;
import com.michaelhope.service.SoftwareEngineerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/software-engineer")
public class SoftwareEngineerController {

    private final SoftwareEngineerService service;

    public SoftwareEngineerController(SoftwareEngineerService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SoftwareEngineerResponse>> getEngineers() {
        return ResponseEntity.ok(service.getAllSoftwareEngineers());
    }

    @GetMapping("{id}")
    public ResponseEntity<SoftwareEngineerResponse> getEngineerById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getSoftwareEngineerById(id));
    }

    @PostMapping
    public ResponseEntity<SoftwareEngineerResponse> addEngineer(@Valid @RequestBody SoftwareEngineerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addSoftwareEngineer(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<SoftwareEngineerResponse> updateEngineer(
            @PathVariable Integer id,
            @Valid @RequestBody SoftwareEngineerRequest request) {
        return ResponseEntity.ok(service.updateSoftwareEngineer(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteEngineer(@PathVariable Integer id) {
        service.deleteSoftwareEngineer(id);
        return ResponseEntity.noContent().build();
    }
}
