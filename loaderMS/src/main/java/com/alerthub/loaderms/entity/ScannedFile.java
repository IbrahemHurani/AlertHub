package com.alerthub.loaderms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "scanned_files",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_scanned_file_name_provider",
        columnNames = {"file_name", "provider"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScannedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String provider;

    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;

    private boolean success;

    @Column(name = "records_loaded")
    private int recordsLoaded;
}
