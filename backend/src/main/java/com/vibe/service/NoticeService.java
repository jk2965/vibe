package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.NoticeMapper;
import com.vibe.model.NoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class NoticeService {

    @Autowired
    private NoticeMapper mapper;

    public PageInfo<NoticeVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<NoticeVO> list = mapper.findAll();
        return new PageInfo<>(list);
    }

    public NoticeVO getDetail(String id) {
        mapper.incrementViews(id);
        return mapper.findById(id);
    }

    public NoticeVO write(NoticeVO notice) {
        notice.setId(UUID.randomUUID().toString());
        notice.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.insert(notice);
        return notice;
    }

    public void update(String id, String title, String content) {
        NoticeVO notice = new NoticeVO();
        notice.setId(id);
        notice.setTitle(title);
        notice.setContent(content);
        mapper.update(notice);
    }

    public void delete(String id) {
        mapper.delete(id);
    }
}
