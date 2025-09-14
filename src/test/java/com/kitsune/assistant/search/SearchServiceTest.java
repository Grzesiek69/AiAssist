package com.kitsune.assistant.search;

import com.kitsune.assistant.model.ProductDoc;
import com.kitsune.assistant.repo.ProductDocRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SearchServiceTest {

  ProductDocRepository repo = Mockito.mock(ProductDocRepository.class);
  SearchService service = new SearchService(repo, null) {
    @Override
    public List<ProductDoc> search(String query, QueryFilter override, int limit) {
      return repo.findAll().stream()
          .filter(d -> override.make() == null || override.make().equals(d.getMake()))
          .filter(d -> override.chassis() == null || override.chassis().equals(d.getChassis()))
          .filter(d -> override.material() == null || override.material().equals(d.getMaterial()))
          .filter(d -> override.priceMax() == null || (d.getPrice() != null && d.getPrice().compareTo(override.priceMax()) <= 0))
          .limit(limit)
          .toList();
    }
  };

  @BeforeEach
  void setup() {
    var p1 = ProductDoc.builder().type("product").handle("h1").sku("s1").title("t1")
        .make("BMW").chassis("G80").material("carbon")
        .price(new BigDecimal("2000")).currency("PLN").url("u1").build();
    var p2 = ProductDoc.builder().type("product").handle("h2").sku("s2").title("t2")
        .make("BMW").chassis("G80").material("alu")
        .price(new BigDecimal("1500")).currency("PLN").url("u2").build();
    when(repo.findAll()).thenReturn(List.of(p1, p2));
  }

  @Test
  void filtersMakeChassisMaterialAndPrice() {
    var filter = QueryFilter.builder()
        .make("BMW").chassis("G80").material("carbon")
        .priceMax(new BigDecimal("2500"))
        .build();
    var res = service.search("", filter, 10);
    assertThat(res).hasSize(1);
    assertThat(res.get(0).getSku()).isEqualTo("s1");
  }
}
