package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.TeamNoticeMapper;
import com.vibe.model.TeamNoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class TeamNoticeService {

    @Autowired
    private TeamNoticeMapper mapper;

    @Autowired
    private FileService fileService;

    public PageInfo<TeamNoticeVO> getListByTeam(String team, int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<TeamNoticeVO> list = mapper.findByTeam(team);
        return new PageInfo<>(list);
    }

    public TeamNoticeVO getDetail(String id) {
        mapper.incrementViews(id);
        TeamNoticeVO notice = mapper.findById(id);
        if (notice != null) {
            notice.setFiles(fileService.getFilesByBoard(id));
        }
        return notice;
    }

    public TeamNoticeVO write(TeamNoticeVO notice) {
        notice.setId(UUID.randomUUID().toString());
        notice.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(notice);
        return notice;
    }

    public void update(String id, String title, String content) {
        TeamNoticeVO notice = new TeamNoticeVO();
        notice.setId(id);
        notice.setTitle(title);
        notice.setContent(content);
        mapper.update(notice);
    }

    public void delete(String id) {
        fileService.deleteFilesByBoard(id);
        mapper.delete(id);
    }
}
