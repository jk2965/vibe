package com.vibe.controller;

import com.vibe.model.ArchiveFileVO;
import com.vibe.model.BoardEntity;
import com.vibe.service.AbstractBoardService;
import com.vibe.service.FileService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * 모든 게시판 컨트롤러의 공통 HTTP 흐름을 정의하는 추상 클래스 (Template Method 패턴).
 * getDetail, update, delete, setRequired, uploadFile은 모든 게시판이 동일하므로 여기서 구현.
 * getList, write는 게시판마다 파라미터/권한이 달라 서브클래스에서 직접 구현.
 * 서브클래스: BoardController, NoticeController, TeamNoticeController,
 *             ArchiveController, TeamArchiveController, FaqController, QnaController
 */
public abstract class AbstractBoardController<T extends BoardEntity> {

    @Autowired protected UserService userService;
    @Autowired protected FileService fileService;
    @Value("${file.upload-dir}") protected String uploadDir;

    // ── 서브클래스가 반드시 구현하는 훅 ──────────────────────────────

    /** 사용할 서비스 반환 */
    protected abstract AbstractBoardService<T> getService();

    /**
     * 작성 권한 검사 (게시판마다 다름).
     * 자유게시판: 누구나 / 공지사항: 관리자·팀장 / 팀 게시판: 같은 팀 팀장 등
     */
    protected abstract boolean canWrite(String requesterId, T item);

    /**
     * 삭제 권한 검사 (기본: 작성자 또는 관리자).
     * 필요 시 서브클래스에서 오버라이드.
     */
    protected boolean canDelete(String requesterId, T item) {
        return item.getAuthorId().equals(requesterId) || userService.isAdmin(requesterId);
    }

    // ── 공통 엔드포인트 ───────────────────────────────────────────

    /**
     * 게시글 상세 조회.
     * 팀 게시판처럼 팀 소속 검사가 필요하면 서브클래스에서 오버라이드.
     */
    // requesterId는 팀 게시판에서 소속 검사에 사용 (일반 게시판은 무시)
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id,
                                       @RequestParam(required = false) String requesterId) {
        T item = getService().getDetail(id);
        if (item == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(item);
    }

    /**
     * 게시글 수정 (작성자 본인만 가능).
     * 모든 게시판이 동일한 흐름이므로 오버라이드 불필요.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        T item = getService().getDetail(id);
        if (item == null) return ResponseEntity.notFound().build();
        if (!item.getAuthorId().equals(requesterId))
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        String title   = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (content == null || content.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        getService().update(id, title, content, body.getOrDefault("tags", ""));
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 게시글 삭제 (권한 검사는 canDelete() 훅으로 위임).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id,
                                    @RequestParam String requesterId) {
        T item = getService().getDetail(id);
        if (item == null) return ResponseEntity.notFound().build();
        if (!canDelete(requesterId, item))
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        getService().delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 필독 여부 설정.
     * FAQ·Q&A처럼 필독 기능이 없는 게시판은 서비스의 doSetRequired()가 빈 구현이므로 무시됨.
     */
    @PatchMapping("/{id}/required")
    public ResponseEntity<?> setRequired(@PathVariable String id,
                                         @RequestParam String requesterId,
                                         @RequestBody Map<String, Integer> body) {
        getService().setRequired(id, body.getOrDefault("isRequired", 0));
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 첨부파일 업로드 (모든 게시판 동일).
     * 디스크 저장 후 FileService로 메타데이터를 DB에 기록.
     */
    @PostMapping("/{id}/files")
    public ResponseEntity<?> uploadFile(@PathVariable String id,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String requesterId) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.write(Paths.get(uploadDir, storedName), file.getBytes());
        ArchiveFileVO saved = fileService.saveFile(
            id, file.getOriginalFilename(), storedName, file.getSize(), requesterId);
        return ResponseEntity.ok(saved);
    }
}
