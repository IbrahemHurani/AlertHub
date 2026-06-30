package com.alerthub.metricms.service;

import com.alerthub.metricms.entity.Metric;
import com.alerthub.metricms.exception.MetricNotFoundException;
import com.alerthub.metricms.repository.MetricRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricService {

    private final MetricRepository metricRepository;

    public MetricService(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    public Metric createMetric(Metric metric) {
        return metricRepository.save(metric);
    }

    public List<Metric> getAllMetrics() {
        return metricRepository.findAll();
    }

    public Metric getMetricById(Long id) {
        return metricRepository.findById(id)
                .orElseThrow(() -> new MetricNotFoundException("Metric not found with id: " + id));
    }

    public List<Metric> getMetricsByUserId(Integer userId) {
        return metricRepository.findByUserId(userId);
    }

    public Metric updateMetric(Long id, Metric updatedMetric) {
        Metric existingMetric = getMetricById(id);

        existingMetric.setUserId(updatedMetric.getUserId());
        existingMetric.setName(updatedMetric.getName());
        existingMetric.setLabel(updatedMetric.getLabel());
        existingMetric.setThreshold(updatedMetric.getThreshold());
        existingMetric.setTimeFrameHours(updatedMetric.getTimeFrameHours());

        return metricRepository.save(existingMetric);
    }

    public void deleteMetric(Long id) {
        Metric metric = getMetricById(id);
        metricRepository.delete(metric);
    }
}