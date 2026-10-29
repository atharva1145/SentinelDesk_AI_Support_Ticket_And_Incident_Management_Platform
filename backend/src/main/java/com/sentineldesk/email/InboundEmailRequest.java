package com.sentineldesk.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class InboundEmailRequest {

    @NotBlank(message = "Sender email (from) is required")
    @Email(message = "Invalid email format")
    private String from;

    private String senderName;

    private String to;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Email body is required")
    private String body;

    private String htmlBody;

    public InboundEmailRequest() {}

    public InboundEmailRequest(String from, String senderName, String subject, String body) {
        this.from = from;
        this.senderName = senderName;
        this.subject = subject;
        this.body = body;
    }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getHtmlBody() { return htmlBody; }
    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
}
