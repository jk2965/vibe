package com.vibe.service;

import com.vibe.mapper.ArchiveFileMapper;
import com.vibe.model.ArchiveFileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private ArchiveFileMapper fileMapper;

    public List<ArchiveFileVO> getFilesByBoard(String boardId) {
        return fileMapper.findByBoardId(boardId);
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

    public void deleteFilesByBoard(String boardId) {
        fileMapper.deleteByBoardId(boardId);
    }
}
