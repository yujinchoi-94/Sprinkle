# 개발 환경
- Spring boot 2.1.8
- Spring Data JPA
- H2 DB
- Maven
- Swagger

# API
- swagger : http://localhost:8080/swagger-ui.html
- schema를 누르면 파라미터에 대한 설명을 볼 수 있습니다.

## 공통 헤더

| 헤더명    | type    | description    |
| --------- | ------- | -------------- |
| X-USER-ID | integer | 요청 아이디    |
| X-ROOM-ID | string  | 요청 룸 아이디 |



## 뿌리기

```
POST /sprinkle
```

### Request Fields

| 필드명    | type    | description        |
| --------- | ------- | ------------------ |
| Amount    | integer | 뿌릴 금액 (min: 1) |
| userCount | Integer | 뿌릴 인원 (min: 1) |

### Response Fields

| description | type    | description        |
| :---------- | ------- | ------------------ |
| token       | integer | 생성된 뿌리기 토큰 |



## 뿌린 금액 받기

```
POST /sprinkle/receive/{token}
```

### Path Parameters

| 필드명 | type    | description      |
| ------ | ------- | ---------------- |
| token  | integer | 받을 뿌리기 토큰 |

### Response Fields

| description | type    | description |
| :---------- | ------- | ----------- |
| amount      | integer | 받은 금액   |

### Error Response

| HttpStatus | error code | message                                        |
| ---------- | ---------- | ---------------------------------------------- |
| 400        | 1000       | 뿌리기 당 한 사용자는 한번만 받을 수 있습니다. |
| 400        | 1001       | 뿌린 건은 10분간만 유효합니다.                 |
| 400        | 1004       | 더 이상 뿌릴 금액이 없습니다.                  |
| 400        | 1005       | 유효하지 않은 토큰입니다.                      |
| 503        | 1006       | 잠시 후 다시 시도하세요.                       |



## 뿌리기 조회

```
GET /sprinkle/{token}
```

### Path Parameters

| 필드명 | type    | description        |
| ------ | ------- | ------------------ |
| token  | integer | 조회할 뿌리기 토큰 |

### Response Fields

| description         | type    | description           |
| :------------------ | ------- | --------------------- |
| sprinkleAmount      | integer | 받은 금액             |
| createdAt           | date    | 뿌리기 된 시각        |
| receiveAmount       | integer | 전체 받기 완료된 금액 |
| receiverInfo        | List    |                       |
| receiverInfo.userId | int     | 받은 사용자 아이디    |
| receiverInfo.amount | int     | 받은 금액             |

### Error Response

| HttpStatus | error code | message                                       |
| ---------- | ---------- | --------------------------------------------- |
| 400        | 1002       | 뿌린 건에 대한 조회는 7일동안 할 수 있습니다. |
| 400        | 1005       | 유효하지 않은 토큰입니다.                     |



# 핵심 문제 해결 전략
- 뿌리기 API
  - 뿌리기 대상 유저가 많아 RECEIVER 테이블에 INSERT 쿼리가 많이 발생하더라도 성능에 영향이 없도록 JPA의 지연 쓰기를 사용했다. 이를 위해 키의 생성 방식을 `SEQUENCE`로 사용했다.
- 받기 API
  - 받기 API가 동시 호출 될 경우 데이터의 유실을 막기 위해 RECEIVER 테이블에 Version 필드를 추가하여 Optimistic Lock을 적용하였다.
- 조회 API
  - `@Transactional(readOnly = true)`로 설정하여 dirty checking이 발생하지 않도록 하여 읽기 성능을 높였다.
  
# DB 스키마
<img width="714" alt="스크린샷 2021-03-07 오후 7 21 30" src="https://user-images.githubusercontent.com/30385786/110236651-5395dc80-7f7a-11eb-899f-b53f9ec7c1d2.png">