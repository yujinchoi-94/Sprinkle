package com.yujinchoi.kakaopay.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Receiver {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private Integer userId;
	private Integer amount;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sprinkle_id")
	private Sprinkle sprinkle;
	@Version
	private Long version;
}
