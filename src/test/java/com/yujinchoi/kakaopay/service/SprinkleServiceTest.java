package com.yujinchoi.kakaopay.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yujinchoi.kakaopay.model.Receiver;
import com.yujinchoi.kakaopay.model.Sprinkle;
import com.yujinchoi.kakaopay.model.request.CommonRequest;
import com.yujinchoi.kakaopay.model.request.SprinkleRequest;
import com.yujinchoi.kakaopay.model.response.SprinkleInfo;
import com.yujinchoi.kakaopay.repository.ReceiverRepository;
import com.yujinchoi.kakaopay.repository.SprinkleRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SprinkleServiceTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private SprinkleService sprinkleService;
	@Autowired
	private ReceiverRepository receiverRepository;
	@Autowired
	private SprinkleRepository sprinkleRepository;
	@Autowired
	private ObjectMapper objectMapper;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	String roomId = "rid-x12r-123";

	private HttpHeaders httpHeaders;
	private SprinkleRequest sprinkleRequest;
	private CommonRequest commonRequest;

	private static final int USER_ID = 1234;
	private static final String ROOM_ID = "RID-12345";

	private static final int AMOUNT = 10;
	private static final int USER_COUNT = 5;

	private static final String TOKEN = "aSd";
	private static final Integer RECEIVE_USER_ID = 12434;

	@Before
	public void setUp() throws Exception {
		httpHeaders = new HttpHeaders();
		httpHeaders.add("X-USER-ID", Integer.toString(USER_ID));
		httpHeaders.add("X-ROOM-ID", ROOM_ID);

		sprinkleRequest = new SprinkleRequest();
		sprinkleRequest.setAmount(AMOUNT);
		sprinkleRequest.setUserCount(USER_COUNT);

		commonRequest = new CommonRequest();
	}

	@Test
	@Transactional
	public void test() throws Exception {
		// 1. 뿌리기
		MvcResult sprinkleResult = mockMvc.perform(post("/sprinkle").headers(httpHeaders)
			.content(objectMapper.writeValueAsString(sprinkleRequest))
			.contentType("application/json"))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus()))
			.andReturn();

		Map sprinkleResultMap = objectMapper.readValue(sprinkleResult.getResponse().getContentAsString(), Map.class);
		String token = (String)sprinkleResultMap.get("token");
		Assert.assertEquals(3, token.length());

		List<Sprinkle> sprinkleList = sprinkleRepository.findAll();
		Assert.assertEquals(1, sprinkleList.size());
		Assert.assertEquals(AMOUNT, sprinkleList.get(0).getAmount().intValue());
		Assert.assertEquals(USER_COUNT, sprinkleList.get(0).getUserCount().intValue());
		Assert.assertEquals(USER_ID, sprinkleList.get(0).getUserId().intValue());
		Assert.assertEquals(token, sprinkleList.get(0).getToken());
		Assert.assertEquals(ROOM_ID, sprinkleList.get(0).getRoomId());

		List<Receiver> receiverList = receiverRepository.findAll();
		Assert.assertEquals(USER_COUNT, receiverList.size());
		int totalReceiverAmount = 0;
		for (Receiver receiver : receiverList) {
			Assert.assertEquals(sprinkleList.get(0), receiver.getSprinkle());
			Assert.assertNull(receiver.getUserId());
			totalReceiverAmount += receiver.getAmount();
		}
		Assert.assertEquals(AMOUNT, totalReceiverAmount);

		// 2. 받기
		commonRequest.setToken(token);
		MvcResult receiveResult = mockMvc.perform(post("/sprinkle/receive")
			.header("X-USER-ID", RECEIVE_USER_ID)
			.header("X-ROOM-ID", ROOM_ID)
			.content(objectMapper.writeValueAsString(commonRequest))
			.contentType("application/json"))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus()))
			.andReturn();

		Map receiveResultMap = objectMapper.readValue(receiveResult.getResponse().getContentAsString(), Map.class);
		Integer receiveAmount = (Integer)receiveResultMap.get("amount");
		Assert.assertTrue(receiveAmount < AMOUNT);
		Assert.assertTrue(receiveAmount > 0);

		// 3. 조회
		MvcResult getResult = mockMvc.perform(get("/sprinkle").headers(httpHeaders)
			.content(objectMapper.writeValueAsString(commonRequest))
			.contentType("application/json"))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()))
			.andReturn();

		SprinkleInfo sprinkleInfo = objectMapper.readValue(getResult.getResponse().getContentAsString(), SprinkleInfo.class);
		Assert.assertEquals(receiveAmount, sprinkleInfo.getReceiveAmount());
		Assert.assertEquals(AMOUNT, sprinkleInfo.getSprinkleAmount().intValue());
		Assert.assertEquals(1, sprinkleInfo.getReceiverInfo().size());
		Assert.assertEquals(receiveAmount, sprinkleInfo.getReceiverInfo().get(0).getAmount());
		Assert.assertEquals(RECEIVE_USER_ID, sprinkleInfo.getReceiverInfo().get(0).getUserId());
	}

	// @Test
	@Transactional
	public void test_receive() throws InterruptedException {
		Sprinkle sprinkle = new Sprinkle();
		String token = "1234";
		sprinkle.setToken(token);
		sprinkle.setRoomId("roomid");
		sprinkle.setUserId(1111);
		sprinkle.setUserCount(3);
		sprinkle.setAmount(100);
		sprinkleService.sprinkle(sprinkle);

		Sprinkle sprinkleByToken = sprinkleRepository.findByTokenAndRoomId(token, roomId);
		Assert.assertNotNull(sprinkleByToken);

		// when
		final ExecutorService executor = Executors.newFixedThreadPool(3);

		for (int i = 0; i < 3; i++) {
			executor.execute(() -> sprinkleService.receive(token, 1111, roomId));
		}
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);

		Assert.assertNotNull(sprinkleRepository.findByTokenAndRoomId(token, roomId));

		List<Receiver> receivers = receiverRepository.findAll();
		int totalAmount = 0;
		for (Receiver receiver : receivers) {
			totalAmount += receiver.getAmount();
			System.out.println(receiver);
		}

		Assert.assertEquals(100, totalAmount);
	}
}
