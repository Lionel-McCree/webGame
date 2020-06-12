package com.example.demo.controller;
import com.alibaba.fastjson.JSON;
import com.example.demo.constants.MessageTypeEnum;
import com.example.demo.constants.StatusEnum;
//import com.example.demo.service.QuestionService;
import com.example.demo.model.ChatMessage;

import com.example.demo.model.ReportForm;
import com.example.demo.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;

import org.springframework.stereotype.Controller;
import com.example.demo.model.MessageReply;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;


import com.example.demo.repository.PlayerData;
@Log4j
@Controller
public class ChatController{
    private MatchPlayer matchPlayer = new MatchPlayer();
    @Autowired
    public jdbcTemp jdbctemp;
    @Autowired
    private QueryService queryService;
    @Autowired
    SimpMessageSendingOperations messageTemplate;

    @Autowired
    TimeService timeService;

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private PlayerData playData = PlayerData.getInstance();
    @MessageMapping("/game.add_user")
    @SendToUser("/topic/game")
    public MessageReply addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor)throws JsonProcessingException {
        MessageReply message = new MessageReply();
        Integer sender = chatMessage.getSender();

        ChatMessage result = new ChatMessage();
        result.setType(MessageTypeEnum.SEARCH);
        //result.setReceiver(Collections.singletonList(sender));
        if(playData.StatusContainsKey(sender)){
            message.setCode(201);
            message.setStatus("该用户已登录");

        } else {
            //result.setContent(mapper.writeValueAsString(userToStatus.keySet().stream().filter(k -> userToStatus.get(k).equals(StatusEnum.SEARCH)).toArray()));
            message.setCode(200);
            message.setStatus("准备匹配，待应答");
            headerAccessor.getSessionAttributes().put("userID", sender);
            //将sessionid 放入到数据表中
            String id =headerAccessor.getSessionId();
            playData.putUserSessionID(sender,id);
        }
        message.setChatMessage(result);
        return message;
    }



    //点对点需要在主题前加上“/user”
    @MessageMapping("/game.search")
    @SendToUser("/topic/game")
    public MessageReply searchResult(@Payload ChatMessage chatMessage) {
        MessageReply message = new MessageReply();
        ChatMessage result = new ChatMessage();
        Integer sender = chatMessage.getSender();
        //添加状态，设置用户正在搜索中
        playData.putStatus(sender,StatusEnum.SEARCH);
        //设置匹配是否超时，超时取消匹配
        playData.isConnect.put(sender,Boolean.TRUE);

        Integer Rank = queryService.getRank(sender);
        result.setType(MessageTypeEnum.MATCHING);
        MatchPlayer.putCompetitorIntoPlayPool(sender,Rank);
        message.setCode(200);
        message.setStatus("成功添加用户到线程池，等待回应");
        message.setChatMessage(result);

        //将用户添加到匹配池中
        return message;
    }

    @MessageMapping("/game.Matching")
    @SendToUser("/topic/game")
    public MessageReply Match(@Payload ChatMessage chatMessage){
        MessageReply message = new MessageReply();
        ChatMessage result = new ChatMessage();
        Integer sender = chatMessage.getSender();
        //如果在配对列表中出现了该用户的ID,表明配对成功，然后设置回信消息
        if (playData.PairContainsKey(sender) ){

                message.setCode(200);
                message.setStatus("匹配成功，等待回应");
                result.setType(MessageTypeEnum.MATCH);         //MATCH 提示匹配成功
                //设置这个到新的信息响应中
                // playData.setStatus(sender,StatusEnum.IN_GAME);

        }else {
            //判断用户是否还在匹配中，若用户因超时断开匹配，设置回复码204
            if (!playData.isConnect.containsKey(sender)){
                message.setCode(204);
            }else {                                            //这里是循环与客户端交互，确认是否匹配完成，MATCHING提示匹配中
                message.setCode(200);
                message.setStatus("尚未匹配成功，请等待");
                result.setType(MessageTypeEnum.MATCHING);
            }
        }
        message.setChatMessage(result);
        return message;
    }
    @MessageMapping("/game.quest")
    @SendToUser("/topic/game")
    public MessageReply quest(@Payload ChatMessage chatMessage){
        MessageReply message = new MessageReply();
        ChatMessage result = new ChatMessage();
        message.setCode(200);
        Integer sender = chatMessage.getSender();
        String strQuest = playData.userQuest.get(sender);
        result.setContent(strQuest);
        result.setType(MessageTypeEnum.GQUEST);
        message.setChatMessage(result);
        return message;
    }

    @MessageMapping("/game.InGame")
    @SendToUser("/topic/game")
    public MessageReply inExam(@Payload ChatMessage chatMessage){
        MessageReply message = new MessageReply();
        ChatMessage result = new ChatMessage();
        Integer  sender = chatMessage.getSender();
        message.setCode(200);
        message.setStatus("开始");
        playData.setStatus(sender,StatusEnum.IN_GAME);               //收到用户确认，设置用户状态为游戏中
        result.setType(MessageTypeEnum.DO_EXAM);
        message.setChatMessage(result);
        //删除临时保存的问题信息
        playData.userQuest.remove(sender);
        //TODO 开启另外一个线程，开始计时，超过三分钟便退出游戏。
        //TODO 需要再将其封装一下
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(180000);
                    if (playData.StatusContainsKey(sender) && playData.getStatus(sender) == StatusEnum.IN_GAME) {
                        String sessionID = playData.getUserSessionID(sender);
                        timeService.Overtime(messageTemplate,sessionID);
                        playData.userOvertime.put(sender,0);
                        //删除自身状态
                        Integer opponent = playData.getPair(sender);
                        playData.deletePair(sender);
                        playData.deleteStatus(sender);
                        if (playData.StatusContainsKey(opponent) && playData.getStatus(opponent).equals(StatusEnum.FINISH)){
                            Integer oppoScore = playData.getReport(opponent).getScore();
                            queryService.summaryResult(sender,0,opponent,oppoScore);
                            ReportForm reportForm = new ReportForm(opponent, 0, 0);
                            String content = JSON.toJSONString(reportForm);
                            String sessionid = playData.getUserSessionID(opponent);
                            timeService.oppoSummary(messageTemplate,sessionid,content);
                            playData.deleteStatus(opponent);
                            playData.deleteReport(opponent);
                            playData.deletePair(opponent);
                            playData.userOvertime.remove(sender);
                        }
                        if (playData.userOvertime.containsKey(opponent)) {
                            playData.userOvertime.remove(sender);
                            playData.userOvertime.remove(opponent);
                            //设置玩家结果，未完成，0分
                        }
                    }
                }catch (InterruptedException ee)     {
                    System.out.println(ee);
                }
            }
        }).start();
        //
        return message;
    }

    @MessageMapping("/game.submit")
    @SendToUser("/topic/game")
    public MessageReply completeExam(@Payload ReportForm reportForm) throws JsonProcessingException{
        MessageReply message = new MessageReply();
        Integer sender = reportForm.getID();
        //TODO 此处还需要解析客户端结果字符串
        playData.setStatus(sender,StatusEnum.FINISH);
        playData.putReport(sender, reportForm);
        logger.info(sender + "结算成功" + reportForm);
        ChatMessage result = new ChatMessage();
        result.setSender(sender);
        result.setType(MessageTypeEnum.WAIT);
        message.setCode(200);
        message.setStatus("完成，等待结算");
        message.setChatMessage(result);
        return message;
    }
    @MessageMapping("/game.finish")
    @SendToUser("/topic/game")
    public MessageReply report(@Payload ChatMessage chatMessage){
        MessageReply message = new MessageReply();
        Integer sender = chatMessage.getSender();
        Integer opponent = playData.getPair(sender);
        ChatMessage result = new ChatMessage();
        //------------
        Integer score = playData.getReport(sender).getScore();
        try {
            if (playData.StatusContainsKey(opponent) && (playData.getStatus(opponent).equals( StatusEnum.FINISH))) {
                //result.setReceiver(Collections.singletonList(sender));
                //此种情况发生时，对这局游戏进行结算
                String oppoContent = JSON.toJSONString(playData.getReport(sender));
                String oppoSessionid = playData.getUserSessionID(opponent);
                timeService.oppoSummary(messageTemplate,oppoSessionid,oppoContent);

                result.setType(MessageTypeEnum.FINISH_PAIR);
                message.setCode(200);
                String content = JSON.toJSONString(playData.getReport(opponent));
                result.setContent(content);                     //设置对方分数
                message.setStatus("成功，结算完成");
                message.setChatMessage(result);

                //TODO
                //删除对方用户离开状态，删除状态，删除匹配
                playData.deleteStatus(opponent);
                playData.deleteStatus(sender);
                playData.deletePair(opponent);
                playData.deletePair(sender);
                //TODO 从记分表中查询分数并结算
                Integer opponentScore = playData.getReport(opponent).getScore();
                queryService.summaryResult(sender, score, opponent, opponentScore);
                //删除分数表
                playData.deleteReport(opponent);
                playData.deleteReport(sender);


            } else if (playData.StatusContainsKey(opponent) && playData.getStatus(opponent).equals(StatusEnum.IN_GAME)) {
                result.setSender(sender);
                result.setType(MessageTypeEnum.FINISH_NOPAIR);
                message.setCode(200);
                message.setStatus("完成，等待结算");
                message.setChatMessage(result);
            } else if (playData.LeaveContainsKey(opponent) || playData.userOvertime.containsKey(opponent)) {
                queryService.summaryResult(sender, score, opponent, 0);

                playData.deleteUserToLeave(opponent);
                playData.userOvertime.remove(opponent);
                //删除自身状态
                playData.deletePair(sender);
                playData.deleteStatus(sender);
                playData.deleteReport(sender);

                ReportForm reportForm = new ReportForm(opponent, 0, 0);
                String content = JSON.toJSONString(reportForm);
                result.setType(MessageTypeEnum.FINISH_PAIR);
                message.setCode(200);
                message.setStatus("");
                result.setContent(content);
                message.setChatMessage(result);
            }
        }catch (Throwable a){
            logger.info(sender + "开始发生结算时错误");
            playData.printStatus();
        }
        return message;
    }
}
