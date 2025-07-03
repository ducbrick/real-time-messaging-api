package com.ducbrick.real_time_messaging_api.entities;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "message")
@NoArgsConstructor
@Getter @Setter
@Builder
@AllArgsConstructor
public class Message {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotEmpty
	private String content;

	@ManyToOne
	@JoinColumn(name = "sender_id")
	@NotNull
	@Valid
	private User sender;

	@ManyToMany
	@JoinTable(
			name = "message_receiver",
			joinColumns = @JoinColumn(name = "message_id"),
			inverseJoinColumns = @JoinColumn(name = "receiver_id")
	)
	private List<User> receivers;
}
