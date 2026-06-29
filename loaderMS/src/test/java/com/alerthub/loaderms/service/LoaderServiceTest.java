package com.alerthub.loaderms.service;

import com.alerthub.loaderms.dto.ScannedFileResponseDto;
import com.alerthub.loaderms.dto.ScanResultDto;
import com.alerthub.loaderms.entity.PlatformInformation;
import com.alerthub.loaderms.entity.ScannedFile;
import com.alerthub.loaderms.repository.ScannedFileRepository;
import com.alerthub.loaderms.service.impl.LoaderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loaderService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(loaderService, "dataDirectory", tempDir.toString());
    }

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
    void scanAllProviders_shouldSkipSubdirectories() throws IOException {
        Path githubDir = Files.createDirectories(tempDir.resolve("github"));
        Files.createDirectories(githubDir.resolve("subdir"));  // subdirectory — must be ignored

        ScanResultDto result = loaderService.scanAllProviders();

        assertThat(result.getFilesScanned()).isZero();
        verify(scanAuditService, never()).saveRecordsAndAudit(any(), any(), any());
        verify(scanAuditService, never()).saveFailureAudit(any(), any());
    }

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
        assertThat(saved.getSprint()).isEqualTo("Sprint-1");
        assertThat(saved.getProvider()).isEqualTo("clickup");
    }

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
        assertThat(saved.getProvider()).isEqualTo("jira");
    }

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
        assertThat(result.get(0).getProvider()).isEqualTo("github");
        assertThat(result.get(1).isSuccess()).isFalse();
        assertThat(result.get(0).getFileName()).isEqualTo("github_file.json");
    }
}
