package com.alerthub.loaderms.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.alerthub.loaderms.dto.ScanResultDto;
import com.alerthub.loaderms.dto.ScannedFileResponseDto;
import com.alerthub.loaderms.entity.PlatformInformation;
import com.alerthub.loaderms.entity.ScannedFile;
import com.alerthub.loaderms.repository.ScannedFileRepository;
import com.alerthub.loaderms.service.impl.LoaderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class LoaderServiceTest {

    @Mock
    private ScannedFileRepository scannedFileRepository;

    @Mock
    private ScanAuditService scanAuditService;

    @InjectMocks
    private LoaderServiceImpl loaderService;

    @TempDir
    Path tempDir;

    @SuppressWarnings({"null", "unused"})
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loaderService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(loaderService, "dataDirectory", tempDir.toString());
    }

    // -------------------------------------------------------------------------
    // scanAllProviders — file skipping
    // -------------------------------------------------------------------------

    @Test
    void scanAllProviders_shouldSkipAlreadyScannedFiles() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.writeString(githubDir.resolve("github_2024_01_01T00_00_00.json"), "[]");

        when(scannedFileRepository.existsByFileNameAndProvider("github_2024_01_01T00_00_00.json", "github"))
                .thenReturn(true);

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getSkippedFiles()).contains("github_2024_01_01T00_00_00.json");
        assertThat(result.getFilesScanned()).isZero();
        verify(scanAuditService, never()).saveRecordsAndAudit(any(), any(), any());
    }

    @Test
    void scanAllProviders_shouldSkipSubdirectories() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.createDirectories(githubDir.resolve("subdir"));

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFilesScanned()).isZero();
        verify(scanAuditService, never()).saveRecordsAndAudit(any(), any(), any());
        verify(scanAuditService, never()).saveFailureAudit(any(), any());
    }

    @Test
    void scanAllProviders_shouldReturnZerosWhenNoProviderDirectoriesExist() {
        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFilesScanned()).isZero();
        assertThat(result.getTotalRecordsLoaded()).isZero();
        assertThat(result.getProcessedFiles()).isEmpty();
        assertThat(result.getSkippedFiles()).isEmpty();
        assertThat(result.getFailedFiles()).isEmpty();
        verify(scanAuditService, never()).saveRecordsAndAudit(any(), any(), any());
    }

    @Test
    void scanAllProviders_shouldReturnZerosWhenProviderDirectoryIsEmpty() throws IOException {
        Files.createDirectories(tempDir.resolve("github"));

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFilesScanned()).isZero();
        assertThat(result.getTotalRecordsLoaded()).isZero();
        verify(scanAuditService, never()).saveRecordsAndAudit(any(), any(), any());
    }

    // -------------------------------------------------------------------------
    // scanAllProviders — GitHub parsing
    // -------------------------------------------------------------------------

    @Test
    void scanAllProviders_shouldParseGitHubFileAndSaveRecords() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.writeString(githubDir.resolve("github_2024_01_01T00_00_00.json"), """
                [
                  {
                    "manager_id": "mgr001",
                    "projects": "ProjectA",
                    "assignee": "dev001",
                    "label": "bug",
                    "devloper_id": "dev001",
                    "issue": 5,
                    "environment": "production",
                    "user_story": "US-101",
                    "point": 3,
                    "sprint": "Sprint-1"
                  }
                ]
                """);

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), anyString())).thenReturn(1);

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFilesScanned()).isEqualTo(1);
        assertThat(result.getTotalRecordsLoaded()).isEqualTo(1);
        assertThat(result.getProcessedFiles()).contains("github_2024_01_01T00_00_00.json");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlatformInformation>> captor = ArgumentCaptor.forClass(List.class);
        verify(scanAuditService).saveRecordsAndAudit(captor.capture(), eq("github_2024_01_01T00_00_00.json"), eq("github"));

        PlatformInformation saved = captor.getValue().get(0);
        assertThat(saved.getOwnerId()).isEqualTo("mgr001");
        assertThat(saved.getProject()).isEqualTo("ProjectA");
        assertThat(saved.getDeveloperId()).isEqualTo("dev001");
        assertThat(saved.getTaskNumber()).isEqualTo(5);
        assertThat(saved.getTaskPoint()).isEqualTo(3);
        assertThat(saved.getProvider()).isEqualTo("github");
    }

    @Test
    void scanAllProviders_shouldMapNullNumericFieldsToZero() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.writeString(githubDir.resolve("github_null_fields.json"), """
                [
                  {
                    "manager_id": "mgr001",
                    "projects": "ProjectA",
                    "devloper_id": "dev001",
                    "issue": null,
                    "point": null
                  }
                ]
                """);

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), anyString())).thenReturn(1);

        loaderService.scanAllProviders();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlatformInformation>> captor = ArgumentCaptor.forClass(List.class);
        verify(scanAuditService).saveRecordsAndAudit(captor.capture(), anyString(), anyString());

        PlatformInformation saved = captor.getValue().get(0);
        assertThat(saved.getTaskNumber()).isZero();
        assertThat(saved.getTaskPoint()).isZero();
    }

    @Test
    void scanAllProviders_shouldParseMultipleRecordsInOneFile() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.writeString(githubDir.resolve("github_multi.json"), """
                [
                  { "manager_id": "mgr001", "devloper_id": "dev001", "issue": 1, "point": 2 },
                  { "manager_id": "mgr002", "devloper_id": "dev002", "issue": 3, "point": 4 }
                ]
                """);

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), anyString())).thenReturn(2);

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getTotalRecordsLoaded()).isEqualTo(2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlatformInformation>> captor = ArgumentCaptor.forClass(List.class);
        verify(scanAuditService).saveRecordsAndAudit(captor.capture(), anyString(), eq("github"));
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue().get(0).getOwnerId()).isEqualTo("mgr001");
        assertThat(captor.getValue().get(1).getOwnerId()).isEqualTo("mgr002");
    }

    @Test
    void scanAllProviders_shouldProcessEmptyJsonArrayWithoutSavingRecords() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.writeString(githubDir.resolve("github_empty.json"), "[]");

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), anyString())).thenReturn(0);

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFilesScanned()).isEqualTo(1);
        assertThat(result.getTotalRecordsLoaded()).isZero();
        assertThat(result.getProcessedFiles()).contains("github_empty.json");
        verify(scanAuditService).saveRecordsAndAudit(anyList(), eq("github_empty.json"), eq("github"));
    }

    @Test
    void scanAllProviders_shouldProcessMultipleFilesInSameProvider() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.writeString(githubDir.resolve("github_file1.json"), "[{ \"manager_id\": \"mgr001\", \"devloper_id\": \"dev001\" }]");
        Files.writeString(githubDir.resolve("github_file2.json"), "[{ \"manager_id\": \"mgr002\", \"devloper_id\": \"dev002\" }]");

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), anyString())).thenReturn(1);

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFilesScanned()).isEqualTo(2);
        assertThat(result.getTotalRecordsLoaded()).isEqualTo(2);
        assertThat(result.getProcessedFiles()).containsExactlyInAnyOrder("github_file1.json", "github_file2.json");
        verify(scanAuditService, times(2)).saveRecordsAndAudit(anyList(), anyString(), eq("github"));
    }

    // -------------------------------------------------------------------------
    // scanAllProviders — Jira parsing
    // -------------------------------------------------------------------------

    @Test
    void scanAllProviders_shouldParseJiraFileCorrectly() throws IOException {
        Path jiraDir = Files.createDirectories(tempDir.resolve("jira"));
        Files.writeString(jiraDir.resolve("jira_2024_01_01T00_00_00.json"), """
                [
                  {
                    "manager_id": "mgr002",
                    "projects": "ProjectB",
                    "assignee": "dev004",
                    "label": "task",
                    "employeeID": "emp002",
                    "issue": 8,
                    "env": "production",
                    "user_story": "US-201",
                    "point": 2,
                    "sprint": "Sprint-2"
                  }
                ]
                """);

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), anyString())).thenReturn(1);

        loaderService.scanAllProviders();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlatformInformation>> captor = ArgumentCaptor.forClass(List.class);
        verify(scanAuditService).saveRecordsAndAudit(captor.capture(), anyString(), eq("jira"));

        PlatformInformation saved = captor.getValue().get(0);
        assertThat(saved.getOwnerId()).isEqualTo("mgr002");
        assertThat(saved.getDeveloperId()).isEqualTo("emp002");
        assertThat(saved.getEnvironment()).isEqualTo("production");
        assertThat(saved.getUserStory()).isEqualTo("US-201");
        assertThat(saved.getTaskPoint()).isEqualTo(2);
        assertThat(saved.getSprint()).isEqualTo("Sprint-2");
        assertThat(saved.getProvider()).isEqualTo("jira");
    }

    // -------------------------------------------------------------------------
    // scanAllProviders — ClickUp parsing
    // -------------------------------------------------------------------------

    @Test
    void scanAllProviders_shouldParseClickUpFileCorrectly() throws IOException {
        Path clickupDir = Files.createDirectories(tempDir.resolve("clickup"));
        Files.writeString(clickupDir.resolve("clickup_2024_01_01T00_00_00.json"), """
                [
                  {
                    "owner_id": "owner001",
                    "project": "ProjectA",
                    "tag": "worker001",
                    "label": "feature",
                    "worker_id": "worker001",
                    "task": 7,
                    "pr_env": "staging",
                    "user_story": "US-301",
                    "day": 3,
                    "currant_sprint": "Sprint-1"
                  }
                ]
                """);

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), anyString())).thenReturn(1);

        loaderService.scanAllProviders();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlatformInformation>> captor = ArgumentCaptor.forClass(List.class);
        verify(scanAuditService).saveRecordsAndAudit(captor.capture(), anyString(), eq("clickup"));

        PlatformInformation saved = captor.getValue().get(0);
        assertThat(saved.getOwnerId()).isEqualTo("owner001");
        assertThat(saved.getDeveloperId()).isEqualTo("worker001");
        assertThat(saved.getEnvironment()).isEqualTo("staging");
        assertThat(saved.getUserStory()).isEqualTo("US-301");
        assertThat(saved.getTaskPoint()).isEqualTo(3);
        assertThat(saved.getSprint()).isEqualTo("Sprint-1");
        assertThat(saved.getProvider()).isEqualTo("clickup");
    }

    // -------------------------------------------------------------------------
    // scanAllProviders — error handling
    // -------------------------------------------------------------------------

    @Test
    void scanAllProviders_shouldBlacklistFileOnParseError() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.writeString(githubDir.resolve("github_bad.json"), "NOT VALID JSON {{{{");

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFailedFiles()).contains("github_bad.json");
        assertThat(result.getFilesScanned()).isZero();
        verify(scanAuditService).saveFailureAudit("github_bad.json", "github");
    }

    @Test
    void scanAllProviders_shouldNotBlacklistFileOnTransientError() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.writeString(githubDir.resolve("github_valid.json"), "[{ \"manager_id\": \"mgr001\", \"devloper_id\": \"dev001\" }]");

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), anyString()))
                .thenThrow(new RuntimeException("DB unavailable"));

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFailedFiles()).contains("github_valid.json");
        assertThat(result.getFilesScanned()).isZero();
        verify(scanAuditService, never()).saveFailureAudit(anyString(), anyString());
    }

    // -------------------------------------------------------------------------
    // scanAllProviders — multi-provider and mixed results
    // -------------------------------------------------------------------------

    @Test
    void scanAllProviders_shouldSumRecordsAcrossAllThreeProviders() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Path jiraDir   = Files.createDirectories(tempDir.resolve("jira"));
        Path clickupDir = Files.createDirectories(tempDir.resolve("clickup"));

        Files.writeString(githubDir.resolve("github_file.json"),   "[{ \"manager_id\": \"m1\", \"devloper_id\": \"d1\" }]");
        Files.writeString(jiraDir.resolve("jira_file.json"),       "[{ \"manager_id\": \"m2\", \"employeeID\": \"e1\" }]");
        Files.writeString(clickupDir.resolve("clickup_file.json"), "[{ \"owner_id\": \"o1\", \"worker_id\": \"w1\" }]");

        when(scannedFileRepository.existsByFileNameAndProvider(anyString(), anyString())).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), eq("github"))).thenReturn(1);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), eq("jira"))).thenReturn(1);
        when(scanAuditService.saveRecordsAndAudit(anyList(), anyString(), eq("clickup"))).thenReturn(1);

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFilesScanned()).isEqualTo(3);
        assertThat(result.getTotalRecordsLoaded()).isEqualTo(3);
        assertThat(result.getProcessedFiles()).containsExactlyInAnyOrder("github_file.json", "jira_file.json", "clickup_file.json");
    }

    @Test
    void scanAllProviders_shouldReportMixedResultsInSingleScan() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.writeString(githubDir.resolve("github_new.json"),     "[{ \"manager_id\": \"m1\", \"devloper_id\": \"d1\" }]");
        Files.writeString(githubDir.resolve("github_seen.json"),    "[]");
        Files.writeString(githubDir.resolve("github_corrupt.json"), "{{NOT JSON}}");

        when(scannedFileRepository.existsByFileNameAndProvider("github_new.json",     "github")).thenReturn(false);
        when(scannedFileRepository.existsByFileNameAndProvider("github_seen.json",    "github")).thenReturn(true);
        when(scannedFileRepository.existsByFileNameAndProvider("github_corrupt.json", "github")).thenReturn(false);
        when(scanAuditService.saveRecordsAndAudit(anyList(), eq("github_new.json"), anyString())).thenReturn(1);

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getProcessedFiles()).containsExactly("github_new.json");
        assertThat(result.getSkippedFiles()).containsExactly("github_seen.json");
        assertThat(result.getFailedFiles()).containsExactly("github_corrupt.json");
        assertThat(result.getFilesScanned()).isEqualTo(1);
        assertThat(result.getTotalRecordsLoaded()).isEqualTo(1);
        verify(scanAuditService).saveFailureAudit("github_corrupt.json", "github");
    }

    // -------------------------------------------------------------------------
    // getScanHistory
    // -------------------------------------------------------------------------

    @SuppressWarnings("null")
    @Test
    void getScanHistory_shouldReturnPaginatedResults() {
        List<ScannedFile> history = List.of(
                ScannedFile.builder().id(1L).fileName("github_file.json").provider("github")
                        .scannedAt(LocalDateTime.now()).success(true).recordsLoaded(3).build(),
                ScannedFile.builder().id(2L).fileName("jira_file.json").provider("jira")
                        .scannedAt(LocalDateTime.now()).success(false).recordsLoaded(0).build()
        );
        when(scannedFileRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(history));

        List<ScannedFileResponseDto> result = loaderService.getScanHistory(0, 20);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFileName()).isEqualTo("github_file.json");
        assertThat(result.get(0).getProvider()).isEqualTo("github");
        assertThat(result.get(0).isSuccess()).isTrue();
        assertThat(result.get(1).isSuccess()).isFalse();
    }

    @Test
    void getScanHistory_shouldReturnEmptyListWhenNoHistory() {
        when(scannedFileRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<ScannedFileResponseDto> result = loaderService.getScanHistory(0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getScanHistory_shouldPassCorrectPageAndSizeToRepository() {
        when(scannedFileRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        loaderService.getScanHistory(2, 5);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(scannedFileRepository).findAll(pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(2);
        assertThat(captured.getPageSize()).isEqualTo(5);
    }

    @SuppressWarnings("null")
    @Test
    void getScanHistory_shouldMapAllFieldsCorrectly() {
        LocalDateTime scanTime = LocalDateTime.of(2024, 8, 22, 13, 30, 0);
        ScannedFile entity = ScannedFile.builder()
                .id(42L)
                .fileName("jira_data.json")
                .provider("jira")
                .scannedAt(scanTime)
                .success(true)
                .recordsLoaded(7)
                .build();
        when(scannedFileRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        List<ScannedFileResponseDto> result = loaderService.getScanHistory(0, 10);

        assertThat(result).hasSize(1);
        ScannedFileResponseDto dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getFileName()).isEqualTo("jira_data.json");
        assertThat(dto.getProvider()).isEqualTo("jira");
        assertThat(dto.getScannedAt()).isEqualTo(scanTime);
        assertThat(dto.isSuccess()).isTrue();
        assertThat(dto.getRecordsLoaded()).isEqualTo(7);
    }
}
