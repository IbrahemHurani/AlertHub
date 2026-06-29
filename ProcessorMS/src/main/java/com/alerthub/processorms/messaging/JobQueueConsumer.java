package com.alerthub.processorms.messaging;

import com.alerthub.processorms.dto.ActionDTO;
import com.alerthub.processorms.service.ProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobQueueConsumer {

    private final ProcessorService processorService;

    @KafkaListener(topics = "job-queue", groupId = "processor-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeJob(ActionDTO action) {
        log.info("Received Action Event from Job Queue: {}", action.getId());
        processorService.processAction(action);
    }
}
