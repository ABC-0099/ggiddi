package com.meta12.SS8911.service;

import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.dto.SiteUserDTO;
import com.meta12.SS8911.dto.SiteUserEditDTO;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.entity.StudyRecord;
import com.meta12.SS8911.exception.DataNotFoundException;
import com.meta12.SS8911.repository.SiteUserRepository;
import com.meta12.SS8911.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteUserService implements UserDetailsService {
    private final SiteUserRepository siteUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudyRecordRepository studyRecordRepository;

    public List<SiteUser> getAllUsers() {
        return siteUserRepository.findAll();
    }

    public Page<SiteUser> getAllUsers(Pageable pageable) {
        return siteUserRepository.findAll(pageable);
    }

    @Transactional
    public void chugaProc(SiteUserDTO dto) {
        String lowerUsername = dto.getUsername().toLowerCase();

        // 1. 아이디에 대문자가 포함되어 있는지 확인 (정규식: [A-Z])
        if (dto.getUsername().matches(".*[A-Z].*")) {
            throw new IllegalStateException("아이디는 소문자만 입력 가능합니다.");
        }

        if (siteUserRepository.existsByUsername(lowerUsername)) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        SiteUser user = new SiteUser();
        user.setUsername(lowerUsername);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setBirth(dto.getBirth());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setNationality(dto.getNationality());
        user.setUsername(dto.getUsername());
        user.setRole(Role.USER);
        user.setJoinDate(LocalDateTime.now());

        siteUserRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 변환 없이 입력된 username 그대로 조회
        SiteUser siteUser = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return User.builder()
                .username(siteUser.getUsername())
                .password(siteUser.getPassword())
                .roles(siteUser.getRole().name())
                .disabled(siteUser.isWithdrawn())
                .build();
    }

    public SiteUser getUser(String username) {
        // 호출 시에도 소문자 변환을 고려하여 username.toLowerCase() 사용 권장
        return siteUserRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new DataNotFoundException("user not found"));
    }

    public SiteUser getUserByUsername(String username) {
        return siteUserRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

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

    @Transactional
    public void editProc(String username, SiteUserEditDTO dto) {
        SiteUser user = getUserByUsername(username);

        user.setPhone(dto.getPhone());
        user.setNationality(dto.getNationality());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalStateException("현재 비밀번호가 일치하지 않습니다.");
            }
            if (!dto.getNewPassword().equals(dto.getNewPasswordChk())) {
                throw new IllegalStateException("새 비밀번호 확인이 일치하지 않습니다.");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }
        siteUserRepository.save(user);
    }

    public List<Map<String, String>> getHeatmapData(SiteUser user) {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        List<StudyRecord> records = studyRecordRepository.findBySiteUserAndStudyDateBetween(user, start, end);

        return records.stream().map(r -> Map.of(
                "date", r.getStudyDate().toLocalDate().toString(),
                "count", "1"
        )).collect(Collectors.toList());
    }
}