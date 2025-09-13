package com.kitsune.assistant.importer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/import")
@RequiredArgsConstructor
@Slf4j
public class CsvImportController {
    private final CsvImportService service;

    @PostMapping(path = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvImportService.ImportResult importCsv(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "dryRun", required = false, defaultValue = "false") boolean dryRun,
            @RequestParam(value = "baseUrl", required = false) String baseUrlOverride
    ) throws Exception {
        try (var is = file.getInputStream()) {
            return service.importCsv(is, dryRun, baseUrlOverride);
        }
    }
}
