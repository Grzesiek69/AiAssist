package com.kitsune.assistant.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "t_product_idx")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductDoc {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String type;
    private String productId; private String variantId; private String collectionId;
    private String handle; private String sku; private String title; private String vendor;
    @Type(JsonBinaryType.class) @Column(columnDefinition = "jsonb") private List<String> tags;
    @Type(JsonBinaryType.class) @Column(columnDefinition = "jsonb") private List<String> collections;
    @Column(columnDefinition = "text") private String descriptionText;
    @Type(JsonBinaryType.class) @Column(columnDefinition = "jsonb") private JsonNode metafields;
    private BigDecimal price; private String currency; private Boolean available;
    private String imageUrl; private String url;

    private String carModel;     private String chassis;    private String bodyStyle;
    private Boolean lci;         private String material;   private String finish;
    private String make;         private String category;
}
