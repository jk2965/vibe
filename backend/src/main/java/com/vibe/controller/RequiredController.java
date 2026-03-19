package com.vibe.controller;

import com.vibe.mapper.*;
import com.vibe.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 필독(isRequired) 게시글 통합 조회 REST API 컨트롤러.
 * 기본 경로: /api/required
 * 모든 게시판(공지사항, 팀 공지사항, 자유게시판, 전사 자료실, 팀 자료실)의
 * 필독 게시글을 boardType별로 묶어 한 번에 반환한다.
 * 팀별 게시판(teamNotice, teamArchive)은 userId로 소속 팀을 확인 후 해당 팀 것만 반환.
 * 마스터 관리자(isAdmin >= 2)는 모든 팀의 필독 게시글을 받는다.
 */
@RestController
@RequestMapping("/api/required")
@CrossOrigin(origins = "http://localhost:8080")
public class RequiredController {

    // 각 게시판 Mapper를 직접 주입하여 findRequired() 호출
    @Autowired private BoardMapper boardMapper;
    @Autowired private NoticeMapper noticeMapper;
    @Autowired private TeamNoticeMapper teamNoticeMapper;
    @Autowired private ArchiveMapper archiveMapper;
    @Autowired private TeamArchiveMapper teamArchiveMapper;

    // 사용자 팀 및 관리자 레벨 조회를 위해 UserMapper 주입
    @Autowired private UserMapper userMapper;

    /**
     * 필독 게시글 통합 조회 엔드포인트.
     * 프론트엔드에서 GET /api/required?userId={id} 요청
     * 응답: boardType별 필독 목록 (notice, teamNotice, board, archive, teamArchive)
     */
    @GetMapping
    public ResponseEntity<?> getRequired(@RequestParam String userId) {
        // UserMapper.findById()로 요청 사용자 정보 조회
        UserVO user = userMapper.findById(userId);
        Map<String, Object> result = new LinkedHashMap<>();

        // 전사 공지사항 필독 — 모든 사용자에게 동일하게 반환
        result.put("notice", noticeMapper.findRequired());

        // 팀별 공지사항 필독 — 소속 팀 필터링 또는 마스터 관리자 전체 반환
        if (user != null && user.getIsAdmin() != null && user.getIsAdmin() >= 2) {
            // 마스터 관리자(isAdmin=2)는 전체 팀 공지사항 필독 조회
            result.put("teamNotice", teamNoticeMapper.findRequired());
        } else if (user != null && user.getTeam() != null && !user.getTeam().isEmpty()) {
            // 일반 사용자는 자신의 팀 공지사항만 필터링
            List<TeamNoticeVO> teamNotices = teamNoticeMapper.findRequired()
                    .stream()
                    .filter(n -> user.getTeam().equals(n.getTeam()))
                    .collect(Collectors.toList());
            result.put("teamNotice", teamNotices);
        } else {
            result.put("teamNotice", List.of());
        }

        // 자유게시판 필독 — 모든 사용자에게 동일하게 반환
        result.put("board", boardMapper.findRequired());

        // 전사 자료실 필독 — 모든 사용자에게 동일하게 반환
        result.put("archive", archiveMapper.findRequired());

        // 팀별 자료실 필독 — 소속 팀 필터링 또는 마스터 관리자 전체 반환
        if (user != null && user.getIsAdmin() != null && user.getIsAdmin() >= 2) {
            // 마스터 관리자(isAdmin=2)는 전체 팀 자료실 필독 조회
            result.put("teamArchive", teamArchiveMapper.findRequired());
        } else if (user != null && user.getTeam() != null && !user.getTeam().isEmpty()) {
            // 일반 사용자는 자신의 팀 자료실만 필터링
            List<TeamArchiveVO> teamArchives = teamArchiveMapper.findRequired()
                    .stream()
                    .filter(a -> user.getTeam().equals(a.getTeam()))
                    .collect(Collectors.toList());
            result.put("teamArchive", teamArchives);
        } else {
            result.put("teamArchive", List.of());
        }

        return ResponseEntity.ok(result);
    }
}
