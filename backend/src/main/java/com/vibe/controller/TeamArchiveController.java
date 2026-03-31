package com.vibe.controller;

import com.vibe.model.ArchiveFileVO;
import com.vibe.model.TeamArchiveVO;
import com.vibe.model.UserVO;
import com.vibe.service.AbstractBoardService;
import com.vibe.service.TeamArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 팀 자료실(TeamArchive) REST API 컨트롤러.
 * AbstractBoardController로부터 update, delete, setRequired를 상속.
 * getDetail()은 팀 소속 검사가 필요하므로 오버라이드.
 * uploadFile()은 TeamArchiveService.saveFile()을 사용하므로 오버라이드.
 * 작성 권한: 같은 팀 팀장/관리자 또는 마스터(adminLevel=2).
 */
@RestController
@RequestMapping("/api/team-archive")
@CrossOrigin(origins = "http://localhost:8080")
public class TeamArchiveController extends AbstractBoardController<TeamArchiveVO> {

    @Autowired private TeamArchiveService teamArchiveService;

    /** AbstractBoardController가 사용할 서비스 반환 (Template Method 패턴 훅) */
    @Override
    protected AbstractBoardService<TeamArchiveVO> getService() { return teamArchiveService; }

    /**
     * 작성 권한 훅 (Template Method 패턴).
     * 같은 팀 팀장/관리자 또는 마스터(adminLevel=2)만 작성 가능.
     */
    @Override
    protected boolean canWrite(String requesterId, TeamArchiveVO item) {
        int adminLevel = userService.getAdminLevel(requesterId);
        if (adminLevel >= 2) return true;
        UserVO requester = userService.getUser(requesterId);
        boolean isSameTeam = requester != null && item.getTeam().equals(requester.getTeam());
        return isSameTeam && (adminLevel >= 1 || userService.isTeamLeader(requesterId));
    }

    /** 팀 소속 검사 포함 목록 조회 */
    @GetMapping
    public ResponseEntity<?> getList(@RequestParam String team,
                                     @RequestParam String requesterId,
                                     @RequestParam(defaultValue = "1") int pageNum) {
        if (userService.getAdminLevel(requesterId) < 2) {
            UserVO requester = userService.getUser(requesterId);
            if (requester == null || !team.equals(requester.getTeam()))
                return ResponseEntity.status(403).body(Map.of("message", "해당 팀 자료실에 접근 권한이 없습니다."));
        }
        return ResponseEntity.ok(teamArchiveService.getListByTeam(team, pageNum));
    }

    /** 팀 소속 검사 포함 상세 조회 (추상 클래스 오버라이드) */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id,
                                       @RequestParam(required = false) String requesterId) {
        TeamArchiveVO archive = teamArchiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        if (requesterId != null && userService.getAdminLevel(requesterId) < 2) {
            UserVO requester = userService.getUser(requesterId);
            if (requester == null || !archive.getTeam().equals(requester.getTeam()))
                return ResponseEntity.status(403).body(Map.of("message", "접근 권한이 없습니다."));
        }
        return ResponseEntity.ok(archive);
    }

    /** 팀 자료실 게시글 작성 */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody TeamArchiveVO archive) {
        if (archive.getTitle() == null || archive.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (archive.getTeam() == null || archive.getTeam().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "팀을 선택하세요."));
        if (!canWrite(requesterId, archive))
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 해당 팀 팀장/관리자 또는 마스터만 작성할 수 있습니다."));
        return ResponseEntity.ok(teamArchiveService.write(archive));
    }

    // uploadFile()은 AbstractBoardController에서 fileService.saveFile()로 처리 (오버라이드 불필요)

    /** 에디터 인라인 이미지 업로드 (이미지 서빙은 ArchiveController 경로 공유) */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ensureUploadDir();
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.write(Paths.get(uploadDir, storedName), file.getBytes());
        return ResponseEntity.ok(Map.of("url", "http://localhost:8090/api/archive/image/" + storedName));
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
        Paths.get(uploadDir, fileMeta.getStoredName()).toFile().delete();
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void ensureUploadDir() {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }
}
