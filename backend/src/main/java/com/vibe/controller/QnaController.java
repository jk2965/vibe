package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.QnaAnswerVO;
import com.vibe.model.QnaVO;
import com.vibe.service.QnaService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Q&A 게시판 REST API 컨트롤러.
 * 기본 경로: /api/qna
 * 질문: 누구나 작성/수정/삭제 가능 (본인 또는 관리자)
 * 답변: 관리자(adminLevel >= 1) 또는 팀장(isTeamLeader)만 작성 가능
 * 연관 파일: QnaService.java, QnaMapper.xml, QnaAnswerMapper.xml
 * 프론트엔드: Qna.vue, QnaWrite.vue, QnaDetail.vue
 */
@RestController
@RequestMapping("/api/qna")
@CrossOrigin(origins = "http://localhost:8080")
public class QnaController {

    /** Q&A 비즈니스 로직 서비스 */
    @Autowired
    private QnaService qnaService;

    /** 사용자 권한 확인 (관리자/팀장 여부) */
    @Autowired
    private UserService userService;

    /**
     * 질문 목록 페이징 조회.
     * 프론트엔드 Qna.vue에서 GET /api/qna?pageNum={n} 요청
     */
    @GetMapping
    public PageInfo<QnaVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return qnaService.getList(pageNum);
    }

    /**
     * 질문 상세 조회 (본문 + 답변 목록 포함).
     * 프론트엔드 QnaDetail.vue에서 GET /api/qna/{id} 요청
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        QnaVO qna = qnaService.getDetail(id);
        if (qna == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(qna);
    }

    /**
     * 질문 작성. 로그인한 모든 사용자가 작성 가능.
     * 프론트엔드 QnaWrite.vue에서 POST /api/qna 요청
     */
    @PostMapping
    public ResponseEntity<?> write(@RequestBody QnaVO qna) {
        if (qna.getTitle() == null || qna.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (qna.getContent() == null || qna.getContent().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        return ResponseEntity.ok(qnaService.write(qna));
    }

    /**
     * 질문 수정. 작성자 본인만 수정 가능.
     * 프론트엔드 QnaDetail.vue에서 PUT /api/qna/{id}?requesterId={id} 요청
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        QnaVO qna = qnaService.findById(id);
        if (qna == null) return ResponseEntity.notFound().build();
        // 작성자 본인 여부 확인
        if (!qna.getAuthorId().equals(requesterId))
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        qnaService.update(id, title, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 질문 삭제. 작성자 본인 또는 관리자만 삭제 가능.
     * 프론트엔드 QnaDetail.vue에서 DELETE /api/qna/{id}?requesterId={id} 요청
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        QnaVO qna = qnaService.findById(id);
        if (qna == null) return ResponseEntity.notFound().build();
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!qna.getAuthorId().equals(requesterId) && !isAdmin)
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        qnaService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 답변 작성. 관리자(adminLevel >= 1) 또는 팀장만 작성 가능.
     * 프론트엔드 QnaDetail.vue에서 POST /api/qna/{id}/answers 요청
     */
    @PostMapping("/{id}/answers")
    public ResponseEntity<?> writeAnswer(@PathVariable String id,
                                         @RequestBody QnaAnswerVO answer) {
        // 답변 권한: 관리자 또는 팀장 여부 확인
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
     * 답변 수정. 답변 작성자 본인만 수정 가능.
     * 프론트엔드 QnaDetail.vue에서 PUT /api/qna/{id}/answers/{answerId}?requesterId={id} 요청
     */
    @PutMapping("/{id}/answers/{answerId}")
    public ResponseEntity<?> updateAnswer(@PathVariable String id,
                                          @PathVariable String answerId,
                                          @RequestParam String requesterId,
                                          @RequestBody Map<String, String> body) {
        QnaAnswerVO answer = qnaService.findAnswerById(answerId);
        if (answer == null) return ResponseEntity.notFound().build();
        // 답변 작성자 본인 여부 확인
        if (!answer.getAuthorId().equals(requesterId))
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        String content = body.get("content");
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        qnaService.updateAnswer(answerId, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 답변 삭제. 답변 작성자 본인 또는 관리자만 삭제 가능.
     * 프론트엔드 QnaDetail.vue에서 DELETE /api/qna/{id}/answers/{answerId}?requesterId={id} 요청
     */
    @DeleteMapping("/{id}/answers/{answerId}")
    public ResponseEntity<?> deleteAnswer(@PathVariable String id,
                                          @PathVariable String answerId,
                                          @RequestParam String requesterId) {
        QnaAnswerVO answer = qnaService.findAnswerById(answerId);
        if (answer == null) return ResponseEntity.notFound().build();
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!answer.getAuthorId().equals(requesterId) && !isAdmin)
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        qnaService.deleteAnswer(answerId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
