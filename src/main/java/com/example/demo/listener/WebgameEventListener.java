package com.example.demo.listener;
import com.alibaba.fastjson.JSON;
import com.example.demo.constants.StatusEnum;
import com.example.demo.model.ReportForm;
import com.example.demo.repository.PlayerData;
import com.example.demo.service.MatchPlayer;
import com.example.demo.service.QueryService;
import com.example.demo.service.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class WebgameEventListener{
    private static final Logger logger = LoggerFactory.getLogger(WebgameEventListener.class);
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    @Autowired
    private QueryService queryService;
    @Autowired
    private TimeService timeService;
    @EventListener
    public void handleWebGameConnectListener(SessionConnectedEvent event){
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionID = sha.getSessionId();
        logger.info("someone enter game mode, sessionId :" + sessionID);

    }

    @EventListener
    public void handleWebGameDisConnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        try {
            Integer userID = (Integer) headerAccessor.getSessionAttributes().get("userID");
            if(userID != null) {
                PlayerData playerData = PlayerData.getInstance();
                logger.info("User Disconnected : " + userID);
                //TODO 删除状态
                if (playerData.PairContainsKey(userID)&&playerData.getStatus(userID)== StatusEnum.IN_GAME){
                    Integer opponent = playerData.getPair(userID);
                    playerData.deleteStatus(userID);
                    playerData.deletePair(userID);
                    playerData.putUserToLeave(userID,0);
                    if(playerData.StatusContainsKey(opponent)&& playerData.getStatus(opponent).equals(StatusEnum.FINISH)){
                        Integer oppoScore = playerData.getReport(opponent).getScore();
                        queryService.summaryResult(userID,0,opponent,oppoScore);
                        ReportForm reportForm = new ReportForm(opponent, 0, 0);
                        String content = JSON.toJSONString(reportForm);
                        String sessionid = playerData.getUserSessionID(opponent);
                        timeService.oppoSummary(messagingTemplate,sessionid,content);
                        playerData.deleteStatus(opponent);
                        playerData.deleteReport(opponent);
                        playerData.deletePair(opponent);
                        playerData.deleteUserToLeave(userID);
                    }
                }else if (playerData.getStatus(userID)== StatusEnum.SEARCH){
                    MatchPlayer.removePlayerFromMatchPool(userID);
                    playerData.deleteStatus(userID);
                }else if (playerData.PairContainsKey(userID)&&playerData.getStatus(userID)== StatusEnum.FINISH){
                    logger.info(userID + "leave game after finish the match" );
                }
            }
        }catch (NullPointerException ne) {
            logger.info("error happened when try to get session attribute userID",ne);
            }
        }
    @EventListener
    public void hanleWebGameSubscribeListener(SessionSubscribeEvent event){
           logger.info("somebody subscribe the channel");
        }
}