package com.vibe.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vibe.mapper.FaqMapper;
import com.vibe.model.FaqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class FaqService {

    @Autowired
    private FaqMapper faqMapper;

    public PageInfo<FaqVO> getList(int pageNum) {
        PageHelper.startPage(pageNum, 10);
        List<FaqVO> list = faqMapper.findAll();
        return new PageInfo<>(list);
    }

    public FaqVO getDetail(String id) {
        faqMapper.incrementViews(id);
        return faqMapper.findById(id);
    }

    public FaqVO write(FaqVO faq) {
        faq.setId(UUID.randomUUID().toString());
        faq.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        faqMapper.insert(faq);
        return faq;
    }

    public void update(String id, String title, String content) {
        FaqVO faq = new FaqVO();
        faq.setId(id);
        faq.setTitle(title);
        faq.setContent(content);
        faqMapper.update(faq);
    }

    public void delete(String id) {
        faqMapper.delete(id);
    }
}
