package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.ArchiveFileMapper;
import com.vibe.mapper.TeamArchiveMapper;
import com.vibe.model.TeamArchiveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class TeamArchiveService {

    @Autowired
    private TeamArchiveMapper mapper;

    @Autowired
    private ArchiveFileMapper fileMapper;

    public PageInfo<TeamArchiveVO> getListByTeam(String team, int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<TeamArchiveVO> list = mapper.findByTeam(team);
        return new PageInfo<>(list);
    }

    public TeamArchiveVO getDetail(String id) {
        mapper.incrementViews(id);
        TeamArchiveVO archive = mapper.findById(id);
        if (archive != null) {
            archive.setFiles(fileMapper.findByBoardId(id));
        }
        return archive;
    }

    public TeamArchiveVO write(TeamArchiveVO archive) {
        archive.setId(UUID.randomUUID().toString());
        archive.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(archive);
        return archive;
    }

    public void update(String id, String title, String content) {
        TeamArchiveVO archive = new TeamArchiveVO();
        archive.setId(id);
        archive.setTitle(title);
        archive.setContent(content);
        mapper.update(archive);
    }

    public void delete(String id) {
        fileMapper.deleteByBoardId(id);
        mapper.delete(id);
    }
}
