package com.yujinchoi.kakaopay.controller;

import java.security.SecureRandom;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yujinchoi.kakaopay.model.entity.Sprinkle;
import com.yujinchoi.kakaopay.model.http.request.SprinkleRequest;
import com.yujinchoi.kakaopay.model.http.response.SprinkleGetResponse;
import com.yujinchoi.kakaopay.model.http.response.SprinkleReceiveResponse;
import com.yujinchoi.kakaopay.model.http.response.SprinkleResponse;
import com.yujinchoi.kakaopay.service.SprinkleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RequestMapping("/sprinkle")
@RestController
@RequiredArgsConstructor
public class SprinkleController {
	private final SprinkleService sprinkleService;

	@Operation(summary = "뿌리기")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SprinkleResponse sprinkle(@RequestHeader("X-USER-ID") int userId,
		@NotBlank @RequestHeader("X-ROOM-ID") String roomId,
		@Parameter(required = true, schema = @Schema(implementation = SprinkleRequest.class))
		@Valid @RequestBody SprinkleRequest request) {

		Sprinkle sprinkle = new Sprinkle();
		String token = generateToken();
		sprinkle.setToken(token);
		sprinkle.setRoomId(roomId);
		sprinkle.setUserId(userId);
		sprinkle.setUserCount(request.getUserCount());
		sprinkle.setAmount(request.getAmount());

		sprinkleService.sprinkle(sprinkle);

		return new SprinkleResponse(token);
	}

	@Operation(summary = "뿌린 금액 받기")
	@PostMapping("/receive/{token}")
	@ResponseStatus(HttpStatus.CREATED)
	public SprinkleReceiveResponse receiveSprinkle(@RequestHeader("X-USER-ID") int userId,
		@RequestHeader("X-ROOM-ID") String roomId,
		@NotBlank @PathVariable String token) {
		return new SprinkleReceiveResponse(sprinkleService.receive(token, userId, roomId));
	}

	@Operation(summary = "뿌리기 조회")
	@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SprinkleGetResponse.class)))
	@GetMapping("/{token}")
	public SprinkleGetResponse getSprinkle(@RequestHeader("X-USER-ID") int userId,
		@RequestHeader("X-ROOM-ID") String roomId,
		@NotBlank @PathVariable String token) {
		return sprinkleService.get(token, userId, roomId);
	}

	private String generateToken() {
		return RandomStringUtils.random(3, 0, 0, true, false, null, new SecureRandom());
	}

}
