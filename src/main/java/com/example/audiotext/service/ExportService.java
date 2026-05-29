package com.example.audiotext.service;

import com.example.audiotext.model.ExportFormat;
import com.example.audiotext.model.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ExportService {
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
                addParagraph(doc, "AudioText Analyzer — " + p.getTitle());
                addParagraph(doc, "Файл: " + nullToEmpty(p.getOriginalFileName()));
                addParagraph(doc, "Главный текст:"); addParagraph(doc, nullToEmpty(TextVersionSelector.bestTextForExport(p)));
                if (p.getAnalysisResult()!=null) addParagraph(doc, analysisLine(p));
                doc.write(os);
            }
            return out;
        } catch (IOException e){ throw new RuntimeException("Ошибка экспорта DOCX", e);} }

    public Path exportToPdf(Project p){
        try{ Path out=storage.getExportPath(p.getId(), ExportFormat.PDF);
            try(PDDocument doc=new PDDocument()){
                PDPage page=new PDPage(); doc.addPage(page);
                PDFont font = loadPdfFont(doc);
                try(PDPageContentStream cs=new PDPageContentStream(doc,page)){
                    cs.beginText(); cs.setFont(font,12); cs.newLineAtOffset(50,750);
                    for (String line : exportText(p).replace("\r", "").split("\n")) {
                        cs.showText(trimLen(line, 95)); cs.newLineAtOffset(0,-16);
                    }
                    cs.endText();
                }
                doc.save(out.toFile());
            }
            return out;
        } catch (IOException e){ throw new RuntimeException("Ошибка экспорта PDF", e);} }

    private String exportText(Project p) {
        return "Проект: "+p.getTitle()+"\nФайл: "+nullToEmpty(p.getOriginalFileName())+"\n\nГлавный текст:\n"+nullToEmpty(TextVersionSelector.bestTextForExport(p))+"\n\n"+analysisLine(p);
    }
    private String analysisLine(Project p) { if (p.getAnalysisResult()==null) return "Анализ: нет данных"; var a=p.getAnalysisResult(); return "Анализ: слов="+a.wordCount+", предложений="+a.sentenceCount+", абзацев="+a.paragraphCount+", уникальных слов="+a.uniqueWordCount+", слов/мин="+a.wordsPerMinute+"\nКраткое содержание:\n"+nullToEmpty(a.algorithmicSummary); }
    private void addParagraph(XWPFDocument doc, String text) { XWPFParagraph p = doc.createParagraph(); p.createRun().setText(text == null ? "" : text); }
    private PDFont loadPdfFont(PDDocument doc) throws IOException { try { return PDType0Font.load(doc, new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream()); } catch (Exception ex) { throw new IOException("Для PDF с кириллицей добавьте fonts/DejaVuSans.ttf в resources/fonts.", ex); } }
    private String nullToEmpty(String v){ return v==null?"":v; }
    private String trimLen(String v,int n){ String s=nullToEmpty(v).replaceAll("[\\p{Cntrl}&&[^\t]]", " "); return s.length()>n?s.substring(0,n)+"...":s; }
}
