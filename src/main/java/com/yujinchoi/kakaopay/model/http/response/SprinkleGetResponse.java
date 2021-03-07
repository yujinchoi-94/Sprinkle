package com.yujinchoi.kakaopay.model.http.response;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SprinkleGetResponse {
	@Schema(description = "뿌린 금액", required = true)
	private Integer sprinkleAmount;

	@Schema(description = "뿌린 시각", required = true)
	private Date createdAt;

	@Schema(description = "받기 완료된 금액", required = true)
	private Integer receiveAmount;

	@Schema(description = "받기 완료된 정보")
	private List<ReceiverInfo> receiverInfo;
}
