// src/main/java/com/badmintonhub/premisessmartbe/dto/PremisesSearchDTO.java
package com.badmintonhub.premisessmartbe.dto;

import lombok.Data;

@Data
public class PremisesSearchDTO {
    private String location;     // "Tây Sơn", "Đống Đa", ...
    private String keyword;      // tiêu đề/mô tả
    private String type;         // businessType: fnb/retail/office/warehouse/khac
    private Double minPrice;
    private Double maxPrice;
    private Double minArea;
    private Double maxArea;
    private Integer page = 0;
    private Integer size = 20;
    private String sort = "createdAt,desc"; // ví dụ: price,asc
}
