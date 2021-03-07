package com.yujinchoi.kakaopay.model.request;

import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SprinkleRequest {
	@Schema(description = "뿌릴 금액", required = true)
	@Min(1)
	private int amount;

	@Schema(description = "뿌릴 인원", required = true)
	@Min(1)
	private int userCount;
}
