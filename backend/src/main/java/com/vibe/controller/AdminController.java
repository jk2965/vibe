package com.vibe.controller;

import com.vibe.model.UserVO;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8080")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam String requesterId) {
        if (!userService.isAdmin(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
        }
        List<UserVO> users = userService.getAllUsers();
        // 비밀번호 제거 후 반환
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @RequestParam String requesterId,
            @RequestBody Map<String, Object> body) {
        if (!userService.isAdmin(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
        }
        String position = (String) body.get("position");
        Double remainingVacation = body.get("remainingVacation") != null
                ? Double.parseDouble(body.get("remainingVacation").toString()) : null;
        if (remainingVacation != null) {
            if (remainingVacation < 0 || remainingVacation > 365) {
                return ResponseEntity.badRequest().body(Map.of("message", "잔여 연차는 0~365일 범위여야 합니다."));
            }
            if ((remainingVacation * 2) % 1 != 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "잔여 연차는 0.5일 단위로 입력하세요."));
            }
        }
        userService.updateUserInfo(id, position, remainingVacation);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
