package com.kitsune.assistant.search;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class IntentParser {
    private IntentParser(){}

    private static final Pattern PRICE = Pattern.compile("(?:do|<=|<|max)\s*(\d+[\.,]?\d*)\s*(pln|eur)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHASSIS = Pattern.compile("\b([A-Z]\d{2})\b");
    private static final Set<String> BODY = Set.of("sedan","touring","kombi","wagon","estate","coupe","hatch","hatchback");
    private static final Set<String> MAT  = Set.of("carbon","karbon","frp","alu","aluminium","steel","stal","titanium","titan");
    private static final Set<String> FIN  = Set.of("gloss","połysk","mat","matte","satin","satyna","forged");

    static QueryFilter parse(String q){
        var txt = (q==null? "" : q).toLowerCase(Locale.ROOT);

        String chassis = match(CHASSIS, txt);
        String body = BODY.stream().filter(txt::contains).findFirst().map(IntentParser::normBody).orElse(null);
        Boolean lci = txt.contains("lci") ? true : (txt.contains("pre-lci") || txt.contains("prelci") ? false : null);
        String material = MAT.stream().filter(txt::contains).findFirst().map(IntentParser::normMaterial).orElse(null);
        String finish = FIN.stream().filter(txt::contains).findFirst().map(IntentParser::normFinish).orElse(null);

        String category = null;
        if (txt.contains("dyfuzor") || txt.contains("diffuser")) category = "diffuser";
        else if (txt.contains("splitter")) category = "splitter";
        else if (txt.contains("dolot") || txt.contains("intake")) category = "intake";
        else if (txt.contains("wydech") || txt.contains("exhaust")) category = "exhaust";
        else if (txt.contains("grill") || txt.contains("grille")) category = "grille";

        String make = null; String carModel = null;
        if (txt.contains("bmw")) make = "BMW";
        if (txt.contains("audi")) make = "Audi";
        if (txt.contains("mercedes")) make = "Mercedes";
        if (txt.matches(".*\bm3\b.*")) carModel = "M3";
        if (txt.matches(".*\bm4\b.*")) carModel = "M4";

        BigDecimal pMax = null;
        Matcher m = PRICE.matcher(txt.replace(",", "."));
        if (m.find()) pMax = new BigDecimal(m.group(1));

        return QueryFilter.builder()
                .make(make).carModel(carModel).chassis(chassis)
                .bodyStyle(body).lci(lci).material(material).finish(finish)
                .category(category).priceMax(pMax)
                .build();
    }

    private static String normBody(String b){
        return switch (b) { case "kombi","wagon","estate" -> "touring"; case "hatch" -> "hatchback"; default -> b; };
    }
    private static String normMaterial(String m){
        if (m.startsWith("karbon")) return "carbon";
        if (m.startsWith("alu")) return "alu";
        if (m.startsWith("stal")) return "steel";
        if (m.startsWith("titan")) return "titanium";
        return m;
    }
    private static String normFinish(String f){
        return switch (f) { case "połysk" -> "gloss"; case "mat" -> "matte"; case "satyna" -> "satin"; default -> f; };
    }
    private static String match(Pattern p, String s){ var m = p.matcher(s); return m.find()? m.group(1).toUpperCase() : null; }
}
