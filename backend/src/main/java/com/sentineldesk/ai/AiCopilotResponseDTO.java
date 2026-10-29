package com.sentineldesk.ai;

public class AiCopilotResponseDTO {

    private String action;
    private String response;
    private String modelUsed;

    public AiCopilotResponseDTO() {}

    public AiCopilotResponseDTO(String action, String response, String modelUsed) {
        this.action = action;
        this.response = response;
        this.modelUsed = modelUsed;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
}
