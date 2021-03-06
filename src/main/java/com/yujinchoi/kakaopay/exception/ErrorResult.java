package com.yujinchoi.kakaopay.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResult {
	int code;
	String message;
}
