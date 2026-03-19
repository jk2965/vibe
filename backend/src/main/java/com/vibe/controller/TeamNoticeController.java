package com.vibe.controller;

import com.vibe.model.ArchiveFileVO;
import com.vibe.model.TeamNoticeVO;
import com.vibe.model.UserVO;
import com.vibe.service.FileService;
import com.vibe.service.TeamNoticeService;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * 팀 공지사항(TeamNotice) CRUD 및 파일 첨부 REST API 컨트롤러.
 * 기본 경로: /api/team-notice
 * 연관 파일: TeamNoticeService.java (팀 공지 비즈니스 로직), FileService.java (파일 저장),
 *           UserService.java (권한 및 팀 소속 확인), TeamNoticeMapper.xml (DB 쿼리),
 *           프론트엔드 TeamNotice.vue / PostDetailCard.vue에서 요청
 * 접근 권한: 동일 팀 소속 또는 마스터(adminLevel=2) / 작성 권한: 동일 팀 팀장/관리자 또는 마스터
 */
@RestController
@RequestMapping("/api/team-notice")
@CrossOrigin(origins = "http://localhost:8080")
public class TeamNoticeController {

    // TeamNoticeService.java를 통해 팀 공지사항 CRUD 처리
    @Autowired
    private TeamNoticeService teamNoticeService;

    // UserService.java를 통해 팀 소속 및 권한 확인
    @Autowired
    private UserService userService;

    // FileService.java를 통해 첨부파일 메타데이터 DB 저장
    @Autowired
    private FileService fileService;

    // application.properties의 file.upload-dir 경로
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 특정 팀의 공지사항 목록 페이징 조회 엔드포인트. 동일 팀 소속 또는 마스터만 접근 가능.
     * 프론트엔드 TeamNotice.vue에서 GET /api/team-notice?team={팀명}&requesterId={id}&pageNum={n} 요청
     * UserService.java의 getAdminLevel(), getUser(), TeamNoticeService.java의 getListByTeam() 호출
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
                return ResponseEntity.status(403).body(Map.of("message", "해당 팀 공지사항에 접근 권한이 없습니다."));
            }
        }
        // TeamNoticeService.java의 getListByTeam()으로 팀별 공지사항 목록 반환
        return ResponseEntity.ok(teamNoticeService.getListByTeam(team, pageNum));
    }

    /**
     * 팀 공지사항 단건 상세 조회 엔드포인트. 동일 팀 소속 또는 마스터만 접근 가능.
     * 프론트엔드 PostDetailCard.vue에서 GET /api/team-notice/{id}?requesterId={id} 요청
     * TeamNoticeService.java의 getDetail(), UserService.java의 getAdminLevel() 호출
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id, @RequestParam String requesterId) {
        // TeamNoticeService.java의 getDetail()로 팀 공지사항 조회 (없으면 404 반환)
        TeamNoticeVO notice = teamNoticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        // 마스터가 아닌 경우 동일 팀 소속 여부 검사
        int adminLevel = userService.getAdminLevel(requesterId);
        if (adminLevel < 2) {
            UserVO requester = userService.getUser(requesterId);
            if (requester == null || !notice.getTeam().equals(requester.getTeam())) {
                return ResponseEntity.status(403).body(Map.of("message", "접근 권한이 없습니다."));
            }
        }
        return ResponseEntity.ok(notice);
    }

    /**
     * 팀 공지사항 작성 엔드포인트. 해당 팀 팀장/관리자 또는 마스터만 작성 가능.
     * 프론트엔드 TeamNotice.vue에서 POST /api/team-notice?requesterId={id} 요청
     * UserService.java의 getAdminLevel(), isTeamLeader(), getUser(), TeamNoticeService.java의 write() 호출
     */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody TeamNoticeVO notice) {
        // 제목/내용/팀 빈 값 검사
        if (notice.getTitle() == null || notice.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        if (notice.getContent() == null || notice.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        }
        if (notice.getTeam() == null || notice.getTeam().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "팀을 선택하세요."));
        }
        // UserService.java의 getAdminLevel(), isTeamLeader()로 권한 확인
        int adminLevel = userService.getAdminLevel(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);
        UserVO requester = userService.getUser(requesterId);
        // 요청자가 대상 팀과 동일 팀인지 확인
        boolean isSameTeam = requester != null && notice.getTeam().equals(requester.getTeam());
        // 마스터(2)가 아니면서 동일 팀 팀장/관리자도 아닌 경우 403 반환
        if (adminLevel < 2 && !(isSameTeam && (adminLevel >= 1 || isTeamLeader))) {
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 해당 팀 팀장/관리자 또는 마스터만 작성할 수 있습니다."));
        }
        // TeamNoticeService.java의 write()로 팀 공지사항 DB 저장
        return ResponseEntity.ok(teamNoticeService.write(notice));
    }

    /**
     * 팀 공지사항 수정 엔드포인트. 작성자 본인만 수정 가능.
     * 프론트엔드 PostDetailCard.vue에서 PUT /api/team-notice/{id}?requesterId={id} 요청
     * TeamNoticeService.java의 getDetail(), update() 호출
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        // TeamNoticeService.java의 getDetail()로 게시글 존재 여부 확인
        TeamNoticeVO notice = teamNoticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        // 작성자 본인 여부 검사
        if (!notice.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        // TeamNoticeService.java의 update()로 제목/내용 업데이트
        teamNoticeService.update(id, title, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 팀 공지사항 삭제 엔드포인트. 작성자 본인 또는 관리자만 삭제 가능.
     * 프론트엔드 PostDetailCard.vue에서 DELETE /api/team-notice/{id}?requesterId={id} 요청
     * TeamNoticeService.java의 delete(), UserService.java의 isAdmin() 호출
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        // TeamNoticeService.java의 getDetail()로 게시글 존재 여부 확인
        TeamNoticeVO notice = teamNoticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        // UserService.java의 isAdmin()으로 관리자 여부 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!notice.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        // TeamNoticeService.java의 delete()로 팀 공지사항 삭제
        teamNoticeService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 팀 공지사항 필독 여부 설정 엔드포인트. 관리자 또는 팀장만 설정 가능.
     * 프론트엔드에서 PATCH /api/team-notice/{id}/required?requesterId={id} 요청
     * TeamNoticeService.java의 setRequired() 호출
     */
    @PatchMapping("/{id}/required")
    public ResponseEntity<?> setRequired(@PathVariable String id,
                                         @RequestParam String requesterId,
                                         @RequestBody Map<String, Integer> body) {
        int value = body.getOrDefault("isRequired", 0);
        // TeamNoticeService.java의 setRequired()로 필독 여부 업데이트
        teamNoticeService.setRequired(id, value);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 팀 공지사항 첨부파일 업로드 엔드포인트. 파일을 서버 디스크에 저장하고 메타데이터를 DB에 기록.
     * 프론트엔드 PostDetailCard.vue에서 POST /api/team-notice/{id}/files 요청
     * FileService.java의 saveFile() 호출
     */
    @PostMapping("/{id}/files")
    public ResponseEntity<?> uploadFile(@PathVariable String id,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String requesterId) throws IOException {
        // 업로드 디렉토리 없으면 생성
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        // UUID로 파일명 충돌 방지 후 디스크에 저장
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, storedName);
        Files.write(path, file.getBytes());
        // FileService.java의 saveFile()로 파일 메타데이터를 DB에 저장
        ArchiveFileVO saved = fileService.saveFile(id, file.getOriginalFilename(), storedName, file.getSize(), requesterId);
        return ResponseEntity.ok(saved);
    }
}
