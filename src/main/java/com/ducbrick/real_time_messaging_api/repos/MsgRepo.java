package com.ducbrick.real_time_messaging_api.repos;

import com.ducbrick.real_time_messaging_api.entities.Message;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MsgRepo extends JpaRepository<Message, Integer> {
	@Query("""
			SELECT m
			FROM Message m
			JOIN m.receivers r
			WHERE
				m.id < :cursor
				AND
				(
					(
						m.sender.id = :id1
						AND
						r.id = :id2
					)
					OR (
						m.sender.id = :id2
						AND
						r.id = :id1
					)
				)
			ORDER BY m.id DESC
			""")
	List<Message> getMsgHistory(@Param("id1") int id1, @Param("id2") int id2, @Param("cursor") int cursor, Limit limit);

	@Query("""
			SELECT m
			FROM Message m
			JOIN m.receivers r
			WHERE
			(
				m.sender.id = :id1
				AND
				r.id = :id2
			)
			OR (
				m.sender.id = :id2
				AND
				r.id = :id1
			)
			ORDER BY m.id DESC
			""")
	List<Message> getMsgHistory(@Param("id1") int id1, @Param("id2") int id2, Limit limit);
}
