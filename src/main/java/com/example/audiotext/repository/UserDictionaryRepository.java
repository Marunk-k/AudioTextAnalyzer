package com.example.audiotext.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserDictionaryRepository {
    public record Entry(Long id, String sourceValue, boolean enabled) {}
    private final JdbcTemplate jdbc;
    public UserDictionaryRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}

    public List<Entry> findByUserLogin(String login){
        String sql = "select de.id,de.source_value,de.enabled from dictionary_entries de join dictionaries d on d.id=de.dictionary_id join users u on u.id=d.user_id where u.login=? and d.type='FILLER_WORDS' order by de.id desc";
        return jdbc.query(sql,(rs,n)->new Entry(rs.getLong("id"), rs.getString("source_value"), rs.getBoolean("enabled")),login);
    }
    public List<String> findEnabledValuesByUserLogin(String login){
        return jdbc.queryForList("select de.source_value from dictionary_entries de join dictionaries d on d.id=de.dictionary_id join users u on u.id=d.user_id where u.login=? and d.type='FILLER_WORDS' and de.enabled=true", String.class, login);
    }
    public void add(String login, String value){
        Long dictId = ensureDict(login);
        jdbc.update("insert into dictionary_entries(dictionary_id,source_value,enabled,created_at) values(?,?,true,?)", dictId, value.trim().toLowerCase(), LocalDateTime.now());
    }
    public void delete(String login, Long id){ jdbc.update("delete from dictionary_entries de using dictionaries d, users u where de.id=? and de.dictionary_id=d.id and d.user_id=u.id and u.login=?", id, login); }
    public void toggle(String login, Long id, boolean enabled){ jdbc.update("update dictionary_entries de set enabled=? from dictionaries d, users u where de.id=? and de.dictionary_id=d.id and d.user_id=u.id and u.login=?", enabled, id, login); }
    private Long ensureDict(String login){
        var ids = jdbc.queryForList("select d.id from dictionaries d join users u on u.id=d.user_id where u.login=? and d.type='FILLER_WORDS' limit 1", Long.class, login);
        if(!ids.isEmpty()) return ids.get(0);
        Long userId = jdbc.queryForObject("select id from users where login=?", Long.class, login);
        jdbc.update("insert into dictionaries(user_id,name,type,created_at) values(?,?,'FILLER_WORDS',?)", userId, "Мои слова-паразиты", LocalDateTime.now());
        return jdbc.queryForObject("select d.id from dictionaries d where d.user_id=? and d.type='FILLER_WORDS' order by d.id desc limit 1", Long.class, userId);
    }
}
