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
Ты получаешь текст после автоматической транскрибации и локальной предобработки.
Твоя задача — выполнить аккуратную корректуру, а не пересказ.

Что нужно сделать:
1. Восстановить знаки препинания.
2. Исправить грамматику и очевидные ошибки распознавания, если исправление однозначно по контексту.
3. Разбить текст на понятные предложения и абзацы.
4. Сохранить исходный смысл и порядок событий.
5. Сохранить все важные детали.
6. Сохранить вступление и финальные обращения автора, если они есть.
7. Не добавлять новых событий, объяснений и образов.
8. Не сокращать текст.
9. Не делать литературную адаптацию.
10. Не превращать текст в краткий пересказ.

Важно:
- Не добавляй факты, которых нет в исходном тексте.
- Не удаляй финальные фразы вроде "спасибо за прослушивание", "подписывайтесь", "всем пока".
- Верни только исправленный текст без комментариев.

Текст:
%s
""".formatted(text); }

    private String buildSummaryPrompt(String text){ return """
Ты получаешь текст после AI-постобработки.
Составь краткое содержание в 3–5 предложениях.

Требования:
1. Не добавляй факты, которых нет в тексте.
2. Сохраняй причинно-следственную связь событий.
3. Если в тексте есть сюжетный поворот, обязательно отрази его.
4. Не пересказывай рекламное вступление и финальные призывы подписаться, если они не относятся к основному содержанию.
5. Не делай выводы за пределами текста.
6. Не искажай субъект действия.

Важно:
Если герой видит, представляет, вспоминает, засыпает или ошибочно воспринимает событие, не описывай это как объективный факт.

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
