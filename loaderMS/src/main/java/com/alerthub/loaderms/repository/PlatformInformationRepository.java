package com.alerthub.loaderms.repository;

import com.alerthub.loaderms.entity.PlatformInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformInformationRepository extends JpaRepository<PlatformInformation, Long> {
}
