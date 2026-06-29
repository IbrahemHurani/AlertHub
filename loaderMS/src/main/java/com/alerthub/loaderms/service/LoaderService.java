package com.alerthub.loaderms.service;

import com.alerthub.loaderms.dto.ScanResultDto;
import com.alerthub.loaderms.dto.ScannedFileResponseDto;

import java.util.List;

public interface LoaderService {

    ScanResultDto scanAllProviders();

    List<ScannedFileResponseDto> getScanHistory(int page, int size);
}
