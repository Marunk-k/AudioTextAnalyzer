package com.example.audiotext.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DictionaryService {
    private final JdbcTemplate jdbc;

    public DictionaryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        init();
    }

    private void init() {
        jdbc.execute("create table if not exists system_dictionary(source text primary key, replacement text not null)");
        jdbc.execute("create table if not exists user_dictionary(username text not null, source text not null, replacement text not null, primary key(username,source))");
    }

    public Map<String, String> getSystemDictionary() {
        return jdbc.query("select source,replacement from system_dictionary order by source", rs -> {
            Map<String, String> m = new LinkedHashMap<>();
            while (rs.next()) m.put(rs.getString("source"), rs.getString("replacement"));
            return m;
        });
    }

    public Map<String, String> getUserDictionary(String username) {
        return jdbc.query("select source,replacement from user_dictionary where username=? order by source", rs -> {
            Map<String, String> m = new LinkedHashMap<>();
            while (rs.next()) m.put(rs.getString("source"), rs.getString("replacement"));
            return m;
        }, username);
    }

    public Map<String, String> getEffectiveDictionaryForCurrentUser(Map<String, String> baseDictionary) {
        Map<String, String> merged = new LinkedHashMap<>(baseDictionary);
        merged.putAll(getSystemDictionary());
        String username = currentUsername();
        if (username != null) merged.putAll(getUserDictionary(username));
        return merged;
    }

    public void saveUserTerm(String username, String source, String replacement) {
        jdbc.update("insert into user_dictionary(username,source,replacement) values(?,?,?) on conflict(username,source) do update set replacement=excluded.replacement",
                username, source, replacement);
    }

    public void deleteUserTerm(String username, String source) {
        jdbc.update("delete from user_dictionary where username=? and source=?", username, source);
    }

    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return auth.getName();
    }
}
