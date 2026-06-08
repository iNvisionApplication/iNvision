package com.invision.web.Invision.model;

import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.Category;
import com.invision.web.Invision.enums.Condition;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name="assets")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")  // snake_case
    private Long assetId;

    @NotNull
    @Column(name = "title")
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    @Column(name = "serial_number", unique = true)  // snake_case
    private String serialNumber;

    @Column(name = "acquisition_date")
    private LocalDateTime acquisitionDate;

    @Column(name = "cost", precision = 19, scale = 4)
    private BigDecimal cost;

    @NotNull
    @Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition")
    private Condition condition;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssetStatus status;

    @Column(name = "photo_path")  // snake_case
    private String photoPath;
}