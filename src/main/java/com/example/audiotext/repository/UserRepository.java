package com.example.audiotext.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    public UserRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Long findIdByLogin(String login){ return jdbc.queryForObject("select id from users where login=?", Long.class, login); }
}
