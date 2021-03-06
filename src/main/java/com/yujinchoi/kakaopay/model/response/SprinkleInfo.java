package com.yujinchoi.kakaopay.model.response;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SprinkleInfo {
	private Integer sprinkleAmount;
	private Date createdAt;
	private Integer receiveAmount;
	private List<ReceiverInfo> receiverInfo;
}
