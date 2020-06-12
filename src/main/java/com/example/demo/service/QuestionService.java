package com.example.demo.service;
import com.alibaba.fastjson.JSON;
import com.example.demo.model.Competitor;
import com.example.demo.model.Questons;
import com.example.demo.model.englishQuest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@Service
//public class QuestionService {
//
//    @Autowired
//    private QuestionRepository questionRepository;
//
//    public List<Question> getQuestions(int limit) {
//        Set<Integer> ids = new HashSet<>();
//        int min = 1;
//        int max = (int) questionRepository.count();
//        Random random = new Random();
//        while (ids.size()<limit) {
//            int id = random.nextInt(max) % (max - min + 1) + min;
//            ids.add(id);
//        }
//        return ids.stream().map(questionRepository::findOne).collect(Collectors.toList());
//    }
//}
@Service
public class QuestionService {

    //    private static DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;// = jdbcTemp.jdbcTemplate1;// = new JdbcTemplate(dataSource);

//    public static QuestionService questionService = new QuestionService();
//    public static QuestionService getInstance(){
//        return questionService;
//    }

    private final Logger logger = LoggerFactory.getLogger(QuestionService.class);



    public String Generate(int id){

        //查询用户计划SQL语句
        String sql_plan = "select `plan` from `user` where `id` = " + id;
        //执行语句，返回String类型数据
        String plan = jdbcTemplate.queryForObject(sql_plan, String.class);
        //随机取出40个单词
        String sql_word = "select  *  from `" + plan + "` ORDER BY RAND() LIMIT 40";
        List<Map<String, Object>> list_word = jdbcTemplate.queryForList(sql_word);

        englishQuest[] quests = new englishQuest[20];
        for (int i = 0; i < 20; i++){
            quests[i] = new englishQuest();
        }
        for (int i = 0; i < 10; i++) {
            String q = list_word.get(i).get("word").toString();

            quests[i].setQuestion(q);
            setAnsAndCorr(i, quests, list_word);
        }
        for (int i = 10; i < 20; i++){
            String q = list_word.get(i).get("word").toString();
            String sentence = list_word.get(i).get("sentence").toString();
            //需要将sentence分割一遍
            String sentence_english = sentenceSplit(sentence);
            String qu = q +":  "+ sentence_english;
            quests[i].setQuestion(qu);
            setAnsAndCorr(i, quests, list_word);
        }
        Questons questons = new Questons();
        questons.setQuestions(quests);
        String result = JSON.toJSONString(questons);
        return result;
    }

    private void setAnsAndCorr(int i, englishQuest[] quests, List<Map<String, Object>> list_word)
    {
        int min = 20;
        int max = 40;

        //新建数组，四个答案数组
        String[] ans = new String[4];

        //随机生成正确答案选项的位置
        Random random2 = new Random();
        int corr = random2.nextInt(4);

        //设置问题正确答案的位置
        quests[i].setCorrectAnswer(corr+1);

        //设置正确答案的内容
        ans[corr] = list_word.get(i).get("meaning").toString();

        //随机从列表中，选取从20 - 40 的单词
        Random random1 = new Random();
        for (int j = 0; j < 4; j++) {
            //如果是正确答案选项，跳过设置
            if ( j == corr){
                continue;
            }
            //设置其他答案的内容

            int s = random1.nextInt(max) % (max - min + 1) + min;
            String mean = list_word.get(s-1).get("meaning").toString();
            ans[j] = mean;
        }
        //设置问题
        quests[i].setAnswers(ans);
    }
    private String sentenceSplit(String sent){
        String[] list=sent.split("[1-9]\\.\\s+");
        String reg_charset = "[\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(reg_charset);
        Matcher m = p.matcher(list[1]);
        m.find();
        int index = list[1].indexOf(m.group());
        String sentence_english = list[1].substring(0, index-1);
        String sentence_meaning = list[1].substring(index);
        return sentence_english;
    }


}