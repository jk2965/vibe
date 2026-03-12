package com.vibe.controller;

import com.github.pagehelper.Page;
import com.vibe.model.AttendanceRecordVO;
import com.vibe.model.AttendanceRecordSearchParamVO;
import com.vibe.service.AttendanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:8080")
public class AttendanceRecordController {

    @Autowired
    private AttendanceRecordService service;

    @GetMapping
    public Page<AttendanceRecordVO> getAllRecords(AttendanceRecordSearchParamVO param) {
        return service.getAllRecords(param);
    }

    @PostMapping
    public AttendanceRecordVO createRecord(@RequestBody AttendanceRecordVO record) {
        return service.saveRecord(record);
    }

    @PutMapping
    public AttendanceRecordVO updateRecord(@RequestBody AttendanceRecordVO record) {
        return service.updateRecord(record);
    }
}
