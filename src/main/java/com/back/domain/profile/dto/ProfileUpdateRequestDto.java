package com.back.domain.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequestDto {

    @Size(min = 1, max = 10, message = "닉네임은 1~10자")
    private String nickname;
}
