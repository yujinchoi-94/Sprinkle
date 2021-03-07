package com.yujinchoi.kakaopay.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Sprinkle {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	private Integer userId;
	private String roomId;
	private Integer userCount;
	private String token;
	private Integer amount;
	private Date createdAt;
	@PrePersist
	public void beforeCreate() {
		createdAt = new Date();
	}
}
