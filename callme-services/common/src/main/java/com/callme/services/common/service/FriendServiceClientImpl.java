package com.callme.services.common.service;

import com.callme.services.common.model.FriendQuery;
import com.callme.services.common.model.UserStatusView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class FriendServiceClientImpl implements FriendServiceClient {
    private RestTemplate restTemplate = new RestTemplate();
    @Value("${FRIEND_SVC_URL}")
    private String friendServiceURL;

    @Override
    public boolean areFriends(Long user1, Long user2) {
        System.out.println("Sending request to friend service to check if user %d and %d are friends".formatted(
                user1,
                user2
        ));
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            FriendQuery query = new FriendQuery(user1, user2);
            HttpEntity<String> request = new HttpEntity<>(new ObjectMapper().writeValueAsString(query), headers);
            ResponseEntity<?> response = restTemplate.exchange(
                    friendServiceURL + "/friends/are_friends",
                    HttpMethod.POST,
                    request,
                    String.class
            );
            return response.getStatusCodeValue() == 200;
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }
}
