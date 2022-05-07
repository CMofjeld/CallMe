package com.callme.services.websocketservice.configuration;

import com.callme.services.websocketservice.service.WebsocketService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebsocketService websocketService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketService, "/websocket/**")
                .addInterceptors(handshakeInterceptor())
                .setAllowedOrigins("*");
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                // Get section of path associated with user ID
                String path = request.getURI().getPath();
                System.out.println(path);
                String userIdString = path.substring(path.lastIndexOf('/') + 1);
                try {
                    Long userId = Long.valueOf(userIdString);
                    attributes.put("userId", userId);
                    System.out.println("Successfully parsed user ID: " + userId);
                    return true;
                } catch (NumberFormatException e) {
                    System.err.println("Invalid path for handshake. Couldn't parse user ID to long.");
                    return false;
                }
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
                System.out.println("After handshake");
            }
        };
    }
}
