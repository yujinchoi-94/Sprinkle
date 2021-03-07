package com.yujinchoi.kakaopay.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.yujinchoi.kakaopay.model.entity.Receiver;
import com.yujinchoi.kakaopay.model.entity.Sprinkle;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ReceiverRepositoryTest {
	@Autowired
	ReceiverRepository receiverRepository;
	@Autowired
	SprinkleRepository sprinkleRepository;

	private Receiver receiver;
	private static final Integer AMOUNT = 10 ;
	private static final Integer RECEIVE_AMOUNT = 4;
	private static final Integer USER_ID = 124;
	private static final String ROOM_ID = "RID-1234";
	private static final Integer RECEIVE_USER_ID = 12434;
	private Sprinkle sprinkle;
	private static final String TOKEN = "aEa";
	private static final Integer USER_COUNT = 3;

	@Before
	public void setUp() throws Exception {
		sprinkle = new Sprinkle();
		sprinkle.setUserId(USER_ID);
		sprinkle.setRoomId(ROOM_ID);
		sprinkle.setToken(TOKEN);
		sprinkle.setAmount(AMOUNT);
		sprinkle.setUserCount(USER_COUNT);

		receiver = new Receiver();
		receiver.setAmount(RECEIVE_AMOUNT);
		receiver.setUserId(RECEIVE_USER_ID);
		receiver.setSprinkle(sprinkle);
	}

	@Test
	public void test_existsBySprinkleAndUserId() {
		sprinkleRepository.save(sprinkle);
		receiverRepository.save(receiver);

		Assert.assertTrue(receiverRepository.existsBySprinkleAndUserId(sprinkle, RECEIVE_USER_ID));
	}

	@Test
	public void test_findFirstBySprinkleAndUserIdIsNull() {
		receiver.setUserId(null);

		sprinkleRepository.save(sprinkle);
		receiverRepository.save(receiver);

		Receiver queryResult = receiverRepository.findFirstBySprinkleAndUserIdIsNull(sprinkle);
		Assert.assertNull(queryResult.getUserId());
	}
}
