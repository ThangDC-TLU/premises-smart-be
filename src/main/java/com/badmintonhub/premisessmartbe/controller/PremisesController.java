package com.badmintonhub.premisessmartbe.controller;

import com.badmintonhub.premisessmartbe.dto.ListingDetailDTO;
import com.badmintonhub.premisessmartbe.dto.PremisesRequest;
import com.badmintonhub.premisessmartbe.dto.PremisesSimilarDTO;
import com.badmintonhub.premisessmartbe.entity.Premises;
import com.badmintonhub.premisessmartbe.service.PremisesService;
import com.badmintonhub.premisessmartbe.service.PremisesSimilarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/premises")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class PremisesController {
    private final PremisesService service;
    private final PremisesSimilarService similarService;

    public PremisesController(PremisesService service, PremisesSimilarService similarService) {
        this.service = service;
        this.similarService = similarService;
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

    // GET /api/premises/{id} — xem chi tiết
    @GetMapping("/{id}")
    public ListingDetailDTO getDetail(@PathVariable Long id) {
        return service.getDetail(id);
    }

    // GET /api/premises/similar/{id}?limit=6
    @GetMapping("/similar/{id}")
    public List<PremisesSimilarDTO> getSimilar(@PathVariable Long id,
                                               @RequestParam(required = false) Integer limit) {
        return similarService.findSimilar(id, limit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
