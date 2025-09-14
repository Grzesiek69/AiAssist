package com.kitsune.assistant.chat;

import com.kitsune.assistant.chat.dto.ChatItem;
import com.kitsune.assistant.chat.dto.ChatReply;
import com.kitsune.assistant.chat.dto.ChatRequest;
import com.kitsune.assistant.model.ProductDoc;
import com.kitsune.assistant.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ChatController {
  private final SearchService searchService;

  @PostMapping("/chat")
  public ChatReply chat(@RequestBody ChatRequest req) {
    String msg = Optional.ofNullable(req.message()).orElse("");
    int limit = Optional.ofNullable(req.limit()).orElse(3);
    List<ProductDoc> docs = searchService.search(msg, limit);
    if (docs.isEmpty()) {
      return ChatReply.builder()
          .answer("Nie znalazłem nic pasującego. Chcesz, żebym poszukał alternatyw?")
          .items(List.of())
          .build();
    }
    return ChatReply.builder()
        .answer(AnswerFormatter.format(docs))
        .items(docs.stream().map(ChatItem::from).toList())
        .build();
  }
}
