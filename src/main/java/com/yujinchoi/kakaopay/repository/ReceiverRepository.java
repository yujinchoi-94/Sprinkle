package com.yujinchoi.kakaopay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yujinchoi.kakaopay.model.entity.Receiver;
import com.yujinchoi.kakaopay.model.entity.Sprinkle;

@Repository
public interface ReceiverRepository extends JpaRepository<Receiver, Long> {
	boolean existsBySprinkleAndUserId(Sprinkle sprinkle, Integer userId);
	Receiver findFirstBySprinkleAndUserIdIsNull(Sprinkle sprinkle);
	List<Receiver> findBySprinkleAndUserIdIsNotNull(Sprinkle sprinkle);
}
