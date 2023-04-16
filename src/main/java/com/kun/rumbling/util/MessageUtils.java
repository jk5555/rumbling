package com.kun.rumbling.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kun.rumbling.domain.ChatMessage;
import com.kun.rumbling.domain.ChatgptProxyInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author kun.jiang@going-link.com 2023/3/8 16:24
 */
public class MessageUtils {



    public static String callOpenAI(RestTemplate template, String token, String message) {
        try {
            if (StringUtils.isBlank(token)) {
                throw new Exception("openAI token is null");
            }
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
            httpHeaders.set("Authorization", "Bearer " + token);
            //请求头告诉服务端数据可以经过gzip压缩，但是restTemplate默认不支持gzip，导致乱码，所以注释掉
            //httpHeaders.set("Accept-Encoding", "gzip, deflate, br");
            httpHeaders.set("Accept", "application/json;charset=UTF-8");
            httpHeaders.set("Content-Type", "application/json;charset=UTF-8");
            httpHeaders.set("User-Agent", "PostmanRuntime/7.26.8");
            HttpEntity<Map<String, Object>> mapHttpEntity = new HttpEntity<>(requestParam, httpHeaders);
            ResponseEntity<Map> response = template.postForEntity("https://api.openai.com/v1/chat/completions", mapHttpEntity, Map.class);
            List choices = (List) response.getBody().get("choices");
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


    public static String callOpenAI(ChatgptProxyInfo proxyInfo, String token, String message) {
        if (Objects.isNull(proxyInfo) || StringUtils.isBlank(proxyInfo.getProxyIp()) || Objects.isNull(proxyInfo.getProxyPort())) {
            return Strings.EMPTY;
        }
        System.setProperty("proxyType", "4");
        System.setProperty("proxyPort", Integer.toString(proxyInfo.getProxyPort()));
        System.setProperty("proxyHost", proxyInfo.getProxyIp());
        System.setProperty("proxySet", "true");

        String url = "https://api.openai.com/v1/chat/completions";

        try {
            SSLContext sc = SSLContext.getInstance("SSL");

            // 指定信任https
            sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
            // 运行错误，好像只能用https
            // URL console = new URL(url);
            // 可用用http
            URL console = new URL(null, url, new sun.net.www.protocol.https.Handler());
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //之前没有加下面这句设置编码方法也接口可以用。后面不行，前辈建议加上的。说是有些接口存在兼容性的问题
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.connect();
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(message.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            System.out.println("返回结果：" + conn.getResponseMessage());

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String curLine = "";
            String result="";
            while ((curLine = reader.readLine()) != null) {
                result += curLine;
                //System.out.println(curLine);
            }

            is.close();
            Map body = JsonUtils.readValue(result, Map.class);
            List choices = (List) body.get("choices");
            Map messageMap = (Map) ((Map) choices.get(0)).get("message");
            String resultMessage = Objects.toString(messageMap.get("content"));
            return resultMessage;

        } catch (Exception e) {
            e.printStackTrace();
            return ChatMessage.buildChatGptMsg().resetMessage(e.getMessage()).connectMessage("出了点小差错...", -1).getMessage();
        }
    }

    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }





}
