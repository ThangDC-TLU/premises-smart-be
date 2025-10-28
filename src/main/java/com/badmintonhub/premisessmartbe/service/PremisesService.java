package com.badmintonhub.premisessmartbe.service;

import com.badmintonhub.premisessmartbe.dto.PremisesRequest;
import com.badmintonhub.premisessmartbe.entity.Premises;
import com.badmintonhub.premisessmartbe.entity.User;
import com.badmintonhub.premisessmartbe.repository.PremisesRepository;
import com.badmintonhub.premisessmartbe.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PremisesService {
    private final PremisesRepository repository;
    private final UserRepository userRepository;

    public PremisesService(PremisesRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Premises createPremises(PremisesRequest req, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Premises p = Premises.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .price(req.getPrice())
                .areaM2(req.getAreaM2())
                .businessType(req.getBusinessType())
                .address(req.getAddress())
                .district(req.getDistrict())
                .city(req.getCity())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .images(req.getImages())
                .coverImage(req.getCoverImage())
                .user(owner)                 // <-- gán owner từ JWT
                .build();

        return repository.save(p);
    }

    public List<Premises> getAll() {
        return repository.findAll();
    }

    public Premises getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
