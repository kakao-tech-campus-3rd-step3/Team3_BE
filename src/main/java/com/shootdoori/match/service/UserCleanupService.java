package com.shootdoori.match.service;

import com.shootdoori.match.entity.User;
import com.shootdoori.match.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserCleanupService {

    @Transactional
    public void permanentlyDeleteUsers(List<User> user) {

        // 1. 인증/보안 관련 토큰 삭제

        // 2. 매치 관련 데이터 삭제

        // 3. 팀 관련 데이터 삭제
        // 팀장&부팀장 체크 후 위임 또는 팀 삭제 처리

        // 4. 프로필(user) 삭제
    }
}
