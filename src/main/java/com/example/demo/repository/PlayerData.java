package com.example.demo.repository;


import com.example.demo.constants.StatusEnum;
import com.example.demo.model.ReportForm;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayerData{
    //记录用户状态的并发HashMap
    private  ConcurrentHashMap<Integer, StatusEnum> userToStatus = new ConcurrentHashMap<>();
    //记录是否匹配超时
    public  ConcurrentHashMap<Integer, Boolean> isConnect = new ConcurrentHashMap<>();
    //暂时存储用户的问题
    public  ConcurrentHashMap<Integer,String> userQuest = new ConcurrentHashMap<>();
    //public  ConcurrentHashMap<Integer,Integer> questSignal = new ConcurrentHashMap<>();
    //记录两个用户的匹配的并发HashMap
    //两边相互记录
    private  ConcurrentHashMap<Integer, Integer> userToPlay = new ConcurrentHashMap<>();

    public ConcurrentHashMap<Integer,Integer> userOvertime = new ConcurrentHashMap<>();
    private  ConcurrentHashMap<Integer, Integer> userToLeave = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer,String> UserToSessionID = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ReportForm> reportCard = new ConcurrentHashMap<>();
    //private final  ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    // private final  Lock readLock = readWriteLock.readLock();
    //private final  Lock writeLock = readWriteLock.writeLock();

    public void putReport(Integer key, ReportForm value){ reportCard.put(key, value);}
    public void deleteReport(Integer key){reportCard.remove(key);}
    public boolean reportContainsKey(Integer key){return reportCard.containsKey(key);}
    public ReportForm getReport(Integer key){return reportCard.get(key);}

    public void putUserSessionID(Integer key, String value){ UserToSessionID.put(key,value);}
    public String getUserSessionID(Integer key){return UserToSessionID.get(key);}

    public void putUserToLeave(Integer key, Integer value){ userToLeave.put(key, value);}
    public void deleteUserToLeave(Integer key){userToLeave.remove(key);}
    public boolean LeaveContainsKey(Integer key){return userToLeave.containsKey(key);}
    //删除某个人的匹配
    public void deletePair(Integer key){
        userToPlay.remove(key);
    }
    public void setStatus(Integer key, StatusEnum value){
        userToStatus.replace(key,value);
    }
    public void putStatus(Integer key, StatusEnum value){
        userToStatus.put(key, value);
    }
    public void deleteStatus(Integer key){
        userToStatus.remove(key);
    }
    public void putPair(Integer key, Integer value){
        userToPlay.put(key,value);
    }
    public Integer getPair(Integer key){
        return userToPlay.get(key);
    }
    public StatusEnum getStatus(Integer key){
        return userToStatus.get(key);
    }
    public boolean StatusContainsKey(Integer key){
        return userToStatus.containsKey(key);
    }
    public boolean PairContainsKey(Integer key){return userToPlay.containsKey(key);}
    public void printStatus(){
        System.out.println(userToStatus);
    }
    private static PlayerData data = new PlayerData();

    public static PlayerData getInstance(){
        return data;
    }




}