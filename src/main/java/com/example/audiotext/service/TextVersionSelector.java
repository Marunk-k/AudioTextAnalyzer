package com.example.audiotext.service;

import com.example.audiotext.model.Project;

public final class TextVersionSelector {
    private TextVersionSelector() {}

    public static String bestTextForAnalysis(Project p) {
        if (p == null) return "";
        if (hasText(p.getAiText())) return p.getAiText();
        if (hasText(p.getProcessedText())) return p.getProcessedText();
        return p.getRawText() == null ? "" : p.getRawText();
    }

    public static String bestTextForExport(Project p) {
        return bestTextForAnalysis(p);
    }

    public static boolean hasText(String text) {
        return text != null && !text.isBlank();
    }
}
