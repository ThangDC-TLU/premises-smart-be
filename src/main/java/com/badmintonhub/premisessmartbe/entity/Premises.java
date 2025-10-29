package com.badmintonhub.premisessmartbe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "premises")
public class Premises {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double price;
    private Double areaM2;
    private String businessType;

    private String locationText;

    private Double latitude;
    private Double longitude;

    // Lưu nhiều ảnh
    @ElementCollection
    @CollectionTable(name = "premises_images", joinColumns = @JoinColumn(name = "premises_id"))
    @Column(name = "image_url")
    private List<String> images;

    private String coverImage; // ảnh đại diện

    // Nếu muốn ràng buộc user đăng tin:
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
