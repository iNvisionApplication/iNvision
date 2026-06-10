package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Condition;

import java.time.LocalDateTime;

public record AssetRequestDTO(String title, String category, String serialNumber, LocalDateTime acquisitionDate, Double cost, String location,
                              Condition condition, String path) {
}
