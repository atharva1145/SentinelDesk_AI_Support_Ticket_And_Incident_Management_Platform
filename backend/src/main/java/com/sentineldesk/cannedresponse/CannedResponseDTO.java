package com.sentineldesk.cannedresponse;

public class CannedResponseDTO {

    private Long id;
    private String title;
    private String shortcut;
    private String category;
    private String content;

    public CannedResponseDTO() {}

    public CannedResponseDTO(CannedResponse c) {
        this.id = c.getId();
        this.title = c.getTitle();
        this.shortcut = c.getShortcut();
        this.category = c.getCategory();
        this.content = c.getContent();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getShortcut() { return shortcut; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
}
