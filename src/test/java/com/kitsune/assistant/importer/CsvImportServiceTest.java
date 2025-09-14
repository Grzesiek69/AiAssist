package com.kitsune.assistant.importer;

import com.kitsune.assistant.config.ShopifyConfig;
import com.kitsune.assistant.model.ProductDoc;
import com.kitsune.assistant.repo.ProductDocRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

  @Mock ProductDocRepository repo;
  @Mock ShopifyConfig cfg;
  @InjectMocks CsvImportService service;

  @Test
  void importCreatesAndUpdates() throws Exception {
    when(cfg.getBaseUrl()).thenReturn("https://example.com");
    when(repo.findFirstByHandleAndSku(anyString(), anyString())).thenReturn(Optional.empty());

    InputStream is = new ClassPathResource("products.csv").getInputStream();
    var res1 = service.importCsv(is, false, null);
    assertThat(res1.created()).isEqualTo(1);
    assertThat(res1.updated()).isEqualTo(0);

    when(repo.findFirstByHandleAndSku(anyString(), anyString())).thenReturn(Optional.of(new ProductDoc()));
    is = new ClassPathResource("products.csv").getInputStream();
    var res2 = service.importCsv(is, false, null);
    assertThat(res2.created()).isEqualTo(0);
    assertThat(res2.updated()).isEqualTo(1);
  }
}
