package com.kun.rumbling.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kun.rumbling.domain.ChatMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author kun.jiang@going-link.com 2023/3/8 16:24
 */
public class MessageUtils {



    public static String callOpenAI(RestTemplate template, String token, String message) {
        try {
            String requestMessage = StringUtils.substringAfter(message, ChatMessage.CHAT_GPT_PREFIX);
            String param = """
                    {
                         "model": "gpt-3.5-turbo",
                         "messages": [
                             {
                                 "role": "system",
                                 "content": "You are a helpful assistant."
                             },
                             {
                                 "role": "user",
                                 "content": "%s"
                             }
                         ]
                     }
                    """;
            Map<String, Object> requestParam = JsonUtils.readValue(String.format(param, requestMessage), new TypeReference<>() {
            });
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", token);
            httpHeaders.set("Accept-Encoding", "gzip, deflate, br");
            httpHeaders.set("Accept", "*/*");
            httpHeaders.set("User-Agent", "PostmanRuntime/7.26.8");
            HttpEntity<Map<String, Object>> mapHttpEntity = new HttpEntity<>(requestParam, httpHeaders);
            ResponseEntity<Map> response = template.postForEntity("https://api.openai.com/v1/chat/completions", mapHttpEntity, Map.class);
            Map body = response.getBody();
            List choices = (List) body.get("choices");
            Map messageMap = (Map) ((Map) choices.get(0)).get("message");
            String resultMessage = Objects.toString(messageMap.get("content"));
            return resultMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return ChatMessage.buildChatGptMsg().resetMessage(e.getMessage()).connectMessage("出了点小差错...", -1).getMessage();
        }
    }


    public static String callProxy(RestTemplate template, String token, String sessionId, String message) {
        try {
            String requestMessage = StringUtils.substringAfter(message, ChatMessage.CHAT_GPT_PREFIX);
            String param = """
                    {
                          "apiKey": "%s",
                          "sessionId": "%s",
                          "content": "%s"
                      }
                    """;
            Map<String, Object> requestParam = JsonUtils.readValue(String.format(param, token, sessionId, requestMessage), new TypeReference<>() {
            });
            ResponseEntity<Map> response = new RestTemplate().postForEntity("https://", requestParam, Map.class);
            Map body = response.getBody();
            //todo....api还没确定
            String resultMessage = Objects.toString(body.get("text"));
            return resultMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return ChatMessage.buildChatGptMsg().resetMessage(e.getMessage()).connectMessage("出了点小差错...", -1).getMessage();
        }
    }




}
