// src/main/java/com/badmintonhub/premisessmartbe/controller/PremisesController.java
package com.badmintonhub.premisessmartbe.controller;

import com.badmintonhub.premisessmartbe.dto.ListingDetailDTO;
import com.badmintonhub.premisessmartbe.dto.PremisesRequest;
import com.badmintonhub.premisessmartbe.dto.PremisesSearchDTO;
import com.badmintonhub.premisessmartbe.dto.PremisesSimilarDTO;
import com.badmintonhub.premisessmartbe.entity.Premises;
import com.badmintonhub.premisessmartbe.service.PremisesSearchService;
import com.badmintonhub.premisessmartbe.service.PremisesService;
import com.badmintonhub.premisessmartbe.service.PremisesSimilarService;
import org.springframework.data.domain.Page;
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
    private final PremisesSearchService searchService;

    public PremisesController(PremisesService service,
                              PremisesSimilarService similarService,
                              PremisesSearchService searchService) {
        this.service = service;
        this.similarService = similarService;
        this.searchService = searchService;
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

    @GetMapping("/{id}")
    public ListingDetailDTO getDetail(@PathVariable Long id) {
        return service.getDetail(id);
    }

    @GetMapping("/similar/{id}")
    public List<PremisesSimilarDTO> getSimilar(@PathVariable Long id,
                                               @RequestParam(required = false) Integer limit) {
        return similarService.findSimilar(id, limit);
    }

    // >>>>>>>>>>>>> NEW: /api/premises/search
    @GetMapping("/search")
    public Page<Premises> search(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "type") String businessType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        PremisesSearchDTO q = new PremisesSearchDTO();
        q.setLocation(location);
        q.setKeyword(keyword);
        q.setType(businessType);
        q.setMinPrice(minPrice);
        q.setMaxPrice(maxPrice);
        q.setMinArea(minArea);
        q.setMaxArea(maxArea);
        q.setPage(page);
        q.setSize(size);
        q.setSort(sort);
        return searchService.search(q);
    }
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    @PutMapping("/{id}")
    public ResponseEntity<Premises> updatePut(@PathVariable Long id,
                                              @RequestBody PremisesRequest req,
                                              @AuthenticationPrincipal Jwt jwt) {
        Premises updated = service.updatePremises(id, req, jwt);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Premises> updatePatch(@PathVariable Long id,
                                                @RequestBody PremisesRequest req,
                                                @AuthenticationPrincipal Jwt jwt) {
        Premises updated = service.updatePremises(id, req, jwt);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
