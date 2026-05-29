package com.example.audiotext.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDictionaryRepository {
    public static final String FILLER_WORDS = "FILLER_WORDS";
    public static final String REPLACEMENTS = "REPLACEMENTS";
    public record Entry(Long id, String type, String sourceValue, String targetValue, boolean enabled) {}
    private final JdbcTemplate jdbc;
    public UserDictionaryRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}

    public List<Entry> findEntriesByUserLoginAndType(String login, String type){
        String sql = "select de.id,d.type,de.source_value,de.target_value,de.enabled from dictionary_entries de join dictionaries d on d.id=de.dictionary_id join users u on u.id=d.user_id where u.login=? and d.type=? order by de.id desc";
        return jdbc.query(sql,(rs,n)->new Entry(rs.getLong("id"), rs.getString("type"), rs.getString("source_value"), rs.getString("target_value"), rs.getBoolean("enabled")),login,type);
    }
    public List<Entry> findByUserLogin(String login){ return findEntriesByUserLoginAndType(login, FILLER_WORDS); }
    public List<String> findEnabledValuesByUserLogin(String login){
        return jdbc.queryForList("select de.source_value from dictionary_entries de join dictionaries d on d.id=de.dictionary_id join users u on u.id=d.user_id where u.login=? and d.type=? and de.enabled=true", String.class, login, FILLER_WORDS);
    }
    public Map<String,String> findEnabledReplacementsByUserLogin(String login){
        String sql = "select de.source_value,de.target_value from dictionary_entries de join dictionaries d on d.id=de.dictionary_id join users u on u.id=d.user_id where u.login=? and d.type=? and de.enabled=true order by de.id";
        return jdbc.query(sql, rs -> { Map<String,String> m = new LinkedHashMap<>(); while(rs.next()) m.put(rs.getString("source_value"), rs.getString("target_value")); return m; }, login, REPLACEMENTS);
    }
    public void add(String login, String value){ add(login, FILLER_WORDS, value, null); }
    public void add(String login, String type, String sourceValue, String targetValue){
        Long dictId = ensureDict(login, type);
        jdbc.update("delete from dictionary_entries where dictionary_id=? and lower(source_value)=lower(?)", dictId, sourceValue.trim());
        jdbc.update("insert into dictionary_entries(dictionary_id,source_value,target_value,enabled,created_at) values(?,?,?,?,?)", dictId, sourceValue.trim().toLowerCase(), targetValue == null ? null : targetValue.trim(), true, LocalDateTime.now());
    }
    public void delete(String login, Long id){ jdbc.update("delete from dictionary_entries where id in (select de.id from dictionary_entries de join dictionaries d on d.id=de.dictionary_id join users u on u.id=d.user_id where de.id=? and u.login=?)", id, login); }
    private Long ensureDict(String login, String type){
        var ids = jdbc.queryForList("select d.id from dictionaries d join users u on u.id=d.user_id where u.login=? and d.type=? limit 1", Long.class, login, type);
        if(!ids.isEmpty()) return ids.get(0);
        Long userId = jdbc.queryForObject("select id from users where login=?", Long.class, login);
        String name = FILLER_WORDS.equals(type) ? "Мои слова-паразиты" : "Мои замены";
        jdbc.update("insert into dictionaries(user_id,name,type,is_system,created_at) values(?,?,?,?,?)", userId, name, type, false, LocalDateTime.now());
        return jdbc.queryForObject("select d.id from dictionaries d where d.user_id=? and d.type=? order by d.id desc limit 1", Long.class, userId, type);
    }
}
