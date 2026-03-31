package com.vibe.controller;

import com.vibe.model.TeamNoticeVO;
import com.vibe.model.UserVO;
import com.vibe.service.AbstractBoardService;
import com.vibe.service.TeamNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 팀 공지사항(TeamNotice) REST API 컨트롤러.
 * AbstractBoardController로부터 공통 CRUD를 상속.
 * getList, getDetail은 팀 소속 검사가 필요하므로 오버라이드.
 * 작성 권한: 같은 팀 팀장/관리자 또는 마스터(adminLevel=2).
 */
@RestController
@RequestMapping("/api/team-notice")
@CrossOrigin(origins = "http://localhost:8080")
public class TeamNoticeController extends AbstractBoardController<TeamNoticeVO> {

    @Autowired private TeamNoticeService teamNoticeService;

    /** AbstractBoardController가 사용할 서비스 반환 (Template Method 패턴 훅) */
    @Override
    protected AbstractBoardService<TeamNoticeVO> getService() { return teamNoticeService; }

    /**
     * 작성 권한 훅 (Template Method 패턴).
     * 같은 팀 팀장/관리자 또는 마스터(adminLevel=2)만 작성 가능.
     */
    @Override
    protected boolean canWrite(String requesterId, TeamNoticeVO item) {
        int adminLevel = userService.getAdminLevel(requesterId);
        if (adminLevel >= 2) return true;
        UserVO requester = userService.getUser(requesterId);
        boolean isSameTeam = requester != null && item.getTeam().equals(requester.getTeam());
        return isSameTeam && (adminLevel >= 1 || userService.isTeamLeader(requesterId));
    }

    /** 팀 소속 검사 포함 목록 조회 */
    @GetMapping
    public ResponseEntity<?> getList(@RequestParam String team,
                                     @RequestParam String requesterId,
                                     @RequestParam(defaultValue = "1") int pageNum) {
        if (userService.getAdminLevel(requesterId) < 2) {
            UserVO requester = userService.getUser(requesterId);
            if (requester == null || !team.equals(requester.getTeam()))
                return ResponseEntity.status(403).body(Map.of("message", "해당 팀 공지사항에 접근 권한이 없습니다."));
        }
        return ResponseEntity.ok(teamNoticeService.getListByTeam(team, pageNum));
    }

    /** 팀 소속 검사 포함 상세 조회 (추상 클래스 오버라이드) */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable String id,
                                       @RequestParam(required = false) String requesterId) {
        TeamNoticeVO notice = teamNoticeService.getDetail(id);
        if (notice == null) return ResponseEntity.notFound().build();
        if (requesterId != null && userService.getAdminLevel(requesterId) < 2) {
            UserVO requester = userService.getUser(requesterId);
            if (requester == null || !notice.getTeam().equals(requester.getTeam()))
                return ResponseEntity.status(403).body(Map.of("message", "접근 권한이 없습니다."));
        }
        return ResponseEntity.ok(notice);
    }

    /** 팀 공지사항 작성 */
    @PostMapping
    public ResponseEntity<?> write(@RequestParam String requesterId,
                                   @RequestBody TeamNoticeVO notice) {
        if (notice.getTitle() == null || notice.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        if (notice.getContent() == null || notice.getContent().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "내용을 입력하세요."));
        if (notice.getTeam() == null || notice.getTeam().isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "팀을 선택하세요."));
        if (!canWrite(requesterId, notice))
            return ResponseEntity.status(403).body(Map.of("message", "글쓰기 권한이 없습니다."));
        return ResponseEntity.ok(teamNoticeService.write(notice));
    }
}
