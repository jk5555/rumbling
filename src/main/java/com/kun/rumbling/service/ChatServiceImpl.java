package com.kun.rumbling.service;

import com.kun.rumbling.dao.ChatMessageDao;
import com.kun.rumbling.domain.ChatMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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
    private RestTemplate restTemplate;


    /**
     * 处理消息，以【@chatGPT 】开头的消息 视为调用 chatGPT 处理
     *
     * @param chatMessage 聊天消息
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void processMessage(final ChatMessage chatMessage) {
        if (Objects.isNull(chatMessage)) {
            return;
        }
        //不管说了啥先存到数据库
        chatMessage.setBatchNum(BATCH_NUM);
        chatMessageDao.insert(chatMessage);
        String message = chatMessage.getMessage();
        //判断消息是否需要chatGPT处理
        if (StringUtils.startsWith(message, "@chatGPT ")) {
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
            String message = StringUtils.substringAfter(chatMessage.getMessage(), "@chatGPT ");
            HashMap<Object, Object> requestMap = new HashMap<>();
            requestMap.put( "model","text-davinci-003");
            requestMap.put("prompt", message);
            requestMap.put( "temperature",0.9);
            requestMap.put("max_tokens", 525);
            requestMap.put("top_p",1);
            requestMap.put("frequency_penalty",0);
            requestMap.put("presence_penalty",0.6);
            requestMap.put("stop", Arrays.asList(" Human:", " AI:"));
            MultiValueMap<String, String> requestHeaders = new HttpHeaders();
            requestHeaders.set("Authorization", "Bearer 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");
            HttpEntity requestEntity = new HttpEntity(requestMap, requestHeaders);
            Map<String, Object> map = restTemplate.postForObject("https://api.openai.com/v1/completions", requestEntity, Map.class);
            Object choices = map.get("choices");
            List list = (List) choices;
            String text = (String) ((Map) list.get(0)).get("text");
            ChatMessage res = ChatMessage.buildChatGptMsg();
            res.setMessage(text);
            sendMsg(res.pointUser(chatMessage.getUser()));
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
