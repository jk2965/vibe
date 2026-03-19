package com.vibe.service;

import com.vibe.mapper.ScheduleMapper;
import com.vibe.model.ScheduleVO;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 일정(Schedule) 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * ScheduleController.java에서 호출되며, ScheduleMapper.java를 통해 DB와 연동됨.
 * 전사 일정, 팀별 일정, 개인 일정을 구분하여 조회할 수 있음.
 * 캘린더 화면(프론트엔드 CalendarView.vue)에 데이터를 제공함.
 */
@Service
public class ScheduleService {

    // ScheduleMapper.java를 통해 SCHEDULE 테이블 CRUD 수행 (생성자 주입 방식 사용)
    private final ScheduleMapper scheduleMapper;

    /**
     * 생성자 주입(Constructor Injection) 방식으로 ScheduleMapper를 주입.
     * @Autowired 없이 단일 생성자는 Spring이 자동으로 의존성 주입함.
     */
    public ScheduleService(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    /**
     * 전체 일정 목록 조회 (팀/개인 구분 없이 모든 일정).
     * ScheduleMapper.java의 findAll()로 SCHEDULE 테이블 전체 조회.
     * 관리자 화면에서 전체 일정을 볼 때 사용.
     */
    public List<ScheduleVO> getAll() {
        return scheduleMapper.findAll();
    }

    /**
     * 특정 팀의 일정 목록 조회.
     * ScheduleMapper.java의 findByTeam()으로 팀명(team) 기준 필터링.
     * 팀 캘린더 화면에서 해당 팀의 일정만 표시할 때 사용.
     */
    public List<ScheduleVO> getByTeam(String team) {
        return scheduleMapper.findByTeam(team);
    }

    /**
     * 특정 사용자의 개인 일정 목록 조회.
     * ScheduleMapper.java의 findByUserId()로 사용자 ID 기준 필터링.
     * 개인 캘린더 화면에서 본인 일정만 표시할 때 사용.
     */
    public List<ScheduleVO> getByUserId(String userId) {
        return scheduleMapper.findByUserId(userId);
    }

    /**
     * 새 일정 생성: UUID로 고유 ID 생성, 현재 시각을 작성일로 설정 후 DB에 저장.
     * ScheduleMapper.java의 insert()로 SCHEDULE 테이블에 등록.
     */
    public ScheduleVO create(ScheduleVO schedule) {
        // UUID를 사용해 유일한 일정 ID 생성
        schedule.setId(UUID.randomUUID().toString());
        // 현재 시각을 'yyyy-MM-dd HH:mm:ss' 형식으로 생성일 설정
        schedule.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        scheduleMapper.insert(schedule);
        return schedule;
    }

    /**
     * 일정 수정: 전달받은 ScheduleVO 내용으로 기존 일정 업데이트.
     * URL의 id를 ScheduleVO에 설정하여 대상 레코드를 특정.
     * ScheduleMapper.java의 update()로 SCHEDULE 테이블 수정.
     */
    public void update(String id, ScheduleVO schedule) {
        // URL 경로에서 전달된 id를 ScheduleVO에 주입하여 수정 대상 특정
        schedule.setId(id);
        scheduleMapper.update(schedule);
    }

    /**
     * 일정 삭제: 지정된 ID의 일정을 DB에서 제거.
     * ScheduleMapper.java의 delete()로 SCHEDULE 테이블에서 해당 레코드 삭제.
     */
    public void delete(String id) {
        scheduleMapper.delete(id);
    }

    /**
     * 일정 ID로 단건 조회.
     * ScheduleMapper.java의 findById()로 조회. 수정 전 데이터 확인 등에 활용됨.
     */
    public ScheduleVO findById(String id) {
        return scheduleMapper.findById(id);
    }
}
