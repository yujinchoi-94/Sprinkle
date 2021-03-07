package com.yujinchoi.kakaopay.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
	ALREADY_RECEIVED(HttpStatus.BAD_REQUEST, 1000, "뿌리기 당 한 사용자는 한번만 받을 수 있습니다."),
	RECEIVE_TIME_EXPIRED(HttpStatus.BAD_REQUEST, 1001, "뿌린 건은 10분간만 유효합니다."),
	GET_TIME_EXPIRED(HttpStatus.BAD_REQUEST, 1002, "뿌린 건에 대한 조회는 7일동안 할 수 있습니다."),
	CAN_NOT_RECEIVE(HttpStatus.BAD_REQUEST, 1003, "자신이 뿌리기한 건은 자신이 받을 수 없습니다."),
	NO_AMOUNT_REMAIN(HttpStatus.BAD_REQUEST, 1004, "더 이상 뿌릴 금액이 없습니다."),
	INVALID_TOKEN(HttpStatus.BAD_REQUEST, 1005, "유효하지 않은 토큰입니다."),
	TRY_LATER(HttpStatus.SERVICE_UNAVAILABLE, 1006, "잠시 후 다시 시도하세요."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1007, "서버 관리자에게 문의하세요.");

	private HttpStatus httpStatus;
	private int code;
	private String message;

	ErrorCode(HttpStatus httpStatus, int code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
