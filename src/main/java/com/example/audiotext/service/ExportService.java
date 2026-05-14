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
            String body="Проект: "+p.getTitle()+"\nФайл: "+p.getOriginalFileName()+"\n\n"+(p.getProcessedText()==null?"":p.getProcessedText())+"\n\nAI:\n"+(p.getAiText()==null?"":p.getAiText());
            Files.writeString(out, body, StandardCharsets.UTF_8); return out; }catch(Exception e){throw new RuntimeException("Ошибка экспорта TXT",e);} }

    public Path exportToJson(Project p){
        try{ Path out=storage.getExportPath(p.getId(), ExportFormat.JSON);
            Map<String,Object> payload = new LinkedHashMap<>();
            payload.put("id", p.getId()); payload.put("title", p.getTitle()); payload.put("status", p.getStatus());
            payload.put("rawText", p.getRawText()); payload.put("processedText", p.getProcessedText()); payload.put("aiText", p.getAiText()); payload.put("analysis", p.getAnalysisResult());
            Files.writeString(out, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload), StandardCharsets.UTF_8);
            return out;
        }catch(Exception e){throw new RuntimeException("Ошибка экспорта JSON",e);} }

    public Path exportToDocx(Project p){
        try{ Path out=storage.getExportPath(p.getId(), ExportFormat.DOCX);
            try (XWPFDocument doc = new XWPFDocument(); OutputStream os = Files.newOutputStream(out)) {
                XWPFParagraph h = doc.createParagraph(); h.createRun().setText("AudioText Analyzer — " + p.getTitle());
                XWPFParagraph meta = doc.createParagraph(); meta.createRun().setText("Файл: " + p.getOriginalFileName());
                XWPFParagraph text = doc.createParagraph(); text.createRun().setText(nullToEmpty(p.getProcessedText()));
                if (p.getAiText()!=null && !p.getAiText().isBlank()) { XWPFParagraph ai = doc.createParagraph(); ai.createRun().setText("AI версия: " + p.getAiText()); }
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
                    cs.showText("AudioText Analyzer: "+nullToEmpty(p.getTitle())); cs.newLineAtOffset(0,-18);
                    cs.showText("Файл: "+nullToEmpty(p.getOriginalFileName())); cs.newLineAtOffset(0,-18);
                    cs.showText(trimLen(p.getProcessedText(),120)); cs.endText();
                }
                doc.save(out.toFile());
            }
            return out;
        } catch (IOException e){ throw new RuntimeException("Ошибка экспорта PDF", e);} }

    public Path exportToSrt(Project p){
        try{ Path out=storage.getExportPath(p.getId(), ExportFormat.SRT);
            StringBuilder sb=new StringBuilder(); int idx=1;
            for(var seg: p.getSegments()){
                sb.append(idx++).append("\n"); sb.append(toSrt(seg.getStart())).append(" --> ").append(toSrt(seg.getEnd())).append("\n"); sb.append(seg.getText()==null?"":seg.getText()).append("\n\n");
            }
            if(sb.length()==0) throw new IllegalStateException("Сегменты недоступны для SRT экспорта");
            Files.writeString(out,sb.toString(),StandardCharsets.UTF_8); return out;
        }catch(Exception e){ throw new RuntimeException("Ошибка экспорта SRT",e);} }

    private PDFont loadPdfFont(PDDocument doc) throws IOException {
        try { return PDType0Font.load(doc, new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream()); }
        catch (Exception ex) { throw new IOException("Для PDF с кириллицей добавьте fonts/DejaVuSans.ttf в resources/fonts.", ex); }
    }
    private String nullToEmpty(String v){ return v==null?"":v; }
    private String trimLen(String v,int n){ String s=nullToEmpty(v); return s.length()>n?s.substring(0,n)+"...":s; }
    private String toSrt(double sec){ int ms=(int)Math.round(sec*1000); int h=ms/3600000; ms%=3600000; int m=ms/60000; ms%=60000; int s=ms/1000; ms%=1000; return String.format("%02d:%02d:%02d,%03d",h,m,s,ms);} 
}
