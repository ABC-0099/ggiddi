package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.SiteUserDTO;
import com.meta12.SS8911.dto.SiteUserEditDTO;
import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.exception.DataNotFoundException;
import com.meta12.SS8911.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SiteUserService implements UserDetailsService {
    private final SiteUserRepository siteUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void chugaProc(SiteUserDTO dto) {
        if (siteUserRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        SiteUser user = new SiteUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setBirth(dto.getBirth());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setNationality(dto.getNationality());

        user.setRole(Role.USER);
        user.setJoinDate(LocalDateTime.now());

        siteUserRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SiteUser siteUser = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // ★ 탈퇴한 계정은 로그인 자체를 막음 (Spring Security가 자동으로 걸러줌)
        return User.builder()
                .username(siteUser.getUsername())
                .password(siteUser.getPassword())
                .roles(siteUser.getRole().name())
                .disabled(siteUser.isWithdrawn())
                .build();
    }

    public SiteUser getUser(String username) {
        return siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("user not found"));
    }

    public SiteUser getUserByUsername(String username) {
        return siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // ★ 회원정보 수정
    @Transactional
    public void updateInfo(String username, SiteUserEditDTO dto) {
        SiteUser user = getUserByUsername(username);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setBirth(dto.getBirth());
        user.setNationality(dto.getNationality());

        siteUserRepository.save(user);
    }

    // ★ 회원 탈퇴 (소프트 삭제)
    @Transactional
    public void withdraw(String username, String currentPassword) {
        SiteUser user = getUserByUsername(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        user.setWithdrawn(true);
        user.setWithdrawnDate(LocalDateTime.now());

        siteUserRepository.save(user);
    }

    // SiteUserService.java 파일 내부

    public void editProc(String username, SiteUserEditDTO dto) {
        // 1. 유저 찾기
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // 2. 정보 수정
        user.setPhone(dto.getPhone());
        user.setNationality(dto.getNationality());

        // 3. 비밀번호 변경 (필요 시)
        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            // 현재 비밀번호 검증
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalStateException("현재 비밀번호가 일치하지 않습니다.");
            }
            // 새 비밀번호 일치 확인
            if (!dto.getNewPassword().equals(dto.getNewPasswordChk())) {
                throw new IllegalStateException("새 비밀번호 확인이 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        // 4. 저장
        siteUserRepository.save(user);
    }
}