package com.badmintonhub.premisessmartbe.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremisesRequest {
    private String title;
    private String description;
    private Double price;
    private Double areaM2;
    private String businessType;

    private String locationText;

    private Double latitude;
    private Double longitude;

    private List<String> images;   // FE gửi mảng link ảnh Cloudinary
    private String coverImage;     // ảnh đầu tiên
}
