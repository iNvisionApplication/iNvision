package com.invision.web.Invision.dto;

import com.invision.web.Invision.model.Category;
import com.invision.web.Invision.model.Condition;
import com.invision.web.Invision.model.AssetStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssetResponseDTO(String title, Category category, String serialNumber, LocalDateTime acquisitionDate, BigDecimal cost, String location,
                               Condition condition, String path, AssetStatus status) {
}
