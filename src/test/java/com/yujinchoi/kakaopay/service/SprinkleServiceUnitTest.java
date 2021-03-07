package com.yujinchoi.kakaopay.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.yujinchoi.kakaopay.repository.SprinkleRepository;
import com.yujinchoi.kakaopay.exception.ServiceException;
import com.yujinchoi.kakaopay.model.Receiver;
import com.yujinchoi.kakaopay.model.Sprinkle;
import com.yujinchoi.kakaopay.model.response.SprinkleGetResponse;
import com.yujinchoi.kakaopay.repository.ReceiverRepository;

@RunWith(MockitoJUnitRunner.class)
public class SprinkleServiceUnitTest {
	@Mock
	private ReceiverRepository receiverRepository;
	@Mock
	private SprinkleRepository sprinkleRepository;
	@InjectMocks
	private SprinkleService sprinkleService;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private Sprinkle sprinkle;

	Integer amount = 100;
	Integer userCount = 4;
	String token = "Xsa";
	String roomId = "rid-x12r-123";
	Integer userId = 11234;
	Date createdAt = new Date();
	Integer receiverId = 11233;

	@Before
	public void setUp() throws Exception {
		sprinkle = new Sprinkle();

		sprinkle.setAmount(amount);
		sprinkle.setUserCount(userCount);
		sprinkle.setToken(token);
		sprinkle.setRoomId(roomId);
		sprinkle.setUserId(userId);
		sprinkle.setCreatedAt(createdAt);
	}

	@Test
	public void test_sprinkle() {
		// given
		// when
		sprinkleService.sprinkle(sprinkle);


		// then
		ArgumentCaptor<Receiver> argumentCaptor = ArgumentCaptor.forClass(Receiver.class);
		Mockito.verify(sprinkleRepository, Mockito.times(1)).save(sprinkle);
		Mockito.verify(receiverRepository, Mockito.times(userCount)).save(argumentCaptor.capture());

		List<Receiver> receiverList = argumentCaptor.getAllValues();
		int actualAmount = 0;
		for (Receiver receiver : receiverList) {
			actualAmount += receiver.getAmount();
			Assert.assertEquals(sprinkle, receiver.getSprinkle());
		}
		Assert.assertEquals(amount.intValue(), actualAmount);
	}

	@Test
	public void test_sprinkle_userCount_is_gt_than_amount() {
		// given
		Integer amount = 1;
		sprinkle.setAmount(amount);
		sprinkle.setUserCount(amount + 3);

		// when
		sprinkleService.sprinkle(sprinkle);


		// then
		ArgumentCaptor<Receiver> argumentCaptor = ArgumentCaptor.forClass(Receiver.class);
		Mockito.verify(sprinkleRepository, Mockito.times(1)).save(sprinkle);
		Mockito.verify(receiverRepository, Mockito.times(amount + 3)).save(argumentCaptor.capture());

		List<Receiver> receiverList = argumentCaptor.getAllValues();
		int actualAmount = 0;
		for (Receiver receiver : receiverList) {
			actualAmount += receiver.getAmount();
			Assert.assertEquals(sprinkle, receiver.getSprinkle());
		}
		Assert.assertEquals(amount.intValue(), actualAmount);
	}

	@Test
	public void test_sprinkle_userCount_is_one() {
		// given
		sprinkle.setUserCount(1);

		// when
		sprinkleService.sprinkle(sprinkle);


		// then
		ArgumentCaptor<Receiver> argumentCaptor = ArgumentCaptor.forClass(Receiver.class);
		Mockito.verify(sprinkleRepository, Mockito.times(1)).save(sprinkle);
		Mockito.verify(receiverRepository, Mockito.times(1)).save(argumentCaptor.capture());

		List<Receiver> receiverList = argumentCaptor.getAllValues();
		Assert.assertEquals(1, receiverList.size());
		Assert.assertEquals(sprinkle, receiverList.get(0).getSprinkle());
		Assert.assertEquals(amount.intValue(), receiverList.get(0).getAmount().intValue());
	}

	@Test
	public void test_receive() {
		// given
		Integer receiverId = 1111;
		Integer receiveAmount = 10;
		Receiver receiver = new Receiver();
		receiver.setUserId(null);
		receiver.setAmount(receiveAmount);
		sprinkleService.sprinkle(sprinkle);

		Mockito.when(sprinkleRepository.findByTokenAndRoomId(token, roomId)).thenReturn(sprinkle);
		Mockito.when(receiverRepository.existsBySprinkleAndUserId(sprinkle, receiverId)).thenReturn(false);
		Mockito.when(receiverRepository.findFirstBySprinkleAndUserIdIsNull(sprinkle)).thenReturn(receiver);

		// when
		// then
		Assert.assertEquals(receiveAmount, sprinkleService.receive(token, receiverId, roomId));
	}

	@Test
	public void test_receive_can_not_receive_exception() {
		// given
		sprinkleService.sprinkle(sprinkle);

		Mockito.when(sprinkleRepository.findByTokenAndRoomId(token, roomId)).thenReturn(sprinkle);

		// then
		expectedException.expectMessage("자신이 뿌리기한 건은 자신이 받을 수 없습니다.");
		expectedException.expect(ServiceException.class);

		// when
		sprinkleService.receive(token, userId, roomId);
	}

	@Test
	public void test_receive_receive_time_expired_exception() {
		// given
		sprinkle.setCreatedAt(Date.from(Instant.now().minus(Duration.ofMinutes(10))));
		sprinkleService.sprinkle(sprinkle);

		Mockito.when(sprinkleRepository.findByTokenAndRoomId(token, roomId)).thenReturn(sprinkle);

		// then
		expectedException.expectMessage("뿌린 건은 10분간만 유효합니다.");
		expectedException.expect(ServiceException.class);

		// when
		sprinkleService.receive(token, receiverId, roomId);
	}

	@Test
	public void test_receive_already_received_exception() {
		// given
		sprinkleService.sprinkle(sprinkle);

		Mockito.when(sprinkleRepository.findByTokenAndRoomId(token, roomId)).thenReturn(sprinkle);
		Mockito.when(receiverRepository.existsBySprinkleAndUserId(sprinkle, receiverId)).thenReturn(true);

		// then
		expectedException.expectMessage("뿌리기 당 한 사용자는 한번만 받을 수 있습니다.");
		expectedException.expect(ServiceException.class);

		// when
		sprinkleService.receive(token, receiverId, roomId);
	}

	@Test
	public void test_receive_no_amount_remain() {
		// given
		sprinkleService.sprinkle(sprinkle);

		Mockito.when(sprinkleRepository.findByTokenAndRoomId(token, roomId)).thenReturn(sprinkle);
		Mockito.when(receiverRepository.existsBySprinkleAndUserId(sprinkle, receiverId)).thenReturn(false);
		Mockito.when(receiverRepository.findFirstBySprinkleAndUserIdIsNull(sprinkle)).thenReturn(null);

		// then
		expectedException.expectMessage("더 이상 뿌릴 금액이 없습니다.");
		expectedException.expect(ServiceException.class);

		// when
		sprinkleService.receive(token, receiverId, roomId);
	}


	@Test
	public void test_get() {
		// given
		sprinkleService.sprinkle(sprinkle);
		Receiver receiver1 = new Receiver();
		receiver1.setAmount(1);
		Receiver receiver2 = new Receiver();
		receiver2.setAmount(2);
		Receiver receiver3 = new Receiver();
		receiver3.setAmount(3);
		List<Receiver> receiverList = Arrays.asList(receiver1, receiver2, receiver3);


		// when
		Mockito.when(sprinkleRepository.findByTokenAndUserIdAndRoomId(token, userId, roomId)).thenReturn(sprinkle);
		Mockito.when(receiverRepository.findBySprinkleAndUserIdIsNotNull(sprinkle)).thenReturn(receiverList);
		SprinkleGetResponse sprinkleGetResponse = sprinkleService.get(token, userId, roomId);

		// then
		Assert.assertEquals(sprinkle.getCreatedAt(), sprinkleGetResponse.getCreatedAt());
		Assert.assertEquals(sprinkle.getAmount(), sprinkleGetResponse.getSprinkleAmount());
		Assert.assertEquals(receiverList.size(), sprinkleGetResponse.getReceiverInfo().size());
		Assert.assertEquals(6, sprinkleGetResponse.getReceiveAmount().intValue());
	}

	@Test
	public void test_get_invalid_token_exception() {
		// given
		Mockito.when(sprinkleRepository.findByTokenAndUserIdAndRoomId(token, userId, roomId)).thenReturn(null);

		// then
		expectedException.expectMessage("유효하지 않은 토큰입니다.");
		expectedException.expect(ServiceException.class);

		// when
		sprinkleService.get(token, userId, roomId);
	}

	@Test
	public void test_get_receive_time_expired_exception() {
		// given
		sprinkle.setCreatedAt(Date.from(Instant.now().minus(Duration.ofDays(7))));
		sprinkleService.sprinkle(sprinkle);
		Mockito.when(sprinkleRepository.findByTokenAndUserIdAndRoomId(token, userId, roomId)).thenReturn(sprinkle);

		// then
		expectedException.expectMessage("뿌린 건에 대한 조회는 7일동안 할 수 있습니다.");
		expectedException.expect(ServiceException.class);

		// when
		sprinkleService.get(token, userId, roomId);
	}
}
