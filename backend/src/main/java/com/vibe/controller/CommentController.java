package com.vibe.controller;

import com.vibe.model.CommentVO;
import com.vibe.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 자유게시판 댓글 CRUD REST API 컨트롤러.
 * 기본 경로: /api/comment
 * 연관 파일: CommentService.java (댓글 비즈니스 로직), CommentMapper.xml (DB 쿼리),
 *           프론트엔드 PostDetailCard.vue에서 GET/POST/PUT/DELETE /api/comment 요청
 */
@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "http://localhost:8080")
public class CommentController {

    // CommentService.java를 통해 댓글 CRUD 처리
    @Autowired
    private CommentService commentService;

    /**
     * 특정 게시글의 댓글 목록 조회 엔드포인트.
     * 프론트엔드 PostDetailCard.vue에서 GET /api/comment?boardId={id} 요청
     * CommentService.java의 getByBoardId() 호출
     */
    @GetMapping
    public List<CommentVO> getComments(@RequestParam String boardId) {
        // CommentService.java의 getByBoardId()로 해당 게시글의 댓글 목록 반환
        return commentService.getByBoardId(boardId);
    }

    /**
     * 댓글 작성 엔드포인트. 내용 빈 값 검사 후 저장.
     * 프론트엔드 PostDetailCard.vue에서 POST /api/comment 요청
     * CommentService.java의 write() 호출
     */
    @PostMapping
    public ResponseEntity<?> write(@RequestBody CommentVO comment) {
        // 댓글 내용 빈 값 검사
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "댓글 내용을 입력하세요."));
        }
        // CommentService.java의 write()로 댓글 DB 저장
        return ResponseEntity.ok(commentService.write(comment));
    }

    /**
     * 댓글 수정 엔드포인트. 작성자 본인만 수정 가능.
     * 프론트엔드 PostDetailCard.vue에서 PUT /api/comment/{id}?requesterId={id} 요청
     * CommentService.java의 findById(), update() 호출
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        // CommentService.java의 findById()로 댓글 존재 여부 확인
        CommentVO comment = commentService.findById(id);
        if (comment == null) return ResponseEntity.notFound().build();
        // 작성자 본인 여부 검사
        if (!comment.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "댓글 내용을 입력하세요."));
        }
        // CommentService.java의 update()로 댓글 내용 업데이트
        commentService.update(id, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 댓글 삭제 엔드포인트. 작성자 본인만 삭제 가능.
     * 프론트엔드 PostDetailCard.vue에서 DELETE /api/comment/{id}?requesterId={id} 요청
     * CommentService.java의 findById(), delete() 호출
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        // CommentService.java의 findById()로 댓글 존재 여부 확인
        CommentVO comment = commentService.findById(id);
        if (comment == null) return ResponseEntity.notFound().build();
        // 작성자 본인 여부 검사 (댓글은 관리자 삭제 권한 미부여, 본인만 삭제 가능)
        if (!comment.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        // CommentService.java의 delete()로 댓글 삭제
        commentService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
