package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.FaqVO;
import com.vibe.service.FaqService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * FAQ 게시판 CRUD REST API 컨트롤러.
 * 기본 경로: /api/faq
 * 연관 파일: FaqService.java (FAQ 비즈니스 로직), UserService.java (권한 확인),
 *           FaqMapper.xml (DB 쿼리),
 *           프론트엔드 Faq.vue / PostDetailCard.vue에서 요청
 * 작성 권한: 관리자(isAdmin)만 가능, 수정/삭제는 작성자 본인 또는 관리자
 */
@RestController
@RequestMapping("/api/faq")
@CrossOrigin(origins = "http://localhost:8080")
public class FaqController {

    // FaqService.java를 통해 FAQ CRUD 처리
    @Autowired
    private FaqService faqService;

    // UserService.java를 통해 관리자 권한 확인
    @Autowired
    private UserService userService;

    /**
     * FAQ 목록 페이징 조회 엔드포인트.
     * 프론트엔드 Faq.vue에서 GET /api/faq?pageNum={n} 요청
     * FaqService.java의 getList() 호출
     */
    @GetMapping
    public PageInfo<FaqVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        // FaqService.java의 getList()로 페이징된 FAQ 목록 반환
        return faqService.getList(pageNum);
    }

    /**
     * FAQ 단건 상세 조회 엔드포인트.
     * 프론트엔드 PostDetailCard.vue에서 GET /api/faq/{id} 요청
     * FaqService.java의 getDetail() 호출
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        // FaqService.java의 getDetail()로 FAQ 조회 (없으면 404 반환)
        FaqVO faq = faqService.getDetail(id);
        if (faq == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(faq);
    }

    /**
     * FAQ 작성 엔드포인트. 관리자만 작성 가능.
     * 프론트엔드 Faq.vue에서 POST /api/faq?requesterId={id} 요청
     * UserService.java의 isAdmin(), FaqService.java의 write() 호출
     */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody FaqVO faq) {
        // UserService.java의 isAdmin()으로 관리자 여부 확인 (관리자만 FAQ 작성 가능)
        if (!userService.isAdmin(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "관리자만 FAQ를 작성할 수 있습니다."));
        }
        // 질문(제목) 빈 값 검사
        if (faq.getTitle() == null || faq.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "질문을 입력하세요."));
        }
        // FaqService.java의 write()로 FAQ DB 저장
        return ResponseEntity.ok(faqService.write(faq));
    }

    /**
     * FAQ 수정 엔드포인트. 작성자 본인 또는 관리자만 수정 가능.
     * 프론트엔드 PostDetailCard.vue에서 PUT /api/faq/{id}?requesterId={id} 요청
     * FaqService.java의 getDetail(), update() 호출
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        // FaqService.java의 getDetail()로 FAQ 존재 여부 확인
        FaqVO faq = faqService.getDetail(id);
        if (faq == null) return ResponseEntity.notFound().build();
        // UserService.java의 isAdmin()으로 관리자 여부 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        // 작성자도 아니고 관리자도 아닌 경우 403 반환
        if (!faq.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "질문을 입력하세요."));
        // FaqService.java의 update()로 질문(title)/답변(content) 업데이트
        faqService.update(id, title, body.get("content"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * FAQ 삭제 엔드포인트. 작성자 본인 또는 관리자만 삭제 가능.
     * 프론트엔드 PostDetailCard.vue에서 DELETE /api/faq/{id}?requesterId={id} 요청
     * FaqService.java의 delete(), UserService.java의 isAdmin() 호출
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        // FaqService.java의 getDetail()로 FAQ 존재 여부 확인
        FaqVO faq = faqService.getDetail(id);
        if (faq == null) return ResponseEntity.notFound().build();
        // UserService.java의 isAdmin()으로 관리자 여부 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!faq.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        // FaqService.java의 delete()로 FAQ 삭제
        faqService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
