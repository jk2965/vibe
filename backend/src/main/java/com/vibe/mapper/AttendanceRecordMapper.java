package com.vibe.mapper;

import com.github.pagehelper.Page;
import com.vibe.model.AttendanceRecordVO;
import com.vibe.model.AttendanceRecordSearchParamVO;

import org.apache.ibatis.annotations.Mapper;
import java.util.*;

//mapper 파일 예시, mapper.xml 파일과 매핑됨
@Mapper
public interface AttendanceRecordMapper {

    Page<AttendanceRecordVO> getAttendanceRecordList(AttendanceRecordSearchParamVO param);
    //AttendanceRecordService 파일에 선언한대로 선언

    void insertAttendanceRecord(AttendanceRecordVO record);

    void updateAttendanceRecord(AttendanceRecordVO record);
}
