package com.badmintonhub.premisessmartbe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PremisesDetailDTO {
    private Long id;

    private String title;
    private String description;
    private Double price;
    private Double areaM2;
    private String businessType;

    private String locationText;
    private Double latitude;
    private Double longitude;

    private String coverImage;
    private List<String> images;

    // Chỉ expose thông tin tối thiểu của chủ tin để tránh lộ dữ liệu nhạy cảm
    private OwnerDTO owner;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OwnerDTO {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
    }
}
