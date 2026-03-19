package com.vibe.service;

import com.github.pagehelper.Page;
import com.vibe.model.AttendanceRecordVO;
import com.vibe.model.AttendanceRecordSearchParamVO;
import com.vibe.mapper.AttendanceRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * 근태 기록(AttendanceRecord) 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * AttendanceRecordController.java에서 호출되며, AttendanceRecordMapper.java를 통해 DB와 연동됨.
 * 직원의 출퇴근 기록 조회, 등록, 수정 기능을 제공함.
 * 관리자(HR)만 전체 조회 및 수정이 가능하도록 컨트롤러에서 권한 체크됨.
 */
@Service
public class AttendanceRecordService {

    // AttendanceRecordMapper.java를 통해 ATTENDANCE_RECORD 테이블 CRUD 수행
    @Autowired
    private AttendanceRecordMapper mapper;

    /**
     * 검색 파라미터(날짜 범위, 사용자 ID 등)를 기반으로 근태 기록 목록 조회.
     * AttendanceRecordSearchParamVO.java에 검색 조건이 담겨 있음.
     * AttendanceRecordMapper.java의 getAttendanceRecordList()로 조건 조회.
     * PageHelper의 Page 객체를 반환하여 페이징 정보 포함.
     */
    public Page<AttendanceRecordVO> getAllRecords(AttendanceRecordSearchParamVO param) {
        return mapper.getAttendanceRecordList(param);
    }

    /**
     * 새 근태 기록 등록: UUID로 고유 ID 생성 후 DB에 저장.
     * AttendanceRecordController.java에서 출퇴근 기록 시 호출.
     * AttendanceRecordMapper.java의 insertAttendanceRecord()로 ATTENDANCE_RECORD 테이블에 등록.
     */
    public AttendanceRecordVO saveRecord(AttendanceRecordVO record) {
        // UUID를 사용해 유일한 근태 기록 ID 생성
        record.setId(UUID.randomUUID().toString());
        mapper.insertAttendanceRecord(record);
        return record;
    }

    /**
     * 기존 근태 기록 수정: 수정된 출퇴근 시각 등을 DB에 업데이트.
     * AttendanceRecordController.java에서 관리자가 기록 수정 시 호출.
     * AttendanceRecordMapper.java의 updateAttendanceRecord()로 해당 레코드 수정.
     */
    public AttendanceRecordVO updateRecord(AttendanceRecordVO record) {
        mapper.updateAttendanceRecord(record);
        return record;
    }
}
