package com.callme.services.common.service;

import com.callme.services.common.model.UserStatusMapper;
import com.callme.services.common.model.UserStatusView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class StatusServiceClientImpl implements StatusServiceClient {
    private RestTemplate restTemplate = new RestTemplate();
    private UserStatusMapper userStatusMapper = new UserStatusMapper();
    @Value("${STATUS_SVC_URL}")
    private String statusServiceURL;
    @Override
    public boolean setUserStatus(UserStatusView userStatusView) {
        System.out.println("Sending request to status service to set user %d status to %s".formatted(
                userStatusView.getId(),
                userStatusView.getStatus()
        ));
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(userStatusMapper.serializeUserStatus(userStatusView), headers);
            ResponseEntity<?> response = restTemplate.exchange(
                    statusServiceURL + "/status",
                    HttpMethod.POST,
                    request,
                    String.class
            );
            return response.getStatusCodeValue() == 201;
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    @Override
    public Optional<UserStatusView> getUserStatus(Long id) {
        System.out.println("Sending request to status service to get user %d's status".formatted(id));
        try {
            UserStatusView userStatusView = restTemplate.getForObject(
                    statusServiceURL + "/status/" + id,
                    UserStatusView.class
            );
            return Optional.of(userStatusView);
        } catch (Exception e) {
            System.err.println(e);
        }
        return Optional.empty();
    }
}
