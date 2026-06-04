package com.invision.web.Invision.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Data
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long assetId;

    private String title;

    private Category category;

    private String serialNumber;

    private LocalDateTime acquisitionDate;

    private double cost;

    private String location;

    private Condition condition;

    private String photoPath;

}
