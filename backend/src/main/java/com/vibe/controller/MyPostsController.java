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

    @Autowired private BoardMapper boardMapper;
    @Autowired private NoticeMapper noticeMapper;
    @Autowired private TeamNoticeMapper teamNoticeMapper;
    @Autowired private ArchiveMapper archiveMapper;
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
