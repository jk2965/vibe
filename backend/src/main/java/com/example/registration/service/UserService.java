package com.example.registration.service;

import com.example.registration.dto.RegisterRequest;
import com.example.registration.dto.RegisterResponse;
import com.example.registration.model.User;
import com.example.registration.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        User user = new User(
            request.getName(),
            request.getEmail(),
            request.getPassword(),
            request.getPhone()
        );

        User saved = userRepository.save(user);
        return new RegisterResponse(
            saved.getId(),
            saved.getName(),
            saved.getEmail(),
            saved.getPhone(),
            "회원가입이 완료되었습니다!"
        );
    }
}
