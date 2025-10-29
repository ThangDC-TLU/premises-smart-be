package com.badmintonhub.premisessmartbe.service;// imports cần thiết
import com.badmintonhub.premisessmartbe.dto.PremisesSimilarDTO;
import com.badmintonhub.premisessmartbe.entity.Premises;
import com.badmintonhub.premisessmartbe.repository.PremisesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PremisesSimilarService {

    private final PremisesRepository repo;

    // (tuỳ chọn) dùng static class thay vì local record để IDE không cảnh báo
    private static final class Scored {
        final Premises p;
        final double score;
        Scored(Premises p, double score) { this.p = p; this.score = score; }
    }

    @Transactional(readOnly = true)
    public List<PremisesSimilarDTO> findSimilar(Long id, Integer limit) {
        int topN = (limit == null || limit <= 0) ? 6 : Math.min(limit, 12);

        Premises base = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Premises not found: " + id));

        List<Premises> candidates = repo.findCandidatesForSimilar(base.getId(), base.getBusinessType());
        if (candidates.isEmpty()) return List.of();

        String token = normalizeAddrToken(base.getLocationText());

        double basePrice = optDouble(base.getPrice());
        double baseArea  = optDouble(base.getAreaM2());
        Double baseLat   = base.getLatitude();
        Double baseLng   = base.getLongitude();

        List<Scored> scored = new ArrayList<>(candidates.size());
        for (Premises p : candidates) {
            double score = 0.0;

            double distKm = distanceKm(baseLat, baseLng, p.getLatitude(), p.getLongitude());
            if (!Double.isNaN(distKm)) {
                score += Math.max(0, 50.0 / (1.0 + distKm));
            }

            double price = optDouble(p.getPrice());
            if (basePrice > 0 && price > 0) {
                double diff = Math.abs(price - basePrice) / basePrice;
                score += Math.max(0, 30 * (1.0 - Math.min(diff, 1.0)));
            }

            double area = optDouble(p.getAreaM2());
            if (baseArea > 0 && area > 0) {
                double diff = Math.abs(area - baseArea) / baseArea;
                score += Math.max(0, 15 * (1.0 - Math.min(diff, 1.0)));
            }

            if (!token.isBlank()) {
                String candTok = normalizeAddrToken(p.getLocationText());
                if (!candTok.isBlank() && candTok.equals(token)) score += 10;
            }

            if (firstImageOrCover(p) != null) score += 2;

            scored.add(new Scored(p, score));
        }

        Comparator<Scored> cmp = Comparator
                .comparingDouble((Scored s) -> s.score).reversed()
                .thenComparingDouble(s -> {
                    double price = optDouble(s.p.getPrice());
                    return Math.abs(price - basePrice);
                });

        // JDK 11: dùng Collectors.toList()
        return scored.stream()
                .sorted(cmp)
                .limit(topN)
                .map(s -> toDTO(s.p))
                .collect(Collectors.toList());
    }

    // ---- Helpers ----

    private PremisesSimilarDTO toDTO(Premises p) {
        return PremisesSimilarDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .price(p.getPrice())
                .area_m2(p.getAreaM2())
                .coverImage(firstImageOrCover(p))
                .address(p.getLocationText())
                .build();
    }

    private static String firstImageOrCover(Premises p) {
        if (p.getCoverImage() != null && !p.getCoverImage().isBlank()) return p.getCoverImage();
        if (p.getImages() != null && !p.getImages().isEmpty()) return p.getImages().get(0);
        return null;
    }

    private static double optDouble(Double v) { return v == null ? 0.0 : v; }

    private static String normalizeAddrToken(String addr) {
        if (addr == null) return "";
        String s = addr.toLowerCase(Locale.ROOT);

        List<String> hints = List.of("quận ", "q.", "huyện ", "thị xã ", "tx.",
                "cầu giấy", "thanh xuân", "đống đa",
                "hai bà trưng", "ba đình", "tây hồ");
        for (String h : hints) {
            int i = s.indexOf(h);
            if (i >= 0) {
                String tail = s.substring(i).trim();
                String[] parts = tail.split("[,|-]");
                return parts[0].trim();
            }
        }
        int comma = s.indexOf(',');
        if (comma >= 0) {
            String head = s.substring(0, comma).trim();
            if (!head.isBlank()) return head;
        }
        return s.trim();
    }

    private static double distanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return Double.NaN;
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                + Math.sin(dLon/2)*Math.sin(dLon/2) - 1; // nhỏ trick? bỏ đi:
        // sửa công thức đúng:
        a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
