package com.kun.rumbling.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kun.rumbling.dao.ChatMessageDao;
import com.kun.rumbling.domain.ChatMessage;
import com.kun.rumbling.util.JsonUtils;
import com.kun.rumbling.util.MessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {

    /**
     * 当前chatGPT是否正在处理中，为是时将拒掉要chatGPT处理的消息
     */
    private volatile boolean chatGptProcessFlag = false;
    private static final String BATCH_NUM = UUID.randomUUID().toString();


    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private ChatMessageDao chatMessageDao;

    @Autowired
    @Qualifier("proxyRestTemplate")
    private RestTemplate proxyRestTemplate;


    /**
     * 处理消息，以【@chatGPT 】开头的消息 视为调用 chatGPT 处理
     *
     * @param chatMessage 聊天消息
     * @return
     */
    @Transactional
    public void processMessage(final ChatMessage chatMessage) {

        if (Objects.isNull(chatMessage)) {
            return;
        }
        //不管说了啥先存到数据库
        chatMessage.setBatchNum(BATCH_NUM);
        chatMessageDao.insert(chatMessage);
        String message = chatMessage.getMessage();
        //判断消息是否需要chatGPT处理
        if (StringUtils.startsWith(message, ChatMessage.CHAT_GPT_PREFIX)) {
            ((ChatService) AopContext.currentProxy()).processChatGptMessage(chatMessage);
        }
    }

    @Async
    public void processChatGptMessage(ChatMessage chatMessage) {
        synchronized (this) {
            if (this.chatGptProcessFlag) {
                sendMsg(ChatMessage.buildChatGptMsg().pointUser(chatMessage.getUser()));
            }
            this.chatGptProcessFlag = true;
        }
        try {
            ChatMessage gptMessage = ChatMessage.buildChatGptMsg().resetMessage(MessageUtils.callProxy(chatMessageDao.getToken(), BATCH_NUM, chatMessage.getMessage())).pointUser(chatMessage.getUser());
            sendMsg(gptMessage);
            chatMessageDao.insert(gptMessage);
        } finally {
            this.chatGptProcessFlag = false;
        }
    }

    public void sendMsg(ChatMessage message) {
        if (Objects.nonNull(message)) {
            simpMessagingTemplate.convertAndSend("/topic/chat/all", message);
        }
    }


}
