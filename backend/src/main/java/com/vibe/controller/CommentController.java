package com.vibe.controller;

import com.vibe.model.CommentVO;
import com.vibe.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "http://localhost:8080")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    public List<CommentVO> getComments(@RequestParam String boardId) {
        return commentService.getByBoardId(boardId);
    }

    @PostMapping
    public ResponseEntity<?> write(@RequestBody CommentVO comment) {
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "댓글 내용을 입력하세요."));
        }
        return ResponseEntity.ok(commentService.write(comment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        CommentVO comment = commentService.findById(id);
        if (comment == null) return ResponseEntity.notFound().build();
        if (!comment.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "댓글 내용을 입력하세요."));
        }
        commentService.update(id, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        CommentVO comment = commentService.findById(id);
        if (comment == null) return ResponseEntity.notFound().build();
        if (!comment.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        commentService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
