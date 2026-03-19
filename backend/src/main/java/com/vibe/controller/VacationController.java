package com.vibe.controller;

import com.vibe.model.UserVO;
import com.vibe.model.VacationVO;
import com.vibe.service.UserService;
import com.vibe.service.VacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 연차/휴가(Vacation) 조회/신청/취소 REST API 컨트롤러.
 * 기본 경로: /api/vacation
 * 연관 파일: VacationService.java (연차 신청 및 잔여 연차 차감 로직),
 *           UserService.java (잔여 연차 조회), VacationMapper.xml (DB 쿼리),
 *           프론트엔드 Vacation.vue에서 GET/POST/DELETE /api/vacation 요청
 */
@RestController
@RequestMapping("/api/vacation")
@CrossOrigin(origins = "http://localhost:8080")
public class VacationController {

    // VacationService.java를 통해 연차 신청/조회/삭제 처리
    @Autowired
    private VacationService service;

    // UserService.java를 통해 사용자 잔여 연차 정보 조회
    @Autowired
    private UserService userService;

    /**
     * 특정 사용자의 연차 신청 목록 조회 엔드포인트.
     * 프론트엔드 Vacation.vue에서 GET /api/vacation?userId={id} 요청
     * VacationService.java의 getByUserId() 호출
     */
    @GetMapping
    public List<VacationVO> getVacations(@RequestParam String userId) {
        // VacationService.java의 getByUserId()로 해당 사용자의 연차 목록 반환
        return service.getByUserId(userId);
    }

    /**
     * 사용자의 잔여 연차 및 직책 조회 엔드포인트.
     * 프론트엔드 Vacation.vue에서 GET /api/vacation/remaining?userId={id} 요청
     * UserService.java의 getUser()로 사용자 정보(잔여연차, 직책) 조회
     */
    @GetMapping("/remaining")
    public ResponseEntity<?> getRemaining(@RequestParam String userId) {
        // UserService.java의 getUser()로 사용자 정보 조회 (없으면 404 반환)
        UserVO user = userService.getUser(userId);
        if (user == null) return ResponseEntity.notFound().build();
        // 잔여 연차(remainingVacation)와 직책(position) 반환
        return ResponseEntity.ok(Map.of(
            "remainingVacation", user.getRemainingVacation() != null ? user.getRemainingVacation() : 0,
            "position", user.getPosition() != null ? user.getPosition() : ""
        ));
    }

    /**
     * 연차 신청 엔드포인트. 잔여 연차 부족 시 예외 처리하여 400 반환.
     * 프론트엔드 Vacation.vue에서 POST /api/vacation 요청
     * VacationService.java의 save() 호출 (내부적으로 잔여 연차 차감 처리)
     */
    @PostMapping
    public ResponseEntity<?> createVacation(@RequestBody VacationVO vacation) {
        try {
            // VacationService.java의 save()로 연차 신청 저장 (잔여 연차 부족 시 IllegalStateException 발생)
            return ResponseEntity.ok(service.save(vacation));
        } catch (IllegalStateException e) {
            // 잔여 연차 부족 등 비즈니스 예외 발생 시 400 반환
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 연차 신청 취소(삭제) 엔드포인트. 잔여 연차를 복원한다.
     * 프론트엔드 Vacation.vue에서 DELETE /api/vacation/{id} 요청
     * VacationService.java의 delete() 호출 (내부적으로 잔여 연차 복원 처리)
     */
    @DeleteMapping("/{id}")
    public void deleteVacation(@PathVariable String id) {
        // VacationService.java의 delete()로 연차 신청 삭제 및 잔여 연차 복원
        service.delete(id);
    }
}
