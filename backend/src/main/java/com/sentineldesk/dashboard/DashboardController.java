package com.sentineldesk.dashboard;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Analytics & Dashboard", description = "Real-time ticket aggregations, SLA compliance metrics, and workload analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get high-level KPI metrics summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getSummaryMetrics() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummaryMetrics()));
    }

    @GetMapping("/charts")
    @Operation(summary = "Get aggregated chart datasets (priority, category, workload, trend)")
    public ResponseEntity<ApiResponse<DashboardChartsDTO>> getChartData() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getChartData()));
    }
}
