package com.kitsune.assistant.search;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.Set;

@Builder
public record QueryFilter(
    String make, String carModel, String chassis,
    String bodyStyle, Boolean lci,
    String material, String finish,
    String category, String vendor,
    BigDecimal priceMin, BigDecimal priceMax,
    Set<String> mustTags
) {}
