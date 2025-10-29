// src/main/java/com/badmintonhub/premisessmartbe/dto/PremisesSimilarDTO.java
package com.badmintonhub.premisessmartbe.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PremisesSimilarDTO {
    private Long id;
    private String title;
    private Double price;
    private Double area_m2;     // snake_case cho FE
    private String coverImage;  // hoặc images[0]
    private String address;     // locationText
}
