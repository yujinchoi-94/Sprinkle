package com.yujinchoi.kakaopay.controller;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yujinchoi.kakaopay.model.request.CommonRequest;
import com.yujinchoi.kakaopay.model.request.SprinkleRequest;
import com.yujinchoi.kakaopay.model.Sprinkle;
import com.yujinchoi.kakaopay.model.response.SprinkleInfo;
import com.yujinchoi.kakaopay.service.SprinkleService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/sprinkle")
@RestController
@RequiredArgsConstructor
public class SprinkleController {
	private final SprinkleService sprinkleService;

	@PostMapping
	public ResponseEntity sprinkle(@RequestHeader("X-USER-ID") int userId,
		@NotBlank @RequestHeader("X-ROOM-ID") String roomId,
		@Valid @RequestBody SprinkleRequest request) {
		Sprinkle sprinkle = new Sprinkle();
		String token = generateToken();
		sprinkle.setToken(token);
		sprinkle.setRoomId(roomId);
		sprinkle.setUserId(userId);
		sprinkle.setUserCount(request.getUserCount());
		sprinkle.setAmount(request.getAmount());

		sprinkleService.sprinkle(sprinkle);

		Map map = new HashMap();
		map.put("token", token);

		return new ResponseEntity<>(map, HttpStatus.CREATED);
	}

	@PostMapping("/receive")
	public ResponseEntity receiveSprinkle(@RequestHeader("X-USER-ID") int userId,
		@RequestHeader("X-ROOM-ID") String roomId,
		@Valid @RequestBody CommonRequest commonRequest) {
		int amount = sprinkleService.receive(commonRequest.getToken(), userId, roomId);

		Map map = new HashMap();
		map.put("amount", amount);
		return new ResponseEntity(map, HttpStatus.CREATED);
	}

	@GetMapping
	public SprinkleInfo getSprinkle(@RequestHeader("X-USER-ID") int userId,
		@RequestHeader("X-ROOM-ID") String roomId,
		@Valid @RequestBody CommonRequest commonRequest) {
		return sprinkleService.get(commonRequest.getToken(), userId, roomId);
	}

	private String generateToken() {
		return RandomStringUtils.random(3, 0, 0, true, false, null, new SecureRandom());
	}

}
