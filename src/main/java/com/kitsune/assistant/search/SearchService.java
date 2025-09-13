package com.kitsune.assistant.search;

import com.kitsune.assistant.model.ProductDoc;
import com.kitsune.assistant.repo.ProductDocRepository;
import com.kitsune.assistant.util.TextUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final ProductDocRepository repo;
    private final EntityManager em;

    public List<ProductDoc> search(String query, int limit) { return search(query, null, limit); }

    public List<ProductDoc> search(String query, QueryFilter override, int limit) {
        var q = TextUtil.norm(query == null ? "" : query);

        if (override == null) {
            var bySku = trySku(q);
            if (!bySku.isEmpty()) return bySku;
            var byHandle = repo.findTop5ByHandleContainingIgnoreCaseOrderByAvailableDesc(q);
            if (!byHandle.isEmpty()) return byHandle;
        }

        var parsed = IntentParser.parse(query == null ? "" : query);
        var f = QueryFilter.builder()
                .make(nz(override, parsed, QueryFilter::make))
                .carModel(nz(override, parsed, QueryFilter::carModel))
                .chassis(nz(override, parsed, QueryFilter::chassis))
                .bodyStyle(nz(override, parsed, QueryFilter::bodyStyle))
                .lci(nz(override, parsed, QueryFilter::lci))
                .material(nz(override, parsed, QueryFilter::material))
                .finish(nz(override, parsed, QueryFilter::finish))
                .category(nz(override, parsed, QueryFilter::category))
                .vendor(nz(override, parsed, QueryFilter::vendor))
                .priceMin(nz(override, parsed, QueryFilter::priceMin))
                .priceMax(nz(override, parsed, QueryFilter::priceMax))
                .mustTags(nz(override, parsed, QueryFilter::mustTags))
                .build();

        var sql = new StringBuilder("select *, ts_rank_cd(searchable, plainto_tsquery('simple', :qtxt)) as rank from t_product_idx where (available is true or available is null) ");
        var params = new HashMap<String,Object>();
        params.put("qtxt", q);

        if (f.make() != null)       { sql.append(" and make = :make"); params.put("make", f.make()); }
        if (f.carModel() != null)   { sql.append(" and car_model = :model"); params.put("model", f.carModel()); }
        if (f.chassis() != null)    { sql.append(" and chassis = :chassis"); params.put("chassis", f.chassis()); }
        if (f.bodyStyle() != null)  { sql.append(" and body_style = :body"); params.put("body", f.bodyStyle()); }
        if (f.lci() != null)        { sql.append(" and lci = :lci"); params.put("lci", f.lci()); }
        if (f.material() != null)   { sql.append(" and material = :mat"); params.put("mat", f.material()); }
        if (f.finish() != null)     { sql.append(" and finish = :fin"); params.put("fin", f.finish()); }
        if (f.category() != null)   { sql.append(" and category = :cat"); params.put("cat", f.category()); }
        if (f.vendor() != null)     { sql.append(" and vendor = :vendor"); params.put("vendor", f.vendor()); }
        if (f.priceMin() != null)   { sql.append(" and price is not null and price >= :pmin"); params.put("pmin", f.priceMin()); }
        if (f.priceMax() != null)   { sql.append(" and price is not null and price <= :pmax"); params.put("pmax", f.priceMax()); }
        if (f.mustTags()!=null && !f.mustTags().isEmpty()) {
            sql.append(" and tags ?& :mtags");
            params.put("mtags", f.mustTags().toArray(new String[0]));
        }
        if (!q.isBlank()) {
            sql.append(" and (searchable @@ plainto_tsquery('simple', :qtxt) or unaccent(title) ilike concat('%', unaccent(:qtxt), '%')) ");
        }
        sql.append(" order by rank desc nulls last, available desc, price asc nulls last limit :lim");

        Query nq = em.createNativeQuery(sql.toString(), ProductDoc.class);
        params.forEach(nq::setParameter);
        nq.setParameter("lim", Math.min(Math.max(limit,1), 50));

        @SuppressWarnings("unchecked")
        var list = (List<ProductDoc>) nq.getResultList();
        if (!list.isEmpty()) return list;
        return repo.searchFulltext(q, limit);
    }

    private List<ProductDoc> trySku(String q) {
        var up = (q==null?"" : q).toUpperCase(Locale.ROOT);
        if (up.matches(".*[A-Z0-9\-]{4,}.*")) {
            var token = Arrays.stream(up.split("\s+"))
                    .filter(t -> t.matches("[A-Z0-9\-]{4,}"))
                    .findFirst().orElse(null);
            if (token != null) return repo.findTop5BySkuIgnoreCaseOrderByAvailableDesc(token);
        }
        return List.of();
    }

    private static <T> T nz(QueryFilter a, QueryFilter b, java.util.function.Function<QueryFilter, T> f) {
        var v = (a!=null)? f.apply(a) : null;
        return v != null ? v : (b!=null? f.apply(b) : null);
    }
}
