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

@Service
public class VacationService {

    @Autowired
    private VacationMapper mapper;

    @Autowired
    private UserService userService;

    public VacationVO save(VacationVO vacation) {
        double deduct = calcDeductDays(vacation.getType(), vacation.getStartDate(), vacation.getEndDate());
        if (deduct > 0) {
            com.vibe.model.UserVO user = userService.getUser(vacation.getUserId());
            double remaining = user != null && user.getRemainingVacation() != null ? user.getRemainingVacation() : 0;
            if (remaining < deduct) {
                throw new IllegalStateException("잔여 연차가 부족합니다. (잔여: " + remaining + "일, 필요: " + deduct + "일)");
            }
            userService.deductVacation(vacation.getUserId(), deduct);
        }
        vacation.setId(UUID.randomUUID().toString());
        vacation.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(vacation);
        return vacation;
    }

    public List<VacationVO> getByUserId(String userId) {
        return mapper.findByUserId(userId);
    }

    public void delete(String id) {
        VacationVO vacation = mapper.findById(id);
        mapper.delete(id);
        if (vacation != null) {
            double restore = calcDeductDays(vacation.getType(), vacation.getStartDate(), vacation.getEndDate());
            if (restore > 0) {
                userService.restoreVacation(vacation.getUserId(), restore);
            }
        }
    }

    private double calcDeductDays(String type, String startDate, String endDate) {
        if (type == null) return 0;
        if (type.equals("반차(오전)") || type.equals("반차(오후)")) return 0.5;
        if (type.equals("연차")) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                return ChronoUnit.DAYS.between(start, end) + 1;
            } catch (Exception e) {
                return 1;
            }
        }
        return 0; // 병가, 기타는 차감 없음
    }
}
