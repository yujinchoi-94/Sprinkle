package com.yujinchoi.kakaopay;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.List;

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
import com.yujinchoi.kakaopay.model.request.SprinkleRequest;
import com.yujinchoi.kakaopay.model.response.SprinkleGetResponse;
import com.yujinchoi.kakaopay.model.response.SprinkleReceiveResponse;
import com.yujinchoi.kakaopay.model.response.SprinkleResponse;
import com.yujinchoi.kakaopay.repository.ReceiverRepository;
import com.yujinchoi.kakaopay.repository.SprinkleRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SprinkleIntegrationTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ReceiverRepository receiverRepository;
	@Autowired
	private SprinkleRepository sprinkleRepository;
	@Autowired
	private ObjectMapper objectMapper;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private HttpHeaders httpHeaders;
	private SprinkleRequest sprinkleRequest;

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

		SprinkleResponse sprinkleResponse = objectMapper.readValue(sprinkleResult.getResponse().getContentAsString(), SprinkleResponse.class);
		String token = sprinkleResponse.getToken();
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
		MvcResult receiveResult = mockMvc.perform(post("/sprinkle/receive/{token}", token)
			.header("X-USER-ID", RECEIVE_USER_ID)
			.header("X-ROOM-ID", ROOM_ID))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus()))
			.andReturn();

		SprinkleReceiveResponse sprinkleReceiveResponse = objectMapper.readValue(receiveResult.getResponse().getContentAsString(), SprinkleReceiveResponse.class);
		Assert.assertTrue(sprinkleReceiveResponse.getAmount() < AMOUNT);
		Assert.assertTrue(sprinkleReceiveResponse.getAmount() > 0);

		// 3. 조회
		MvcResult getResult = mockMvc.perform(get("/sprinkle/{token}", token).headers(httpHeaders)
			.contentType("application/json"))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()))
			.andReturn();

		SprinkleGetResponse sprinkleGetResponse = objectMapper.readValue(getResult.getResponse().getContentAsString(), SprinkleGetResponse.class);
		Assert.assertEquals(sprinkleReceiveResponse.getAmount(), sprinkleGetResponse.getReceiveAmount());
		Assert.assertEquals(AMOUNT, sprinkleGetResponse.getSprinkleAmount().intValue());
		Assert.assertEquals(1, sprinkleGetResponse.getReceiverInfo().size());
		Assert.assertEquals(sprinkleReceiveResponse.getAmount(), sprinkleGetResponse.getReceiverInfo().get(0).getAmount());
		Assert.assertEquals(RECEIVE_USER_ID, sprinkleGetResponse.getReceiverInfo().get(0).getUserId());
	}
}
