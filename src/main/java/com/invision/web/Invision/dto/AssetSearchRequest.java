package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.Category;
import com.invision.web.Invision.enums.Condition;
import com.invision.web.Invision.enums.Location;
import jakarta.validation.constraints.Size;

public record AssetSearchRequest(
        @Size(max = 100, message = "Search title is too long")
        String title,

        Category category,

        AssetStatus status,

        Location location,

        Condition condition
) {}
