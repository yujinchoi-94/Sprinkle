package com.yujinchoi.kakaopay.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResult {
	@Schema(description = "에러 코드", required = true)
	int code;

	@Schema(description = "에러 메시지", required = true)
	String message;
}
