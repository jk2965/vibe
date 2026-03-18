package com.vibe.controller;

import com.vibe.model.ScheduleVO;
import com.vibe.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleVO>> list(
            @RequestParam(defaultValue = "0") int adminLevel,
            @RequestParam(required = false) String team) {
        if (adminLevel >= 2) {
            return ResponseEntity.ok(scheduleService.getAll());
        }
        return ResponseEntity.ok(scheduleService.getByTeam(team != null ? team : ""));
    }

    @PostMapping
    public ResponseEntity<ScheduleVO> create(@RequestBody ScheduleVO schedule) {
        if (schedule.getTitle() == null || schedule.getTitle().isBlank())
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(scheduleService.create(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestParam String userId,
                                    @RequestBody ScheduleVO schedule) {
        ScheduleVO existing = scheduleService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        if (!existing.getUserId().equals(userId))
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
        scheduleService.update(id, schedule);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam String userId) {
        ScheduleVO existing = scheduleService.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        if (!existing.getUserId().equals(userId))
            return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
        scheduleService.delete(id);
        return ResponseEntity.ok().build();
    }
}
