package com.sentineldesk.ai;

import jakarta.validation.constraints.NotBlank;

public class AiCopilotRequest {

    @NotBlank(message = "Action is required (DRAFT_REPLY, SUMMARIZE, NEXT_STEPS, CUSTOM)")
    private String action;

    private String customPrompt;

    public AiCopilotRequest() {}

    public AiCopilotRequest(String action, String customPrompt) {
        this.action = action;
        this.customPrompt = customPrompt;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getCustomPrompt() { return customPrompt; }
    public void setCustomPrompt(String customPrompt) { this.customPrompt = customPrompt; }
}
