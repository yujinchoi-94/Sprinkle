package com.yujinchoi.kakaopay.exception;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ServiceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResult serviceException(HttpServletRequest req, ServiceException e) {
		return new ErrorResult(e.getCode(), e.getMessage());
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ErrorResult objectOptimisticLockingFailureException (HttpServletRequest req, ObjectOptimisticLockingFailureException e) {
		return new ErrorResult(ErrorCode.TRY_LATER.getCode(), ErrorCode.TRY_LATER.getMessage());
	}
}
