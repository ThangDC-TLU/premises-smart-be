package com.badmintonhub.premisessmartbe.controller;

import com.badmintonhub.premisessmartbe.dto.PremisesRequest;
import com.badmintonhub.premisessmartbe.entity.Premises;
import com.badmintonhub.premisessmartbe.service.PremisesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/premises")
@CrossOrigin(origins = "*")
public class PremisesController {
    private final PremisesService service;

    public PremisesController(PremisesService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Premises> create(@RequestBody PremisesRequest req,
                                           @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
       Premises created = service.createPremises(req, email);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Premises>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
