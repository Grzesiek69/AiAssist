package com.kitsune.assistant.search;

import com.kitsune.assistant.chat.dto.ChatItem;
import com.kitsune.assistant.model.ProductDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    private final SearchService search;

    @GetMapping
    public SearchResponse search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "make", required = false) String make,
            @RequestParam(value = "model", required = false) String carModel,
            @RequestParam(value = "chassis", required = false) String chassis,
            @RequestParam(value = "body", required = false) String bodyStyle,
            @RequestParam(value = "lci", required = false) Boolean lci,
            @RequestParam(value = "material", required = false) String material,
            @RequestParam(value = "finish", required = false) String finish,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "priceMin", required = false) BigDecimal priceMin,
            @RequestParam(value = "priceMax", required = false) BigDecimal priceMax,
            @RequestParam(value = "tag", required = false) Set<String> mustTags,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit
    ) {
        var f = QueryFilter.builder()
                .make(n(make)).carModel(n(carModel)).chassis(n(chassis)).bodyStyle(n(bodyStyle))
                .lci(lci).material(n(material)).finish(n(finish)).category(n(category))
                .vendor(n(vendor)).priceMin(priceMin).priceMax(priceMax)
                .mustTags(mustTags==null || mustTags.isEmpty()? null : mustTags)
                .build();

        var results = search.search(q, f, limit);
        var items = results.stream().map(ChatItem::from).collect(Collectors.toList());
        return new SearchResponse(items.size(), items, results);
    }

    private static String n(String s){ return (s==null || s.isBlank())? null : s.trim(); }

    public record SearchResponse(int count,
                                 List<com.kitsune.assistant.chat.dto.ChatItem> items,
                                 List<ProductDoc> raw) {}
}
