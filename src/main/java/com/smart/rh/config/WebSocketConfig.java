package com.smart.rh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket configuration.
 *
 * <h3>Client connection (Angular)</h3>
 * 
 * <pre>
 *   const socket = new SockJS('http://localhost:8080/ws');
 *   const client = Stomp.over(socket);
 *   client.connect({}, () => {
 *     client.subscribe('/topic/attendance', (msg) => { ... });
 *   });
 * </pre>
 *
 * <h3>Topics</h3>
 * <ul>
 * <li>{@code /topic/attendance} — broadcast on every successful recognition
 * event</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // In-memory simple broker: clients subscribe to /topic/**
        config.enableSimpleBroker("/topic");
        // Prefix for @MessageMapping controller methods (not used yet but reserved)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                // Allow Angular dev (4200, 4201, 4202) + common prod origins; SockJS enforces its own
                // CORS
                .setAllowedOriginPatterns(
                        "http://localhost:4200",
                        "http://localhost:4201",
                        "http://localhost:4202",
                        "http://localhost:80",
                        "http://localhost",
                        "http://127.0.0.1:4200",
                        "http://127.0.0.1:4201",
                        "http://127.0.0.1:4202")
                .withSockJS();
    }
}
