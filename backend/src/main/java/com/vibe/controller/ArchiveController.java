package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.ArchiveFileVO;
import com.vibe.model.ArchiveVO;
import com.vibe.service.AbstractBoardService;
import com.vibe.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 전사 자료실(Archive) REST API 컨트롤러.
 * AbstractBoardController로부터 getDetail, update, setRequired를 상속.
 * delete()는 물리 파일 삭제가 필요하므로 오버라이드.
 * uploadFile()은 ArchiveService.saveFile()을 사용하므로 오버라이드.
 * 작성 권한: 팀장(isTeamLeader) 또는 관리자(isAdmin)만 가능.
 */
@RestController
@RequestMapping("/api/archive")
@CrossOrigin(origins = "http://localhost:8080")
public class ArchiveController extends AbstractBoardController<ArchiveVO> {

    @Autowired private ArchiveService archiveService;

    /** AbstractBoardController가 사용할 서비스 반환 (Template Method 패턴 훅) */
    @Override
    protected AbstractBoardService<ArchiveVO> getService() { return archiveService; }

    /**
     * 작성 권한 훅 (Template Method 패턴).
     * 자료실은 팀장 또는 관리자만 작성 가능.
     */
    @Override
    protected boolean canWrite(String requesterId, ArchiveVO item) {
        return userService.isAdmin(requesterId) || userService.isTeamLeader(requesterId);
    }

    /** 자료실 목록 페이징 조회 */
    @GetMapping
    public PageInfo<ArchiveVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return archiveService.getList(pageNum);
    }

    /** 자료실 게시글 작성 (팀장/관리자만 가능) */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody ArchiveVO archive) {
        if (!canWrite(requesterId, archive))
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 팀장 또는 관리자만 작성할 수 있습니다."));
        if (archive.getTitle() == null || archive.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        return ResponseEntity.ok(archiveService.write(archive));
    }

    /**
     * 게시글 삭제 — 삭제 전 첨부파일을 디스크에서 물리적으로 제거 후 서비스 delete() 호출.
     */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        ArchiveVO archive = archiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        if (!canDelete(requesterId, archive))
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        if (archive.getFiles() != null)
            archive.getFiles().forEach(f -> Paths.get(uploadDir, f.getStoredName()).toFile().delete());
        archiveService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // uploadFile()은 AbstractBoardController에서 fileService.saveFile()로 처리 (오버라이드 불필요)

    /** 에디터 인라인 이미지 업로드 */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ensureUploadDir();
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.write(Paths.get(uploadDir, storedName), file.getBytes());
        return ResponseEntity.ok(Map.of("url", "/api/archive/image/" + storedName));
    }

    /** 에디터 인라인 이미지 서빙 */
    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) throws MalformedURLException {
        Path path = Paths.get(uploadDir, filename);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
    }

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

    private void ensureUploadDir() {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }
}
