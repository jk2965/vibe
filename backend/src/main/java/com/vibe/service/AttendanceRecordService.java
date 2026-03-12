package com.vibe.service;

import com.github.pagehelper.Page;
import com.vibe.model.AttendanceRecordVO;
import com.vibe.model.AttendanceRecordSearchParamVO;
import com.vibe.mapper.AttendanceRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AttendanceRecordService {

    @Autowired
    private AttendanceRecordMapper mapper;

    public Page<AttendanceRecordVO> getAllRecords(AttendanceRecordSearchParamVO param) {
        return mapper.getAttendanceRecordList(param);
    }

    public AttendanceRecordVO saveRecord(AttendanceRecordVO record) {
        record.setId(UUID.randomUUID().toString());
        mapper.insertAttendanceRecord(record);
        return record;
    }

    public AttendanceRecordVO updateRecord(AttendanceRecordVO record) {
        mapper.updateAttendanceRecord(record);
        return record;
    }
}
