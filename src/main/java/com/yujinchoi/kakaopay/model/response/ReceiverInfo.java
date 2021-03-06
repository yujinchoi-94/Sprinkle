package com.yujinchoi.kakaopay.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceiverInfo {
	Integer userId;
	Integer amount;
}
