package com.kitsune.assistant.chat.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatReply(String answer, List<ChatItem> items) {}
