package com.robot.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author liangliang
 * @description: RestTemplate 配置类
 * @since 2018/11/19 9:49
 */
@Configuration
public class RestTemplateClientConfig {


@Bean
RestTemplate restTemplate() {
SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
requestFactory.setConnectTimeout(10000);
requestFactory.setReadTimeout(10000);
RestTemplate restTemplate = new RestTemplate(requestFactory);
return restTemplate;
}


}
