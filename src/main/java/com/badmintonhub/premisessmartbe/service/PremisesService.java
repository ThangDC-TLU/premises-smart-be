// src/main/java/com/badmintonhub/premisessmartbe/service/PremisesService.java
package com.badmintonhub.premisessmartbe.service;

import com.badmintonhub.premisessmartbe.dto.ListingDetailDTO;
import com.badmintonhub.premisessmartbe.dto.PremisesListItemDTO;
import com.badmintonhub.premisessmartbe.dto.PremisesRequest;
import com.badmintonhub.premisessmartbe.entity.Premises;
import com.badmintonhub.premisessmartbe.entity.User;
import com.badmintonhub.premisessmartbe.repository.PremisesRepository;
import com.badmintonhub.premisessmartbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
                .locationText(p.getLocationText())
                .rating(0.0)
                .images(p.getImages())
                .amenities(java.util.Collections.emptyList())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
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

    public List<PremisesListItemDTO> getAllForList() {
        // Nếu dùng findAllWithUser() thì gọi hàm đó
        return repository.findAll().stream().map(this::toListItem).toList();
    }

    private PremisesListItemDTO toListItem(Premises p) {
        String ownerEmail = null;
        if (p.getUser() != null) {
            ownerEmail = p.getUser().getEmail();
        }
        return new PremisesListItemDTO(
                p.getId(),
                p.getTitle(),
                p.getPrice(),
                p.getAreaM2(),
                p.getBusinessType(),
                p.getLocationText(),
                p.getLatitude(),
                p.getLongitude(),
                p.getCoverImage(),
                p.getImages(),
                p.getCreatedAt(),
                ownerEmail
        );
    }

    @Transactional
    public Premises updatePremises(Long id, PremisesRequest req, Jwt jwt) {
        Premises p = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Premises not found: " + id));

        // --- quyền sửa: chủ tin hoặc ADMIN
        String editorEmail = jwt != null ? jwt.getSubject() : null;
        boolean isOwner = p.getUser() != null
                && editorEmail != null
                && editorEmail.equalsIgnoreCase(p.getUser().getEmail());
        boolean isAdmin = false;
        if (jwt != null) {
            var roles = jwt.getClaimAsStringList("roles");
            if (roles != null) isAdmin = roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
            // fallback khi dùng scope/authorities khác
            if (!isAdmin) {
                var scope = jwt.getClaimAsString("scope");
                isAdmin = scope != null && scope.contains("admin");
            }
        }
        if (!(isOwner || isAdmin)) {
            throw new AccessDeniedException("Bạn không có quyền sửa tin này");
        }

        // --- merge non-null fields
        if (StringUtils.hasText(req.getTitle()))         p.setTitle(req.getTitle());
        if (StringUtils.hasText(req.getDescription()))   p.setDescription(req.getDescription());
        if (req.getPrice() != null)                      p.setPrice(req.getPrice());
        if (req.getAreaM2() != null)                     p.setAreaM2(req.getAreaM2());
        if (StringUtils.hasText(req.getBusinessType()))  p.setBusinessType(req.getBusinessType());
        if (StringUtils.hasText(req.getLocationText()))  p.setLocationText(req.getLocationText());
        if (req.getLatitude() != null)                   p.setLatitude(req.getLatitude());
        if (req.getLongitude() != null)                  p.setLongitude(req.getLongitude());

        // ảnh
        if (req.getImages() != null)                     p.setImages(req.getImages());
        if (StringUtils.hasText(req.getCoverImage()))    p.setCoverImage(req.getCoverImage());

        // fallback cover nếu đang null
        if (!StringUtils.hasText(p.getCoverImage())
                && p.getImages() != null && !p.getImages().isEmpty()) {
            p.setCoverImage(p.getImages().get(0));
        }

        return repository.save(p);
    }
}
