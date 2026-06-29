package com.alerthub.loaderms.service.impl;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.alerthub.loaderms.dto.ClickUpDataDto;
import com.alerthub.loaderms.dto.GitHubDataDto;
import com.alerthub.loaderms.dto.JiraDataDto;
import com.alerthub.loaderms.dto.ScanResultDto;
import com.alerthub.loaderms.dto.ScannedFileResponseDto;
import com.alerthub.loaderms.entity.PlatformInformation;
import com.alerthub.loaderms.exception.FileParsingException;
import com.alerthub.loaderms.repository.ScannedFileRepository;
import com.alerthub.loaderms.service.LoaderService;
import com.alerthub.loaderms.service.ScanAuditService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoaderServiceImpl implements LoaderService {

    private static final String GITHUB = "github";
    private static final String JIRA = "jira";
    private static final String CLICKUP = "clickup";

    private final ScannedFileRepository scannedFileRepository;
    private final ScanAuditService scanAuditService;
    private final ObjectMapper objectMapper;

    @Value("${loader.data.directory:data}")
    private String dataDirectory;
    // Fix #8: resolve relative path to absolute at startup so deployment location is unambiguous


    // Fix #2: synchronized prevents scheduler and manual POST from running concurrently,
    // eliminating the TOCTOU window on the existsByFileNameAndProvider check.
    @Override
    public synchronized ScanResultDto scanAllProviders() {
        List<String> processed = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        int totalRecords = 0;

        for (String provider : List.of(GITHUB, JIRA, CLICKUP)) {
            File providerDir = new File(dataDirectory + File.separator + provider);
            if (!providerDir.exists() || !providerDir.isDirectory()) {
                log.warn("Provider directory not found: {}", providerDir.getAbsolutePath());
                continue;
            }

            File[] files = providerDir.listFiles();
            if (files == null) continue;

            for (File file : files) {
                // Fix #3: skip subdirectories — listFiles() returns everything under the folder
                if (!file.isFile()) continue;

                String fileName = file.getName();

                // Fix #4: uniqueness is now (file_name, provider), so check both
                if (scannedFileRepository.existsByFileNameAndProvider(fileName, provider)) {
                    skipped.add(fileName);
                    log.debug("Skipping already scanned file: {}", fileName);
                    continue;
                }

                try {
                    List<PlatformInformation> records = parseFile(file, provider);

                    // Fix #1: saveRecordsAndAudit runs in REQUIRES_NEW — always commits independently
                    int count = scanAuditService.saveRecordsAndAudit(records, fileName, provider);
                    totalRecords += count;
                    processed.add(fileName);
                    log.info("Processed file: {} ({} records)", fileName, count);

                } catch (FileParsingException e) {
                    // Fix #5: parse failures are permanent — blacklist the file
                    failed.add(fileName);
                    log.error("Permanent parse failure for file {}: {}", fileName, e.getMessage());
                    scanAuditService.saveFailureAudit(fileName, provider);

                } catch (Exception e) {
                    // Fix #5: transient failures (DB down, I/O error) — log but do NOT blacklist,
                    // so the file is retried on the next scan
                    failed.add(fileName);
                    log.error("Transient error processing file {}, will retry next scan: {}", fileName, e.getMessage(), e);
                }
            }
        }

        return ScanResultDto.builder()
                .filesScanned(processed.size())
                .totalRecordsLoaded(totalRecords)
                .processedFiles(processed)
                .skippedFiles(skipped)
                .failedFiles(failed)
                .build();
    }

    // Fix #6: paginated — never loads the full table into memory
    @Override
    public List<ScannedFileResponseDto> getScanHistory(int page, int size) {
        return scannedFileRepository
                .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scannedAt")))
                .stream()
                .map(ScannedFileResponseDto::from)
                .toList();
    }

    private List<PlatformInformation> parseFile(File file, String provider) {
        return switch (provider) {
            case GITHUB -> parseGitHub(file);
            case JIRA -> parseJira(file);
            case CLICKUP -> parseClickUp(file);
            default -> throw new FileParsingException("Unknown provider: " + provider);
        };
    }

    private List<PlatformInformation> parseGitHub(File file) {
        try {
            List<GitHubDataDto> dtos = objectMapper.readValue(file, new TypeReference<>() {});
            LocalDateTime scanTime = LocalDateTime.now();
            return dtos.stream()
                    .map(dto -> PlatformInformation.builder()
                            .timestamp(scanTime)
                            .ownerId(dto.getManagerId())
                            .project(dto.getProjects())
                            .tag(dto.getAssignee())
                            .label(dto.getLabel())
                            .developerId(dto.getDeveloperId())
                            .taskNumber(nullToZero(dto.getIssue()))
                            .environment(dto.getEnvironment())
                            .userStory(dto.getUserStory())
                            .taskPoint(nullToZero(dto.getPoint()))
                            .sprint(dto.getSprint())
                            .provider(GITHUB)
                            .build())
                    .toList();
        } catch (IOException e) {
            throw new FileParsingException("Failed to parse GitHub file: " + file.getName(), e);
        }
    }

    private List<PlatformInformation> parseJira(File file) {
        try {
            List<JiraDataDto> dtos = objectMapper.readValue(file, new TypeReference<>() {});
            LocalDateTime scanTime = LocalDateTime.now();
            return dtos.stream()
                    .map(dto -> PlatformInformation.builder()
                            .timestamp(scanTime)
                            .ownerId(dto.getManagerId())
                            .project(dto.getProjects())
                            .tag(dto.getAssignee())
                            .label(dto.getLabel())
                            .developerId(dto.getEmployeeID())
                            .taskNumber(nullToZero(dto.getIssue()))
                            .environment(dto.getEnv())
                            .userStory(dto.getUserStory())
                            .taskPoint(nullToZero(dto.getPoint()))
                            .sprint(dto.getSprint())
                            .provider(JIRA)
                            .build())
                    .toList();
        } catch (IOException e) {
            throw new FileParsingException("Failed to parse Jira file: " + file.getName(), e);
        }
    }

    private List<PlatformInformation> parseClickUp(File file) {
        try {
            List<ClickUpDataDto> dtos = objectMapper.readValue(file, new TypeReference<>() {});
            LocalDateTime scanTime = LocalDateTime.now();
            return dtos.stream()
                    .map(dto -> PlatformInformation.builder()
                            .timestamp(scanTime)
                            .ownerId(dto.getOwnerId())
                            .project(dto.getProject())
                            .tag(dto.getTag())
                            .label(dto.getLabel())
                            .developerId(dto.getWorkerId())
                            .taskNumber(nullToZero(dto.getTask()))
                            .environment(dto.getPrEnv())
                            .userStory(dto.getUserStory())
                            .taskPoint(nullToZero(dto.getDay()))
                            .sprint(dto.getCurrentSprint())
                            .provider(CLICKUP)
                            .build())
                    .toList();
        } catch (IOException e) {
            throw new FileParsingException("Failed to parse ClickUp file: " + file.getName(), e);
        }
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
