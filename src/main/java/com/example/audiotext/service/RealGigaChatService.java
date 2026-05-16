package com.example.audiotext.service;

import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder.OAuthBuilder;
import chat.giga.http.client.HttpClientException;
import chat.giga.model.Scope;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;
import com.example.audiotext.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealGigaChatService implements GigaChatService {

    private static final Logger log = LoggerFactory.getLogger(RealGigaChatService.class);
    private static final String AI_UNAVAILABLE = "AI-постобработка временно недоступна. Попробуйте позже.";

    private final AppProperties.Giga props;
    private final GigaChatClient client;

    public RealGigaChatService(AppProperties.Giga props) {
        this.props = props;

        if (props.getCredentials() == null || props.getCredentials().isBlank()) {
            throw new IllegalArgumentException("GigaChat credentials are empty");
        }

        this.client = GigaChatClient.builder()
                .verifySslCerts(props.isVerifySslCerts())
                .authClient(AuthClient.builder()
                        .withOAuth(OAuthBuilder.builder()
                                .scope(resolveScope(props.getScope()))
                                .authKey(props.getCredentials().trim())
                                .build())
                        .build())
                .build();

        log.info(
                "RealGigaChatService initialized: model={}, scope={}, verifySslCerts={}",
                props.getModel(),
                props.getScope(),
                props.isVerifySslCerts()
        );
    }

    @Override
    public String improveText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String prompt = buildImprovePrompt(text);
        return ask(prompt, "improveText", text.length());
    }

    @Override
    public String summarizeText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String prompt = buildSummaryPrompt(text);
        return ask(prompt, "summarizeText", text.length());
    }

    private String ask(String prompt, String operation, int inputLength) {
        log.info(
                "GigaChat {} started: model={}, inputLength={}",
                operation,
                props.getModel(),
                inputLength
        );

        try {
            CompletionResponse response = client.completions(CompletionRequest.builder()
                    .model(resolveModelName())
                    .message(ChatMessage.builder()
                            .role(ChatMessageRole.USER)
                            .content(prompt)
                            .build())
                    .temperature(0.1f)
                    .build());

            String answer = extractAnswer(response);

            if (answer == null || answer.isBlank()) {
                throw new IllegalStateException("GigaChat returned empty response");
            }

            log.info(
                    "GigaChat {} completed: model={}, outputLength={}",
                    operation,
                    props.getModel(),
                    answer.length()
            );

            return answer.trim();

        } catch (HttpClientException ex) {
            log.error(
                    "GigaChat {} HTTP error: status={}, body={}",
                    operation,
                    ex.statusCode(),
                    safeBody(ex.bodyAsString())
            );
            throw new RuntimeException(AI_UNAVAILABLE, ex);

        } catch (Exception ex) {
            log.error("GigaChat {} failed: {}", operation, ex.getMessage(), ex);
            throw new RuntimeException(AI_UNAVAILABLE, ex);
        }
    }

    private String extractAnswer(CompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return null;
        }

        var choice = response.choices().get(0);

        if (choice == null || choice.message() == null) {
            return null;
        }

        return choice.message().content();
    }

    private Scope resolveScope(String value) {
        if (value == null || value.isBlank()) {
            return Scope.GIGACHAT_API_PERS;
        }

        try {
            return Scope.valueOf(value.trim());
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown GigaChat scope '{}', fallback to GIGACHAT_API_PERS", value);
            return Scope.GIGACHAT_API_PERS;
        }
    }

    private String resolveModelName() {
        if (props.getModel() == null || props.getModel().isBlank()) {
            return "GigaChat";
        }

        return props.getModel().trim();
    }

    private String safeBody(String body) {
        if (body == null) {
            return "";
        }

        return body.length() > 1500 ? body.substring(0, 1500) + "..." : body;
    }

    private String buildImprovePrompt(String text) {
        return """
                Ты получаешь текст, автоматически распознанный из аудио.
                Исправь пунктуацию, грамматику и структуру текста.
                Разбей текст на понятные абзацы.
                Не добавляй новых фактов.
                Не меняй смысл.
                Сохрани исходный порядок мыслей.
                Верни только исправленный текст без пояснений.

                Текст:
                %s
                """.formatted(text);
    }

    private String buildSummaryPrompt(String text) {
        return """
                Составь краткое содержание текста в 5–7 предложениях.
                Не добавляй новых фактов.
                Сохрани только основные мысли.
                Верни только краткое содержание без пояснений.

                Текст:
                %s
                """.formatted(text);
    }
}
