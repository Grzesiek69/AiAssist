package com.kitsune.assistant.importer;

import com.kitsune.assistant.config.ShopifyConfig;
import com.kitsune.assistant.model.ProductDoc;
import com.kitsune.assistant.repo.ProductDocRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.kitsune.assistant.importer.CsvColumns.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {
    private final ProductDocRepository repo;
    private final ShopifyConfig cfg;

    @Transactional
    public ImportResult importCsv(InputStream is, boolean dryRun, String baseUrlOverride) throws Exception {
        var baseUrl = (baseUrlOverride != null && !baseUrlOverride.isBlank()) ? baseUrlOverride : cfg.getBaseUrl();
        var parser = CSVParser.parse(is, StandardCharsets.UTF_8,
                CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).setTrim(true).build());

        int created = 0, updated = 0, skipped = 0, total = 0, errors = 0;

        for (CSVRecord r : parser) {
            total++;
            try {
                var handle = get(r, HANDLE);
                if (isBlank(handle)) { skipped++; continue; }

                var sku = blankToNull(get(r, SKU));
                var title = blankToNull(get(r, TITLE));
                var priceStr = blankToNull(get(r, PRICE));
                var currency = blankToNull(get(r, CURRENCY));
                var available = toBool(get(r, AVAILABLE));
                var vendor = blankToNull(get(r, VENDOR));
                var image = blankToNull(get(r, IMAGE));
                var tags = splitList(get(r, TAGS));
                var cols = splitList(get(r, COLLECTIONS));
                var desc = blankToNull(get(r, DESCRIPTION));

                var make = blankToNull(get(r, MAKE));
                var model = blankToNull(get(r, MODEL));
                var chassis = blankToNull(get(r, CHASSIS));
                var body = normalizeBody(blankToNull(get(r, BODY)));
                var lci = toNullableBool(get(r, LCI));
                var material = normalizeMaterial(blankToNull(get(r, MATERIAL)));
                var finish = normalizeFinish(blankToNull(get(r, FINISH)));
                var category = normalizeCategory(blankToNull(get(r, CATEGORY)));

                var url = baseUrl + "/products/" + handle;

                BigDecimal price = null;
                if (priceStr != null) {
                    priceStr = priceStr.replace(",", ".").replaceAll("[^0-9.]", "");
                    if (!priceStr.isBlank()) price = new BigDecimal(priceStr);
                }

                Optional<ProductDoc> existing = (sku != null)
                        ? repo.findFirstByHandleAndSku(handle, sku)
                        : repo.findFirstByHandleAndSkuIsNull(handle);

                if (dryRun) {
                    if (existing.isPresent()) updated++; else created++;
                    continue;
                }

                var doc = existing.orElseGet(ProductDoc::new);
                doc.setType(sku != null ? "variant" : "product");
                doc.setHandle(handle);
                doc.setSku(sku);
                doc.setTitle(title);
                doc.setVendor(vendor);
                doc.setTags(tags);
                doc.setCollections(cols);
                doc.setDescriptionText(desc);
                doc.setPrice(price);
                doc.setCurrency(currency);
                doc.setAvailable(available);
                doc.setImageUrl(image);
                doc.setUrl(url);

                doc.setMake(make);
                doc.setCarModel(model);
                doc.setChassis(chassis == null ? null : chassis.toUpperCase());
                doc.setBodyStyle(body);
                doc.setLci(lci);
                doc.setMaterial(material);
                doc.setFinish(finish);
                doc.setCategory(category);

                repo.save(doc);
                if (existing.isPresent()) updated++; else created++;
            } catch (Exception e) {
                errors++;
                log.warn("CSV import error at line {}: {}", r.getRecordNumber(), e.getMessage());
            }
        }

        return new ImportResult(total, created, updated, skipped, errors);
    }

    public record ImportResult(int total, int created, int updated, int skipped, int errors) {}

    private static String get(CSVRecord r, String key) { return r.isMapped(key) ? r.get(key) : null; }
    private static boolean isBlank(String s){ return s == null || s.isBlank(); }
    private static String blankToNull(String s){ return isBlank(s) ? null : s.trim(); }
    private static Boolean toBool(String s){
        var b = toNullableBool(s);
        return b == null ? null : b;
    }
    private static Boolean toNullableBool(String s){
        if (s == null) return null;
        var x = s.trim().toLowerCase();
        return switch (x) {
            case "1","true","yes","y","tak" -> true;
            case "0","false","no","n","nie" -> false;
            default -> null;
        };
    }
    private static List<String> splitList(String s){
        if (s == null || s.isBlank()) return null;
        var arr = Arrays.stream(s.split("[,;]"))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .toList();
        return arr.isEmpty() ? null : arr;
    }

    private static String normalizeBody(String b){
        if (b == null) return null;
        var x = b.toLowerCase();
        if (x.equals("kombi") || x.equals("wagon") || x.equals("estate")) return "touring";
        if (x.equals("hatch")) return "hatchback";
        return x;
    }
    private static String normalizeMaterial(String m){
        if (m == null) return null;
        var x = m.toLowerCase();
        if (x.startsWith("karbon")) return "carbon";
        if (x.startsWith("alu")) return "alu";
        if (x.contains("titan")) return "titanium";
        if (x.contains("stal")) return "steel";
        return x;
    }
    private static String normalizeFinish(String f){
        if (f == null) return null;
        var x = f.toLowerCase();
        if (x.contains("połysk") || x.contains("polish") || x.contains("gloss")) return "gloss";
        if (x.contains("mat")) return "matte";
        if (x.contains("sat")) return "satin";
        if (x.contains("forged")) return "forged";
        return x;
    }
    private static String normalizeCategory(String c){
        if (c == null) return null;
        var x = c.toLowerCase();
        if (x.contains("dyfuzor") || x.contains("diffuser")) return "diffuser";
        if (x.contains("splitter")) return "splitter";
        if (x.contains("dolot") || x.contains("intake")) return "intake";
        if (x.contains("wydech") || x.contains("exhaust")) return "exhaust";
        if (x.contains("grill") || x.contains("grille")) return "grille";
        return x;
    }
}
