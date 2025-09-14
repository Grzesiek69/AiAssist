package com.kitsune.assistant.chat;

import com.kitsune.assistant.model.ProductDoc;

import java.util.List;

public class AnswerFormatter {
  public static String format(List<ProductDoc> docs) {
    if (docs == null || docs.isEmpty()) {
      return "Nie znalazłem nic pasującego. Chcesz, żebym poszukał alternatyw?";
    }
    StringBuilder sb = new StringBuilder("Konkret:\n");
    for (ProductDoc d : docs) {
      sb.append("• ")
        .append(d.getTitle())
        .append(" [").append(d.getSku()).append("] — ")
        .append(d.getPrice()).append(' ').append(d.getCurrency())
        .append(" → ").append(d.getUrl());
      if (Boolean.TRUE.equals(d.getAvailable())) {
        sb.append(" (dostępne)");
      }
      sb.append("\n");
    }
    return sb.toString().trim();
  }
}
