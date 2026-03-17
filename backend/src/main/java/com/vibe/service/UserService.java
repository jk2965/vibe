package com.vibe.service;

import com.vibe.mapper.UserMapper;
import com.vibe.model.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper mapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean login(String id, String password) {
        UserVO user = mapper.findById(id);
        return user != null && encoder.matches(password, user.getPassword());
    }

    public void register(String id, String username, String password, String position) {
        UserVO user = new UserVO();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setPosition(position != null && !position.isEmpty() ? position : "사원");
        user.setRemainingVacation(15.0);
        mapper.insertUser(user);
    }

    public boolean exists(String id) {
        return mapper.findById(id) != null;
    }

    public String getUsername(String id) {
        UserVO user = mapper.findById(id);
        return user != null ? user.getUsername() : null;
    }

    public UserVO getUser(String id) {
        return mapper.findById(id);
    }

    public void deductVacation(String id, double days) {
        UserVO user = mapper.findById(id);
        if (user != null) {
            double remaining = user.getRemainingVacation() != null ? user.getRemainingVacation() : 0;
            user.setRemainingVacation(Math.max(0, remaining - days));
            mapper.updateRemainingVacation(user);
        }
    }

    public void restoreVacation(String id, double days) {
        UserVO user = mapper.findById(id);
        if (user != null) {
            double remaining = user.getRemainingVacation() != null ? user.getRemainingVacation() : 0;
            user.setRemainingVacation(remaining + days);
            mapper.updateRemainingVacation(user);
        }
    }

    public List<UserVO> getAllUsers() {
        return mapper.findAll();
    }

    public void updateUserInfo(String id, String position, Double remainingVacation, Integer isAdmin, String team, Integer isTeamLeader) {
        UserVO user = new UserVO();
        user.setId(id);
        user.setPosition(position);
        user.setRemainingVacation(remainingVacation);
        user.setIsAdmin(isAdmin);
        user.setTeam(team);
        user.setIsTeamLeader(isTeamLeader);
        mapper.updateUserInfo(user);
    }

    public void updateTeam(String id, String team) {
        UserVO user = new UserVO();
        user.setId(id);
        user.setTeam(team);
        mapper.updateTeam(user);
    }

    public boolean isTeamLeader(String id) {
        UserVO user = mapper.findById(id);
        return user != null && Integer.valueOf(1).equals(user.getIsTeamLeader());
    }

    public List<UserVO> getUsersByTeam(String team) {
        return mapper.findByTeam(team);
    }

    public boolean isAdmin(String id) {
        UserVO user = mapper.findById(id);
        return user != null && user.getIsAdmin() != null && user.getIsAdmin() >= 1;
    }

    public boolean isSuperAdmin(String id) {
        UserVO user = mapper.findById(id);
        return user != null && Integer.valueOf(2).equals(user.getIsAdmin());
    }

    public int getAdminLevel(String id) {
        UserVO user = mapper.findById(id);
        return user != null && user.getIsAdmin() != null ? user.getIsAdmin() : 0;
    }

    public boolean hasTeamLeader(String team) {
        return mapper.countTeamLeaderByTeam(team) > 0;
    }
}
