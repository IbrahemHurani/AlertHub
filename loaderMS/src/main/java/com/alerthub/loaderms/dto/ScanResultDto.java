package com.alerthub.loaderms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultDto {

    private int filesScanned;
    private int totalRecordsLoaded;
    private List<String> processedFiles;
    private List<String> skippedFiles;
    private List<String> failedFiles;
}
