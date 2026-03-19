package com.vibe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Vibe 사내 그룹웨어 백엔드 애플리케이션의 진입점 클래스.
 * Spring Boot 자동 설정, 컴포넌트 스캔, 빈 등록을 일괄 활성화한다.
 * 연관 파일: application.properties (DB/포트 설정), 모든 Controller/Service/Mapper 빈이 이 클래스에서 출발한다.
 */
@SpringBootApplication
public class VibeApplication {

    /**
     * 애플리케이션 시작 메서드. Spring Boot 내장 Tomcat 서버를 구동한다 (기본 포트 8090).
     * 프론트엔드(Vue, http://localhost:8080)가 이 서버에 API 요청을 보낸다.
     */
    public static void main(String[] args) {
        SpringApplication.run(VibeApplication.class, args);
    }

}