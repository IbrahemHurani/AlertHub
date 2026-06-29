package com.alerthub.loaderms.service;

import com.alerthub.loaderms.entity.PlatformInformation;
import com.alerthub.loaderms.entity.ScannedFile;
import com.alerthub.loaderms.repository.PlatformInformationRepository;
import com.alerthub.loaderms.repository.ScannedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles all audit and record persistence in independent transactions so that
 * a failure in one file's processing never prevents the audit record from being saved.
 */
@Service
@RequiredArgsConstructor
public class ScanAuditService {

    private final PlatformInformationRepository platformInformationRepository;
    private final ScannedFileRepository scannedFileRepository;

    /**
     * Saves the parsed records and the success audit entry atomically.
     * REQUIRES_NEW ensures this transaction is independent of any outer transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int saveRecordsAndAudit(List<PlatformInformation> records, String fileName, String provider) {
        platformInformationRepository.saveAll(records);
        scannedFileRepository.save(ScannedFile.builder()
                .fileName(fileName)
                .provider(provider)
                .scannedAt(LocalDateTime.now())
                .success(true)
                .recordsLoaded(records.size())
                .build());
        return records.size();
    }

    /**
     * Saves a permanent failure audit entry in its own transaction so it always
     * commits regardless of any outer transaction state.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailureAudit(String fileName, String provider) {
        scannedFileRepository.save(ScannedFile.builder()
                .fileName(fileName)
                .provider(provider)
                .scannedAt(LocalDateTime.now())
                .success(false)
                .recordsLoaded(0)
                .build());
    }
}
