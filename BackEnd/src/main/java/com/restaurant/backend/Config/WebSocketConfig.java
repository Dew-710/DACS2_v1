package com.restaurant.backend.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.restaurant.backend.websocket.IoTWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final IoTWebSocketHandler ioTWebSocketHandler;

    @Autowired
    public WebSocketConfig(IoTWebSocketHandler ioTWebSocketHandler) {
        this.ioTWebSocketHandler = ioTWebSocketHandler;
    }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            System.out.println("🔧 Registering WebSocket handlers...");
            WebSocketHandshakeInterceptor interceptor = new WebSocketHandshakeInterceptor();
            
            // ESP32 devices
            registry.addHandler(ioTWebSocketHandler, "/ws/iot")
                    .setAllowedOrigins("*")
                    .addInterceptors(interceptor);
            System.out.println("✅ WebSocket handler registered: /ws/iot");

            // Kitchen display systems
            registry.addHandler(ioTWebSocketHandler, "/ws/kitchen")
                    .setAllowedOrigins("*")
                    .addInterceptors(interceptor);

            // Staff mobile apps
            registry.addHandler(ioTWebSocketHandler, "/ws/staff")
                    .setAllowedOrigins("*")
                    .addInterceptors(interceptor);
            System.out.println("✅ WebSocket handler registered: /ws/staff");
            System.out.println("✅ All WebSocket handlers registered successfully");
    }
}
