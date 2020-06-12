package com.example.demo.service;

import com.alibaba.fastjson.JSON;
import com.example.demo.constants.MessageTypeEnum;
import com.example.demo.model.ChatMessage;
import com.example.demo.model.MessageReply;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.stereotype.Service;

@Service
public class TimeService {
    public void Overtime(SimpMessageSendingOperations simpMessageSendingOperations, String sessionID){
        MessageReply message = new MessageReply();
        ChatMessage result = new ChatMessage();
        message.setCode(200);
        message.setStatus("超时");
        result.setType(MessageTypeEnum.OVERTIME);
        message.setChatMessage(result);
        String reply = JSON.toJSONString(message);
        simpMessageSendingOperations.convertAndSendToUser(sessionID, "/topic/game",reply,createHeaders(sessionID));
    }
    public void oppoSummary( SimpMessageSendingOperations simpMessageSendingOperations, String sessionID, String content){
        MessageReply message = new MessageReply();
        ChatMessage result = new ChatMessage();
        message.setCode(200);
        message.setStatus("结算");
        result.setType(MessageTypeEnum.FINISH_PAIR);
        result.setContent(content);
        message.setChatMessage(result);
        String reply = JSON.toJSONString(message);
        simpMessageSendingOperations.convertAndSendToUser(sessionID, "/topic/game",reply,createHeaders(sessionID));
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
