// src/main/java/com/badmintonhub/premisessmartbe/service/PremisesService.java
package com.badmintonhub.premisessmartbe.service;

import com.badmintonhub.premisessmartbe.dto.ListingDetailDTO;
import com.badmintonhub.premisessmartbe.dto.PremisesRequest;
import com.badmintonhub.premisessmartbe.entity.Premises;
import com.badmintonhub.premisessmartbe.entity.User;
import com.badmintonhub.premisessmartbe.repository.PremisesRepository;
import com.badmintonhub.premisessmartbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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

    // đổi key trong DB (fnb/retail/office) sang label hiển thị cho FE
    private String toLabel(String key) {
        if (key == null) return "Khác";
        return switch (key.toLowerCase()) {
            case "fnb" -> "F&B";
            case "retail" -> "Bán lẻ";
            case "office" -> "Văn phòng";
            default -> key;
        };
    }


    @Transactional(readOnly = true)
    public ListingDetailDTO getDetail(Long id) {
        Premises p = repository.findWithUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Premises not found: " + id));

        String cover = p.getCoverImage();
        if (cover == null && p.getImages() != null && !p.getImages().isEmpty()) {
            cover = p.getImages().get(0);
        }

        return ListingDetailDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .price(p.getPrice())
                .area_m2(p.getAreaM2())
                .businessType(toLabel(p.getBusinessType()))
                .address(p.getLocationText())
                .rating(0.0)
                .images(p.getImages())
                .amenities(java.util.Collections.emptyList())
                .description(p.getDescription())
                .createdAt(null)
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .coverImage(cover)
                .owner(mapOwner(p.getUser()))  // ← map entity → DTO
                .similar(java.util.Collections.emptyList())
                .build();
    }

    private ListingDetailDTO.Owner mapOwner(User u) {
        if (u == null) return null;

        // Tùy tên field trong User của bạn. Ví dụ nếu User có:
        // - getFullName() hoặc getName()
        // - getPhone() hoặc getPhoneNumber()
        // - getAvatarUrl() hoặc getAvatar()
        String name   = safeFirstNonNull(u.getFullName());
        String phone  = safeFirstNonNull(u.getPhone());

        return ListingDetailDTO.Owner.builder()
                .name(name)
                .phone(phone)
                .build();
    }

    @SafeVarargs
    private static <T> T safeFirstNonNull(T... vals) {
        for (T v : vals) if (v != null && !(v instanceof String s && s.isBlank())) return v;
        return null;
    }


    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Premises> getAll() {
        return repository.findAll();
    }
}
