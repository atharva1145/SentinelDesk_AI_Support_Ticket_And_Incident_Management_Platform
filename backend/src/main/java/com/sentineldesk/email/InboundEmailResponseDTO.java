package com.sentineldesk.email;

public class InboundEmailResponseDTO {

    private String action; // TICKET_CREATED or COMMENT_ADDED
    private String ticketNumber;
    private Long ticketId;
    private String message;
    private String senderEmail;

    public InboundEmailResponseDTO() {}

    public InboundEmailResponseDTO(String action, String ticketNumber, Long ticketId, String message, String senderEmail) {
        this.action = action;
        this.ticketNumber = ticketNumber;
        this.ticketId = ticketId;
        this.message = message;
        this.senderEmail = senderEmail;
    }

    public String getAction() { return action; }
    public String getTicketNumber() { return ticketNumber; }
    public Long getTicketId() { return ticketId; }
    public String getMessage() { return message; }
    public String getSenderEmail() { return senderEmail; }
}
