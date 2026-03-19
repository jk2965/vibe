package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.ArchiveFileVO;
import com.vibe.model.NoticeVO;
import com.vibe.service.FileService;
import com.vibe.service.NoticeService;
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
 * 전사 공지사항(Notice) CRUD 및 파일 첨부 REST API 컨트롤러.
 * 기본 경로: /api/notice
 * 연관 파일: NoticeService.java (공지 비즈니스 로직), FileService.java (파일 저장),
 *           UserService.java (권한 확인), NoticeMapper.xml (DB 쿼리),
 *           프론트엔드 Notice.vue / PostDetailCard.vue에서 요청
 * 작성 권한: 팀장(isTeamLeader) 또는 관리자(isAdmin)만 가능
 */
@RestController
@RequestMapping("/api/notice")
@CrossOrigin(origins = "http://localhost:8080")
public class NoticeController {

    // NoticeService.java를 통해 공지사항 CRUD 처리
    @Autowired
    private NoticeService noticeService;

    // UserService.java를 통해 작성/삭제 권한 확인
    @Autowired
    private UserService userService;

    // FileService.java를 통해 첨부파일 메타데이터 DB 저장
    @Autowired
    private FileService fileService;

    // application.properties의 file.upload-dir 경로
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 공지사항 목록 페이징 조회 엔드포인트.
     * 프론트엔드 Notice.vue에서 GET /api/notice?pageNum={n} 요청
     * NoticeService.java의 getList() 호출
     */
    @GetMapping
    public PageInfo<NoticeVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        // NoticeService.java의 getList()로 페이징된 공지사항 목록 반환
        return noticeService.getList(pageNum);
    }

    /**
     * 공지사항 단건 상세 조회 엔드포인트.
     * 프론트엔드 PostDetailCard.vue에서 GET /api/notice/{id} 요청
     * NoticeService.java의 getDetail() 호출
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        // NoticeService.java의 getDetail()로 공지사항 조회 (없으면 404 반환)
        NoticeVO notice = noticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(notice);
    }

    /**
     * 공지사항 작성 엔드포인트. 팀장 또는 관리자만 작성 가능.
     * 프론트엔드 Notice.vue에서 POST /api/notice?requesterId={id} 요청
     * UserService.java의 isAdmin(), isTeamLeader(), NoticeService.java의 write() 호출
     */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody NoticeVO notice) {
        // 제목/내용 빈 값 검사
        if (notice.getTitle() == null || notice.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        if (notice.getContent() == null || notice.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        }
        // UserService.java의 isAdmin(), isTeamLeader()로 작성 권한 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        boolean isTeamLeader = userService.isTeamLeader(requesterId);
        if (!isAdmin && !isTeamLeader) {
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 팀장 또는 관리자만 작성할 수 있습니다."));
        }
        // NoticeService.java의 write()로 공지사항 DB 저장
        return ResponseEntity.ok(noticeService.write(notice));
    }

    /**
     * 공지사항 수정 엔드포인트. 작성자 본인만 수정 가능.
     * 프론트엔드 PostDetailCard.vue에서 PUT /api/notice/{id}?requesterId={id} 요청
     * NoticeService.java의 getDetail(), update() 호출
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        // NoticeService.java의 getDetail()로 공지사항 존재 여부 확인
        NoticeVO notice = noticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        // 작성자 본인 여부 검사
        if (!notice.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        // NoticeService.java의 update()로 제목/내용 업데이트
        noticeService.update(id, title, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 공지사항 삭제 엔드포인트. 작성자 본인 또는 관리자만 삭제 가능.
     * 프론트엔드 PostDetailCard.vue에서 DELETE /api/notice/{id}?requesterId={id} 요청
     * NoticeService.java의 delete(), UserService.java의 isAdmin() 호출
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        NoticeVO notice = noticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        // UserService.java의 isAdmin()으로 관리자 여부 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        if (!notice.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        // NoticeService.java의 delete()로 공지사항 삭제
        noticeService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 공지사항 첨부파일 업로드 엔드포인트. 파일을 서버 디스크에 저장하고 메타데이터를 DB에 기록.
     * 프론트엔드 PostDetailCard.vue에서 POST /api/notice/{id}/files 요청
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
