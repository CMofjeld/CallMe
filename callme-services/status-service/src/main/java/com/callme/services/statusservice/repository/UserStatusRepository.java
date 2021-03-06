package com.callme.services.statusservice.repository;

import com.callme.services.statusservice.model.UserStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatusRepository extends CrudRepository<UserStatus, Long> {
}
