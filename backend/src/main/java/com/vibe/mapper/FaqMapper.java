package com.vibe.mapper;

import com.vibe.model.FaqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FaqMapper {
    List<FaqVO> findAll();
    FaqVO findById(String id);
    void insert(FaqVO faq);
    void update(FaqVO faq);
    void incrementViews(String id);
    void delete(String id);
}
