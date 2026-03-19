package com.vibe.service;

import com.vibe.mapper.UserMapper;
import com.vibe.model.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 사용자(User) 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * UserController.java에서 호출되며, UserMapper.java를 통해 DB와 연동됨.
 * VacationService.java에서 연차 차감/복구 시 이 서비스를 참조함.
 */
@Service
public class UserService {

    // UserMapper.java를 통해 USERS 테이블 CRUD 수행
    @Autowired
    private UserMapper mapper;

    // 비밀번호 단방향 암호화를 위한 BCrypt 인코더
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 로그인 처리: 아이디로 사용자를 조회한 뒤 입력 비밀번호와 해시값을 비교함.
     * UserMapper.java의 findById()로 사용자 조회.
     */
    public boolean login(String id, String password) {
        UserVO user = mapper.findById(id);
        return user != null && encoder.matches(password, user.getPassword());
    }

    /**
     * 신규 사용자 등록: 비밀번호를 BCrypt로 암호화하고 잔여 연차 15일로 초기화.
     * UserMapper.java의 insertUser()로 DB에 저장.
     */
    public void register(String id, String username, String password, String position) {
        UserVO user = new UserVO();
        user.setId(id);
        user.setUsername(username);
        // 비밀번호를 BCrypt 해시값으로 변환하여 저장
        user.setPassword(encoder.encode(password));
        // 직급 미입력 시 기본값 '사원'으로 설정
        user.setPosition(position != null && !position.isEmpty() ? position : "사원");
        // 신규 가입자 연차 기본값 15일 부여
        user.setRemainingVacation(15.0);
        mapper.insertUser(user);
    }

    /**
     * 아이디 중복 확인: DB에 해당 ID가 존재하면 true 반환.
     * UserMapper.java의 findById()로 조회.
     */
    public boolean exists(String id) {
        return mapper.findById(id) != null;
    }

    /**
     * 사용자 이름(username) 조회.
     * UserMapper.java의 findById()로 조회 후 username 필드 반환.
     */
    public String getUsername(String id) {
        UserVO user = mapper.findById(id);
        return user != null ? user.getUsername() : null;
    }

    /**
     * 사용자 전체 정보를 UserVO 객체로 반환.
     * VacationService.java에서 연차 잔여일 확인 시 호출.
     */
    public UserVO getUser(String id) {
        return mapper.findById(id);
    }

    /**
     * 연차 차감: 현재 잔여 연차에서 지정 일수를 차감하고 0 미만 방지.
     * VacationService.java의 save()에서 연차 신청 시 호출됨.
     * UserMapper.java의 updateRemainingVacation()으로 DB 업데이트.
     */
    public void deductVacation(String id, double days) {
        UserVO user = mapper.findById(id);
        if (user != null) {
            double remaining = user.getRemainingVacation() != null ? user.getRemainingVacation() : 0;
            // 잔여 연차가 0 미만이 되지 않도록 Math.max 적용
            user.setRemainingVacation(Math.max(0, remaining - days));
            mapper.updateRemainingVacation(user);
        }
    }

    /**
     * 연차 복구: 삭제된 휴가 신청에 해당하는 일수를 잔여 연차에 다시 추가.
     * VacationService.java의 delete()에서 휴가 취소 시 호출됨.
     * UserMapper.java의 updateRemainingVacation()으로 DB 업데이트.
     */
    public void restoreVacation(String id, double days) {
        UserVO user = mapper.findById(id);
        if (user != null) {
            double remaining = user.getRemainingVacation() != null ? user.getRemainingVacation() : 0;
            user.setRemainingVacation(remaining + days);
            mapper.updateRemainingVacation(user);
        }
    }

    /**
     * 전체 사용자 목록 조회.
     * UserMapper.java의 findAll()로 USERS 테이블 전체 조회.
     */
    public List<UserVO> getAllUsers() {
        return mapper.findAll();
    }

    /**
     * 사용자 정보 수정: 직급, 잔여 연차, 관리자 여부, 팀, 팀장 여부를 한 번에 업데이트.
     * UserMapper.java의 updateUserInfo()로 DB 업데이트.
     */
    public void updateUserInfo(String id, String position, Double remainingVacation, Integer isAdmin, String team, Integer isTeamLeader) {
        UserVO user = new UserVO();
        user.setId(id);
        user.setPosition(position);
        user.setRemainingVacation(remainingVacation);
        user.setIsAdmin(isAdmin);
        user.setTeam(team);
        user.setIsTeamLeader(isTeamLeader);
        mapper.updateUserInfo(user);
    }

    /**
     * 사용자의 팀 정보만 단독으로 업데이트.
     * UserMapper.java의 updateTeam()으로 TEAM 컬럼만 수정.
     */
    public void updateTeam(String id, String team) {
        UserVO user = new UserVO();
        user.setId(id);
        user.setTeam(team);
        mapper.updateTeam(user);
    }

    /**
     * 해당 사용자가 팀장인지 확인.
     * isTeamLeader 값이 1이면 팀장으로 판단.
     */
    public boolean isTeamLeader(String id) {
        UserVO user = mapper.findById(id);
        return user != null && Integer.valueOf(1).equals(user.getIsTeamLeader());
    }

    /**
     * 특정 팀에 속한 사용자 목록 조회.
     * UserMapper.java의 findByTeam()으로 팀별 조회.
     */
    public List<UserVO> getUsersByTeam(String team) {
        return mapper.findByTeam(team);
    }

    /**
     * 관리자 여부 확인: isAdmin 값이 1 이상이면 관리자(HR 포함)로 판단.
     */
    public boolean isAdmin(String id) {
        UserVO user = mapper.findById(id);
        return user != null && user.getIsAdmin() != null && user.getIsAdmin() >= 1;
    }

    /**
     * 최고 관리자(슈퍼어드민) 여부 확인: isAdmin 값이 정확히 2인 경우만 해당.
     */
    public boolean isSuperAdmin(String id) {
        UserVO user = mapper.findById(id);
        return user != null && Integer.valueOf(2).equals(user.getIsAdmin());
    }

    /**
     * 관리자 레벨 숫자값 반환: 0=일반, 1=HR관리자, 2=최고관리자.
     */
    public int getAdminLevel(String id) {
        UserVO user = mapper.findById(id);
        return user != null && user.getIsAdmin() != null ? user.getIsAdmin() : 0;
    }

    /**
     * 해당 팀에 이미 팀장이 존재하는지 확인.
     * UserMapper.java의 countTeamLeaderByTeam()으로 팀장 수를 조회.
     */
    public boolean hasTeamLeader(String team) {
        return mapper.countTeamLeaderByTeam(team) > 0;
    }
}
