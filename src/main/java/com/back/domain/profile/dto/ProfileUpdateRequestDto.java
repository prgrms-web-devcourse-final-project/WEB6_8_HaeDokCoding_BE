package com.back.domain.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequestDto {

    @Size(min = 1, max = 10, message = "닉네임은 1~10자")
    private String nickname;

    @Email(message = "이메일 형식이 아닙니다")
    private String email;
}
