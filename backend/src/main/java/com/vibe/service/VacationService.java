package com.vibe.service;

import com.vibe.mapper.VacationMapper;
import com.vibe.model.VacationVO;
import com.vibe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * 휴가(Vacation) 신청 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * VacationController.java에서 호출되며, VacationMapper.java를 통해 DB와 연동됨.
 * UserService.java를 통해 연차 잔여일 확인 및 차감/복구 처리를 수행함.
 * 연차, 반차(오전/오후), 병가, 기타 휴가 유형을 처리하며 유형에 따라 차감일이 달라짐.
 */
@Service
public class VacationService {

    // VacationMapper.java를 통해 VACATION 테이블 CRUD 수행
    @Autowired
    private VacationMapper mapper;

    // 연차 차감/복구 및 사용자 정보 조회를 위해 UserService.java를 주입
    @Autowired
    private UserService userService;

    /**
     * 휴가 신청 저장: 유형에 따라 차감일을 계산하고, 잔여 연차 부족 시 예외를 발생시킴.
     * UserService.java의 deductVacation()으로 연차 차감 처리.
     * VacationMapper.java의 insert()로 VACATION 테이블에 등록.
     */
    public VacationVO save(VacationVO vacation) {
        // 휴가 유형과 날짜 범위를 기반으로 차감할 일수 계산
        double deduct = calcDeductDays(vacation.getType(), vacation.getStartDate(), vacation.getEndDate());
        if (deduct > 0) {
            // UserService.java를 통해 현재 잔여 연차 조회
            com.vibe.model.UserVO user = userService.getUser(vacation.getUserId());
            double remaining = user != null && user.getRemainingVacation() != null ? user.getRemainingVacation() : 0;
            // 잔여 연차가 신청 일수보다 부족하면 예외 발생
            if (remaining < deduct) {
                throw new IllegalStateException("잔여 연차가 부족합니다. (잔여: " + remaining + "일, 필요: " + deduct + "일)");
            }
            // UserService.java를 통해 연차 차감 처리
            userService.deductVacation(vacation.getUserId(), deduct);
        }
        // UUID를 사용해 유일한 휴가 신청 ID 생성
        vacation.setId(UUID.randomUUID().toString());
        vacation.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(vacation);
        return vacation;
    }

    /**
     * 특정 사용자의 휴가 신청 목록 조회.
     * VacationMapper.java의 findByUserId()로 해당 사용자의 모든 휴가 내역 조회.
     */
    public List<VacationVO> getByUserId(String userId) {
        return mapper.findByUserId(userId);
    }

    /**
     * 휴가 신청 취소: 해당 휴가를 삭제하고 차감했던 연차를 복구함.
     * VacationMapper.java의 delete()로 VACATION 테이블에서 레코드 삭제.
     * UserService.java의 restoreVacation()으로 차감된 연차 복구.
     */
    public void delete(String id) {
        // 삭제 전에 휴가 정보를 먼저 조회하여 복구할 일수 계산에 사용
        VacationVO vacation = mapper.findById(id);
        mapper.delete(id);
        if (vacation != null) {
            // 취소된 휴가 유형에 따라 복구할 연차 일수 계산
            double restore = calcDeductDays(vacation.getType(), vacation.getStartDate(), vacation.getEndDate());
            if (restore > 0) {
                // UserService.java를 통해 연차 복구 처리
                userService.restoreVacation(vacation.getUserId(), restore);
            }
        }
    }

    /**
     * 휴가 유형과 날짜 범위를 기반으로 차감할 연차 일수를 계산하는 내부 메서드.
     * - 반차(오전)/반차(오후): 0.5일 차감
     * - 연차: 시작일~종료일 사이의 실제 일수 차감 (당일 포함)
     * - 병가, 기타: 차감 없음 (0 반환)
     */
    private double calcDeductDays(String type, String startDate, String endDate) {
        if (type == null) return 0;
        // 반차는 0.5일 차감
        if (type.equals("반차(오전)") || type.equals("반차(오후)")) return 0.5;
        if (type.equals("연차")) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                // 시작일과 종료일 포함한 전체 일수 계산 (+1)
                return ChronoUnit.DAYS.between(start, end) + 1;
            } catch (Exception e) {
                // 날짜 파싱 실패 시 기본값 1일 차감
                return 1;
            }
        }
        return 0; // 병가, 기타는 차감 없음
    }
}
