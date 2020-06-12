package com.example.demo.service;

import com.example.demo.model.Competitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    private final Logger logger = LoggerFactory.getLogger(QueryService.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public Competitor getUser(int id) throws NullPointerException{
        String url = "select `rank` from `user` where id = " + id;
        Integer rankScore = jdbcTemplate.queryForObject(url,Integer.class);
        long currentTime = System.currentTimeMillis();
        Competitor competitor = new Competitor(currentTime,id,rankScore);
        return competitor;
    }
    public Integer getRank(int id){
        String url = "select `rank` from `user` where id = " + id;
        System.out.println(url);
        Integer rankScore = 0;

        if (jdbcTemplate!= null) {
            logger.info("jdbcTemplate isn't null, everything is normal");
            rankScore = jdbcTemplate.queryForObject(url, Integer.class);
        }else {
            logger.info("jdbcTemplate is null, careful something is wrong");
        }

        return rankScore;
    }
    public void summaryResult(Integer player1, Integer rank1, Integer player2, Integer rank2){
        String sql_increase = "update `user` set user.`rank` = user.`rank` +25 where id = ";
        String sql_decrease = "update `user` set user.`rank` = user.`rank` -25 where id = ";
        if(rank1 > rank2){
            String sql = sql_increase + player1;
            String sql2 = sql_decrease + player2;
            jdbcTemplate.execute(sql);
            jdbcTemplate.execute(sql2);
        }else if (rank1.equals(rank2)){
            System.out.println("equal score");
        }else{
            String sql = sql_increase + player2;
            String sql2 = sql_decrease + player1;
            jdbcTemplate.execute(sql);
            jdbcTemplate.execute(sql2);
        }
    }
}
