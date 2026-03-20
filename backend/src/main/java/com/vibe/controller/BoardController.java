package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.ArchiveFileVO;
import com.vibe.model.BoardVO;
import com.vibe.service.BoardService;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * 자유게시판(Board) CRUD 및 파일 첨부 REST API 컨트롤러.
 * 기본 경로: /api/board
 * 연관 파일: BoardService.java (게시글 비즈니스 로직), FileService.java (파일 저장),
 *           UserService.java (권한 확인), BoardMapper.xml (DB 쿼리),
 *           프론트엔드 Board.vue / PostDetailCard.vue에서 요청
 */
@RestController
@RequestMapping("/api/board")
@CrossOrigin(origins = "http://localhost:8080")
public class BoardController {

    // BoardService.java를 통해 게시글 CRUD 처리
    @Autowired
    private BoardService boardService;

    // UserService.java를 통해 관리자 여부 확인 (삭제 권한)
    @Autowired
    private UserService userService;

    // FileService.java를 통해 첨부파일 메타데이터 DB 저장
    @Autowired
    private FileService fileService;

    // application.properties의 file.upload-dir 경로 (파일 저장 위치)
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 게시글 목록 페이징 조회 엔드포인트.
     * 프론트엔드 Board.vue에서 GET /api/board?pageNum={n} 요청
     * BoardService.java의 getList() 호출
     */
    @GetMapping
    public PageInfo<BoardVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        // BoardService.java의 getList()로 페이징된 게시글 목록 반환
        return boardService.getList(pageNum);
    }

    /**
     * 게시글 단건 상세 조회 엔드포인트.
     * 프론트엔드 PostDetailCard.vue에서 GET /api/board/{id} 요청
     * BoardService.java의 getDetail() 호출
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id) {
        // BoardService.java의 getDetail()로 게시글 조회 (없으면 404 반환)
        BoardVO board = boardService.getDetail(id);
        if (board == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(board);
    }

    /**
     * 게시글 작성 엔드포인트. 제목/내용 필수 유효성 검사 후 저장.
     * 프론트엔드 Board.vue에서 POST /api/board 요청
     * BoardService.java의 write() 호출
     */
    @PostMapping
    public ResponseEntity<?> write(@RequestBody BoardVO board) {
        // 제목 빈 값 검사
        if (board.getTitle() == null || board.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        // 내용 빈 값 검사
        if (board.getContent() == null || board.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        }
        // BoardService.java의 write()로 게시글 DB 저장
        return ResponseEntity.ok(boardService.write(board));
    }

    /**
     * 게시글 수정 엔드포인트. 작성자 본인만 수정 가능.
     * 프론트엔드 PostDetailCard.vue에서 PUT /api/board/{id}?requesterId={id} 요청
     * BoardService.java의 getDetail(), update() 호출
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestParam String requesterId,
                                    @RequestBody Map<String, String> body) {
        // BoardService.java의 getDetail()로 게시글 존재 여부 확인
        BoardVO board = boardService.getDetail(id);
        if (board == null) return ResponseEntity.notFound().build();
        // 작성자 본인 여부 검사
        if (!board.getAuthorId().equals(requesterId)) {
            return ResponseEntity.status(403).body(Map.of("message", "수정 권한이 없습니다."));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        String tags = body.getOrDefault("tags", "");
        // BoardService.java의 update()로 제목/내용/태그 업데이트
        boardService.update(id, title, content, tags);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 게시글 삭제 엔드포인트. 작성자 본인 또는 관리자만 삭제 가능.
     * 프론트엔드 PostDetailCard.vue에서 DELETE /api/board/{id}?requesterId={id} 요청
     * BoardService.java의 delete(), UserService.java의 isAdmin() 호출
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String requesterId) {
        BoardVO board = boardService.getDetail(id);
        if (board == null) return ResponseEntity.notFound().build();
        // UserService.java의 isAdmin()으로 관리자 여부 확인
        boolean isAdmin = userService.isAdmin(requesterId);
        // 작성자도 아니고 관리자도 아니면 403 반환
        if (!board.getAuthorId().equals(requesterId) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "삭제 권한이 없습니다."));
        }
        // BoardService.java의 delete()로 게시글 삭제
        boardService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 게시글 필독 여부 설정 엔드포인트. 관리자 또는 팀장만 설정 가능.
     * 프론트엔드에서 PATCH /api/board/{id}/required?requesterId={id} 요청
     * BoardService.java의 setRequired() 호출
     */
    @PatchMapping("/{id}/required")
    public ResponseEntity<?> setRequired(@PathVariable String id,
                                         @RequestParam String requesterId,
                                         @RequestBody Map<String, Integer> body) {
        int value = body.getOrDefault("isRequired", 0);
        // BoardService.java의 setRequired()로 필독 여부 업데이트
        boardService.setRequired(id, value);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 게시글 첨부파일 업로드 엔드포인트. 파일을 서버 디스크에 저장하고 메타데이터를 DB에 기록.
     * 프론트엔드 PostDetailCard.vue에서 POST /api/board/{id}/files 요청
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
