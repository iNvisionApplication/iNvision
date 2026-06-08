package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.Category;
import com.invision.web.Invision.enums.Condition;

public record AssetSearchRequest(String title, Category category, AssetStatus status, String location, Condition condition)
{}
