package com.vibe.controller;

import com.vibe.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 게시글 통합 검색 REST API 컨트롤러.
 * 기본 경로: /api/search
 * 5개 게시판(공지사항, 팀 공지사항, 자유게시판, 전사 자료실, 팀 자료실)에서
 * 제목(title) 또는 태그(tag) 기준으로 게시글을 검색하여 boardType별로 묶어 반환한다.
 * 연관 파일: SearchResult.vue (프론트엔드), 5개 Mapper XML (searchByTitle/searchByTag 쿼리)
 */
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:8080")
public class SearchController {

    // 자유 게시판 검색용 매퍼 (BoardMapper.xml의 searchByTitle/searchByTag 쿼리 사용)
    @Autowired private BoardMapper boardMapper;
    // 전체 공지사항 검색용 매퍼 (NoticeMapper.xml의 searchByTitle/searchByTag 쿼리 사용)
    @Autowired private NoticeMapper noticeMapper;
    // 팀별 공지사항 검색용 매퍼 (TeamNoticeMapper.xml의 searchByTitle/searchByTag 쿼리 사용)
    @Autowired private TeamNoticeMapper teamNoticeMapper;
    // 전사 자료실 검색용 매퍼 (ArchiveMapper.xml의 searchByTitle/searchByTag 쿼리 사용)
    @Autowired private ArchiveMapper archiveMapper;
    // 팀별 자료실 검색용 매퍼 (TeamArchiveMapper.xml의 searchByTitle/searchByTag 쿼리 사용)
    @Autowired private TeamArchiveMapper teamArchiveMapper;

    /**
     * 게시글 통합 검색 엔드포인트.
     * 프론트엔드 SearchResult.vue에서 GET /api/search?type={title|tag}&keyword={검색어} 요청.
     * type=title : 5개 게시판 전체에서 제목 기준 부분 일치 검색 (LIKE '%keyword%')
     * type=tag   : 5개 게시판 전체에서 태그 기준 부분 일치 검색 (LIKE '%keyword%')
     * 응답: boardType별 검색 결과 목록 (notice, teamNotice, board, archive, teamArchive)
     */
    @GetMapping
    public ResponseEntity<?> search(@RequestParam String type, @RequestParam String keyword) {
        // 검색어가 비어 있으면 오류 반환
        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "검색어를 입력하세요."));
        }

        // 검색 결과를 boardType별로 담을 LinkedHashMap (삽입 순서 유지로 UI 표시 순서 고정)
        Map<String, Object> result = new LinkedHashMap<>();

        if ("title".equals(type)) {
            // 제목 기준 검색: 각 Mapper의 searchByTitle() 호출
            result.put("notice",      noticeMapper.searchByTitle(keyword));
            result.put("teamNotice",  teamNoticeMapper.searchByTitle(keyword));
            result.put("board",       boardMapper.searchByTitle(keyword));
            result.put("archive",     archiveMapper.searchByTitle(keyword));
            result.put("teamArchive", teamArchiveMapper.searchByTitle(keyword));
        } else if ("tag".equals(type)) {
            // 태그 기준 검색: 각 Mapper의 searchByTag() 호출 (tags 컬럼 부분 일치)
            result.put("notice",      noticeMapper.searchByTag(keyword));
            result.put("teamNotice",  teamNoticeMapper.searchByTag(keyword));
            result.put("board",       boardMapper.searchByTag(keyword));
            result.put("archive",     archiveMapper.searchByTag(keyword));
            result.put("teamArchive", teamArchiveMapper.searchByTag(keyword));
        } else {
            // 허용되지 않는 type 값 요청 시 400 반환
            return ResponseEntity.badRequest().body(Map.of("message", "검색 타입은 title 또는 tag만 허용됩니다."));
        }

        return ResponseEntity.ok(result);
    }
}
