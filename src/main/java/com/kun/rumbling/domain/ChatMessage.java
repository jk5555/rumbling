package com.kun.rumbling.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author jkun
 */
@Data
public class ChatMessage {


    private Long msgId;

    private String batchNum;

    private String user;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;

    private String message;


    /**
     * 消息拼接
     *
     * @param message 消息
     * @param point   插入点：整数为拼在右边，反之拼在左边
     */
    public ChatMessage connectMessage(String message, int point) {
        this.message = point >= 0 ? this.message + message : message + this.message;
        return this;
    }

    public ChatMessage pointUser(String user) {
        return connectMessage( "@" + user + " ", -1);
    }


    public static ChatMessage buildChatGptMsg(){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser("chatGPT机器人");
        chatMessage.setSendTime(new Date());
        chatMessage.setMessage("当前正在处理前一位用户的请求，请稍后再试！");
        return chatMessage;
    }


}
