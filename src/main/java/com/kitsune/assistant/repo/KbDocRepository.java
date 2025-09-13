package com.kitsune.assistant.repo;

import com.kitsune.assistant.model.KbDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface KbDocRepository extends JpaRepository<KbDoc, Long> {
    @Query(value = """
      select * from t_kb_docs 
      where lang = :lang 
        and searchable @@ plainto_tsquery('simple', :q)
      order by ts_rank_cd(searchable, plainto_tsquery('simple', :q)) desc
      limit :limit
    """, nativeQuery = true)
    List<KbDoc> search(String q, String lang, int limit);
}
