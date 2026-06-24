package com.meta12.SS8911;

import com.meta12.SS8911.config.Role;
import com.meta12.SS8911.entity.SiteUser;
import com.meta12.SS8911.repository.SiteUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class Ss8911ApplicationTests {

	@Autowired
	private SiteUserRepository siteUserRepository; // 이거 추가!

	@Test
	void contextLoads() {
	}

	@Test
	void admin(){
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String password = passwordEncoder.encode("1234");

		SiteUser siteUser = new SiteUser();

		siteUser.setUsername("admin888");
		siteUser.setName("관리자");
		siteUser.setPassword(password);
		siteUser.setPhone("010-0000-0000");
		siteUser.setBirth("1999-01-01");
		siteUser.setRole(Role.ADMIN);
		siteUserRepository.save(siteUser);

	}

}
