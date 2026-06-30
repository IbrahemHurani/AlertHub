package com.alerthub.metricms.exception;

public class MetricNotFoundException extends RuntimeException {

    public MetricNotFoundException(String message) {
        super(message);
    }
}