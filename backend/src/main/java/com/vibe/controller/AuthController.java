package com.vibe.controller;

import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증(로그인/회원가입) 관련 REST API 컨트롤러.
 * 기본 경로: /api/auth
 * 연관 파일: UserService.java (사용자 조회/검증), UserMapper.xml (DB 쿼리),
 *           프론트엔드 Login.vue에서 POST /api/auth/login 요청
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080")
public class AuthController {

    // UserService.java를 통해 사용자 정보 조회 및 인증 처리
    @Autowired
    private UserService userService;

    /**
     * 로그인 엔드포인트. 아이디/비밀번호를 검증하고 사용자 정보를 반환한다.
     * 프론트엔드 Login.vue에서 POST /api/auth/login 요청
     * UserService.java의 getUser(), login() 호출
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String password = body.get("password");
        // UserService.java의 getUser()로 사용자 조회
        com.vibe.model.UserVO user = userService.getUser(id);
        // UserService.java의 login()으로 비밀번호 일치 여부 확인
        if (user != null && userService.login(id, password)) {
            // 로그인 성공 시 사용자 기본 정보(권한, 팀, 직책 등) 반환
            return ResponseEntity.ok(Map.of(
                "success", true,
                "id", id,
                "username", user.getUsername(),
                "position", user.getPosition() != null ? user.getPosition() : "",
                "adminLevel", user.getIsAdmin() != null ? user.getIsAdmin() : 0,
                "team", user.getTeam() != null ? user.getTeam() : "",
                "isTeamLeader", user.getIsTeamLeader() != null && user.getIsTeamLeader() == 1
            ));
        }
        // 인증 실패 시 401 반환
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "아이디 또는 비밀번호가 틀렸습니다."));
    }

    /**
     * 회원가입 엔드포인트. 중복 아이디 확인 후 신규 사용자를 등록한다.
     * 프론트엔드 Register.vue에서 POST /api/auth/register 요청
     * UserService.java의 exists(), register() 호출
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String username = body.get("username");
        String password = body.get("password");
        String position = body.get("position");
        // UserService.java의 exists()로 아이디 중복 검사
        if (userService.exists(id)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이미 존재하는 아이디입니다."));
        }
        // UserService.java의 register()로 신규 사용자 DB 저장
        userService.register(id, username, password, position);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
