package com.kun.rumbling.controller;

import com.kun.rumbling.domain.ChatMessage;
import com.kun.rumbling.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author jkun
 */
@Controller
public class ChatController {

    private static final Logger LOOGGER = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat/all")
    @SendTo("/topic/chat/all")
    public ChatMessage chatAll(ChatMessage chatMessage) {
        LOOGGER.info("服务端【/chat/all】接收消息：【用户：{}，消息：{}】", chatMessage.getUser(), chatMessage.getMessage());
        chatMessage.setSendTime(new Date());
        chatService.processMessage(chatMessage);
        return chatMessage;
    }

    @GetMapping("/sendMsgByUser")
    @ResponseBody
    public Object sendMsgByUser(String token, String msg) {
        simpMessagingTemplate.convertAndSendToUser(token, "/msg", msg);
        return "success";
    }

    @GetMapping("/sendMsgByAll")
    @ResponseBody
    public Object sendMsgByAll(String msg) {
        simpMessagingTemplate.convertAndSend("/topic", msg);
        return "success";
    }

    @GetMapping("/chat")
    public String test() {
        return "stomp_chat.html";
    }


}
