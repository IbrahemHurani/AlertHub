package com.alerthub.action.controller;

import com.alerthub.action.entity.Action;
import com.alerthub.action.service.ActionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/actions")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    // CREATE → POST /actions
    @PostMapping
    public Action createAction(@RequestBody Action action) {
        return actionService.createAction(action);
    }

    // READ ALL → GET /actions
    @GetMapping
    public List<Action> getAllActions() {
        return actionService.getAllActions();
    }

    // READ ONE → GET /actions/{id}
    @GetMapping("/{id}")
    public Action getActionById(@PathVariable Long id) {
        return actionService.getActionById(id);
    }

    // UPDATE → PUT /actions/{id}
    @PutMapping("/{id}")
    public Action updateAction(@PathVariable Long id, @RequestBody Action action) {
        return actionService.updateAction(id, action);
    }

    // DELETE → DELETE /actions/{id}
    @DeleteMapping("/{id}")
    public String deleteAction(@PathVariable Long id) {
        boolean deleted = actionService.deleteAction(id);
        if (deleted) {
            return "Action " + id + " deleted (soft).";
        } else {
            return "Action " + id + " not found.";
        }
    }
}