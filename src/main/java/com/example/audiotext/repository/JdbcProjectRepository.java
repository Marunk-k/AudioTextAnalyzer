package com.example.audiotext.repository;

import com.example.audiotext.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcProjectRepository implements ProjectRepository {
 private final JdbcTemplate jdbc;
 private final ObjectMapper mapper = new ObjectMapper();
 public JdbcProjectRepository(JdbcTemplate j){jdbc=j; init();}
 private void init(){
   jdbc.execute("create table if not exists projects(id integer primary key autoincrement,title text,original_file_name text,original_file_path text,status text,created_at text,updated_at text,raw_text text,processed_text text,ai_text text,error_message text,duration_seconds real)");
   jdbc.execute("create table if not exists transcription_segments(id integer primary key autoincrement,project_id integer,start_time real,end_time real,text text,confidence real)");
   jdbc.execute("create table if not exists analysis_results(id integer primary key autoincrement,project_id integer,word_count integer,sentence_count integer,paragraph_count integer,unique_word_count integer,average_sentence_length real,words_per_minute real,keywords_json text,filler_words_json text,algorithmic_summary text,created_at text)");
 }
 public Project save(Project p){ LocalDateTime now=LocalDateTime.now(); p.setCreatedAt(now); p.setUpdatedAt(now); KeyHolder kh=new GeneratedKeyHolder(); jdbc.update(con->{ var ps=con.prepareStatement("insert into projects(title,original_file_name,original_file_path,status,created_at,updated_at) values(?,?,?,?,?,?)", java.sql.Statement.RETURN_GENERATED_KEYS); ps.setString(1,p.getTitle()); ps.setString(2,p.getOriginalFileName()); ps.setString(3,p.getOriginalFilePath()); ps.setString(4,p.getStatus().name()); ps.setString(5,now.toString()); ps.setString(6,now.toString()); return ps; },kh); Number key=kh.getKey(); if(key==null) throw new IllegalStateException("Не удалось получить ID созданного проекта"); p.setId(key.longValue()); return p; }
 public Optional<Project> findById(Long id){ var l=jdbc.query("select * from projects where id=?",this::map,id); return l.stream().findFirst().map(p->{p.setSegments(findSegmentsByProjectId(p.getId())); p.setAnalysisResult(loadAnalysis(p.getId())); return p;}); }
 public List<Project> findAll(){ return jdbc.query("select * from projects order by id desc",this::map); }
 public void update(Project p){ p.setUpdatedAt(LocalDateTime.now()); jdbc.update("update projects set status=?,updated_at=?,raw_text=?,processed_text=?,ai_text=?,error_message=?,duration_seconds=? where id=?",p.getStatus().name(),p.getUpdatedAt().toString(),p.getRawText(),p.getProcessedText(),p.getAiText(),p.getErrorMessage(),p.getDurationSeconds(),p.getId()); }
 public void updateStatus(Long id, ProjectStatus status){ jdbc.update("update projects set status=?,updated_at=? where id=?",status.name(),LocalDateTime.now().toString(),id); }
 public void updateTexts(Long id, String rawText, String processedText, String aiText){ jdbc.update("update projects set raw_text=?,processed_text=?,ai_text=?,updated_at=? where id=?",rawText,processedText,aiText,LocalDateTime.now().toString(),id); }
 public void updateAnalysis(Long projectId, TextAnalysisResult r){ try{ jdbc.update("delete from analysis_results where project_id=?",projectId); jdbc.update("insert into analysis_results(project_id,word_count,sentence_count,paragraph_count,unique_word_count,average_sentence_length,words_per_minute,keywords_json,filler_words_json,algorithmic_summary,created_at) values(?,?,?,?,?,?,?,?,?,?,?)",projectId,r.wordCount,r.sentenceCount,r.paragraphCount,r.uniqueWordCount,r.averageSentenceLength,r.wordsPerMinute,mapper.writeValueAsString(r.keywordFrequency),mapper.writeValueAsString(r.fillerWordFrequency),r.algorithmicSummary,LocalDateTime.now().toString()); }catch(Exception e){ throw new RuntimeException(e);} }
 public void saveSegments(Long projectId, List<TranscriptionSegment> segments){ jdbc.update("delete from transcription_segments where project_id=?",projectId); for(var s:segments){ jdbc.update("insert into transcription_segments(project_id,start_time,end_time,text,confidence) values(?,?,?,?,?)",projectId,s.getStart(),s.getEnd(),s.getText(),s.getConfidence()); }}
 public List<TranscriptionSegment> findSegmentsByProjectId(Long projectId){ return jdbc.query("select * from transcription_segments where project_id=? order by id", (rs,n)->seg(rs), projectId); }
 public void deleteById(Long id){ jdbc.update("delete from transcription_segments where project_id=?",id); jdbc.update("delete from analysis_results where project_id=?",id); jdbc.update("delete from projects where id=?",id); }
 private TextAnalysisResult loadAnalysis(Long projectId){ try{ var list=jdbc.query("select * from analysis_results where project_id=? order by id desc limit 1",(rs,n)->{ TextAnalysisResult r=new TextAnalysisResult(); r.wordCount=rs.getInt("word_count"); r.sentenceCount=rs.getInt("sentence_count"); r.paragraphCount=rs.getInt("paragraph_count"); r.uniqueWordCount=rs.getInt("unique_word_count"); r.averageSentenceLength=rs.getDouble("average_sentence_length"); r.wordsPerMinute=rs.getDouble("words_per_minute"); try { r.keywordFrequency=mapper.readValue(rs.getString("keywords_json"),mapper.getTypeFactory().constructMapType(java.util.LinkedHashMap.class,String.class,Integer.class)); r.fillerWordFrequency=mapper.readValue(rs.getString("filler_words_json"),mapper.getTypeFactory().constructMapType(java.util.LinkedHashMap.class,String.class,Integer.class)); } catch (Exception ex) { r.keywordFrequency = new java.util.LinkedHashMap<>(); r.fillerWordFrequency = new java.util.LinkedHashMap<>(); } r.algorithmicSummary=rs.getString("algorithmic_summary"); return r;},projectId); return list.isEmpty()?null:list.get(0);}catch(Exception e){return null;} }
 private Project map(ResultSet rs,int n) throws SQLException{ Project p=new Project(); p.setId(rs.getLong("id")); p.setTitle(rs.getString("title")); p.setOriginalFileName(rs.getString("original_file_name")); p.setOriginalFilePath(rs.getString("original_file_path")); p.setStatus(ProjectStatus.valueOf(rs.getString("status"))); p.setRawText(rs.getString("raw_text")); p.setProcessedText(rs.getString("processed_text")); p.setAiText(rs.getString("ai_text")); p.setErrorMessage(rs.getString("error_message")); p.setDurationSeconds(rs.getDouble("duration_seconds")); return p; }
 private TranscriptionSegment seg(ResultSet rs) throws SQLException{return new TranscriptionSegment(rs.getDouble("start_time"),rs.getDouble("end_time"),rs.getString("text"),rs.getDouble("confidence"));}
}
