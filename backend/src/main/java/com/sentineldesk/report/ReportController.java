package com.sentineldesk.report;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports & Analytics", description = "SLA compliance reports, MTTR/MTTA metrics, and CSV export")
public class ReportController {

    private final SlaReportService reportService;

    public ReportController(SlaReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sla/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Get SLA compliance summary, MTTA, MTTR, and priority breakdown")
    public ResponseEntity<ApiResponse<SlaReportSummaryDTO>> getSlaSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSlaSummary()));
    }

    @GetMapping("/sla/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_MANAGER', 'SUPPORT_AGENT')")
    @Operation(summary = "Export full SLA compliance audit report as CSV")
    public ResponseEntity<byte[]> exportSlaCsv() {
        byte[] csvBytes = reportService.generateSlaCsvReport();

        String filename = "sentineldesk_sla_audit_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}
