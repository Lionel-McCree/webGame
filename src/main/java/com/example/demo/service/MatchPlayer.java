package com.example.demo.service;
import com.example.demo.model.Competitor;
import com.example.demo.repository.PlayerData;
import com.example.demo.util.StaticContextAccessor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class MatchPlayer {
    private static final Logger logger = LoggerFactory.getLogger(MatchPlayer.class);
    private static ScheduledExecutorService sec = Executors.newSingleThreadScheduledExecutor();
    private static int NEED_MATCH_PLAYER_COUNT = 1;
    @Autowired
    private QuestionService questionService1 ;
    private static QuestionService questionService;
    @Autowired
    public void setStaticQuestionService(QuestionService questionService1){
        MatchPlayer.questionService = questionService1;
    }
//    @Autowired
//    QuestionService questionService;

    private static ConcurrentHashMap<Integer,Competitor> playPool = new ConcurrentHashMap<>();
    static {
        logger.info("second thread Matching start!");
        sec.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                matchCompetitor(playPool);
            }
        },1,1, TimeUnit.SECONDS);
    }
    public static void putCompetitorIntoPlayPool(Integer playerID, Integer rank){
        long startTime = System.currentTimeMillis();
        Competitor player = new Competitor(startTime,playerID, rank);
        playPool.put(playerID, player);

    }
    public static void removePlayerFromMatchPool(Integer playerId){
        playPool.remove(playerId);
    }


    private static void  matchCompetitor(ConcurrentHashMap<Integer, Competitor> playPool){
        long startTime = System.currentTimeMillis();
        logger.debug("执行匹配开始|开始时间|"+ startTime);
        try{
            TreeMap<Integer, HashSet<Competitor>> pointMap = new TreeMap<>();
            for (Competitor player : playPool.values()){
               if((System.currentTimeMillis()-player.getStarttime())> 60*10*1000){
                   logger.warn(player.getID() + "匹配超过十分钟，被迫退出");
                   removePlayerFromMatchPool(player.getID());
                   PlayerData playerData = PlayerData.getInstance();
                   playerData.isConnect.remove(player.getID());
                   continue;
               }
                HashSet<Competitor> set = pointMap.get(player.getRank());
                if(set==null){
                    set = new HashSet<Competitor>();
                    set.add(player);
                    pointMap.put(player.getRank(), set);
                }else{
                    set.add(player);
                }
            }

            for (HashSet<Competitor> sameRankPlayers: pointMap.values()) {
                boolean continueMatch = true;
                while(continueMatch){
                    //找出同一分数段里，等待时间最长的玩家，用他来匹配，因为他的区间最大
                    //如果他都不能匹配到，等待时间比他短的玩家更匹配不到
                    Competitor oldest = null;
                    for (Competitor playerMatchPoolInfo : sameRankPlayers) {
                        if(oldest==null){
                            oldest = playerMatchPoolInfo;
                        }else if(playerMatchPoolInfo.getStarttime()<oldest.getStarttime()){
                            oldest = playerMatchPoolInfo;
                        }
                    }
                    if(oldest==null){
                        break;
                    }
                    logger.debug(oldest.getID()+"|为该分数上等待最久时间的玩家开始匹配|rank|"+oldest.getRank());

                    long now = System.currentTimeMillis();
                    int waitSecond = (int)((now-oldest.getStarttime())/1000);

                    logger.debug(oldest.getID()+"|当前时间已经等待的时间|waitSecond|"+waitSecond+"|当前系统时间|"+now+"|开始匹配时间|"+oldest.getStarttime());

                    //按等待时间扩大匹配范围
                    float c2 = 1.5f;
                    int c3 = 5;
                    int c4 = 100;

                    float u = (float) Math.pow(waitSecond, c2);
                    u = u + c3;
                    u = (float) Math.round(u);
                    u = Math.min(u, c4);

                    int min = (oldest.getRank() - (int)u)<0?0:(oldest.getRank() - (int)u);
                    int max = oldest.getRank() + (int)u;

                    logger.debug(oldest.getID()+"|本次搜索rank范围下限|"+min+"|rank范围上限|"+max);

                    int middle = oldest.getRank();

                    List<Competitor> matchPoolPlayer = new ArrayList<>();
                    //从中位数向两边扩大范围搜索
                    for(int searchRankUp = middle,searchRankDown = middle; searchRankUp <= max||searchRankDown>=min;searchRankUp++,searchRankDown--){
                        HashSet<Competitor> thisRankPlayers = pointMap.getOrDefault(searchRankUp,new HashSet<Competitor>());
                        if(searchRankDown!=searchRankUp&&searchRankDown>0){
                            thisRankPlayers.addAll(pointMap.getOrDefault(searchRankDown,new HashSet<Competitor>()));
                        }
                        if(!thisRankPlayers.isEmpty()){
                            if(matchPoolPlayer.size()<NEED_MATCH_PLAYER_COUNT){
                                Iterator<Competitor> it = thisRankPlayers.iterator();
                                while (it.hasNext()) {
                                    Competitor player = it.next();
                                    if(player.getID()!=oldest.getID()){//排除玩家本身
                                        if(matchPoolPlayer.size()<NEED_MATCH_PLAYER_COUNT){
                                            matchPoolPlayer.add(player);
                                            logger.debug(oldest.getID()+"|匹配到玩家|"+player.getID()+"|rank|"+player.getRank());
                                            //移除
                                            it.remove();
                                        }else{
                                            break;
                                        }
                                    }
                                }
                            }else{
                                break;
                            }
                        }
                    }

                    if(matchPoolPlayer.size()==NEED_MATCH_PLAYER_COUNT){
                        logger.debug(oldest.getID()+"|匹配到玩家数量够了|提交匹配成功处理");
                        //自己也匹配池移除
                        sameRankPlayers.remove(oldest);
                        //匹配成功处理
                        matchPoolPlayer.add(oldest);
                        //TODO 把配对的人提交匹配成功处理
                        matchSuccessProcess(matchPoolPlayer);
                    }else{
                        //本分数段等待时间最长的玩家都匹配不到，其他更不用尝试了
                        continueMatch = false;
                        logger.debug(oldest.getID()+"|匹配到玩家数量不够，取消本次匹配");
                        //归还取出来的玩家
                        for(Competitor player:matchPoolPlayer){
                            HashSet<Competitor> sameRankPlayer = pointMap.get(player.getRank());
                            sameRankPlayer.add(player);
                        }
                    }
                }
            }
        }catch (Throwable t){
            logger.info(t.toString());
            long endTime = System.currentTimeMillis();
            logger.debug("执行匹配结束|结束时间|"+endTime+"|耗时|"+(endTime-startTime)+"ms");
        }
    }
    private static void matchSuccessProcess(List<Competitor> matchPoolPlayer){

        PlayerData playerData = PlayerData.getInstance();
        //生成题目
        Integer user1 = matchPoolPlayer.get(0).getID();
        Integer user2 = matchPoolPlayer.get(1).getID();
        playPool.remove(user1);
        playPool.remove(user2);
        logger.info(user1 + "pair with" + user2);
        //QuestionService questionService = StaticContextAccessor.getBean(QuestionService.class);

        if (questionService != null){
            logger.info("questionservice is not null");
        }else {
            logger.info("questservice is null");
        }
        Random random = new Random();
        Boolean os = random.nextBoolean();
        String strQuest;
        if (os.equals(Boolean.TRUE)) {
            logger.info("first selection");
            strQuest = questionService.Generate(user1);
        } else {
            logger.info("second selection");
            strQuest = questionService.Generate(user2);
        }
        logger.info(strQuest);
        playerData.userQuest.put(user1, strQuest);
        playerData.userQuest.put(user2, strQuest);

        playerData.putPair(user1,user2);
        playerData.putPair(user2,user1);
        logger.info("配对提交成功");
        playerData.isConnect.remove(user1);
        playerData.isConnect.remove(user2);


    }
}