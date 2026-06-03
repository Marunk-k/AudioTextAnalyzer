package com.example.audiotext.service;

import com.example.audiotext.model.Project;

public final class TextVersionSelector {
    private TextVersionSelector() {}

    public static String bestTextForAnalysis(Project p) {
        return selectedText(p).text();
    }

    public static SourceTextType selectedTextType(Project p) {
        return selectedText(p).type();
    }

    public static SelectedText selectedText(Project p) {
        if (p == null) return new SelectedText("", SourceTextType.RAW);
        if (hasText(p.getManualText())) return new SelectedText(p.getManualText(), SourceTextType.MANUAL);
        if (hasText(p.getAiText())) return new SelectedText(p.getAiText(), SourceTextType.AI);
        if (hasText(p.getProcessedText())) return new SelectedText(p.getProcessedText(), SourceTextType.PROCESSED);
        return new SelectedText(p.getRawText() == null ? "" : p.getRawText(), SourceTextType.RAW);
    }

    public static String bestTextForExport(Project p) { return bestTextForAnalysis(p); }
    public static boolean hasText(String text) { return text != null && !text.isBlank(); }

    public record SelectedText(String text, SourceTextType type) {}
}
