package com.example.audiotext.service;

import com.example.audiotext.config.AppProperties;
import com.example.audiotext.model.TranscriptionResult;
import com.example.audiotext.model.TranscriptionSegment;
import com.example.audiotext.model.WordInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoskTranscriptionService {
    private final AppProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public VoskTranscriptionService(AppProperties props) { this.props = props; }

    public TranscriptionResult transcribe(Path wavFile) {
        Path modelPath = Path.of(props.getVosk().getModelPath());
        if (!Files.exists(modelPath)) {
            throw new IllegalStateException("Модель Vosk не найдена: " + modelPath.toAbsolutePath());
        }

        try (Model model = new Model(modelPath.toString());
             InputStream is = Files.newInputStream(wavFile);
             Recognizer recognizer = new Recognizer(model, props.getVosk().getSampleRate())) {

            byte[] buffer = new byte[4096];
            StringBuilder text = new StringBuilder();
            List<WordInfo> words = new ArrayList<>();
            List<TranscriptionSegment> segments = new ArrayList<>();

            int n;
            while ((n = is.read(buffer)) >= 0) {
                if (n == 0) break;
                if (recognizer.acceptWaveForm(buffer, n)) {
                    parseChunk(recognizer.getResult(), text, words, segments);
                }
            }
            parseChunk(recognizer.getFinalResult(), text, words, segments);

            TranscriptionResult result = new TranscriptionResult();
            result.setRawText(text.toString().trim());
            result.setWords(words);
            result.setSegments(segments.isEmpty() && !result.getRawText().isBlank()
                    ? List.of(new TranscriptionSegment(0, 0, result.getRawText(), 0.0))
                    : segments);
            double duration = segments.isEmpty() ? 0.0 : segments.get(segments.size() - 1).getEnd();
            result.setDurationSeconds(duration);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка распознавания Vosk: " + e.getMessage(), e);
        }
    }

    private void parseChunk(String json, StringBuilder text, List<WordInfo> words, List<TranscriptionSegment> segments) {
        try {
            JsonNode node = mapper.readTree(json);
            String chunkText = node.path("text").asText("").trim();
            if (!chunkText.isBlank()) {
                if (text.length() > 0) text.append(' ');
                text.append(chunkText);
            }

            JsonNode resultArray = node.path("result");
            if (resultArray.isArray() && resultArray.size() > 0) {
                double start = resultArray.get(0).path("start").asDouble(0.0);
                double end = resultArray.get(resultArray.size() - 1).path("end").asDouble(start);
                double conf = 0.0;
                for (JsonNode w : resultArray) {
                    String word = w.path("word").asText("");
                    double ws = w.path("start").asDouble(0.0);
                    double we = w.path("end").asDouble(ws);
                    double wc = w.path("conf").asDouble(0.0);
                    conf += wc;
                    words.add(new WordInfo(word, ws, we, wc));
                }
                conf = conf / resultArray.size();
                segments.add(new TranscriptionSegment(start, end, chunkText, conf));
            }
        } catch (Exception ignored) {
            // В случае нестандартного JSON от движка не останавливаем весь pipeline.
        }
    }
}
