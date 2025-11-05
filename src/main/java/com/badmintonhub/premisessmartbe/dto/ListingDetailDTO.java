// src/main/java/com/badmintonhub/premisessmartbe/dto/ListingDetailDTO.java
package com.badmintonhub.premisessmartbe.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ListingDetailDTO {
    private Long id;
    private String title;
    private Double price;          // giữ Double đúng entity
    private Double area_m2;        // snake_case để FE dùng trực tiếp
    private String businessType;   // hiển thị (đã map nhãn)
    private String locationText;        // map từ locationText
    private Double rating;         // mặc định 0.0
    private List<String> images;
    private List<String> amenities;// mặc định []
    private String description;
    private Instant createdAt;      // mặc định null
    private Double latitude;
    private Double longitude;
    private String coverImage;     // ưu tiên coverImage, fallback images[0]

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Owner {
        private String name;
        private String phone;
        private String avatar;
    }
    private Owner owner;           // mặc định null

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Similar {
        private Long id;
        private String title;
        private Double price;
        private Double area_m2;
        private String img;
    }
    private List<Similar> similar; // mặc định []
}
