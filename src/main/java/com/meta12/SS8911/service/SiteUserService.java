package com.meta12.SS8911.service;

import com.meta12.SS8911.Dto.SiteUserDTO;
import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.SiteUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiteUserService implements UserDetailsService {
    private final SiteUserRepository siteUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void chugaProc(SiteUserDTO dto) {
        // [추가] 가입 전 중복 아이디 체크
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

        user.setRole(Role.USER); // 가입 시 기본 권한 USER 부여

        siteUserRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SiteUser siteUser = siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return User.builder()
                .username(siteUser.getUsername())
                .password(siteUser.getPassword())
                .roles(siteUser.getRole().name())
                .build();
    }

    public SiteUser getUserByUsername(String username) {
        return siteUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}