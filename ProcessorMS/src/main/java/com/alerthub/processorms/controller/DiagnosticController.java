package com.alerthub.processorms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/processor")
@Tag(name = "Processor Service Diagnostics", description = "Endpoints for platform status verifications")
public class DiagnosticController {

    @GetMapping("/status")
    @Operation(summary = "Get Microservice Health Status", description = "Verifies if the microservice is operational")
    public String getStatus() {
        return "Processor Service is running seamlessly (Stateless Node).";
    }
}
