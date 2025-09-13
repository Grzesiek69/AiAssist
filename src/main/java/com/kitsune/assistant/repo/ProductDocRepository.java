package com.kitsune.assistant.repo;

import com.kitsune.assistant.model.ProductDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductDocRepository extends JpaRepository<ProductDoc, Long> {
    List<ProductDoc> findTop5BySkuIgnoreCaseOrderByAvailableDesc(String sku);
    List<ProductDoc> findTop5ByHandleContainingIgnoreCaseOrderByAvailableDesc(String handle);

    Optional<ProductDoc> findFirstByHandleAndSku(String handle, String sku);
    Optional<ProductDoc> findFirstByHandleAndSkuIsNull(String handle);

    @Query(value = """
            select * from t_product_idx 
            where (available is true or available is null)
              and searchable @@ plainto_tsquery('simple', :q)
            order by ts_rank_cd(searchable, plainto_tsquery('simple', :q)) desc
            limit :limit
            """, nativeQuery = true)
    List<ProductDoc> searchFulltext(String q, int limit);
}
