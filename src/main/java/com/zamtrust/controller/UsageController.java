package com.zamtrust.controller;

import com.zamtrust.domain.Plan;
import com.zamtrust.domain.User;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.UserRepository;
import com.zamtrust.service.UsageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exposes the authenticated user's current plan, monthly usage, and remaining
 * quota. Used by the frontend to render usage meters and upgrade prompts.
 */
@RestController
@RequestMapping("/api/usage")
public class UsageController {

    private final UsageService usageService;
    private final UserRepository userRepository;

    public UsageController(UsageService usageService, UserRepository userRepository) {
        this.usageService = usageService;
        this.userRepository = userRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> current(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Plan plan = usageService.planOf(user.getId());

        long signaturesUsed = usageService.currentUsage(user.getId(), "SIGN");
        long signaturesLimit = usageService.limitFor(plan, "SIGN");

        long apiUsed = usageService.currentUsage(user.getId(), "API_CALL");
        long apiLimit = usageService.limitFor(plan, "API_CALL");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("periodKey", YearMonth.now().toString());
        body.put("plan", plan.name());
        body.put("planDisplayName", plan.displayName());

        Map<String, Object> sig = new LinkedHashMap<>();
        sig.put("used", signaturesUsed);
        sig.put("limit", signaturesLimit);
        sig.put("remaining", Math.max(0, signaturesLimit - signaturesUsed));
        sig.put("unlimited", plan.isUnlimited());
        body.put("signatures", sig);

        Map<String, Object> api = new LinkedHashMap<>();
        api.put("used", apiUsed);
        api.put("limit", apiLimit);
        api.put("remaining", Math.max(0, apiLimit - apiUsed));
        api.put("enabled", plan.apiEnabled());
        body.put("apiCalls", api);

        return ResponseEntity.ok(body);
    }
}
