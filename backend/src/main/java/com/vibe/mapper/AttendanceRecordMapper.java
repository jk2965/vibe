package com.vibe.mapper;

import com.github.pagehelper.Page;
import com.vibe.model.AttendanceRecordVO;
import com.vibe.model.AttendanceRecordSearchParamVO;

import org.apache.ibatis.annotations.Mapper;
import java.util.*;

// AttendanceRecordMapper.xml과 연결됨 — 근태 기록 관련 DB 접근 인터페이스
// AttendanceRecordService.java에서 호출됨 / PageHelper 라이브러리를 통한 페이징 처리 지원
@Mapper
public interface AttendanceRecordMapper {

    // 검색 조건(사용자, 날짜 범위, 유형 등)에 따른 근태 기록 목록을 페이징 조회
    // AttendanceRecordService 파일에 선언한대로 선언
    Page<AttendanceRecordVO> getAttendanceRecordList(AttendanceRecordSearchParamVO param);

    // 새 근태 기록(출근, 퇴근, 외출 등)을 DB에 등록
    void insertAttendanceRecord(AttendanceRecordVO record);

    // 기존 근태 기록을 수정 (오기입 정정, HR 관리자 수정 등)
    void updateAttendanceRecord(AttendanceRecordVO record);
}
