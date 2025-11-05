package com.badmintonhub.premisessmartbe.dto;

import java.time.Instant;
import java.util.List;

public record PremisesListItemDTO(
        Long id,
        String title,
        Double price,
        Double areaM2,
        String businessType,
        String locationText,
        Double latitude,
        Double longitude,
        String coverImage,
        List<String> images,
        Instant createdAt,
        String ownerEmail
) {}
