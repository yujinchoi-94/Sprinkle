package com.yujinchoi.kakaopay.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class SprinkleReceiveResponse {
	public SprinkleReceiveResponse() { }

	@Schema(description = "받은 금액", required = true)
	private Integer amount;
}
