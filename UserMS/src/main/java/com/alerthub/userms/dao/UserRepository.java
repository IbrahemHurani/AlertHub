package com.alerthub.userms.dao;

import com.alerthub.userms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
