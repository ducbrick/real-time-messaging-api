package com.ducbrick.real_time_messaging_api.repos;

import com.ducbrick.real_time_messaging_api.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MsgRepo extends JpaRepository<Message, Integer> {
}
