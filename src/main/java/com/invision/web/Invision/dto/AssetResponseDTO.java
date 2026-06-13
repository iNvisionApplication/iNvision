package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Category;
import com.invision.web.Invision.enums.Condition;
import com.invision.web.Invision.enums.AssetStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssetResponseDTO(Long assetId, String title, Category category, String serialNumber, LocalDateTime acquisitionDate, BigDecimal cost, String location,
                               Condition condition, String path, AssetStatus status) {
}
