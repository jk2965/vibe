package com.vibe.controller;

import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String password = body.get("password");
        String username = userService.getUsername(id);
        if (username != null && userService.login(id, password)) {
            return ResponseEntity.ok(Map.of("success", true, "id", id, "username", username));
        }
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "아이디 또는 비밀번호가 틀렸습니다."));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String username = body.get("username");
        String password = body.get("password");
        if (userService.exists(id)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이미 존재하는 아이디입니다."));
        }
        userService.register(id, username, password);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
