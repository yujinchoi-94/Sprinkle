package com.yujinchoi.kakaopay.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ReceiverInfo {
	Integer userId;
	Integer amount;
}
