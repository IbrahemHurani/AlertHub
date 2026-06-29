package com.alerthub.processorms.service;

import com.alerthub.processorms.client.LoaderClient;
import com.alerthub.processorms.client.LoggerClient;
import com.alerthub.processorms.client.MetricClient;
import com.alerthub.processorms.dto.ActionDTO;
import com.alerthub.processorms.dto.LogRequestDTO;
import com.alerthub.processorms.dto.MetricDTO;
import com.alerthub.processorms.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessorService {

    private final MetricClient metricClient;
    private final LoaderClient loaderClient;
    private final LoggerClient loggerClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void processAction(ActionDTO action) {
        log.info("Processing Action ID: {}, Name: {}", action.getId(), action.getName());

        try {
            boolean conditionMet = evaluateConditionMatrix(action.getCondition(), action.getOwnerId());

            if (conditionMet) {
                String topic = action.getActionType().equalsIgnoreCase("sms") ? "sms" : "email";
                NotificationEvent event = new NotificationEvent(action.getTo(), action.getMessage());

                // 1. Dispatch Kafka Event to appropriate topic
                kafkaTemplate.send(topic, event);
                log.info("Dispatched notification to Kafka topic [{}], target: {}", topic, action.getTo());

                // 2. Transmit Log via Feign to MongoDB Logger service as required
                sendLogToLoggerService(String.format("Successfully inserted message into Kafka topic: %s for action %s", topic, action.getId()), "INFO");
            } else {
                log.info("Action {} thresholds were not met. Notification skipped.", action.getId());
            }
        } catch (Exception e) {
            log.error("Failed to process action " + action.getId(), e);
            sendLogToLoggerService("Error processing action " + action.getId() + ": " + e.getMessage(), "ERROR");
            throw e; // Bubble up for Kafka error handling rules
        }
    }

    private boolean evaluateConditionMatrix(List<List<Integer>> matrix, String ownerId) {
        if (matrix == null || matrix.isEmpty()) return false;

        // Outer list represents OR
        for (List<Integer> andGroup : matrix) {
            boolean andGroupResult = true;

            // Inner list represents AND
            for (Integer metricId : andGroup) {
                if (!isMetricSatisfied(metricId, ownerId)) {
                    andGroupResult = false;
                    break; // Fail-fast for performance
                }
            }

            // If any nested inner block evaluated entirely to true, condition yields true overall
            if (andGroupResult) {
                return true;
            }
        }
        return false;
    }

    private boolean isMetricSatisfied(Integer metricId, String ownerId) {
        // Query Metric Definition via Feign
        MetricDTO metric = metricClient.getMetricById(metricId);
        if (metric == null) return false;

        // Query Scanned DB Metrics via Feign
        long taskCount = loaderClient.countLabelTasks(metric.getLabel(), metric.getTimeFrameHours(), ownerId);

        return taskCount >= metric.getThreshold();
    }

    private void sendLogToLoggerService(String msg, String level) {
        try {
            LogRequestDTO logDto = new LogRequestDTO(LocalDateTime.now(), "Processor-Service", level, msg);
            loggerClient.sendLog(logDto);
        } catch (Exception ex) {
            log.error("Failed to forward logging information to Remote Logger Service microservice", ex);
        }
    }
}
