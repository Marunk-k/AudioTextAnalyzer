package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RealGigaChatService implements GigaChatService {
    private static final Logger log = LoggerFactory.getLogger(RealGigaChatService.class);
    private static final String AI_UNAVAILABLE = "AI-постобработка временно недоступна. Попробуйте позже.";
    private final AppProperties.Giga props;
    private final RestClient oauthClient;
    private final RestClient chatClient;

    public RealGigaChatService(AppProperties.Giga props) {
        this.props = props;
        this.oauthClient = RestClient.builder().baseUrl("https://ngw.devices.sberbank.ru:9443").build();
        this.chatClient = RestClient.builder().baseUrl("https://gigachat.devices.sberbank.ru").build();
    }

    @Override
    public String improveText(String text) {
        if (text == null || text.isBlank()) return "";
        return ask(buildImprovePrompt(text), "improveText", text.length());
    }

    @Override
    public String summarizeText(String text) {
        if (text == null || text.isBlank()) return "";
        return ask(buildSummaryPrompt(text), "summarizeText", text.length());
    }

    private String ask(String prompt, String operation, int inputLength) {
        log.info("GigaChat {} started: model={}, inputLength={}", operation, props.getModel(), inputLength);
        try {
            String accessToken = obtainAccessToken();
            ChatResponse response = chatClient.post()
                    .uri("/api/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(Map.of(
                            "model", props.getModel(),
                            "messages", List.of(Map.of("role", "user", "content", prompt)),
                            "temperature", 0.1,
                            "stream", false
                    ))
                    .retrieve()
                    .body(ChatResponse.class);

            String answer = extractResponseContent(response);
            if (answer == null || answer.isBlank()) {
                throw new IllegalStateException("GigaChat вернул пустой ответ");
            }
            log.info("GigaChat {} completed: model={}, outputLength={}", operation, props.getModel(), answer.length());
            return answer.trim();
        } catch (Exception ex) {
            log.error("GigaChat {} failed: {}", operation, ex.getMessage());
            throw new RuntimeException(AI_UNAVAILABLE);
        }
    }

    private String obtainAccessToken() {
        TokenResponse tokenResponse = oauthClient.post()
                .uri("/api/v2/oauth")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + props.getCredentials().trim())
                .header("RqUID", UUID.randomUUID().toString())
                .body("scope=" + props.getScope())
                .retrieve()
                .body(TokenResponse.class);
        if (tokenResponse == null || tokenResponse.accessToken == null || tokenResponse.accessToken.isBlank()) {
            throw new IllegalStateException("Не удалось получить access token");
        }
        return tokenResponse.accessToken;
    }

    private String extractResponseContent(ChatResponse response) {
        if (response == null || response.choices == null || response.choices.isEmpty()) return null;
        Message message = response.choices.get(0).message;
        return message != null ? message.content : null;
    }

    private String buildImprovePrompt(String text){ return """
Ты получаешь текст, автоматически распознанный из аудио.
Исправь пунктуацию, грамматику и структуру текста.
Разбей текст на понятные абзацы.
Не добавляй новых фактов.
Не меняй смысл.
Сохрани исходный порядок мыслей.
Верни только исправленный текст без пояснений.

Текст:
%s
""".formatted(text); }

    private String buildSummaryPrompt(String text){ return """
Составь краткое содержание текста в 5–7 предложениях.
Не добавляй новых фактов.
Сохрани только основные мысли.
Верни только краткое содержание без пояснений.

Текст:
%s
""".formatted(text); }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TokenResponse { @JsonProperty("access_token") public String accessToken; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatResponse { public List<Choice> choices; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice { public Message message; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Message { public String content; }
}
