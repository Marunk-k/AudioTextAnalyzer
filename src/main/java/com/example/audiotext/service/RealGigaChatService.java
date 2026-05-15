package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;

public class RealGigaChatService implements GigaChatService {
    private static final Logger log = LoggerFactory.getLogger(RealGigaChatService.class);
    private final AppProperties.Giga props;

    public RealGigaChatService(AppProperties.Giga props){this.props=props;}

    @Override
    public String improveText(String text){ return ask(buildImprovePrompt(text)); }
    @Override
    public String summarizeText(String text){ return ask(buildSummaryPrompt(text)); }

    private String ask(String prompt){
        try{
            // TODO: direct API call through gigachat-java client can be wired here once credentials are provided in runtime.
            return prompt.substring(prompt.indexOf("Текст:\n")+7).trim();
        }catch(Exception ex){
            log.error("GigaChat request failed", ex);
            throw new RuntimeException("AI-постобработка временно недоступна. Попробуйте позже.");
        }
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
""".formatted(text==null?"":text); }

    private String buildSummaryPrompt(String text){ return """
Составь краткое содержание текста в 5–7 предложениях.
Не добавляй новых фактов.
Сохрани только основные мысли.
Верни только краткое содержание без пояснений.

Текст:
%s
""".formatted(text==null?"":text); }
}
