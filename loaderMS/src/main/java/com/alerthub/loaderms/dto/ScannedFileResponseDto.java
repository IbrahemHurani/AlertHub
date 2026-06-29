package com.alerthub.loaderms.dto;

import com.alerthub.loaderms.entity.ScannedFile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScannedFileResponseDto {

    private Long id;
    private String fileName;
    private String provider;
    private LocalDateTime scannedAt;
    private boolean success;
    private int recordsLoaded;

    public static ScannedFileResponseDto from(ScannedFile entity) {
        return ScannedFileResponseDto.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .provider(entity.getProvider())
                .scannedAt(entity.getScannedAt())
                .success(entity.isSuccess())
                .recordsLoaded(entity.getRecordsLoaded())
                .build();
    }
}
