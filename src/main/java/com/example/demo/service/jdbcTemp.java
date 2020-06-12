package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class jdbcTemp {
    @Autowired
    public JdbcTemplate jdbcTemplate;

    public static JdbcTemplate jdbcTemplate1;

    @Autowired
    public void setJdbcTemplate1(JdbcTemplate jdbcTemplate2){
        jdbcTemp.jdbcTemplate1 = jdbcTemplate2;
    }
}
