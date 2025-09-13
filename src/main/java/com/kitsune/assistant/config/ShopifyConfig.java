package com.kitsune.assistant.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Getter
public class ShopifyConfig {
    @Value("${assistant.shopify.shop}") private String shop;
    @Value("${assistant.shopify.adminToken}") private String adminToken;
    @Value("${assistant.shopify.apiVersion}") private String apiVersion;
    @Value("${assistant.baseUrl}") private String baseUrl;

    @Bean
    public WebClient shopifyWebClient() {
        var strategies = ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
            .build();
        return WebClient.builder()
                .baseUrl("https://" + shop + ".myshopify.com/admin/api/" + apiVersion + "/graphql.json")
                .defaultHeader("X-Shopify-Access-Token", adminToken)
                .defaultHeader("Content-Type", "application/json")
                .exchangeStrategies(strategies)
                .build();
    }
}
