package com.vibe.mapper;

import com.vibe.model.VacationVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VacationMapper {
    void insert(VacationVO vacation);
    List<VacationVO> findByUserId(String userId);
    VacationVO findById(String id);
    void delete(String id);
}
