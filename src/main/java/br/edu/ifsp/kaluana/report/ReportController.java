package br.edu.ifsp.kaluana.report;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ReportResponseDTO ownReport(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "7") int days) {
        return reportService.ownReport(userId, days);
    }

    @GetMapping("/linked/{primaryUserId}")
    @PreAuthorize("hasRole('RESPONSIBLE')")
    public ReportResponseDTO linkedReport(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID primaryUserId,
            @RequestParam(defaultValue = "7") int days) {
        return reportService.linkedReport(userId, primaryUserId, days);
    }

    @PostMapping("/linked/{primaryUserId}/export")
    @PreAuthorize("hasRole('RESPONSIBLE')")
    public ResponseEntity<Void> recordExport(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID primaryUserId,
            @RequestParam(defaultValue = "7") int days) {
        reportService.recordExport(userId, primaryUserId, days);
        return ResponseEntity.noContent().build();
    }
}
