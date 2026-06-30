package com.alerthub.metricms.repository;

import com.alerthub.metricms.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {
    List<Metric> findByUserId(Integer userId);
}
