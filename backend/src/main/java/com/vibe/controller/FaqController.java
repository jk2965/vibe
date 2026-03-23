package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.ArchiveFileVO;
import com.vibe.model.FaqVO;
import com.vibe.service.FaqService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

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

    // FaqService.java를 통해 FAQ CRUD 및 파일 메타데이터 처리
    @Autowired
    private FaqService faqService;

    // UserService.java를 통해 관리자 권한 확인
    @Autowired
    private UserService userService;

    // application.properties의 file.upload-dir 경로 (파일 저장 위치)
    @Value("${file.upload-dir}")
    private String uploadDir;

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
        // 물리 파일 삭제 (서버 디스크에서 실제 파일 제거)
        deletePhysicalFiles(id);
        // FaqService.java의 delete()로 DB에서 FAQ 및 파일 메타데이터 삭제
        faqService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * FAQ 첨부파일 업로드 엔드포인트. 파일을 서버 디스크에 저장하고 메타데이터를 DB에 기록.
     * 프론트엔드 FaqWrite.vue / FaqEdit.vue에서 POST /api/faq/{id}/files 요청
     * FaqService.java의 saveFile() 호출
     */
    @PostMapping("/{id}/files")
    public ResponseEntity<?> uploadFile(@PathVariable String id,
                                        @RequestParam MultipartFile file,
                                        @RequestParam(required = false) String requesterId) throws IOException {
        // 업로드 디렉토리 없으면 생성
        ensureUploadDir();
        // UUID로 파일명 충돌 방지 후 디스크에 저장
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, storedName);
        Files.write(path, file.getBytes());
        // FaqService.java의 saveFile()로 파일 메타데이터를 DB에 저장 (archive_file 테이블 공유)
        ArchiveFileVO saved = faqService.saveFile(id, file.getOriginalFilename(), storedName, file.getSize(), requesterId);
        return ResponseEntity.ok(saved);
    }

    /**
     * FAQ 첨부파일 다운로드 엔드포인트. 파일 메타데이터를 조회하여 원본 파일명으로 다운로드 응답.
     * 프론트엔드 FileList.vue에서 GET /api/faq/files/{fileId}/download 요청
     * FaqService.java의 getFile() 호출
     */
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) throws MalformedURLException {
        // FaqService.java의 getFile()로 파일 메타데이터 조회
        ArchiveFileVO fileMeta = faqService.getFile(fileId);
        if (fileMeta == null) return ResponseEntity.notFound().build();
        Path path = Paths.get(uploadDir, fileMeta.getStoredName());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        // Content-Disposition 헤더로 원본 파일명 지정하여 다운로드 응답
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileMeta.getOriginalName() + "\"")
                .body(resource);
    }

    /**
     * FAQ 첨부파일 단건 삭제 엔드포인트. 업로더 본인 또는 관리자만 삭제 가능. 디스크 파일도 함께 제거.
     * 프론트엔드 FileList.vue에서 DELETE /api/faq/files/{fileId}?requesterId={id} 요청
     * FaqService.java의 getFile(), deleteFile() 호출
     */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId, @RequestParam String requesterId) {
        // FaqService.java의 getFile()로 파일 메타데이터 조회
        ArchiveFileVO fileMeta = faqService.getFile(fileId);
        if (fileMeta == null) return ResponseEntity.notFound().build();
        // UserService.java의 isAdmin()으로 관리자 여부 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        String uploaderId = fileMeta.getUploaderId();
        if (uploaderId != null && !uploaderId.equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "파일 삭제 권한이 없습니다."));
        }
        // 서버 디스크에서 실제 파일 삭제
        Paths.get(uploadDir, fileMeta.getStoredName()).toFile().delete();
        // FaqService.java의 deleteFile()로 DB에서 파일 메타데이터 삭제
        faqService.deleteFile(fileId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 업로드 디렉토리가 존재하지 않을 경우 생성하는 내부 유틸리티 메서드.
     */
    private void ensureUploadDir() {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }

    /**
     * FAQ 게시글에 첨부된 모든 파일을 서버 디스크에서 물리적으로 삭제하는 내부 유틸리티 메서드.
     * delete() 메서드에서 게시글 삭제 전에 호출된다.
     */
    private void deletePhysicalFiles(String faqId) {
        FaqVO faq = faqService.getDetail(faqId);
        if (faq == null || faq.getFiles() == null) return;
        // 게시글에 첨부된 모든 파일을 디스크에서 순차 삭제
        for (ArchiveFileVO file : faq.getFiles()) {
            Paths.get(uploadDir, file.getStoredName()).toFile().delete();
        }
    }
}
