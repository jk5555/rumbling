package com.kun.rumbling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

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


}
