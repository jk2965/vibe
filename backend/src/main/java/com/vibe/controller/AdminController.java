package com.vibe.controller;

import com.vibe.model.UserVO;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자 기능(사용자 목록 조회, 사용자 정보 수정) REST API 컨트롤러.
 * 기본 경로: /api/admin
 * 연관 파일: UserService.java (권한 확인 및 사용자 정보 변경), UserMapper.xml (DB 쿼리),
 *           프론트엔드 AdminPage.vue에서 GET /api/admin/users, PUT /api/admin/users/{id} 요청
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8080")
public class AdminController {

    // UserService.java를 통해 사용자 권한 확인 및 정보 수정
    @Autowired
    private UserService userService;

    /**
     * 전체 사용자 목록 조회 엔드포인트. 관리자는 전체, 팀장은 자신의 팀원만 조회 가능.
     * 프론트엔드 AdminPage.vue에서 GET /api/admin/users?requesterId={id} 요청
     * UserService.java의 isAdmin(), isTeamLeader(), getAllUsers(), getUsersByTeam() 호출
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam String requesterId) {
        // UserService.java의 isAdmin(), isTeamLeader()로 요청자 권한 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);

        // 관리자도 팀장도 아닌 경우 403 반환
        if (!isAdmin && !isTeamLeader) {
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
        }

        List<UserVO> users;
        if (isAdmin) {
            // UserService.java의 getAllUsers()로 전체 사용자 조회
            users = userService.getAllUsers();
        } else {
            // 팀장: 자신의 팀 인원만
            UserVO requester = userService.getUser(requesterId);
            if (requester.getTeam() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "소속 팀이 지정되지 않았습니다."));
            }
            // UserService.java의 getUsersByTeam()으로 팀 소속 사용자만 조회
            users = userService.getUsersByTeam(requester.getTeam());
        }
        // 비밀번호 필드는 응답에서 제거 (보안)
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 정보 수정 엔드포인트. 권한 레벨에 따라 수정 가능 항목이 다르다.
     * 프론트엔드 AdminPage.vue에서 PUT /api/admin/users/{id}?requesterId={id} 요청
     * UserService.java의 isAdmin(), isSuperAdmin(), updateUserInfo(), updateTeam() 호출
     * 권한 레벨: 0=일반, 1=관리자, 2=마스터(슈퍼 관리자)
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @RequestParam String requesterId,
            @RequestBody Map<String, Object> body) {

        // UserService.java의 권한 확인 메서드 호출
        boolean isAdmin = userService.isAdmin(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);
        boolean isSuperAdmin = userService.isSuperAdmin(requesterId);

        // 관리자도 팀장도 아닌 경우 접근 거부
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
            // UserService.java의 updateTeam()으로 팀 소속만 변경
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
            // UserService.java의 hasTeamLeader()로 팀장 중복 여부 검사
            if (userService.hasTeamLeader(targetTeam)) {
                return ResponseEntity.badRequest().body(Map.of("message", targetTeam + "에 이미 팀장이 있습니다."));
            }
        }
        // 권한 변경은 슈퍼 관리자만 가능
        if (newAdminLevel != null) {
            if (!isSuperAdmin) {
                return ResponseEntity.status(403).body(Map.of("message", "권한 변경은 슈퍼 관리자만 가능합니다."));
            }
            // 자기 자신의 권한은 변경 불가
            if (id.equals(requesterId)) {
                return ResponseEntity.badRequest().body(Map.of("message", "자신의 권한은 변경할 수 없습니다."));
            }
            // 권한 레벨은 0~2 범위만 허용
            if (newAdminLevel < 0 || newAdminLevel > 2) {
                return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 권한 레벨입니다."));
            }
        }
        // 잔여 연차 유효성 검사 (0~365일, 0.5일 단위)
        if (remainingVacation != null) {
            if (remainingVacation < 0 || remainingVacation > 365) {
                return ResponseEntity.badRequest().body(Map.of("message", "잔여 연차는 0~365일 범위여야 합니다."));
            }
            if ((remainingVacation * 2) % 1 != 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "잔여 연차는 0.5일 단위로 입력하세요."));
            }
        }
        // UserService.java의 updateUserInfo()로 직책/잔여연차/권한레벨/팀/팀장여부 일괄 수정
        userService.updateUserInfo(id, position, remainingVacation, newAdminLevel, team, isTeamLeaderVal);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
