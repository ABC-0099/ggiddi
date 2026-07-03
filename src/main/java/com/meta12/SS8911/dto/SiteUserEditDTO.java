package com.meta12.SS8911.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SiteUserEditDTO {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    private String birth;
    private String nationality;

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    private String newPassword;
    private String newPasswordChk;

    public String getNewPasswordChk() {
        return newPasswordChk;
    }
}