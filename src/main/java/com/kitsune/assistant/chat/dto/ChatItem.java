package com.kitsune.assistant.chat.dto;

import com.kitsune.assistant.model.ProductDoc;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ChatItem(String title, String sku, BigDecimal price, String currency, Boolean available, String url) {
  public static ChatItem from(ProductDoc d) {
    return ChatItem.builder()
        .title(d.getTitle())
        .sku(d.getSku())
        .price(d.getPrice())
        .currency(d.getCurrency())
        .available(d.getAvailable())
        .url(d.getUrl())
        .build();
  }
}
