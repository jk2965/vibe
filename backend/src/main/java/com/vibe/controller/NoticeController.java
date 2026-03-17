package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.NoticeVO;
import com.vibe.service.NoticeService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notice")
@CrossOrigin(origins = "http://localhost:8080")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @GetMapping
    public PageInfo<NoticeVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return noticeService.getList(pageNum);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        NoticeVO notice = noticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(notice);
    }

    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody NoticeVO notice) {
        if (notice.getTitle() == null || notice.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        if (notice.getContent() == null || notice.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        }
        boolean isAdmin = userService.isAdmin(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);
        if (!isAdmin && !isTeamLeader) {
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 팀장 또는 관리자만 작성할 수 있습니다."));
        }
        return ResponseEntity.ok(noticeService.write(notice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        NoticeVO notice = noticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        if (!notice.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        noticeService.update(id, title, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        NoticeVO notice = noticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!notice.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        noticeService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
