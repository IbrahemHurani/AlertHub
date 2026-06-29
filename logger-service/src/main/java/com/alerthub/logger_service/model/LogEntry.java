package com.alerthub.logger_service.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//the MongoDB document
@Document(collection = "logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    @Id
    private String id; //Unique identifier for each log entry.
    private Instant timestamp;// the date and time when the log entry was created
    private String serviceName;//the name of the microservice that generated the log
    private String logLevel;// INFO, WARN, ERROR, DEBUG
    private String message;//the log message 
}
