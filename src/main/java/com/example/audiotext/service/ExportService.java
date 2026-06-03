package com.example.audiotext.service;

import com.example.audiotext.model.ExportFormat;
import com.example.audiotext.model.Project;
import com.example.audiotext.model.TextAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {
    private static final float PDF_MARGIN = 50;
    private static final float PDF_FONT_SIZE = 12;
    private static final float PDF_LEADING = 16;

    private final StorageService storage;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public ExportService(StorageService storage){this.storage=storage;}

    public Path exportToTxt(Project p){
        try{ Path out=storage.getExportPath(p.getId(), ExportFormat.TXT);
            Files.writeString(out, exportText(p), StandardCharsets.UTF_8); return out; }catch(Exception e){throw new RuntimeException("Ошибка экспорта TXT",e);} }

    public Path exportToJson(Project p){
        try{ Path out=storage.getExportPath(p.getId(), ExportFormat.JSON);
            Map<String,Object> payload = new LinkedHashMap<>();
            payload.put("id", p.getId()); payload.put("title", p.getTitle()); payload.put("fileName", p.getOriginalFileName()); payload.put("status", p.getStatus());
            payload.put("selectedText", TextVersionSelector.bestTextForExport(p)); payload.put("rawText", p.getRawText()); payload.put("processedText", p.getProcessedText()); payload.put("aiText", p.getAiText()); payload.put("manualText", p.getManualText()); payload.put("analysis", p.getAnalysisResult());
            Files.writeString(out, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload), StandardCharsets.UTF_8);
            return out;
        }catch(Exception e){throw new RuntimeException("Ошибка экспорта JSON",e);} }

    public Path exportToDocx(Project p){
        try{ Path out=storage.getExportPath(p.getId(), ExportFormat.DOCX);
            try (XWPFDocument doc = new XWPFDocument(); OutputStream os = Files.newOutputStream(out)) {
                for (String line : exportText(p).split("\\R", -1)) addParagraph(doc, line);
                doc.write(os);
            }
            return out;
        } catch (IOException e){ throw new RuntimeException("Ошибка экспорта DOCX", e);} }

    public Path exportToPdf(Project p){
        try{ Path out=storage.getExportPath(p.getId(), ExportFormat.PDF);
            try(PDDocument doc=new PDDocument()){
                PDFont font = loadPdfFont(doc);
                PdfWriter writer = new PdfWriter(doc, font);
                for (String paragraph : exportText(p).replace("\r", "").split("\n", -1)) {
                    if (paragraph.isBlank()) writer.blankLine();
                    else for (String line : wrap(paragraph, font, PDF_FONT_SIZE, writer.contentWidth())) writer.writeLine(line);
                }
                writer.close();
                doc.save(out.toFile());
            }
            return out;
        } catch (IOException e){ throw new RuntimeException("Ошибка экспорта PDF", e);} }

    private String exportText(Project p) {
        return "Проект: " + nullToEmpty(p.getTitle())
                + "\nФайл: " + nullToEmpty(p.getOriginalFileName())
                + "\n\nИтоговый текст:\n" + nullToEmpty(TextVersionSelector.bestTextForExport(p))
                + "\n\n" + analysisText(p.getAnalysisResult());
    }

    private String analysisText(TextAnalysisResult a) {
        if (a == null) return "Анализ: нет данных";
        StringBuilder sb = new StringBuilder();
        sb.append("Анализ:\n")
                .append("Слов: ").append(a.wordCount).append('\n')
                .append("Предложений: ").append(a.sentenceCount).append('\n')
                .append("Абзацев: ").append(a.paragraphCount).append('\n')
                .append("Уникальных слов: ").append(a.uniqueWordCount).append('\n')
                .append("Средняя длина предложения: ").append(String.format(java.util.Locale.ROOT, "%.2f", a.averageSentenceLength)).append('\n')
                .append("Слов в минуту: ").append(String.format(java.util.Locale.ROOT, "%.2f", a.wordsPerMinute)).append('\n');
        if (a.sourceTextType != null) sb.append("Источник анализа: ").append(a.sourceTextType).append('\n');
        sb.append("\nКраткое содержание:\n").append(nullToEmpty(a.algorithmicSummary)).append('\n');
        if (a.keywordFrequency != null && !a.keywordFrequency.isEmpty()) sb.append("\nКлючевые слова:\n").append(formatMap(a.keywordFrequency)).append('\n');
        if (a.fillerWordFrequency != null && !a.fillerWordFrequency.isEmpty()) sb.append("\nСлова-паразиты:\n").append(formatMap(a.fillerWordFrequency));
        return sb.toString();
    }

    private String formatMap(Map<String, Integer> values) {
        StringBuilder sb = new StringBuilder();
        values.forEach((key, value) -> sb.append(key).append(" — ").append(value).append('\n'));
        return sb.toString().trim();
    }

    private void addParagraph(XWPFDocument doc, String text) { XWPFParagraph p = doc.createParagraph(); p.createRun().setText(text == null ? "" : text); }
    private PDFont loadPdfFont(PDDocument doc) throws IOException {
        ClassPathResource bundledFont = new ClassPathResource("fonts/DejaVuSans.ttf");
        if (bundledFont.exists()) {
            return PDType0Font.load(doc, bundledFont.getInputStream());
        }

        for (String candidate : List.of(
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/local/share/fonts/DejaVuSans.ttf",
                "C:/Windows/Fonts/arial.ttf"
        )) {
            Path path = Path.of(candidate);
            if (Files.exists(path)) {
                return PDType0Font.load(doc, path.toFile());
            }
        }

        throw new IOException("Для PDF с кириллицей добавьте Unicode-шрифт DejaVuSans.ttf в src/main/resources/fonts или установите системный DejaVuSans.ttf.");
    }
    private String nullToEmpty(String v){ return v==null?"":v; }

    private List<String> wrap(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (textWidth(candidate, font, fontSize) <= maxWidth) {
                line.setLength(0); line.append(candidate);
            } else {
                if (!line.isEmpty()) lines.add(line.toString());
                line.setLength(0);
                if (textWidth(word, font, fontSize) <= maxWidth) line.append(word);
                else lines.addAll(splitLongWord(word, font, fontSize, maxWidth));
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    private List<String> splitLongWord(String word, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> parts = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        for (char c : word.toCharArray()) {
            String candidate = part.toString() + c;
            if (textWidth(candidate, font, fontSize) <= maxWidth) part.append(c);
            else { if (!part.isEmpty()) parts.add(part.toString()); part.setLength(0); part.append(c); }
        }
        if (!part.isEmpty()) parts.add(part.toString());
        return parts;
    }

    private float textWidth(String text, PDFont font, float fontSize) throws IOException { return font.getStringWidth(text) / 1000 * fontSize; }

    private static class PdfWriter implements AutoCloseable {
        private final PDDocument doc; private final PDFont font; private PDPageContentStream cs; private float y;
        PdfWriter(PDDocument doc, PDFont font) throws IOException { this.doc=doc; this.font=font; newPage(); }
        float contentWidth(){ return PDRectangle.LETTER.getWidth() - PDF_MARGIN * 2; }
        void writeLine(String line) throws IOException { ensureSpace(); cs.showText(line); cs.newLineAtOffset(0, -PDF_LEADING); y -= PDF_LEADING; }
        void blankLine() throws IOException { ensureSpace(); cs.newLineAtOffset(0, -PDF_LEADING); y -= PDF_LEADING; }
        private void ensureSpace() throws IOException { if (y <= PDF_MARGIN) newPage(); }
        private void newPage() throws IOException { if (cs != null) { cs.endText(); cs.close(); } PDPage page = new PDPage(PDRectangle.LETTER); doc.addPage(page); cs = new PDPageContentStream(doc, page); cs.beginText(); cs.setFont(font, PDF_FONT_SIZE); y = page.getMediaBox().getHeight() - PDF_MARGIN; cs.newLineAtOffset(PDF_MARGIN, y); }
        public void close() throws IOException { if (cs != null) { cs.endText(); cs.close(); } }
    }
}
