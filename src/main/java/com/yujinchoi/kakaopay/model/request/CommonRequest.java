package com.yujinchoi.kakaopay.model.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommonRequest {
	@NotBlank
	private String token;
}
