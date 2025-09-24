package com.back.global.rsData;

public record RsData<T>(int code, String message, T data) {

    public static<T> RsData<T> of(int code, String message, T data) {
        return new RsData<>(code, message, data);
    }
    public static<T> RsData<T> of(int code, String message) {
        return new RsData<>(code,message, null);
    }

    //성공 편의 메소드
    public static <T> RsData<T> successOf(T data) {
        return of(200,"success", data);
    }

    //실패 편의 메소드
    public static <T> RsData<T> failOf(T data) {
        return of(500,"fail",data);
    }

    // 실패 편의 메소드 (메시지 포함)
    public static <T> RsData<T> failOf(String message) {
        return of(500, message, null);
    }

}
