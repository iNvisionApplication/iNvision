package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.Condition;

import java.time.LocalDateTime;

public record AssetResponseDTO(Long assetId, String title, String category, String serialNumber, LocalDateTime acquisitionDate, Double cost, String location,
                               Condition condition, String path, AssetStatus status) {
}
