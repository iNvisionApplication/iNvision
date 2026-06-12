package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Category;
import com.invision.web.Invision.enums.Condition;
import com.invision.web.Invision.enums.Location;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record AssetRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\s-_]+$", message = "Title contains invalid characters")
        String title,

        @NotNull(message = "Category is required")
        Category category,

        @NotBlank(message = "Serial number is required")
        @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Serial number must be alphanumeric")
        String serialNumber,

        @NotNull(message = "Acquisition date is required")
        @PastOrPresent(message = "Acquisition date cannot be in the future")
        LocalDateTime acquisitionDate,

        @PositiveOrZero(message = "Cost cannot be negative")
        double cost,

        @NotNull(message = "Location is required")
        Location location,

        @NotNull(message = "Condition is required")
        Condition condition,

        @Size(message = "File path is required")
        String path
) {}
