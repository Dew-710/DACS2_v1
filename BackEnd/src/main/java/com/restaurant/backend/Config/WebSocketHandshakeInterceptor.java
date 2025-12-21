package com.restaurant.backend.Config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            System.out.println("🤝 WebSocket Handshake Request:");
            System.out.println("   URI: " + request.getURI());
            System.out.println("   Method: " + request.getMethod());
            System.out.println("   Headers: " + request.getHeaders());
            System.out.println("   Remote Address: " + request.getRemoteAddress());
            
            // Validate WebSocket upgrade request
            String upgradeHeader = request.getHeaders().getFirst("Upgrade");
            if (upgradeHeader == null || !upgradeHeader.equalsIgnoreCase("websocket")) {
                System.err.println("⚠️ Invalid WebSocket request - missing Upgrade header");
                System.err.println("   This might be a regular HTTP request instead of WebSocket");
                // Still allow, but log warning
            }
            
            // Allow all origins for ESP32 connections
            response.getHeaders().add("Access-Control-Allow-Origin", "*");
            response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.getHeaders().add("Access-Control-Allow-Headers", "*");
            
            return true; // Always allow handshake
        } catch (Exception e) {
            System.err.println("❌ Error in WebSocket handshake interceptor:");
            e.printStackTrace();
            return false; // Reject handshake on error
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            System.err.println("❌ WebSocket handshake failed: " + exception.getMessage());
            exception.printStackTrace();
        } else {
            System.out.println("✅ WebSocket handshake successful");
        }
    }
}
