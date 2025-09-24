package com.back.global.exception;


import com.back.global.rsData.RsData;

/**
 * 서비스 예외를 나타내는 클래스
 * 서비스 계층에서 발생하는 오류를 처리하기 위해 사용
 * @param code 오류 코드
 * @param msg 오류 메시지
 */

public class ServiceException extends RuntimeException {
    private final int code;
    private final String msg;

    public ServiceException(int code, String msg) {
        super(code + " : " + msg);
        this.code = code;
        this.msg = msg;
    }
    public RsData<Void> getRsData()  {
        return RsData.of(code,msg);
    }
}
