package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.ArchiveFileMapper;
import com.vibe.mapper.ArchiveMapper;
import com.vibe.model.ArchiveVO;
import com.vibe.model.ArchiveFileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ArchiveService {

    @Autowired
    private ArchiveMapper mapper;

    @Autowired
    private ArchiveFileMapper fileMapper;

    public PageInfo<ArchiveVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<ArchiveVO> list = mapper.findAll();
        return new PageInfo<>(list);
    }

    public ArchiveVO getDetail(String id) {
        mapper.incrementViews(id);
        ArchiveVO archive = mapper.findById(id);
        if (archive != null) {
            archive.setFiles(fileMapper.findByBoardId(id));
        }
        return archive;
    }

    public ArchiveVO write(ArchiveVO archive) {
        archive.setId(UUID.randomUUID().toString());
        archive.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(archive);
        return archive;
    }

    public void update(String id, String title, String content) {
        ArchiveVO archive = new ArchiveVO();
        archive.setId(id);
        archive.setTitle(title);
        archive.setContent(content);
        mapper.update(archive);
    }

    public void delete(String id) {
        fileMapper.deleteByBoardId(id);
        mapper.delete(id);
    }

    public ArchiveFileVO saveFile(String boardId, String originalName, String storedName, long fileSize, String uploaderId) {
        ArchiveFileVO file = new ArchiveFileVO();
        file.setId(UUID.randomUUID().toString());
        file.setBoardId(boardId);
        file.setOriginalName(originalName);
        file.setStoredName(storedName);
        file.setFileSize(fileSize);
        file.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        file.setUploaderId(uploaderId);
        fileMapper.insert(file);
        return file;
    }

    public ArchiveFileVO getFile(String fileId) {
        return fileMapper.findById(fileId);
    }

    public void deleteFile(String fileId) {
        fileMapper.deleteById(fileId);
    }
}
