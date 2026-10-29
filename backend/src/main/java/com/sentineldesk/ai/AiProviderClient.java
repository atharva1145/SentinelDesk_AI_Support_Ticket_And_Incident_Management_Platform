package com.sentineldesk.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.ticket.TicketPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AiProviderClient {

    private static final Logger log = LoggerFactory.getLogger(AiProviderClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    @Value("${sentineldesk.ai.enabled:false}")
    private boolean enabled;

    @Value("${sentineldesk.ai.provider:openai}")
    private String provider;

    @Value("${sentineldesk.ai.api-key:}")
    private String apiKey;

    @Value("${sentineldesk.ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${sentineldesk.ai.model:gpt-4o-mini}")
    private String model;

    @Value("${sentineldesk.ai.timeout-ms:10000}")
    private int timeoutMs;

    public AiProviderClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }

    public AiTriageResult classifyTicket(String title, String description) {
        if (!isEnabled()) {
            log.info("AI Provider is not configured or disabled. Using deterministic rule-based triage fallback.");
            return generateDeterministicFallback(title, description);
        }

        try {
            return callExternalLlmApi(title, description);
        } catch (Exception ex) {
            log.error("AI Provider call failed: {}. Gracefully falling back to rule-based triage.", ex.getMessage());
            return generateDeterministicFallback(title, description);
        }
    }

    private AiTriageResult callExternalLlmApi(String title, String description) throws Exception {
        // Untrusted input sanitization: strip prompt manipulation attempts
        String sanitizedTitle = sanitizePromptInput(title);
        String sanitizedDesc = sanitizePromptInput(description);

        String systemPrompt = "You are SentinelDesk AI, an automated IT service management triage system. " +
                "Classify the ticket into JSON format with the following keys strictly: " +
                "\"category\" (one of SOFTWARE, HARDWARE, NETWORK, ACCESS, SECURITY, OUTAGE, OTHER), " +
                "\"suggestedPriority\" (one of P1, P2, P3, P4), " +
                "\"summary\" (concise 1-sentence issue summary), " +
                "\"suggestedDomain\" (technical domain), " +
                "\"suggestedTeam\" (e.g. Tier 1 Service Desk, Network Operations (NOC), Cloud & Infrastructure, Security Operations (SOC)), " +
                "\"diagnosticQuestions\" (array of 2 diagnostic questions), " +
                "\"suggestedActions\" (array of 2 troubleshooting steps), " +
                "\"priorityExplanation\" (why this priority was assigned), " +
                "\"confidenceEstimate\" (number between 0 and 100). " +
                "Return ONLY a valid JSON object without markdown fences.";

        String userPrompt = "TICKET TITLE: " + sanitizedTitle + "\n\nTICKET DESCRIPTION: " + sanitizedDesc;

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.1,
                "response_format", Map.of("type", "json_object")
        );

        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl.replaceAll("/$", "") + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .timeout(Duration.ofMillis(timeoutMs))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            AiTriageResult result = parseAndValidateJson(content);
            result.setModelProvider(provider);
            result.setModelName(model);
            return result;
        } else {
            log.warn("AI Provider returned non-200 code: {} - {}", response.statusCode(), response.body());
            return generateDeterministicFallback(title, description);
        }
    }

    public AiTriageResult parseAndValidateJson(String jsonContent) {
        try {
            // Strip any accidental markdown formatting
            String clean = jsonContent.trim();
            if (clean.startsWith("```json")) {
                clean = clean.substring(7);
            }
            if (clean.startsWith("```")) {
                clean = clean.substring(3);
            }
            if (clean.endsWith("```")) {
                clean = clean.substring(0, clean.length() - 3);
            }
            clean = clean.trim();

            JsonNode node = objectMapper.readTree(clean);

            AiTriageResult result = new AiTriageResult();

            String catStr = node.path("category").asText("OTHER").toUpperCase();
            try {
                result.setCategory(TicketCategory.valueOf(catStr));
            } catch (Exception e) {
                result.setCategory(TicketCategory.OTHER);
            }

            String prioStr = node.path("suggestedPriority").asText("P3").toUpperCase();
            try {
                result.setSuggestedPriority(TicketPriority.valueOf(prioStr));
            } catch (Exception e) {
                result.setSuggestedPriority(TicketPriority.P3);
            }

            result.setSummary(node.path("summary").asText("Automated issue triage"));
            result.setSuggestedDomain(node.path("suggestedDomain").asText("General IT"));
            result.setSuggestedTeam(node.path("suggestedTeam").asText("Tier 1 Service Desk"));
            result.setPriorityExplanation(node.path("priorityExplanation").asText("Evaluated based on standard impact matrix"));

            double conf = node.path("confidenceEstimate").asDouble(85.0);
            result.setConfidenceEstimate(BigDecimal.valueOf(conf));

            List<String> questions = new ArrayList<>();
            node.path("diagnosticQuestions").forEach(q -> questions.add(q.asText()));
            if (questions.isEmpty()) {
                questions.add("Can the affected user reproduce the issue consistently?");
                questions.add("Are other team members in the same department experiencing this?");
            }
            result.setDiagnosticQuestions(questions);

            List<String> actions = new ArrayList<>();
            node.path("suggestedActions").forEach(a -> actions.add(a.asText()));
            if (actions.isEmpty()) {
                actions.add("Verify network and account status.");
                actions.add("Review relevant service health dashboards.");
            }
            result.setSuggestedActions(actions);

            return result;
        } catch (Exception e) {
            log.error("Failed to parse AI response JSON: {}", e.getMessage());
            return null;
        }
    }

    public AiTriageResult generateDeterministicFallback(String title, String description) {
        String combined = (title + " " + description).toLowerCase();

        AiTriageResult result = new AiTriageResult();
        result.setModelProvider("rule-engine-fallback");
        result.setModelName("sentinel-rules-v1");

        if (combined.contains("outage") || combined.contains("production down") || combined.contains("database crash") || combined.contains("all users affected")) {
            result.setCategory(TicketCategory.OUTAGE);
            result.setSuggestedPriority(TicketPriority.P1);
            result.setSuggestedDomain("Infrastructure & Cloud Ops");
            result.setSuggestedTeam("Cloud & Infrastructure");
            result.setSummary("Critical service outage affecting multiple users or systems");
            result.setPriorityExplanation("Classified P1 due to keywords indicating widespread production outage");
            result.setConfidenceEstimate(BigDecimal.valueOf(90.0));
            result.setDiagnosticQuestions(List.of("Is the production cluster reachable?", "When did the monitoring alerts trigger?"));
            result.setSuggestedActions(List.of("Engage incident manager immediately.", "Check CloudWatch / Datadog alert boards."));
        } else if (combined.contains("vpn") || combined.contains("dns") || combined.contains("network") || combined.contains("wifi") || combined.contains("firewall")) {
            result.setCategory(TicketCategory.NETWORK);
            result.setSuggestedPriority(TicketPriority.P2);
            result.setSuggestedDomain("Network Engineering");
            result.setSuggestedTeam("Network Operations (NOC)");
            result.setSummary("Network or connectivity disruption reported");
            result.setPriorityExplanation("Classified P2 due to departmental or network connectivity degradation");
            result.setConfidenceEstimate(BigDecimal.valueOf(88.0));
            result.setDiagnosticQuestions(List.of("Can the user ping the gateway?", "Is the VPN tunnel establishing a handshake?"));
            result.setSuggestedActions(List.of("Verify gateway router logs.", "Test DNS resolution from office subnet."));
        } else if (combined.contains("password") || combined.contains("permission") || combined.contains("access") || combined.contains("login") || combined.contains("mfa")) {
            result.setCategory(TicketCategory.ACCESS);
            result.setSuggestedPriority(TicketPriority.P3);
            result.setSuggestedDomain("Identity & Access");
            result.setSuggestedTeam("Security Operations (SOC)");
            result.setSummary("Account access or credential assistance request");
            result.setPriorityExplanation("Classified P3 standard user access request");
            result.setConfidenceEstimate(BigDecimal.valueOf(92.0));
            result.setDiagnosticQuestions(List.of("Is the account locked in Active Directory?", "Has the requester completed security verification?"));
            result.setSuggestedActions(List.of("Verify identity against HR roster.", "Trigger self-service MFA reset."));
        } else if (combined.contains("security") || combined.contains("phishing") || combined.contains("breach") || combined.contains("malware")) {
            result.setCategory(TicketCategory.SECURITY);
            result.setSuggestedPriority(TicketPriority.P1);
            result.setSuggestedDomain("Cybersecurity");
            result.setSuggestedTeam("Security Operations (SOC)");
            result.setSummary("Potential security incident or malicious activity report");
            result.setPriorityExplanation("Classified P1 due to security threat risk");
            result.setConfidenceEstimate(BigDecimal.valueOf(95.0));
            result.setDiagnosticQuestions(List.of("Did the user enter credentials into the suspicious link?", "Is the workstation isolated from network?"));
            result.setSuggestedActions(List.of("Revoke active user sessions.", "Quarantine affected host in EDR."));
        } else {
            result.setCategory(TicketCategory.SOFTWARE);
            result.setSuggestedPriority(TicketPriority.P3);
            result.setSuggestedDomain("General Applications");
            result.setSuggestedTeam("Tier 1 Service Desk");
            result.setSummary("General software or user application issue");
            result.setPriorityExplanation("Default standard priority for general support ticket");
            result.setConfidenceEstimate(BigDecimal.valueOf(80.0));
            result.setDiagnosticQuestions(List.of("What version of the client software is installed?", "Does a system restart resolve the problem?"));
            result.setSuggestedActions(List.of("Clear application cache and restart.", "Review application event logs."));
        }

        return result;
    }

    private String sanitizePromptInput(String input) {
        if (input == null) return "";
        // Mitigate prompt injection by stripping explicit instructions attempting to jailbreak system prompt
        return input.replace("```", "")
                .replace("system:", "")
                .replace("IGNORE ALL PREVIOUS INSTRUCTIONS", "[REDACTED_PROMPT_ATTEMPT]")
                .trim();
    }
}
