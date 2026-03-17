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

@RestController
@RequestMapping("/api/archive")
@CrossOrigin(origins = "http://localhost:8080")
public class ArchiveController {

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private UserService userService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping
    public PageInfo<ArchiveVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return archiveService.getList(pageNum);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        ArchiveVO archive = archiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(archive);
    }

    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody ArchiveVO archive) {
        boolean isAdmin = userService.isAdmin(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);
        if (!isAdmin && !isTeamLeader) {
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 팀장 또는 관리자만 작성할 수 있습니다."));
        }
        if (archive.getTitle() == null || archive.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        return ResponseEntity.ok(archiveService.write(archive));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        ArchiveVO archive = archiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        if (!archive.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        archiveService.update(id, title, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        ArchiveVO archive = archiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!archive.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        // 물리 파일 삭제
        deletePhysicalFiles(id);
        archiveService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 파일 업로드
    @PostMapping("/{id}/files")
    public ResponseEntity<?> uploadFile(@PathVariable String id,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String requesterId) throws IOException {
        ensureUploadDir();
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, storedName);
        Files.write(path, file.getBytes());
        ArchiveFileVO saved = archiveService.saveFile(id, file.getOriginalFilename(), storedName, file.getSize(), requesterId);
        return ResponseEntity.ok(saved);
    }

    // 이미지 업로드 (Quill 에디터용)
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ensureUploadDir();
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, storedName);
        Files.write(path, file.getBytes());
        String url = "/api/archive/image/" + storedName;
        return ResponseEntity.ok(Map.of("url", url));
    }

    // 이미지 서빙
    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) throws MalformedURLException {
        Path path = Paths.get(uploadDir, filename);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    // 파일 다운로드
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) throws MalformedURLException {
        ArchiveFileVO fileMeta = archiveService.getFile(fileId);
        if (fileMeta == null) return ResponseEntity.notFound().build();
        Path path = Paths.get(uploadDir, fileMeta.getStoredName());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileMeta.getOriginalName() + "\"")
                .body(resource);
    }

    // 첨부파일 단건 삭제
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId, @RequestParam String requesterId) {
        ArchiveFileVO fileMeta = archiveService.getFile(fileId);
        if (fileMeta == null) return ResponseEntity.notFound().build();
        boolean isAdmin = userService.isAdmin(requesterId);
        String uploaderId = fileMeta.getUploaderId();
        if (uploaderId != null && !uploaderId.equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "파일 삭제 권한이 없습니다."));
        }
        Path path = Paths.get(uploadDir, fileMeta.getStoredName());
        path.toFile().delete();
        archiveService.deleteFile(fileId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void ensureUploadDir() {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }

    private void deletePhysicalFiles(String boardId) {
        ArchiveVO archive = archiveService.getDetail(boardId);
        if (archive == null || archive.getFiles() == null) return;
        for (ArchiveFileVO file : archive.getFiles()) {
            Paths.get(uploadDir, file.getStoredName()).toFile().delete();
        }
    }
}
