package com.vibe.mapper;

import com.vibe.model.UserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    UserVO findById(String id);
    List<UserVO> findAll();
    List<UserVO> findByTeam(String team);
    void insertUser(UserVO user);
    void updateRemainingVacation(UserVO user);
    void updateUserInfo(UserVO user);
    void updateTeam(UserVO user);
    int countTeamLeaderByTeam(String team);
}
