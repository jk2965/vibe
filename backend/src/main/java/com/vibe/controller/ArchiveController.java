package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.ArchiveFileVO;
import com.vibe.model.ArchiveVO;
import com.vibe.service.ArchiveService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
 * 전사 자료실(Archive) CRUD, 파일 업로드/다운로드/삭제, 이미지 서빙 REST API 컨트롤러.
 * 기본 경로: /api/archive
 * 연관 파일: ArchiveService.java (자료실 비즈니스 로직 및 파일 메타데이터 관리),
 *           UserService.java (권한 확인), ArchiveMapper.xml (DB 쿼리),
 *           프론트엔드 Archive.vue / PostDetailCard.vue에서 요청
 * 작성 권한: 팀장(isTeamLeader) 또는 관리자(isAdmin)만 가능
 */
@RestController
@RequestMapping("/api/archive")
@CrossOrigin(origins = "http://localhost:8080")
public class ArchiveController {

    // ArchiveService.java를 통해 자료실 CRUD 및 파일 메타데이터 처리
    @Autowired
    private ArchiveService archiveService;

    // UserService.java를 통해 작성/삭제 권한 확인
    @Autowired
    private UserService userService;

    // application.properties의 file.upload-dir 경로 (파일 저장 위치)
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 자료실 게시글 목록 페이징 조회 엔드포인트.
     * 프론트엔드 Archive.vue에서 GET /api/archive?pageNum={n} 요청
     * ArchiveService.java의 getList() 호출
     */
    @GetMapping
    public PageInfo<ArchiveVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        // ArchiveService.java의 getList()로 페이징된 자료실 목록 반환
        return archiveService.getList(pageNum);
    }

    /**
     * 자료실 게시글 단건 상세 조회 엔드포인트.
     * 프론트엔드 PostDetailCard.vue에서 GET /api/archive/{id} 요청
     * ArchiveService.java의 getDetail() 호출
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        // ArchiveService.java의 getDetail()로 자료실 게시글 조회 (없으면 404 반환)
        ArchiveVO archive = archiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(archive);
    }

    /**
     * 자료실 게시글 작성 엔드포인트. 팀장 또는 관리자만 작성 가능.
     * 프론트엔드 Archive.vue에서 POST /api/archive?requesterId={id} 요청
     * UserService.java의 isAdmin(), isTeamLeader(), ArchiveService.java의 write() 호출
     */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody ArchiveVO archive) {
        // UserService.java의 isAdmin(), isTeamLeader()로 작성 권한 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);
        if (!isAdmin && !isTeamLeader) {
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 팀장 또는 관리자만 작성할 수 있습니다."));
        }
        // 제목 빈 값 검사
        if (archive.getTitle() == null || archive.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        // ArchiveService.java의 write()로 자료실 게시글 DB 저장
        return ResponseEntity.ok(archiveService.write(archive));
    }

    /**
     * 자료실 게시글 수정 엔드포인트. 작성자 본인만 수정 가능.
     * 프론트엔드 PostDetailCard.vue에서 PUT /api/archive/{id}?requesterId={id} 요청
     * ArchiveService.java의 getDetail(), update() 호출
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        // ArchiveService.java의 getDetail()로 게시글 존재 여부 확인
        ArchiveVO archive = archiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        // 작성자 본인 여부 검사
        if (!archive.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        String tags = body.getOrDefault("tags", "");
        // ArchiveService.java의 update()로 제목/내용/태그 업데이트
        archiveService.update(id, title, content, tags);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 자료실 게시글 삭제 엔드포인트. 작성자 본인 또는 관리자만 삭제 가능. 첨부파일도 함께 물리 삭제.
     * 프론트엔드 PostDetailCard.vue에서 DELETE /api/archive/{id}?requesterId={id} 요청
     * ArchiveService.java의 delete(), UserService.java의 isAdmin(), deletePhysicalFiles() 호출
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        ArchiveVO archive = archiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        // UserService.java의 isAdmin()으로 관리자 여부 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!archive.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        // 물리 파일 삭제 (서버 디스크에서 실제 파일 제거)
        deletePhysicalFiles(id);
        // ArchiveService.java의 delete()로 DB에서 게시글 및 파일 메타데이터 삭제
        archiveService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 자료실 게시글 필독 여부 설정 엔드포인트. 관리자 또는 팀장만 설정 가능.
     * 프론트엔드에서 PATCH /api/archive/{id}/required?requesterId={id} 요청
     * ArchiveService.java의 setRequired() 호출
     */
    @PatchMapping("/{id}/required")
    public ResponseEntity<?> setRequired(@PathVariable String id,
                                         @RequestParam String requesterId,
                                         @RequestBody Map<String, Integer> body) {
        int value = body.getOrDefault("isRequired", 0);
        // ArchiveService.java의 setRequired()로 필독 여부 업데이트
        archiveService.setRequired(id, value);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 자료실 첨부파일 업로드 엔드포인트. 파일을 서버 디스크에 저장하고 메타데이터를 DB에 기록.
     * 프론트엔드 PostDetailCard.vue에서 POST /api/archive/{id}/files 요청
     * ArchiveService.java의 saveFile() 호출
     */
    // 파일 업로드
    @PostMapping("/{id}/files")
    public ResponseEntity<?> uploadFile(@PathVariable String id,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String requesterId) throws IOException {
        // 업로드 디렉토리 없으면 생성
        ensureUploadDir();
        // UUID로 파일명 충돌 방지 후 디스크에 저장
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, storedName);
        Files.write(path, file.getBytes());
        // ArchiveService.java의 saveFile()로 파일 메타데이터를 DB에 저장
        ArchiveFileVO saved = archiveService.saveFile(id, file.getOriginalFilename(), storedName, file.getSize(), requesterId);
        return ResponseEntity.ok(saved);
    }

    /**
     * 에디터 인라인 이미지 업로드 엔드포인트. 에디터(Quill/Tiptap)에서 이미지 삽입 시 사용.
     * 프론트엔드 TiptapEditor.vue에서 POST /api/archive/image 요청
     * 업로드 후 접근 가능한 이미지 URL을 반환한다.
     */
    // 이미지 업로드 (Quill 에디터용)
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        // 업로드 디렉토리 없으면 생성
        ensureUploadDir();
        // UUID로 파일명 충돌 방지 후 디스크에 저장
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, storedName);
        Files.write(path, file.getBytes());
        // 프론트엔드에서 에디터 내 이미지 src로 사용할 URL 반환
        String url = "/api/archive/image/" + storedName;
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * 에디터 인라인 이미지 서빙 엔드포인트. 에디터 내 이미지 태그의 src로 사용.
     * 프론트엔드 PostDetailCard.vue 본문 내 <img> 태그에서 GET /api/archive/image/{filename} 요청
     */
    // 이미지 서빙
    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) throws MalformedURLException {
        // 디스크에서 이미지 파일 로드 후 반환 (없으면 404)
        Path path = Paths.get(uploadDir, filename);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    /**
     * 첨부파일 다운로드 엔드포인트. 파일 메타데이터를 조회하여 원본 파일명으로 다운로드 응답.
     * 프론트엔드 PostDetailCard.vue에서 GET /api/archive/files/{fileId}/download 요청
     * ArchiveService.java의 getFile() 호출
     */
    // 파일 다운로드
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) throws MalformedURLException {
        // ArchiveService.java의 getFile()로 파일 메타데이터 조회
        ArchiveFileVO fileMeta = archiveService.getFile(fileId);
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
     * 첨부파일 단건 삭제 엔드포인트. 업로더 본인 또는 관리자만 삭제 가능. 디스크 파일도 함께 제거.
     * 프론트엔드 PostDetailCard.vue에서 DELETE /api/archive/files/{fileId}?requesterId={id} 요청
     * ArchiveService.java의 getFile(), deleteFile() 호출
     */
    // 첨부파일 단건 삭제
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId, @RequestParam String requesterId) {
        // ArchiveService.java의 getFile()로 파일 메타데이터 조회
        ArchiveFileVO fileMeta = archiveService.getFile(fileId);
        if (fileMeta == null) return ResponseEntity.notFound().build();
        // UserService.java의 isAdmin()으로 관리자 여부 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        String uploaderId = fileMeta.getUploaderId();
        if (uploaderId != null && !uploaderId.equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "파일 삭제 권한이 없습니다."));
        }
        // 서버 디스크에서 실제 파일 삭제
        Path path = Paths.get(uploadDir, fileMeta.getStoredName());
        path.toFile().delete();
        // ArchiveService.java의 deleteFile()로 DB에서 파일 메타데이터 삭제
        archiveService.deleteFile(fileId);
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
     * 게시글에 첨부된 모든 파일을 서버 디스크에서 물리적으로 삭제하는 내부 유틸리티 메서드.
     * delete() 메서드에서 게시글 삭제 전에 호출된다.
     * ArchiveService.java의 getDetail()로 파일 목록 조회 후 순차 삭제
     */
    private void deletePhysicalFiles(String boardId) {
        ArchiveVO archive = archiveService.getDetail(boardId);
        if (archive == null || archive.getFiles() == null) return;
        // 게시글에 첨부된 모든 파일을 디스크에서 순차 삭제
        for (ArchiveFileVO file : archive.getFiles()) {
            Paths.get(uploadDir, file.getStoredName()).toFile().delete();
        }
    }
}
