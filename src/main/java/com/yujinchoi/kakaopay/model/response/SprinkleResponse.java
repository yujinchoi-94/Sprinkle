package com.yujinchoi.kakaopay.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SprinkleResponse {
	public SprinkleResponse() { }

	@Schema(description = "뿌리기 요청 고유 토큰", required = true)
	private String token;
}
