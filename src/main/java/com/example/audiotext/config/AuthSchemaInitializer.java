package com.example.audiotext.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthSchemaInitializer {
    public AuthSchemaInitializer(JdbcTemplate jdbc) {
        jdbc.execute("create table if not exists users(username text primary key,password text not null,enabled integer not null)");
        jdbc.execute("create table if not exists authorities(username text not null,authority text not null,unique(username,authority))");
    }
}
