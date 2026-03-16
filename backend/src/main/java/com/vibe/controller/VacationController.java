package com.vibe.controller;

import com.vibe.model.UserVO;
import com.vibe.model.VacationVO;
import com.vibe.service.UserService;
import com.vibe.service.VacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vacation")
@CrossOrigin(origins = "http://localhost:8080")
public class VacationController {

    @Autowired
    private VacationService service;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<VacationVO> getVacations(@RequestParam String userId) {
        return service.getByUserId(userId);
    }

    @GetMapping("/remaining")
    public ResponseEntity<?> getRemaining(@RequestParam String userId) {
        UserVO user = userService.getUser(userId);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of(
            "remainingVacation", user.getRemainingVacation() != null ? user.getRemainingVacation() : 0,
            "position", user.getPosition() != null ? user.getPosition() : ""
        ));
    }

    @PostMapping
    public ResponseEntity<?> createVacation(@RequestBody VacationVO vacation) {
        try {
            return ResponseEntity.ok(service.save(vacation));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public void deleteVacation(@PathVariable String id) {
        service.delete(id);
    }
}
