package com.yujinchoi.kakaopay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yujinchoi.kakaopay.model.Sprinkle;

public interface SprinkleRepository extends JpaRepository<Sprinkle, Long> {
	Sprinkle findByTokenAndRoomId(String token, String roomId);
	Sprinkle findByTokenAndUserIdAndRoomId(String token, int userId, String roomId);
}
