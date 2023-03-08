package com.kun.rumbling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @author jkun
 */
@SpringBootApplication
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
public class RumblingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RumblingApplication.class, args);
    }

    @Bean
    public RestTemplate registerRestTemplate() {
        return new RestTemplate();
    }

    // 添加代理
    @Bean("proxyRestTemplate")
    public RestTemplate proxy() {
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("v219.toddns.gq",443)));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        return restTemplate;
    }



}
