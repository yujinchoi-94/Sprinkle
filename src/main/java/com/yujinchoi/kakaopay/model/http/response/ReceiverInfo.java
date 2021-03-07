package com.yujinchoi.kakaopay.model.http.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ReceiverInfo {
	@Schema(description = "받은 사용자 아이디", required = true)
	Integer userId;

	@Schema(description = "받은 금액", required = true)
	Integer amount;
}
