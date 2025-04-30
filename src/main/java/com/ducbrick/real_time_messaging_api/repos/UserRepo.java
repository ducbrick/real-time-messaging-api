package com.ducbrick.real_time_messaging_api.repos;

import com.ducbrick.real_time_messaging_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository <User, Integer> {
}
