package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.ArchiveFileVO;
import com.vibe.model.FaqVO;
import com.vibe.service.AbstractBoardService;
import com.vibe.service.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * FAQ 게시판 REST API 컨트롤러.
 * AbstractBoardController로부터 getDetail을 상속.
 * update()는 관리자도 수정 가능하므로 오버라이드 (기본: 작성자만).
 * delete()는 물리 파일 삭제가 필요하므로 오버라이드.
 * uploadFile()은 FaqService.saveFile()을 사용하므로 오버라이드.
 * 작성 권한: 관리자(isAdmin)만 가능.
 */
@RestController
@RequestMapping("/api/faq")
@CrossOrigin(origins = "http://localhost:8080")
public class FaqController extends AbstractBoardController<FaqVO> {

    @Autowired private FaqService faqService;

    /** AbstractBoardController가 사용할 서비스 반환 (Template Method 패턴 훅) */
    @Override
    protected AbstractBoardService<FaqVO> getService() { return faqService; }

    /**
     * 작성 권한 훅 (Template Method 패턴).
     * FAQ는 관리자만 작성 가능.
     */
    @Override
    protected boolean canWrite(String requesterId, FaqVO item) {
        return userService.isAdmin(requesterId);
    }

    /** FAQ 목록 페이징 조회 */
    @GetMapping
    public PageInfo<FaqVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return faqService.getList(pageNum);
    }

    /** FAQ 작성 (관리자만 가능) */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody FaqVO faq) {
        if (!canWrite(requesterId, faq))
            return ResponseEntity.status(403).body(Map.of("message", "관리자만 FAQ를 작성할 수 있습니다."));
        if (faq.getTitle() == null || faq.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "질문을 입력하세요."));
        return ResponseEntity.ok(faqService.write(faq));
    }

    /**
     * FAQ 수정 — 작성자 본인 또는 관리자 모두 수정 가능.
     * 기본 구현(작성자만)과 달리 관리자도 허용하므로 오버라이드.
     */
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        FaqVO faq = faqService.getDetail(id);
        if (faq == null) return ResponseEntity.notFound().build();
        if (!faq.getAuthorId().equals(requesterId) && !userService.isAdmin(requesterId))
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        String title = body.get("title");
        if (title == null || title.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "질문을 입력하세요."));
        faqService.update(id, title, body.get("content"), "");
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * FAQ 삭제 — 삭제 전 첨부파일을 디스크에서 물리적으로 제거 후 서비스 delete() 호출.
     */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        FaqVO faq = faqService.getDetail(id);
        if (faq == null) return ResponseEntity.notFound().build();
        if (!canDelete(requesterId, faq))
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        if (faq.getFiles() != null)
            faq.getFiles().forEach(f -> Paths.get(uploadDir, f.getStoredName()).toFile().delete());
        faqService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // uploadFile()은 AbstractBoardController에서 fileService.saveFile()로 처리 (오버라이드 불필요)

    /** 첨부파일 다운로드 — fileService.getFile()로 메타데이터 조회 */
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) throws MalformedURLException {
        ArchiveFileVO fileMeta = fileService.getFile(fileId);
        if (fileMeta == null) return ResponseEntity.notFound().build();
        Path path = Paths.get(uploadDir, fileMeta.getStoredName());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileMeta.getOriginalName() + "\"")
                .body(resource);
    }

    /** 첨부파일 단건 삭제 — fileService.getFile()/deleteFile()로 처리 */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId, @RequestParam String requesterId) {
        ArchiveFileVO fileMeta = fileService.getFile(fileId);
        if (fileMeta == null) return ResponseEntity.notFound().build();
        String uploaderId = fileMeta.getUploaderId();
        if (uploaderId != null && !uploaderId.equals(requesterId) && !userService.isAdmin(requesterId))
            return ResponseEntity.status(403).body(Map.of("message", "파일 삭제 권한이 없습니다."));
        Paths.get(uploadDir, fileMeta.getStoredName()).toFile().delete();
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(Map.of("success", true));
    }


}
