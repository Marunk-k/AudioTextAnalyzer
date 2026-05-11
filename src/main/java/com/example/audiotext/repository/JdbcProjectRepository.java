package com.example.audiotext.repository;
import com.example.audiotext.model.*;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.stereotype.Repository;import java.sql.*;import java.time.LocalDateTime;import java.util.*;
@Repository
public class JdbcProjectRepository implements ProjectRepository {
 private final JdbcTemplate jdbc; public JdbcProjectRepository(JdbcTemplate j){jdbc=j; init();}
 private void init(){ jdbc.execute("create table if not exists projects(id integer primary key autoincrement,title text,original_file_name text,original_file_path text,status text,created_at text,updated_at text,raw_text text,processed_text text,ai_text text,error_message text,duration_seconds real)"); }
 public Project save(Project p){ LocalDateTime now=LocalDateTime.now(); p.setCreatedAt(now); p.setUpdatedAt(now); jdbc.update("insert into projects(title,original_file_name,original_file_path,status,created_at,updated_at) values(?,?,?,?,?,?)",p.getTitle(),p.getOriginalFileName(),p.getOriginalFilePath(),p.getStatus().name(),now.toString(),now.toString()); Long id=jdbc.queryForObject("select last_insert_rowid()",Long.class); p.setId(id); return p; }
 public Optional<Project> findById(Long id){ var l=jdbc.query("select * from projects where id=?",this::map,id); return l.stream().findFirst(); }
 public List<Project> findAll(){ return jdbc.query("select * from projects order by id desc",this::map); }
 public void update(Project p){ p.setUpdatedAt(LocalDateTime.now()); jdbc.update("update projects set status=?,updated_at=?,raw_text=?,processed_text=?,ai_text=?,error_message=?,duration_seconds=? where id=?",p.getStatus().name(),p.getUpdatedAt().toString(),p.getRawText(),p.getProcessedText(),p.getAiText(),p.getErrorMessage(),p.getDurationSeconds(),p.getId()); }
 private Project map(ResultSet rs,int n) throws SQLException{ Project p=new Project(); p.setId(rs.getLong("id")); p.setTitle(rs.getString("title")); p.setOriginalFileName(rs.getString("original_file_name")); p.setOriginalFilePath(rs.getString("original_file_path")); p.setStatus(ProjectStatus.valueOf(rs.getString("status"))); p.setRawText(rs.getString("raw_text")); p.setProcessedText(rs.getString("processed_text")); p.setAiText(rs.getString("ai_text")); p.setErrorMessage(rs.getString("error_message")); p.setDurationSeconds(rs.getDouble("duration_seconds")); return p; }
}
