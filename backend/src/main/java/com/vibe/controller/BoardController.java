package com.vibe.controller;

import com.github.pagehelper.PageInfo;
import com.vibe.model.BoardVO;
import com.vibe.service.AbstractBoardService;
import com.vibe.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 자유게시판(Board) REST API 컨트롤러.
 * AbstractBoardController로부터 getDetail, update, delete, setRequired, uploadFile을 상속.
 * 이 클래스에서는 getList, write, 권한 훅만 정의.
 */
@RestController
@RequestMapping("/api/board")
@CrossOrigin(origins = "http://localhost:8080")
public class BoardController extends AbstractBoardController<BoardVO> {

    @Autowired private BoardService boardService;

    /** AbstractBoardController가 사용할 서비스 반환 (Template Method 패턴 훅) */
    @Override
    protected AbstractBoardService<BoardVO> getService() { return boardService; }

    /**
     * 작성 권한 훅 (Template Method 패턴).
     * 자유게시판은 누구나 작성 가능.
     */
    @Override
    protected boolean canWrite(String requesterId, BoardVO item) { return true; }

    /** 게시글 목록 페이징 조회 */
    @GetMapping
    public PageInfo<BoardVO> getList(@RequestParam(defaultValue = "1") int pageNum) {
        return boardService.getList(pageNum);
    }

    /** 게시글 작성 (권한 검사 없음 — 누구나 작성 가능) */
    @PostMapping
    public ResponseEntity<?> write(@RequestBody BoardVO board) {
        if (board.getTitle() == null || board.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (board.getContent() == null || board.getContent().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        return ResponseEntity.ok(boardService.write(board));
    }
}
