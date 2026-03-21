package com.staynest.controller;

import com.staynest.dto.response.DashboardStatsResponse;
import com.staynest.entity.User;
import com.staynest.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for dashboard statistics
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get my dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getMyDashboardStats(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        DashboardStatsResponse stats = dashboardService.getDashboardStats(user.getUserId());
        return  ResponseEntity.ok(stats);
    }
}
