package com.example.audiotext.repository;

import com.example.audiotext.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;import java.sql.SQLException;import java.time.LocalDateTime;import java.util.*;

@Repository
public class JdbcProjectRepository implements ProjectRepository {
    private final JdbcTemplate jdbc; private final ObjectMapper mapper = new ObjectMapper();
    public JdbcProjectRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Project save(Project p){ LocalDateTime now=LocalDateTime.now(); p.setCreatedAt(now); p.setUpdatedAt(now); KeyHolder kh=new GeneratedKeyHolder();
        jdbc.update(c->{var ps=c.prepareStatement("insert into projects(user_id,title,original_file_name,status,created_at,updated_at) values(?,?,?,?,?,?)",java.sql.Statement.RETURN_GENERATED_KEYS); ps.setLong(1,p.getUserId()); ps.setString(2,p.getTitle()); ps.setString(3,p.getOriginalFileName()); ps.setString(4,p.getStatus().name()); ps.setObject(5,now); ps.setObject(6,now); return ps;},kh);
        p.setId(kh.getKey().longValue()); return p; }
    public Optional<Project> findById(Long id){ var l=jdbc.query(baseSql()+" where p.id=?",this::map,id); return l.stream().findFirst().map(this::hydrate);} public Optional<Project> findByIdAndOwner(Long id,String login){ var l=jdbc.query(baseSql()+" where p.id=? and u.login=?",this::map,id,login); return l.stream().findFirst().map(this::hydrate);} public List<Project> findAll(){ return jdbc.query(baseSql()+" order by p.id desc",this::map);} public List<Project> findAllByOwner(String login){ return jdbc.query(baseSql()+" where u.login=? order by p.id desc",this::map,login);} 
    public void update(Project p){ p.setUpdatedAt(LocalDateTime.now()); jdbc.update("update projects set title=?,status=?,updated_at=?,error_message=?,duration_seconds=? where id=?",p.getTitle(),p.getStatus().name(),p.getUpdatedAt(),p.getErrorMessage(),p.getDurationSeconds(),p.getId()); upsertTexts(p); }
    public void updateStatus(Long id,ProjectStatus status){ jdbc.update("update projects set status=?,updated_at=? where id=?",status.name(),LocalDateTime.now(),id);} 
    public void updateAnalysis(Long projectId, TextAnalysisResult r){ try{ jdbc.update("delete from analysis_results where project_id=?",projectId); jdbc.update("insert into analysis_results(project_id,word_count,sentence_count,paragraph_count,unique_word_count,average_sentence_length,words_per_minute,keywords_json,filler_words_json,summary,created_at,updated_at) values(?,?,?,?,?,?,?,?,?::jsonb,?,?,?)",projectId,r.wordCount,r.sentenceCount,r.paragraphCount,r.uniqueWordCount,r.averageSentenceLength,r.wordsPerMinute,mapper.writeValueAsString(r.keywordFrequency),mapper.writeValueAsString(r.fillerWordFrequency),r.algorithmicSummary,LocalDateTime.now(),LocalDateTime.now()); }catch(Exception e){throw new RuntimeException(e);} }
    public void saveSegments(Long projectId,List<TranscriptionSegment> segments){} public List<TranscriptionSegment> findSegmentsByProjectId(Long projectId){ return List.of(); }
    public void deleteById(Long id){ jdbc.update("delete from projects where id=?",id);} 
    private void upsertTexts(Project p){ jdbc.update("insert into project_texts(project_id,raw_text,processed_text,ai_text,manual_text,created_at,updated_at) values(?,?,?,?,?,?,?) on conflict(project_id) do update set raw_text=excluded.raw_text,processed_text=excluded.processed_text,ai_text=excluded.ai_text,manual_text=excluded.manual_text,updated_at=excluded.updated_at",p.getId(),p.getRawText(),p.getProcessedText(),p.getAiText(),p.getManualText(),LocalDateTime.now(),LocalDateTime.now()); }
    private Project hydrate(Project p){ p.setAnalysisResult(loadAnalysis(p.getId())); return p; }
    private String baseSql(){ return "select p.*,u.login as owner_login,pt.raw_text,pt.processed_text,pt.ai_text,pt.manual_text from projects p join users u on u.id=p.user_id left join project_texts pt on pt.project_id=p.id"; }
    private Project map(ResultSet rs,int n)throws SQLException{ Project p=new Project(); p.setId(rs.getLong("id")); p.setUserId(rs.getLong("user_id")); p.setOwnerLogin(rs.getString("owner_login")); p.setTitle(rs.getString("title")); p.setOriginalFileName(rs.getString("original_file_name")); p.setStatus(ProjectStatus.valueOf(rs.getString("status"))); p.setRawText(rs.getString("raw_text")); p.setProcessedText(rs.getString("processed_text")); p.setAiText(rs.getString("ai_text")); p.setManualText(rs.getString("manual_text")); p.setErrorMessage(rs.getString("error_message")); p.setDurationSeconds(rs.getDouble("duration_seconds")); p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); return p; }
    private TextAnalysisResult loadAnalysis(Long projectId){
        try{
            var list=jdbc.query("select * from analysis_results where project_id=? order by id desc limit 1",(rs,n)->{
                TextAnalysisResult r=new TextAnalysisResult();
                r.wordCount=rs.getInt("word_count");
                r.sentenceCount=rs.getInt("sentence_count");
                r.paragraphCount=rs.getInt("paragraph_count");
                r.uniqueWordCount=rs.getInt("unique_word_count");
                r.averageSentenceLength=rs.getDouble("average_sentence_length");
                r.wordsPerMinute=rs.getDouble("words_per_minute");
                try {
                    r.keywordFrequency=mapper.readValue(rs.getString("keywords_json"),LinkedHashMap.class);
                    r.fillerWordFrequency=mapper.readValue(rs.getString("filler_words_json"),LinkedHashMap.class);
                } catch (Exception ex) {
                    r.keywordFrequency=new LinkedHashMap<>();
                    r.fillerWordFrequency=new LinkedHashMap<>();
                }
                r.algorithmicSummary=rs.getString("summary");
                return r;
            },projectId);
            return list.isEmpty()?null:list.get(0);
        }catch(Exception e){return null;}
    }
}
