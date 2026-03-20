package com.vibe.controller;

import com.vibe.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 내 게시글 통합 조회 REST API 컨트롤러.
 * 기본 경로: /api/my-posts
 * 모든 게시판(공지사항, 팀 공지사항, 자유게시판, 전사 자료실, 팀 자료실)에서
 * 로그인 사용자가 작성한 게시글을 boardType별로 묶어 한 번에 반환한다.
 */
@RestController
@RequestMapping("/api/my-posts")
@CrossOrigin(origins = "http://localhost:8080")
public class MyPostsController {

    // 자유 게시판 내 게시글 조회용 매퍼 (BoardMapper.xml의 findByAuthor 쿼리 사용)
    @Autowired private BoardMapper boardMapper;
    // 전체 공지사항 내 게시글 조회용 매퍼 (NoticeMapper.xml의 findByAuthor 쿼리 사용)
    @Autowired private NoticeMapper noticeMapper;
    // 팀별 공지사항 내 게시글 조회용 매퍼 (TeamNoticeMapper.xml의 findByAuthor 쿼리 사용)
    @Autowired private TeamNoticeMapper teamNoticeMapper;
    // 전사 자료실 내 게시글 조회용 매퍼 (ArchiveMapper.xml의 findByAuthor 쿼리 사용)
    @Autowired private ArchiveMapper archiveMapper;
    // 팀별 자료실 내 게시글 조회용 매퍼 (TeamArchiveMapper.xml의 findByAuthor 쿼리 사용)
    @Autowired private TeamArchiveMapper teamArchiveMapper;

    /**
     * 내 게시글 통합 조회 엔드포인트.
     * 프론트엔드에서 GET /api/my-posts?userId={id} 요청
     * 응답: boardType별 내 게시글 목록 (notice, teamNotice, board, archive, teamArchive)
     */
    @GetMapping
    public ResponseEntity<?> getMyPosts(@RequestParam String userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notice",      noticeMapper.findByAuthor(userId));
        result.put("teamNotice",  teamNoticeMapper.findByAuthor(userId));
        result.put("board",       boardMapper.findByAuthor(userId));
        result.put("archive",     archiveMapper.findByAuthor(userId));
        result.put("teamArchive", teamArchiveMapper.findByAuthor(userId));
        return ResponseEntity.ok(result);
    }
}
