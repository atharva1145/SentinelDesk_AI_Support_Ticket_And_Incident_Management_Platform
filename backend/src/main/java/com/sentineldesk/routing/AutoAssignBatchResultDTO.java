package com.sentineldesk.routing;

import java.util.ArrayList;
import java.util.List;

public class AutoAssignBatchResultDTO {

    private int totalAssigned;
    private String strategy;
    private List<AutoAssignResultDTO> assignments = new ArrayList<>();

    public AutoAssignBatchResultDTO() {}

    public AutoAssignBatchResultDTO(int totalAssigned, String strategy, List<AutoAssignResultDTO> assignments) {
        this.totalAssigned = totalAssigned;
        this.strategy = strategy;
        this.assignments = assignments;
    }

    public int getTotalAssigned() { return totalAssigned; }
    public void setTotalAssigned(int totalAssigned) { this.totalAssigned = totalAssigned; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public List<AutoAssignResultDTO> getAssignments() { return assignments; }
    public void setAssignments(List<AutoAssignResultDTO> assignments) { this.assignments = assignments; }
}
