package com.meta12.SS8911.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 이 어노테이션 추가 (Lombok)
public class SiteUserDTO {
    // ... 필드들

    private Long id;

    @NotBlank(message = "아이디는 필수입니다.")
    private String username;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "생년월일은 필수입니다.")
    private String birth;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordChk;

    private String nationality;
}