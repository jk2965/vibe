package com.vibe.service;

import com.vibe.mapper.UserMapper;
import com.vibe.model.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper mapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean login(String id, String password) {
        UserVO user = mapper.findById(id);
        return user != null && encoder.matches(password, user.getPassword());
    }

    public void register(String id, String username, String password) {
        UserVO user = new UserVO();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        mapper.insertUser(user);
    }

    public boolean exists(String id) {
        return mapper.findById(id) != null;
    }

    public String getUsername(String id) {
        UserVO user = mapper.findById(id);
        return user != null ? user.getUsername() : null;
    }
}
