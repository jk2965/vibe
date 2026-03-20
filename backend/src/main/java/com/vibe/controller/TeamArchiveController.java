package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.ArchiveFileVO;
import com.vibe.model.TeamArchiveVO;
import com.vibe.model.UserVO;
import com.vibe.service.ArchiveService;
import com.vibe.service.TeamArchiveService;
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
 * 팀 자료실(TeamArchive) CRUD, 파일 업로드/다운로드/삭제, 이미지 서빙 REST API 컨트롤러.
 * 기본 경로: /api/team-archive
 * 연관 파일: TeamArchiveService.java (팀 자료실 비즈니스 로직),
 *           ArchiveService.java (파일 메타데이터 공유 사용), UserService.java (권한 확인),
 *           TeamArchiveMapper.xml (DB 쿼리),
 *           프론트엔드 TeamArchive.vue / PostDetailCard.vue에서 요청
 * 접근 권한: 동일 팀 소속 또는 마스터(adminLevel=2) / 작성 권한: 동일 팀 팀장/관리자 또는 마스터
 */
@RestController
@RequestMapping("/api/team-archive")
@CrossOrigin(origins = "http://localhost:8080")
public class TeamArchiveController {

    // TeamArchiveService.java를 통해 팀 자료실 CRUD 처리
    @Autowired
    private TeamArchiveService teamArchiveService;

    // ArchiveService.java를 통해 파일 메타데이터 저장/조회/삭제 (전사 자료실과 파일 처리 공유)
    @Autowired
    private ArchiveService archiveService;

    // UserService.java를 통해 팀 소속 및 권한 확인
    @Autowired
    private UserService userService;

    // application.properties의 file.upload-dir 경로 (파일 저장 위치)
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 특정 팀의 자료실 게시글 목록 페이징 조회 엔드포인트. 동일 팀 소속 또는 마스터만 접근 가능.
     * 프론트엔드 TeamArchive.vue에서 GET /api/team-archive?team={팀명}&requesterId={id}&pageNum={n} 요청
     * UserService.java의 getAdminLevel(), getUser(), TeamArchiveService.java의 getListByTeam() 호출
     */
    @GetMapping
    public ResponseEntity<?> getList(@RequestParam String team,
                                     @RequestParam String requesterId,
                                     @RequestParam(defaultValue = "1") int pageNum) {
        // UserService.java의 getAdminLevel()로 권한 레벨 확인 (2=마스터는 모든 팀 접근 가능)
        int adminLevel = userService.getAdminLevel(requesterId);
        if (adminLevel < 2) {
            // 마스터가 아닌 경우 요청자의 팀 소속 검사
            UserVO requester = userService.getUser(requesterId);
            if (requester == null || !team.equals(requester.getTeam())) {
                return ResponseEntity.status(403).body(Map.of("message", "해당 팀 자료실에 접근 권한이 없습니다."));
            }
        }
        // TeamArchiveService.java의 getListByTeam()으로 팀별 자료실 목록 반환
        return ResponseEntity.ok(teamArchiveService.getListByTeam(team, pageNum));
    }

    /**
     * 팀 자료실 게시글 단건 상세 조회 엔드포인트. 동일 팀 소속 또는 마스터만 접근 가능.
     * 프론트엔드 PostDetailCard.vue에서 GET /api/team-archive/{id}?requesterId={id} 요청
     * TeamArchiveService.java의 getDetail(), UserService.java의 getAdminLevel() 호출
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id, @RequestParam String requesterId) {
        // TeamArchiveService.java의 getDetail()로 자료실 게시글 조회 (없으면 404 반환)
        TeamArchiveVO archive = teamArchiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        // 마스터가 아닌 경우 동일 팀 소속 여부 검사
        int adminLevel = userService.getAdminLevel(requesterId);
        if (adminLevel < 2) {
            UserVO requester = userService.getUser(requesterId);
            if (requester == null || !archive.getTeam().equals(requester.getTeam())) {
                return ResponseEntity.status(403).body(Map.of("message", "접근 권한이 없습니다."));
            }
        }
        return ResponseEntity.ok(archive);
    }

    /**
     * 팀 자료실 게시글 작성 엔드포인트. 해당 팀 팀장/관리자 또는 마스터만 작성 가능.
     * 프론트엔드 TeamArchive.vue에서 POST /api/team-archive?requesterId={id} 요청
     * UserService.java의 getAdminLevel(), isTeamLeader(), getUser(), TeamArchiveService.java의 write() 호출
     */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody TeamArchiveVO archive) {
        // 제목/팀 빈 값 검사
        if (archive.getTitle() == null || archive.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        if (archive.getTeam() == null || archive.getTeam().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "팀을 선택하세요."));
        }
        // UserService.java의 getAdminLevel(), isTeamLeader()로 권한 확인
        int adminLevel = userService.getAdminLevel(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);
        UserVO requester = userService.getUser(requesterId);
        // 요청자가 대상 팀과 동일 팀인지 확인
        boolean isSameTeam = requester != null && archive.getTeam().equals(requester.getTeam());
        // 마스터(2)가 아니면서 동일 팀 팀장/관리자도 아닌 경우 403 반환
        if (adminLevel < 2 && !(isSameTeam && (adminLevel >= 1 || isTeamLeader))) {
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 해당 팀 팀장/관리자 또는 마스터만 작성할 수 있습니다."));
        }
        // TeamArchiveService.java의 write()로 팀 자료실 게시글 DB 저장
        return ResponseEntity.ok(teamArchiveService.write(archive));
    }

    /**
     * 팀 자료실 게시글 수정 엔드포인트. 작성자 본인만 수정 가능.
     * 프론트엔드 PostDetailCard.vue에서 PUT /api/team-archive/{id}?requesterId={id} 요청
     * TeamArchiveService.java의 getDetail(), update() 호출
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        // TeamArchiveService.java의 getDetail()로 게시글 존재 여부 확인
        TeamArchiveVO archive = teamArchiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        // 작성자 본인 여부 검사
        if (!archive.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        String tags = body.getOrDefault("tags", "");
        // TeamArchiveService.java의 update()로 제목/내용/태그 업데이트
        teamArchiveService.update(id, title, content, tags);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 팀 자료실 게시글 삭제 엔드포인트. 작성자 본인 또는 관리자만 삭제 가능.
     * 프론트엔드 PostDetailCard.vue에서 DELETE /api/team-archive/{id}?requesterId={id} 요청
     * TeamArchiveService.java의 delete(), UserService.java의 isAdmin() 호출
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        // TeamArchiveService.java의 getDetail()로 게시글 존재 여부 확인
        TeamArchiveVO archive = teamArchiveService.getDetail(id);
        if (archive == null) return ResponseEntity.notFound().build();
        // UserService.java의 isAdmin()으로 관리자 여부 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!archive.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        // TeamArchiveService.java의 delete()로 팀 자료실 게시글 삭제
        teamArchiveService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 팀 자료실 게시글 필독 여부 설정 엔드포인트. 관리자 또는 팀장만 설정 가능.
     * 프론트엔드에서 PATCH /api/team-archive/{id}/required?requesterId={id} 요청
     * TeamArchiveService.java의 setRequired() 호출
     */
    @PatchMapping("/{id}/required")
    public ResponseEntity<?> setRequired(@PathVariable String id,
                                         @RequestParam String requesterId,
                                         @RequestBody Map<String, Integer> body) {
        int value = body.getOrDefault("isRequired", 0);
        // TeamArchiveService.java의 setRequired()로 필독 여부 업데이트
        teamArchiveService.setRequired(id, value);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 팀 자료실 첨부파일 업로드 엔드포인트. 파일을 서버 디스크에 저장하고 메타데이터를 DB에 기록.
     * 프론트엔드 PostDetailCard.vue에서 POST /api/team-archive/{id}/files 요청
     * ArchiveService.java의 saveFile() 호출 (전사 자료실과 파일 저장 로직 공유)
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
     * 프론트엔드 TiptapEditor.vue에서 POST /api/team-archive/image 요청
     * 업로드 후 /api/archive/image/{filename} 경로의 이미지 URL을 반환한다.
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
        // 이미지 서빙은 ArchiveController의 /api/archive/image/{filename} 엔드포인트를 공유
        String url = "http://localhost:8090/api/archive/image/" + storedName;
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * 첨부파일 다운로드 엔드포인트. 파일 메타데이터를 조회하여 원본 파일명으로 다운로드 응답.
     * 프론트엔드 PostDetailCard.vue에서 GET /api/team-archive/files/{fileId}/download 요청
     * ArchiveService.java의 getFile() 호출 (전사 자료실과 파일 조회 로직 공유)
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
     * 첨부파일 단건 삭제 엔드포인트. 디스크 파일과 DB 메타데이터를 함께 삭제.
     * 프론트엔드 PostDetailCard.vue에서 DELETE /api/team-archive/files/{fileId}?requesterId={id} 요청
     * ArchiveService.java의 getFile(), deleteFile() 호출
     */
    // 첨부파일 단건 삭제
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId, @RequestParam String requesterId) {
        // ArchiveService.java의 getFile()로 파일 메타데이터 조회
        ArchiveFileVO fileMeta = archiveService.getFile(fileId);
        if (fileMeta == null) return ResponseEntity.notFound().build();
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
}
