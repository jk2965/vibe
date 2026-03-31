package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.NoticeVO;
import com.vibe.service.AbstractBoardService;
import com.vibe.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 전사 공지사항(Notice) REST API 컨트롤러.
 * AbstractBoardController로부터 공통 CRUD를 상속.
 * 작성 권한: 관리자(isAdmin) 또는 팀장(isTeamLeader)만 가능.
 */
@RestController
@RequestMapping("/api/notice")
@CrossOrigin(origins = "http://localhost:8080")
public class NoticeController extends AbstractBoardController<NoticeVO> {

    @Autowired private NoticeService noticeService;

    /** AbstractBoardController가 사용할 서비스 반환 (Template Method 패턴 훅) */
    @Override
    protected AbstractBoardService<NoticeVO> getService() { return noticeService; }

    /**
     * 작성 권한 훅 (Template Method 패턴).
     * 공지사항은 관리자 또는 팀장만 작성 가능.
     */
    @Override
    protected boolean canWrite(String requesterId, NoticeVO item) {
        return userService.isAdmin(requesterId) || userService.isTeamLeader(requesterId);
    }

    /** 공지사항 목록 페이징 조회 */
    @GetMapping
    public PageInfo<NoticeVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return noticeService.getList(pageNum);
    }

    /** 공지사항 작성 (관리자/팀장만 가능) */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId, @RequestBody NoticeVO notice) {
        if (notice.getTitle() == null || notice.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (notice.getContent() == null || notice.getContent().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        if (!canWrite(requesterId, notice))
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다. 팀장 또는 관리자만 작성할 수 있습니다."));
        return ResponseEntity.ok(noticeService.write(notice));
    }
}
