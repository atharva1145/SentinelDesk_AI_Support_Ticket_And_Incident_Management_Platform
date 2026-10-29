package com.sentineldesk.email;

import com.sentineldesk.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
@Tag(name = "Email Ingestion", description = "Inbound email webhook processor (SendGrid, Mailgun, AWS SES)")
public class InboundEmailController {

    private final InboundEmailService inboundEmailService;

    public InboundEmailController(InboundEmailService inboundEmailService) {
        this.inboundEmailService = inboundEmailService;
    }

    @PostMapping("/inbound")
    @Operation(summary = "Process inbound customer email (creates ticket or appends comment reply)")
    public ResponseEntity<ApiResponse<InboundEmailResponseDTO>> processInboundEmail(
            @Valid @RequestBody InboundEmailRequest request) {
        InboundEmailResponseDTO response = inboundEmailService.processInboundEmail(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response.getMessage(), response));
    }
}
