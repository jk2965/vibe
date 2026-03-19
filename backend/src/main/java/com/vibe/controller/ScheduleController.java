package com.vibe.controller;

import com.vibe.model.ScheduleVO;
import com.vibe.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 일정(Schedule) CRUD REST API 컨트롤러.
 * 기본 경로: /api/schedule
 * 연관 파일: ScheduleService.java (일정 비즈니스 로직), ScheduleMapper.xml (DB 쿼리),
 *           ScheduleVO.java (데이터 모델),
 *           프론트엔드 Calendar.vue에서 GET/POST/PUT/DELETE /api/schedule 요청
 * @CrossOrigin 미적용: Vue 개발 서버 프록시 또는 동일 origin 정책 사용
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    // ScheduleService.java를 통해 일정 CRUD 처리 (생성자 주입 방식 사용)
    private final ScheduleService scheduleService;

    /**
     * 생성자 주입 방식으로 ScheduleService.java를 주입받는다.
     */
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 일정 목록 조회 엔드포인트. 마스터(adminLevel>=2)는 전체, 그 외는 팀별 일정만 조회.
     * 프론트엔드 Calendar.vue에서 GET /api/schedule?adminLevel={n}&team={팀명} 요청
     * ScheduleService.java의 getAll() 또는 getByTeam() 호출
     */
    @GetMapping
    public ResponseEntity<List<ScheduleVO>> list(
            @RequestParam(defaultValue = "0") int adminLevel,
            @RequestParam(required = false) String team) {
        if (adminLevel >= 2) {
            // 마스터 권한: ScheduleService.java의 getAll()로 전체 일정 반환
            return ResponseEntity.ok(scheduleService.getAll());
        }
        // 일반/팀장/관리자: ScheduleService.java의 getByTeam()으로 해당 팀 일정만 반환
        return ResponseEntity.ok(scheduleService.getByTeam(team != null ? team : ""));
    }

    /**
     * 일정 생성 엔드포인트. 제목 빈 값 검사 후 저장.
     * 프론트엔드 Calendar.vue에서 POST /api/schedule 요청
     * ScheduleService.java의 create() 호출
     */
    @PostMapping
    public ResponseEntity<ScheduleVO> create(@RequestBody ScheduleVO schedule) {
        // 제목 빈 값 검사
        if (schedule.getTitle() == null || schedule.getTitle().isBlank())
            return ResponseEntity.badRequest().build();
        // ScheduleService.java의 create()로 일정 DB 저장
        return ResponseEntity.ok(scheduleService.create(schedule));
    }

    /**
     * 일정 수정 엔드포인트. 일정 등록자 본인만 수정 가능.
     * 프론트엔드 Calendar.vue에서 PUT /api/schedule/{id}?userId={id} 요청
     * ScheduleService.java의 findById(), update() 호출
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestParam String userId,
                                    @RequestBody ScheduleVO schedule) {
        // ScheduleService.java의 findById()로 일정 존재 여부 확인
        ScheduleVO existing = scheduleService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        // 등록자 본인 여부 검사
        if (!existing.getUserId().equals(userId))
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
        // ScheduleService.java의 update()로 일정 업데이트
        scheduleService.update(id, schedule);
        return ResponseEntity.ok().build();
    }

    /**
     * 일정 삭제 엔드포인트. 일정 등록자 본인만 삭제 가능.
     * 프론트엔드 Calendar.vue에서 DELETE /api/schedule/{id}?userId={id} 요청
     * ScheduleService.java의 findById(), delete() 호출
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String userId) {
        // ScheduleService.java의 findById()로 일정 존재 여부 확인
        ScheduleVO existing = scheduleService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        // 등록자 본인 여부 검사
        if (!existing.getUserId().equals(userId))
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
        // ScheduleService.java의 delete()로 일정 삭제
        scheduleService.delete(id);
        return ResponseEntity.ok().build();
    }
}
