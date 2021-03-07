package com.yujinchoi.kakaopay.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yujinchoi.kakaopay.exception.ErrorCode;
import com.yujinchoi.kakaopay.exception.ErrorResult;
import com.yujinchoi.kakaopay.exception.ServiceException;
import com.yujinchoi.kakaopay.model.Sprinkle;
import com.yujinchoi.kakaopay.model.request.SprinkleRequest;
import com.yujinchoi.kakaopay.model.response.SprinkleGetResponse;
import com.yujinchoi.kakaopay.model.response.SprinkleReceiveResponse;
import com.yujinchoi.kakaopay.model.response.SprinkleResponse;
import com.yujinchoi.kakaopay.service.SprinkleService;

@WebMvcTest(controllers = SprinkleController.class)
@RunWith(SpringRunner.class)
public class SprinkleControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	SprinkleService sprinkleService;

	private HttpHeaders httpHeaders;
	private SprinkleRequest sprinkleRequest;

	private static final int USER_ID = 1234;
	private static final String ROOM_ID = "RID-12345";

	private static final int AMOUNT = 10;
	private static final int USER_COUNT = 5;

	private static final String TOKEN = "aSd";

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
	public void test_sprinkle() throws Exception {
		MvcResult result = mockMvc.perform(post("/sprinkle").headers(httpHeaders)
			.content(objectMapper.writeValueAsString(sprinkleRequest))
			.contentType("application/json"))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus()))
			.andReturn();

		String content = result.getResponse().getContentAsString();
		SprinkleResponse response = objectMapper.readValue(content, SprinkleResponse.class);

		ArgumentCaptor<Sprinkle> argumentCaptor = ArgumentCaptor.forClass(Sprinkle.class);
		Mockito.verify(sprinkleService, Mockito.times(1)).sprinkle(argumentCaptor.capture());
		Assert.assertEquals(USER_ID, argumentCaptor.getValue().getUserId().intValue());
		Assert.assertEquals(ROOM_ID, argumentCaptor.getValue().getRoomId());
		Assert.assertEquals(AMOUNT, argumentCaptor.getValue().getAmount().intValue());
		Assert.assertEquals(USER_COUNT, argumentCaptor.getValue().getUserCount().intValue());
		Assert.assertEquals(3, argumentCaptor.getValue().getToken().length());
		Assert.assertEquals(argumentCaptor.getValue().getToken(), response.getToken());
	}

	@Test
	public void test_sprinkle_missing_user_id_header() throws Exception {
		mockMvc.perform(post("/sprinkle").header("X-ROOM-ID", ROOM_ID))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()))
			.andExpect(
				mvcResult -> Assert.assertEquals(
					"Missing request header 'X-USER-ID' for method parameter of type int",
					mvcResult.getResponse().getErrorMessage()));
		Mockito.verify(sprinkleService, Mockito.never()).sprinkle(Mockito.any(Sprinkle.class));
	}

	@Test
	public void test_sprinkle_missing_room_id_header() throws Exception {
		mockMvc.perform(post("/sprinkle").header("X-USER-ID", USER_ID))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()))
			.andExpect(
				mvcResult -> Assert.assertEquals(
					"Missing request header 'X-ROOM-ID' for method parameter of type String",
					mvcResult.getResponse().getErrorMessage()));
		Mockito.verify(sprinkleService, Mockito.never()).sprinkle(Mockito.any(Sprinkle.class));
	}

	@Test
	public void test_sprinkle_missing_request_body() throws Exception {
		mockMvc.perform(post("/sprinkle").headers(httpHeaders)
			.contentType("application/json"))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()))
			.andReturn();
		Mockito.verify(sprinkleService, Mockito.never()).sprinkle(Mockito.any(Sprinkle.class));
	}

	@Test
	public void test_receive() throws Exception {
		Mockito.when(sprinkleService.receive(TOKEN, USER_ID, ROOM_ID)).thenReturn(AMOUNT);

		mockMvc.perform(post("/sprinkle/receive/{token}", TOKEN).headers(httpHeaders))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus()))
			.andExpect(mvcResult -> {
					SprinkleReceiveResponse response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), SprinkleReceiveResponse.class);
					Assert.assertEquals(AMOUNT, response.getAmount().intValue());
				}
			);

		Mockito.verify(sprinkleService, Mockito.times(1)).receive(TOKEN, USER_ID, ROOM_ID);
	}

	@Test
	public void test_receive_missing_user_id_header() throws Exception {
		mockMvc.perform(post("/sprinkle/receive/{token}", TOKEN).header("X-ROOM-ID", ROOM_ID))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()))
			.andExpect(
				mvcResult -> Assert.assertEquals(
					"Missing request header 'X-USER-ID' for method parameter of type int",
					mvcResult.getResponse().getErrorMessage()));
		Mockito.verify(sprinkleService, Mockito.never())
			.receive(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void test_receive_missing_room_id_header() throws Exception {
		mockMvc.perform(post("/sprinkle/receive/{token}", TOKEN).header("X-USER-ID", USER_ID))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()))
			.andExpect(
				mvcResult -> Assert.assertEquals(
					"Missing request header 'X-ROOM-ID' for method parameter of type String",
					mvcResult.getResponse().getErrorMessage()));
		Mockito.verify(sprinkleService, Mockito.never())
			.receive(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void test_getSprinkle() throws Exception {
		Mockito.when(sprinkleService.get(TOKEN, USER_ID, ROOM_ID)).thenReturn(new SprinkleGetResponse());

		mockMvc.perform(get("/sprinkle/{token}", TOKEN).headers(httpHeaders))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()));

		Mockito.verify(sprinkleService, Mockito.times(1)).get(TOKEN, USER_ID, ROOM_ID);
	}

	@Test
	public void test_getSprinkle_missing_user_id_header() throws Exception {
		mockMvc.perform(get("/sprinkle/{token}", TOKEN).header("X-ROOM-ID", ROOM_ID))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()))
			.andExpect(
				mvcResult -> Assert.assertEquals(
					"Missing request header 'X-USER-ID' for method parameter of type int",
					mvcResult.getResponse().getErrorMessage()));

		Mockito.verify(sprinkleService, Mockito.never()).get(TOKEN, USER_ID, ROOM_ID);
	}

	@Test
	public void test_getSprinkle_missing_room_id_header() throws Exception {
		mockMvc.perform(get("/sprinkle/{token}", TOKEN).header("X-USER-ID", USER_ID))
			.andExpect(
				mvcResult -> Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()))
			.andExpect(
				mvcResult -> Assert.assertEquals(
					"Missing request header 'X-ROOM-ID' for method parameter of type String",
					mvcResult.getResponse().getErrorMessage()));

		Mockito.verify(sprinkleService, Mockito.never()).get(TOKEN, USER_ID, ROOM_ID);
	}

	@Test
	public void test_sprinkleService_throw_ServiceException() throws Exception {
		Mockito.when(sprinkleService.get(TOKEN, USER_ID, ROOM_ID)).thenThrow(new ServiceException(
			ErrorCode.INVALID_TOKEN));

		MvcResult result = mockMvc.perform(get("/sprinkle/{token}", TOKEN).headers(httpHeaders))
			.andExpect(
				mvcResult -> Assert.assertEquals(ErrorCode.INVALID_TOKEN.getHttpStatus().value(),
					mvcResult.getResponse().getStatus()))
			.andReturn();

		ErrorResult errorResult = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResult.class);
		Assert.assertEquals(ErrorCode.INVALID_TOKEN.getCode(), errorResult.getCode());
		Assert.assertEquals(ErrorCode.INVALID_TOKEN.getMessage(), errorResult.getMessage());
	}

	@Test
	public void test_sprinkleService_throw_ObjectOptimisticLockingFailureException() throws Exception {
		Mockito.when(sprinkleService.receive(TOKEN, USER_ID, ROOM_ID)).thenThrow(new ObjectOptimisticLockingFailureException("", new Throwable()));

		MvcResult result = mockMvc.perform(post("/sprinkle/receive/{token}", TOKEN).headers(httpHeaders))
			.andExpect(
				mvcResult -> Assert.assertEquals(ErrorCode.TRY_LATER.getHttpStatus().value(),
					mvcResult.getResponse().getStatus()))
			.andReturn();

		ErrorResult errorResult = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResult.class);
		Assert.assertEquals(ErrorCode.TRY_LATER.getCode(), errorResult.getCode());
		Assert.assertEquals(ErrorCode.TRY_LATER.getMessage(), errorResult.getMessage());
	}

	@Test
	public void test_sprinkleService_throw_Exception() throws Exception {
		Mockito.when(sprinkleService.receive(TOKEN, USER_ID, ROOM_ID)).thenThrow(new IllegalArgumentException());

		MvcResult result = mockMvc.perform(post("/sprinkle/receive/{token}", TOKEN).headers(httpHeaders))
			.andExpect(
				mvcResult -> Assert.assertEquals(ErrorCode.INTERNAL_ERROR.getHttpStatus().value(),
					mvcResult.getResponse().getStatus()))
			.andReturn();

		ErrorResult errorResult = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResult.class);
		Assert.assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), errorResult.getCode());
		Assert.assertEquals(ErrorCode.INTERNAL_ERROR.getMessage(), errorResult.getMessage());
	}
}
