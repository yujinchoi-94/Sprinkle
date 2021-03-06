package com.yujinchoi.kakaopay.model.response;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SprinkleInfo {
	private Integer sprinkleAmount;
	private Date createdAt;
	private Integer receiveAmount;
	private List<ReceiverInfo> receiverInfo;
}
