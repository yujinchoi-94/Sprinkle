package com.yujinchoi.kakaopay.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceException extends RuntimeException {
	private int code;
	private String message;

	public ServiceException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.code = errorCode.getCode();
		this.message = errorCode.getMessage();
	}
}
