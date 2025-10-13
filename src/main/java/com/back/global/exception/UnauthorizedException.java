package com.back.global.exception;

public class UnauthorizedException extends ServiceException {

    private static final int UNAUTHORIZED_CODE = 401;
    private static final String UNAUTHORIZED_MSG = "로그인이 필요합니다.";

    public UnauthorizedException() {
        super(UNAUTHORIZED_CODE, UNAUTHORIZED_MSG);
    }

    public UnauthorizedException(String msg) {
        super(UNAUTHORIZED_CODE, msg);
    }
}