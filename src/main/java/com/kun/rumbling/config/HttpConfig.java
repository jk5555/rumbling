package com.kun.rumbling.config;


import com.kun.rumbling.dao.ChatMessageDao;
import com.kun.rumbling.domain.ChatgptProxyInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;

@Configuration
public class HttpConfig {

    @Autowired
    private ChatMessageDao chatMessageDao;

    @Bean
    public RestTemplate registerRestTemplate() {
        return new RestTemplate();
    }

    // 添加代理
    @Bean("proxyRestTemplate")
    public RestTemplate proxy() {
        ChatgptProxyInfo proxyInfo = chatMessageDao.getProxyInfo();
        if (Objects.isNull(proxyInfo) || StringUtils.isBlank(proxyInfo.getProxyIp()) || Objects.isNull(proxyInfo.getProxyPort())) {
            return new RestTemplate();
        }
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.valueOf(proxyInfo.getProxyType()),new InetSocketAddress(proxyInfo.getProxyIp(), proxyInfo.getProxyPort())));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        return restTemplate;
    }

}
