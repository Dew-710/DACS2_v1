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
            // ESP32 devices
            registry.addHandler(ioTWebSocketHandler, "/ws/iot")
                    .setAllowedOrigins("*");

            // Kitchen display systems
            registry.addHandler(ioTWebSocketHandler, "/ws/kitchen")
                    .setAllowedOrigins("*");

            // Staff mobile apps
            registry.addHandler(ioTWebSocketHandler, "/ws/staff")
                    .setAllowedOrigins("*");
    }
}
