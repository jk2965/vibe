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
        boolean isAdmin = userService.isAdmin(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);

        if (!isAdmin && !isTeamLeader) {
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
        }

        List<UserVO> users;
        if (isAdmin) {
            users = userService.getAllUsers();
        } else {
            // 팀장: 자신의 팀 인원만
            UserVO requester = userService.getUser(requesterId);
            if (requester.getTeam() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "소속 팀이 지정되지 않았습니다."));
            }
            users = userService.getUsersByTeam(requester.getTeam());
        }
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @RequestParam String requesterId,
            @RequestBody Map<String, Object> body) {

        boolean isAdmin = userService.isAdmin(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);
        boolean isSuperAdmin = userService.isSuperAdmin(requesterId);

        if (!isAdmin && !isTeamLeader) {
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
        }

        // 마스터 계정 보호: 마스터 계정은 마스터만 수정 가능
        UserVO target = userService.getUser(id);
        if (target != null && Integer.valueOf(2).equals(target.getIsAdmin()) && !isSuperAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "마스터 계정은 수정할 수 없습니다."));
        }

        // 팀장은 자기 팀 인원의 소속(team)만 변경 가능 (단, 팀장 본인의 소속은 변경 불가)
        if (!isAdmin && isTeamLeader) {
            UserVO requester = userService.getUser(requesterId);
            if (requester.getTeam() == null || target == null || !requester.getTeam().equals(target.getTeam())) {
                return ResponseEntity.status(403).body(Map.of("message", "다른 팀 인원의 정보는 변경할 수 없습니다."));
            }
            if (Integer.valueOf(1).equals(target.getIsTeamLeader())) {
                return ResponseEntity.status(403).body(Map.of("message", "팀장의 소속은 마스터 계정만 변경할 수 있습니다."));
            }
            String team = (String) body.get("team");
            userService.updateTeam(id, team);
            return ResponseEntity.ok(Map.of("success", true));
        }

        // 관리자/슈퍼 관리자 처리
        String position = (String) body.get("position");
        String team = body.containsKey("team") ? (String) body.get("team") : null;
        Double remainingVacation = body.get("remainingVacation") != null
                ? Double.parseDouble(body.get("remainingVacation").toString()) : null;
        Integer newAdminLevel = body.get("adminLevel") != null
                ? Integer.parseInt(body.get("adminLevel").toString()) : null;
        Integer isTeamLeaderVal = body.get("isTeamLeader") != null
                ? ((Boolean) body.get("isTeamLeader") ? 1 : 0) : null;

        // 소속 변경은 슈퍼 관리자만 가능 (일반 관리자 불가)
        if (team != null && !isSuperAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "소속 변경은 슈퍼 관리자만 가능합니다."));
        }
        // 팀장 권한 부여는 슈퍼 관리자만 가능
        if (isTeamLeaderVal != null && !isSuperAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "팀장 권한 부여는 슈퍼 관리자만 가능합니다."));
        }
        // 팀장 부여 시 해당 팀에 이미 팀장이 있는지 확인
        if (isTeamLeaderVal != null && isTeamLeaderVal == 1) {
            String targetTeam = target != null ? target.getTeam() : null;
            if (targetTeam == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "소속이 없는 인원에게는 팀장을 부여할 수 없습니다."));
            }
            if (userService.hasTeamLeader(targetTeam)) {
                return ResponseEntity.badRequest().body(Map.of("message", targetTeam + "에 이미 팀장이 있습니다."));
            }
        }
        // 권한 변경은 슈퍼 관리자만 가능
        if (newAdminLevel != null) {
            if (!isSuperAdmin) {
                return ResponseEntity.status(403).body(Map.of("message", "권한 변경은 슈퍼 관리자만 가능합니다."));
            }
            if (id.equals(requesterId)) {
                return ResponseEntity.badRequest().body(Map.of("message", "자신의 권한은 변경할 수 없습니다."));
            }
            if (newAdminLevel < 0 || newAdminLevel > 2) {
                return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 권한 레벨입니다."));
            }
        }
        if (remainingVacation != null) {
            if (remainingVacation < 0 || remainingVacation > 365) {
                return ResponseEntity.badRequest().body(Map.of("message", "잔여 연차는 0~365일 범위여야 합니다."));
            }
            if ((remainingVacation * 2) % 1 != 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "잔여 연차는 0.5일 단위로 입력하세요."));
            }
        }
        userService.updateUserInfo(id, position, remainingVacation, newAdminLevel, team, isTeamLeaderVal);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
