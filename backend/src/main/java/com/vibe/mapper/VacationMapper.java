package com.vibe.mapper;

import com.vibe.model.VacationVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// VacationMapper.xml과 연결됨 — 연차·휴가 신청 관련 DB 접근 인터페이스
// VacationService.java에서 호출됨 / 잔여 연차 업데이트는 UserMapper.updateRemainingVacation과 연계
@Mapper
public interface VacationMapper {
    // 새 연차·휴가 신청 내역을 DB에 등록
    void insert(VacationVO vacation);
    // 특정 사용자(userId)의 연차·휴가 신청 목록을 조회
    List<VacationVO> findByUserId(String userId);
    // 연차·휴가 신청 ID로 단일 신청 내역을 조회 (상세 조회 및 취소 처리 시 호출)
    VacationVO findById(String id);
    // 연차·휴가 신청을 DB에서 삭제 (신청 취소 시 호출, 잔여 연차 복원은 VacationService에서 처리)
    void delete(String id);
}
