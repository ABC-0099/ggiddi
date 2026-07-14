package com.meta12.SS8911.service;

import com.meta12.SS8911.dto.SiteUserDTO;
import com.meta12.SS8911.dto.SiteUserEditDTO;
import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.entity.StudyRecord;
import com.meta12.SS8911.exception.DataNotFoundException;
import com.meta12.SS8911.repository.SiteUserRepository;
import com.meta12.SS8911.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // 아이디 중복확인 (AJAX용) - 사용 가능하면 true
    public boolean isUsernameAvailable(String username) {
        return !siteUserRepository.existsByUsername(username);
    }

    // 전화번호 중복확인 (AJAX용) - 사용 가능하면 true
    public boolean isPhoneAvailable(String phone) {
        return !siteUserRepository.existsByPhone(phone);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!username.equals(username.toLowerCase())) {
            throw new UsernameNotFoundException("아이디는 소문자만 사용할 수 있습니다.");
        }

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
        return siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("user not found"));
    }

    public SiteUser getUserByUsername(String username) {
        return siteUserRepository.findByUsername(username)
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
        SiteUser user = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

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