package com.vibe.mapper;

import com.vibe.model.UserVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    UserVO findById(String id);
    void insertUser(UserVO user);
}
