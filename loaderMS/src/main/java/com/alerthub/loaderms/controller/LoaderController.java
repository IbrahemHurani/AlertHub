package com.alerthub.loaderms.controller;

import com.alerthub.loaderms.dto.ScanResultDto;
import com.alerthub.loaderms.dto.ScannedFileResponseDto;
import com.alerthub.loaderms.service.LoaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loader")
@RequiredArgsConstructor
@Tag(name = "Loader", description = "Endpoints for triggering data scans and viewing scan history")
public class LoaderController {

    private final LoaderService loaderService;

    @PostMapping("/scan")
    @Operation(summary = "Trigger manual scan", description = "Manually initiates a scan for new data files from all providers")
    public ResponseEntity<ScanResultDto> triggerScan() {
        return ResponseEntity.ok(loaderService.scanAllProviders());
    }

    // Fix #6: paginated to avoid loading the full table into memory
    // Fix #7: returns ScannedFileResponseDto instead of the JPA entity directly
    @GetMapping("/history")
    @Operation(summary = "Get scan history", description = "Returns a paginated list of all previously scanned files with their status")
    public ResponseEntity<List<ScannedFileResponseDto>> getScanHistory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(loaderService.getScanHistory(page, size));
    }
}
