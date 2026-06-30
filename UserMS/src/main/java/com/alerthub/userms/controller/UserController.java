package com.alerthub.userms.controller;

import com.alerthub.userms.client.SecurityClient;
import com.alerthub.userms.client.MetricClient;
import com.alerthub.userms.client.ActionClient;
import com.alerthub.userms.client.EvaluationClient;
import com.alerthub.userms.dto.UserRequestDTO;
import com.alerthub.userms.dto.UserResponseDTO;
import com.alerthub.userms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // حقن كل الـ Feign Clients المطلوبة للربط
    @Autowired
    private SecurityClient securityClient;

    @Autowired
    private MetricClient metricClient;

    @Autowired
    private ActionClient actionClient;

    @Autowired
    private EvaluationClient evaluationClient;

    // ميثود مساعدة لفحص الصلاحيات من الـ Security Service
    private boolean isNotAuthorized(Long userId, String permission) {
        if (userId == null) return true;
        SecurityClient.AuthorizeRequest request = new SecurityClient.AuthorizeRequest();
        request.setUserId(userId);
        request.setPermission(permission);
        try {
            ResponseEntity<SecurityClient.AuthorizeResponse> response = securityClient.authorizeUserPermission(request);
            return response.getBody() == null || !response.getBody().isAuthorized();
        } catch (Exception e) {
            return true; // في حال تعطل سيرفيس الـ Security نمنع الطلب للأمان
        }
    }

    // ==========================================
    // 1. إدارة المستخدمين (إما مفتوحة أو تحتاج صلاحية المسؤول "read" كمثال)
    // ==========================================

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestAttribute("currentUserId") Long currentUserId, @Valid @RequestBody UserRequestDTO dto) {
        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        return new ResponseEntity<>(userService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping("/getusers")
    public ResponseEntity<?> getAllUsers(@RequestAttribute("currentUserId") Long currentUserId) {
        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        return new ResponseEntity<>(userService.getAll(), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@RequestAttribute("currentUserId") Long currentUserId, @PathVariable Long id) {
        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 2. إدارة الـ Metrics (الربط مع MetricMS)
    // ==========================================

    @PostMapping("/metrics")
    public ResponseEntity<?> createMetric(@RequestAttribute("currentUserId") Long currentUserId, @RequestBody Object metric) {
        if (isNotAuthorized(currentUserId, "createMetric")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN createMetric");
        }
        return ResponseEntity.ok(metricClient.createMetric(metric));
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getAllMetrics(@RequestAttribute("currentUserId") Long currentUserId) {
        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        return ResponseEntity.ok(metricClient.getAllMetrics());
    }

    @PutMapping("/metrics/{id}")
    public ResponseEntity<?> updateMetric(@RequestAttribute("currentUserId") Long currentUserId, @PathVariable Long id, @RequestBody Object metric) {
        if (isNotAuthorized(currentUserId, "updateMetric")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN updateMetric");
        }
        return ResponseEntity.ok(metricClient.updateMetric(id, metric));
    }

    @DeleteMapping("/metrics/{id}")
    public ResponseEntity<?> deleteMetric(@RequestAttribute("currentUserId") Long currentUserId, @PathVariable Long id) {
        if (isNotAuthorized(currentUserId, "deleteMetric")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN deleteMetric");
        }
        return ResponseEntity.ok(metricClient.deleteMetric(id));
    }

    // ==========================================
    // 3. إدارة الـ Actions (الربط مع ActionMS)
    // ==========================================

    @PostMapping("/actions")
    public ResponseEntity<?> createAction(@RequestAttribute("currentUserId") Long currentUserId, @RequestBody Object action) {
        if (isNotAuthorized(currentUserId, "createAction")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN createAction");
        }
        return ResponseEntity.ok(actionClient.createAction(action));
    }

    @GetMapping("/actions")
    public ResponseEntity<?> getAllActions(@RequestAttribute("currentUserId") Long currentUserId) {
        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        return ResponseEntity.ok(actionClient.getAllActions());
    }

    @PutMapping("/actions/{id}")
    public ResponseEntity<?> updateAction(@RequestAttribute("currentUserId") Long currentUserId, @PathVariable Long id, @RequestBody Object action) {
        if (isNotAuthorized(currentUserId, "updateAction")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN updateAction");
        }
        return ResponseEntity.ok(actionClient.updateAction(id, action));
    }

    @DeleteMapping("/actions/{id}")
    public ResponseEntity<?> deleteAction(@RequestAttribute("currentUserId") Long currentUserId, @PathVariable Long id) {
        if (isNotAuthorized(currentUserId, "deleteAction")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN deleteAction");
        }
        return ResponseEntity.ok(actionClient.deleteAction(id));
    }

    // تشغيل الأكشن يدوياً فوراً (Trigger manual process)
    @PostMapping("/actions/{id}/trigger")
    public ResponseEntity<?> triggerActionManually(@RequestAttribute("currentUserId") Long currentUserId, @PathVariable Long id) {
        if (isNotAuthorized(currentUserId, "triggerProcess")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN triggerProcess");
        }
        actionClient.triggerManualProcess(id);
        return ResponseEntity.ok("success");
    }

    // ==========================================
    // 4. نظام التقييم (الربط مع EvaluationMS)
    // ==========================================

    @GetMapping("/evaluation/most-label")
    public ResponseEntity<?> getMostLabel(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestParam String label,
            @RequestParam int since) {
        if (isNotAuthorized(currentUserId, "triggerEvaluation")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN triggerEvaluation");
        }
        return ResponseEntity.ok(evaluationClient.getMostLabel(label, since));
    }

    @GetMapping("/evaluation/developer/{developerId}/label-aggregate")
    public ResponseEntity<?> getLabelAggregate(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable String developerId,
            @RequestParam int since) {
        if (isNotAuthorized(currentUserId, "triggerEvaluation")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN triggerEvaluation");
        }
        return ResponseEntity.ok(evaluationClient.getLabelAggregate(developerId, since));
    }
}