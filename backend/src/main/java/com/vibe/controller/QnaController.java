package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.QnaAnswerVO;
import com.vibe.model.QnaVO;
import com.vibe.service.AbstractBoardService;
import com.vibe.service.QnaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Q&A 게시판 REST API 컨트롤러.
 * AbstractBoardController로부터 getDetail, update, delete를 상속.
 * 질문: 누구나 작성 가능. 수정/삭제는 작성자 본인 또는 관리자(canDelete 기본 구현).
 * 답변: 관리자(adminLevel >= 1) 또는 팀장만 작성 가능.
 */
@RestController
@RequestMapping("/api/qna")
@CrossOrigin(origins = "http://localhost:8080")
public class QnaController extends AbstractBoardController<QnaVO> {

    @Autowired private QnaService qnaService;

    /** AbstractBoardController가 사용할 서비스 반환 (Template Method 패턴 훅) */
    @Override
    protected AbstractBoardService<QnaVO> getService() { return qnaService; }

    /**
     * 작성 권한 훅 (Template Method 패턴).
     * Q&A는 누구나 질문 작성 가능.
     */
    @Override
    protected boolean canWrite(String requesterId, QnaVO item) { return true; }

    /** 질문 목록 페이징 조회 */
    @GetMapping
    public PageInfo<QnaVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return qnaService.getList(pageNum);
    }

    /** 질문 작성 (로그인한 모든 사용자 가능, requesterId 불필요) */
    @PostMapping
    public ResponseEntity<?> write(@RequestBody QnaVO qna) {
        if (qna.getTitle() == null || qna.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (qna.getContent() == null || qna.getContent().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        return ResponseEntity.ok(qnaService.write(qna));
    }

    /**
     * 답변 작성 — 관리자(adminLevel >= 1) 또는 팀장만 작성 가능.
     */
    @PostMapping("/{id}/answers")
    public ResponseEntity<?> writeAnswer(@PathVariable String id,
                                         @RequestBody QnaAnswerVO answer) {
        boolean canAnswer = userService.isAdmin(answer.getAuthorId())
                || userService.isTeamLeader(answer.getAuthorId());
        if (!canAnswer)
            return ResponseEntity.status(403).body(Map.of("message", "답변 권한이 없습니다. 관리자 또는 팀장만 답변할 수 있습니다."));
        if (answer.getContent() == null || answer.getContent().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "답변 내용을 입력하세요."));
        answer.setQnaId(id);
        return ResponseEntity.ok(qnaService.writeAnswer(answer));
    }

    /**
     * 답변 수정 — 답변 작성자 본인만 수정 가능.
     */
    @PutMapping("/{id}/answers/{answerId}")
    public ResponseEntity<?> updateAnswer(@PathVariable String id,
                                          @PathVariable String answerId,
                                          @RequestParam String requesterId,
                                          @RequestBody Map<String, String> body) {
        QnaAnswerVO answer = qnaService.findAnswerById(answerId);
        if (answer == null) return ResponseEntity.notFound().build();
        if (!answer.getAuthorId().equals(requesterId))
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        String content = body.get("content");
        if (content == null || content.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        qnaService.updateAnswer(answerId, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 답변 삭제 — 답변 작성자 본인 또는 관리자만 삭제 가능.
     */
    @DeleteMapping("/{id}/answers/{answerId}")
    public ResponseEntity<?> deleteAnswer(@PathVariable String id,
                                          @PathVariable String answerId,
                                          @RequestParam String requesterId) {
        QnaAnswerVO answer = qnaService.findAnswerById(answerId);
        if (answer == null) return ResponseEntity.notFound().build();
        if (!answer.getAuthorId().equals(requesterId) && !userService.isAdmin(requesterId))
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        qnaService.deleteAnswer(answerId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
