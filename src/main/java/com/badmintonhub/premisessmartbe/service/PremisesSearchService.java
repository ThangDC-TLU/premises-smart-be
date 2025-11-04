// src/main/java/com/badmintonhub/premisessmartbe/service/PremisesSearchService.java
package com.badmintonhub.premisessmartbe.service;

import com.badmintonhub.premisessmartbe.dto.PremisesSearchDTO;
import com.badmintonhub.premisessmartbe.entity.Premises;
import com.badmintonhub.premisessmartbe.repository.PremisesRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PremisesSearchService {

    private final PremisesRepository repo;

    public Page<Premises> search(PremisesSearchDTO q) {
        // sort
        Sort sort = Sort.by("createdAt").descending();
        if (q.getSort() != null && !q.getSort().isBlank()) {
            // format: field,dir
            String[] s = q.getSort().split(",");
            if (s.length == 2) {
                sort = "asc".equalsIgnoreCase(s[1]) ? Sort.by(s[0]).ascending() : Sort.by(s[0]).descending();
            } else {
                sort = Sort.by(q.getSort()).descending();
            }
        }
        Pageable pageable = PageRequest.of(
                q.getPage() != null ? q.getPage() : 0,
                q.getSize() != null ? q.getSize() : 20,
                sort
        );

        Specification<Premises> spec = (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (q.getLocation() != null && !q.getLocation().isBlank()) {
                String like = "%" + q.getLocation().trim() + "%";
                ps.add(cb.like(cb.lower(root.get("locationText")), like.toLowerCase()));
            }

            if (q.getKeyword() != null && !q.getKeyword().isBlank()) {
                String like = "%" + q.getKeyword().trim() + "%";
                Predicate inTitle = cb.like(cb.lower(root.get("title")), like.toLowerCase());
                Predicate inDesc  = cb.like(cb.lower(root.get("description")), like.toLowerCase());
                ps.add(cb.or(inTitle, inDesc));
            }

            if (q.getType() != null && !q.getType().isBlank()) {
                ps.add(cb.equal(cb.lower(root.get("businessType")), q.getType().toLowerCase()));
            }

            if (q.getMinPrice() != null) ps.add(cb.greaterThanOrEqualTo(root.get("price"), q.getMinPrice()));
            if (q.getMaxPrice() != null) ps.add(cb.lessThanOrEqualTo(root.get("price"), q.getMaxPrice()));
            if (q.getMinArea()  != null) ps.add(cb.greaterThanOrEqualTo(root.get("areaM2"), q.getMinArea()));
            if (q.getMaxArea()  != null) ps.add(cb.lessThanOrEqualTo(root.get("areaM2"), q.getMaxArea()));

            return cb.and(ps.toArray(new Predicate[0]));
        };

        return repo.findAll(spec, pageable);
    }
}
