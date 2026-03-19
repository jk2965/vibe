package com.vibe.controller;

import com.github.pagehelper.Page;
import com.vibe.model.AttendanceRecordVO;
import com.vibe.model.AttendanceRecordSearchParamVO;
import com.vibe.service.AttendanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 근태 기록(AttendanceRecord) 조회/생성/수정 REST API 컨트롤러.
 * 기본 경로: /api/attendance
 * 연관 파일: AttendanceRecordService.java (근태 비즈니스 로직),
 *           AttendanceRecordMapper.xml (DB 쿼리), AttendanceRecordVO.java (데이터 모델),
 *           AttendanceRecordSearchParamVO.java (검색 파라미터 모델),
 *           프론트엔드 Attendance.vue에서 GET/POST/PUT /api/attendance 요청
 */
@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:8080")
public class AttendanceRecordController {

    // AttendanceRecordService.java를 통해 근태 기록 CRUD 처리
    @Autowired
    private AttendanceRecordService service;

    /**
     * 근태 기록 목록 조회 엔드포인트. 검색 파라미터(사용자 ID, 날짜 범위 등)로 필터링.
     * 프론트엔드 Attendance.vue에서 GET /api/attendance?{검색조건} 요청
     * AttendanceRecordService.java의 getAllRecords() 호출
     * AttendanceRecordSearchParamVO.java의 필드가 쿼리 파라미터로 자동 바인딩됨
     */
    @GetMapping
    public Page<AttendanceRecordVO> getAllRecords(AttendanceRecordSearchParamVO param) {
        // AttendanceRecordService.java의 getAllRecords()로 검색 조건에 맞는 근태 기록 페이징 반환
        return service.getAllRecords(param);
    }

    /**
     * 근태 기록 생성 엔드포인트. 출근/퇴근 기록을 새로 생성한다.
     * 프론트엔드 Attendance.vue에서 POST /api/attendance 요청
     * AttendanceRecordService.java의 saveRecord() 호출
     */
    @PostMapping
    public AttendanceRecordVO createRecord(@RequestBody AttendanceRecordVO record) {
        // AttendanceRecordService.java의 saveRecord()로 근태 기록 DB 저장
        return service.saveRecord(record);
    }

    /**
     * 근태 기록 수정 엔드포인트. 기존 근태 기록(출근/퇴근 시간 등)을 수정한다.
     * 프론트엔드 Attendance.vue에서 PUT /api/attendance 요청
     * AttendanceRecordService.java의 updateRecord() 호출
     */
    @PutMapping
    public AttendanceRecordVO updateRecord(@RequestBody AttendanceRecordVO record) {
        // AttendanceRecordService.java의 updateRecord()로 근태 기록 업데이트
        return service.updateRecord(record);
    }
}
