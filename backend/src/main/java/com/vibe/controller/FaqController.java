package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.FaqVO;
import com.vibe.service.FaqService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/faq")
@CrossOrigin(origins = "http://localhost:8080")
public class FaqController {

    @Autowired
    private FaqService faqService;

    @Autowired
    private UserService userService;

    @GetMapping
    public PageInfo<FaqVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return faqService.getList(pageNum);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        FaqVO faq = faqService.getDetail(id);
        if (faq == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(faq);
    }

    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody FaqVO faq) {
        if (!userService.isAdmin(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "관리자만 FAQ를 작성할 수 있습니다."));
        }
        if (faq.getTitle() == null || faq.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "질문을 입력하세요."));
        }
        return ResponseEntity.ok(faqService.write(faq));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        FaqVO faq = faqService.getDetail(id);
        if (faq == null) return ResponseEntity.notFound().build();
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!faq.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "질문을 입력하세요."));
        faqService.update(id, title, body.get("content"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        FaqVO faq = faqService.getDetail(id);
        if (faq == null) return ResponseEntity.notFound().build();
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!faq.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        faqService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
