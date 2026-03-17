package com.vibe.controller;

import com.vibe.model.TeamNoticeVO;
import com.vibe.model.UserVO;
import com.vibe.service.TeamNoticeService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/team-notice")
@CrossOrigin(origins = "http://localhost:8080")
public class TeamNoticeController {

    @Autowired
    private TeamNoticeService teamNoticeService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getList(@RequestParam String team,
                                     @RequestParam String requesterId,
                                     @RequestParam(defaultValue = "1") int pageNum) {
        int adminLevel = userService.getAdminLevel(requesterId);
        if (adminLevel < 2) {
            UserVO requester = userService.getUser(requesterId);
            if (requester == null || !team.equals(requester.getTeam())) {
                return ResponseEntity.status(403).body(Map.of("message", "해당 팀 공지사항에 접근 권한이 없습니다."));
            }
        }
        return ResponseEntity.ok(teamNoticeService.getListByTeam(team, pageNum));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id, @RequestParam String requesterId) {
        TeamNoticeVO notice = teamNoticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        int adminLevel = userService.getAdminLevel(requesterId);
        if (adminLevel < 2) {
            UserVO requester = userService.getUser(requesterId);
            if (requester == null || !notice.getTeam().equals(requester.getTeam())) {
                return ResponseEntity.status(403).body(Map.of("message", "접근 권한이 없습니다."));
            }
        }
        return ResponseEntity.ok(notice);
    }

    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody TeamNoticeVO notice) {
        if (notice.getTitle() == null || notice.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        if (notice.getContent() == null || notice.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        }
        if (notice.getTeam() == null || notice.getTeam().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "팀을 선택하세요."));
        }
        int adminLevel = userService.getAdminLevel(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);
        UserVO requester = userService.getUser(requesterId);
        boolean isSameTeam = requester != null && notice.getTeam().equals(requester.getTeam());
        if (adminLevel < 2 && !(isSameTeam && (adminLevel >= 1 || isTeamLeader))) {
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 해당 팀 팀장/관리자 또는 마스터만 작성할 수 있습니다."));
        }
        return ResponseEntity.ok(teamNoticeService.write(notice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        TeamNoticeVO notice = teamNoticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        if (!notice.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        teamNoticeService.update(id, title, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        TeamNoticeVO notice = teamNoticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!notice.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        teamNoticeService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
