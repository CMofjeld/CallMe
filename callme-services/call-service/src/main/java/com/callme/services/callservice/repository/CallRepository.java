package com.callme.services.callservice.repository;

import com.callme.services.callservice.model.Call;
import org.springframework.data.repository.CrudRepository;

public interface CallRepository extends CrudRepository<Call, String> {
}
