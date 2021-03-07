package com.yujinchoi.kakaopay.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yujinchoi.kakaopay.exception.ServiceException;
import com.yujinchoi.kakaopay.model.entity.Sprinkle;
import com.yujinchoi.kakaopay.model.http.request.SprinkleRequest;
import com.yujinchoi.kakaopay.model.http.response.SprinkleReceiveResponse;
import com.yujinchoi.kakaopay.model.http.response.SprinkleResponse;
import com.yujinchoi.kakaopay.repository.SprinkleRepository;
import com.yujinchoi.kakaopay.exception.ErrorCode;
import com.yujinchoi.kakaopay.model.entity.Receiver;
import com.yujinchoi.kakaopay.model.http.response.ReceiverInfo;
import com.yujinchoi.kakaopay.model.http.response.SprinkleGetResponse;
import com.yujinchoi.kakaopay.repository.ReceiverRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SprinkleService {
	private final SprinkleRepository sprinkleRepository;
	private final ReceiverRepository receiverRepository;

	@Transactional
	public SprinkleResponse sprinkle(SprinkleRequest request, int userId, String roomId) {
		String token = generateToken();

		Sprinkle sprinkle = new Sprinkle();
		sprinkle.setToken(token);
		sprinkle.setRoomId(roomId);
		sprinkle.setUserId(userId);
		sprinkle.setUserCount(request.getUserCount());
		sprinkle.setAmount(request.getAmount());

		sprinkleRepository.save(sprinkle);
		for (int amount : randomAmount(sprinkle.getAmount(), sprinkle.getUserCount())) {
			Receiver receiver = new Receiver();
			receiver.setAmount(amount);
			receiver.setSprinkle(sprinkle);
			receiverRepository.save(receiver);
		}

		return new SprinkleResponse(token);
	}

	private String generateToken() {
		return RandomStringUtils.random(3, 0, 0, true, false, null, new SecureRandom());
	}

	private int[] randomAmount(int amount, int userCount) {
		int[] result = new int[userCount];
		int remainUserCount = userCount;

		int minAmount = 1;
		if (userCount > amount) {
			minAmount = 0;
			remainUserCount -= userCount - amount;
		}

		int remain = amount;
		for (int i = 0 ; i < userCount - 1; i++) {
			remainUserCount--;
			int maxAmount = remain - remainUserCount;
			int random = RandomUtils.nextInt(minAmount, maxAmount);
			result[i] = random;
			remain = remain - random;
		}
		result[userCount - 1] = remain;
		return result;
	}

	@Transactional
	public SprinkleReceiveResponse receive(String token, int userId, String roomId) {
		Sprinkle sprinkle = sprinkleRepository.findByTokenAndRoomId(token, roomId);
		if (sprinkle == null) {
			throw new ServiceException(ErrorCode.INVALID_TOKEN);
		}

		if (sprinkle.getUserId().equals(userId)) {
			throw new ServiceException(ErrorCode.CAN_NOT_RECEIVE);
		}

		Instant tenMinsAgo = Instant.now().minus(Duration.ofMinutes(10));
		if (tenMinsAgo.isAfter(sprinkle.getCreatedAt().toInstant())) {
			throw new ServiceException(ErrorCode.RECEIVE_TIME_EXPIRED);
		}

		if (receiverRepository.existsBySprinkleAndUserId(sprinkle, userId)) {
			throw new ServiceException(ErrorCode.ALREADY_RECEIVED);
		}

		Receiver receiver = receiverRepository.findFirstBySprinkleAndUserIdIsNull(sprinkle);
		if (receiver == null) {
			throw new ServiceException(ErrorCode.NO_AMOUNT_REMAIN);
		}

		receiver.setUserId(userId);
		return new SprinkleReceiveResponse(receiver.getAmount());
	}

	@Transactional(readOnly = true)
	public SprinkleGetResponse get(String token, int userId, String roomId) {
		Sprinkle sprinkle = sprinkleRepository.findByTokenAndUserIdAndRoomId(token, userId, roomId);
		if (sprinkle == null) {
			throw new ServiceException(ErrorCode.INVALID_TOKEN);
		}

		Instant sevenDaysAgo = Instant.now().minus(Duration.ofDays(7));
		if (sevenDaysAgo.isAfter(sprinkle.getCreatedAt().toInstant())) {
			throw new ServiceException(ErrorCode.GET_TIME_EXPIRED);
		}

		List<Receiver> receiverList = receiverRepository.findBySprinkleAndUserIdIsNotNull(sprinkle);
		int receiveAmount = 0;
		List<ReceiverInfo> receiverInfos = new ArrayList<>(receiverList.size());
		for (Receiver receiver : receiverList) {
			receiveAmount += receiver.getAmount();
			receiverInfos.add(new ReceiverInfo(receiver.getUserId(), receiver.getAmount()));
		}

		SprinkleGetResponse sprinkleGetResponse = new SprinkleGetResponse();
		sprinkleGetResponse.setCreatedAt(sprinkle.getCreatedAt());
		sprinkleGetResponse.setReceiveAmount(receiveAmount);
		sprinkleGetResponse.setSprinkleAmount(sprinkle.getAmount());
		sprinkleGetResponse.setReceiverInfo(receiverInfos);

		return sprinkleGetResponse;
	}
}
