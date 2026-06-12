package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.Condition;
import com.invision.web.Invision.enums.Location;

import java.time.LocalDateTime;

public record AssetResponseDTO(Long assetId, String title, com.invision.web.Invision.enums.@jakarta.validation.constraints.NotNull Category category, String serialNumber, LocalDateTime acquisitionDate, java.math.BigDecimal cost, Location location,
                               Condition condition, String photoPath, AssetStatus status) {
}
