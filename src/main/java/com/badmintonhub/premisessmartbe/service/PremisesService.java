// src/main/java/com/badmintonhub/premisessmartbe/service/PremisesService.java
package com.badmintonhub.premisessmartbe.service;

import com.badmintonhub.premisessmartbe.dto.PremisesRequest;
import com.badmintonhub.premisessmartbe.entity.Premises;
import com.badmintonhub.premisessmartbe.entity.User;
import com.badmintonhub.premisessmartbe.repository.PremisesRepository;
import com.badmintonhub.premisessmartbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PremisesService {

    private final PremisesRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public Premises createPremises(PremisesRequest req, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Premises p = Premises.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .price(req.getPrice())
                .areaM2(req.getAreaM2())
                .businessType(req.getBusinessType())
                .locationText(req.getLocationText())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .images(req.getImages())
                .coverImage(req.getCoverImage())
                .user(owner) // gán chủ tin
                .build();

        Premises saved = repository.save(p);
        return saved;
    }



    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Premises> getAll() {
        return repository.findAll();
    }
}
