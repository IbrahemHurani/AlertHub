package com.alerthub.userms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "evaluation-service", url = "${app.services.evaluation-url}")
public interface EvaluationClient {

    @GetMapping("/evaluation/developer/most-label")
    Object getMostLabel(
            @RequestParam("label") String label,
            @RequestParam("since") int sinceDays);

    @GetMapping("/evaluation/developer/{developer_id}/label-aggregate")
    Object getLabelAggregate(
            @PathVariable("developer_id") String developerId,
            @RequestParam("since") int sinceDays);

    @GetMapping("/evaluation/developer/{developer_id}/task-amount")
    Object getTaskAmount(
            @PathVariable("developer_id") String developerId,
            @RequestParam("since") int sinceDays);
}