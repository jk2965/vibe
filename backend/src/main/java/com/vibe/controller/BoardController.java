package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.BoardVO;
import com.vibe.service.BoardService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/board")
@CrossOrigin(origins = "http://localhost:8080")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private UserService userService;

    @GetMapping
    public PageInfo<BoardVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return boardService.getList(pageNum);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        BoardVO board = boardService.getDetail(id);
        if (board == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(board);
    }

    @PostMapping
    public ResponseEntity<?> write(@RequestBody BoardVO board) {
        if (board.getTitle() == null || board.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        if (board.getContent() == null || board.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        }
        return ResponseEntity.ok(boardService.write(board));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        BoardVO board = boardService.getDetail(id);
        if (board == null) return ResponseEntity.notFound().build();
        if (!board.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        boardService.update(id, title, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        BoardVO board = boardService.getDetail(id);
        if (board == null) return ResponseEntity.notFound().build();
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!board.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        boardService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
