package com.invision.web.Invision.dto;

import com.invision.web.Invision.model.AssetStatus;
import com.invision.web.Invision.model.Condition;

import java.time.LocalDateTime;

public record AssetResponseDTO(String title, String category, String serialNumber, LocalDateTime acquisitionDate, double cost, String location,
                               Condition condition, String path, AssetStatus status) {
}
