package com.example.demo.model;

import com.example.demo.constants.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private MessageTypeEnum type;
    private String content;
    private Integer sender;

   // private List<Integer> receiver;
}