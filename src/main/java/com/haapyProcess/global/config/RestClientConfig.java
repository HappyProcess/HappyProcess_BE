package com.haapyProcess.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * 외부 API(기상청, Supabase 등) 호출용 공용 RestClient.
     * 연결/읽기 타임아웃을 반드시 지정한다 — 타임아웃이 없으면 외부 응답이 늦을 때
     * @Transactional 안의 호출이 DB 커넥션을 무한 점유하여 커넥션풀이 고갈된다.
     */
    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 연결 3초
        factory.setReadTimeout(10000);   // 읽기 10초 (이미지 업로드 여유 포함)

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
