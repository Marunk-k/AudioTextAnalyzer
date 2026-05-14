package com.example.audiotext.config;
import org.springframework.context.annotation.*;import org.springframework.jdbc.datasource.DriverManagerDataSource;import javax.sql.DataSource;
@Configuration public class DataSourceConfig { @Bean DataSource dataSource(AppProperties p){ var ds=new DriverManagerDataSource(); ds.setDriverClassName("org.sqlite.JDBC"); ds.setUrl("jdbc:sqlite:"+p.getStorage().getDatabasePath()); return ds; } }
