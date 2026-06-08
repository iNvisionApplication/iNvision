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
    private Long assetId;

    @NotNull
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(unique = true)
    private String serialNumber;

    private LocalDateTime acquisitionDate;

    @Column(precision = 19,scale =4)
    private BigDecimal cost;

    @NotNull
    private String location;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssetStatus status;

    private String photoPath;

}
