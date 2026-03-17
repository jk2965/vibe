package com.vibe.mapper;

import com.vibe.model.ArchiveFileVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ArchiveFileMapper {
    void insert(ArchiveFileVO file);
    List<ArchiveFileVO> findByBoardId(String boardId);
    ArchiveFileVO findById(String id);
    void deleteByBoardId(String boardId);
    void deleteById(String id);
}
