package com.alerthub.loaderms.repository;

import com.alerthub.loaderms.entity.ScannedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScannedFileRepository extends JpaRepository<ScannedFile, Long> {

    boolean existsByFileNameAndProvider(String fileName, String provider);
}
