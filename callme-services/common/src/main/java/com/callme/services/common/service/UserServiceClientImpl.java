package com.callme.services.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

@Service
public class UserServiceClientImpl implements UserServiceClient {
    private RestTemplate restTemplate = new RestTemplate();
    @Value("${USER_SVC_URL}")
    private String userServiceURL;
    @Override
    public boolean userExists(Long id) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(userServiceURL + "/user/" + id, String.class);
        } catch (HttpClientErrorException e) {
            // Got 4XX - user doesn't exist
            return false;
        } catch (HttpServerErrorException e) {
            // Got 5xx - error with server
            System.err.println(e);
            return false;
        } catch (UnknownHttpStatusCodeException e) {
            // Unknown HTTP status code
            System.err.println(e);
            return false;
        }
        return true;
    }
}
