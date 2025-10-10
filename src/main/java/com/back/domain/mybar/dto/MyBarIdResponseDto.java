package com.back.domain.mybar.dto;

import com.back.domain.mybar.entity.MyBar;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyBarIdResponseDto {
    private Long id;
    private Long cocktailId;
    private LocalDateTime keptAt;

    public static MyBarIdResponseDto from(MyBar myBar) {
        return MyBarIdResponseDto.builder()
                .id(myBar.getId())
                .cocktailId(myBar.getCocktail().getId())
                .keptAt(myBar.getKeptAt())
                .build();
    }
}
