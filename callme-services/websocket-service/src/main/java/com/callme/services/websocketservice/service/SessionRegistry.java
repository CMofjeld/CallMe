package com.callme.services.websocketservice.service;

import com.callme.services.websocketservice.model.SessionEntry;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionRegistry {
    private final ConcurrentHashMap<String, SessionEntry> sessions = new ConcurrentHashMap<>();

    public Optional<SessionEntry> getSession(String key) {
        return sessions.containsKey(key) ?
                Optional.of(sessions.get(key)) :
                Optional.empty();
    }

    public void setSession(String key, SessionEntry entry) {
        sessions.put(key, entry);
    }

    public void removeSession(String key) {
        sessions.remove(key);
    }
}
