package com.restaurant.backend.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to log all requests to /ws/** endpoints for debugging
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebSocketRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        
        if (path != null && path.startsWith("/ws/")) {
            System.out.println("🔍 Filter: Request to WebSocket endpoint:");
            System.out.println("   Path: " + path);
            System.out.println("   Method: " + httpRequest.getMethod());
            System.out.println("   Upgrade Header: " + httpRequest.getHeader("Upgrade"));
            System.out.println("   Connection Header: " + httpRequest.getHeader("Connection"));
            System.out.println("   Sec-WebSocket-Key: " + httpRequest.getHeader("Sec-WebSocket-Key"));
            System.out.println("   Sec-WebSocket-Version: " + httpRequest.getHeader("Sec-WebSocket-Version"));
            System.out.println("   Forwarding to next filter/handler...");
        }
        
        try {
            chain.doFilter(request, response);
            if (path != null && path.startsWith("/ws/")) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                System.out.println("   ✅ Filter chain completed for: " + path);
                System.out.println("   Response Status: " + httpResponse.getStatus());
                System.out.println("   Response Headers: " + httpResponse.getHeaderNames());
            }
        } catch (Exception e) {
            if (path != null && path.startsWith("/ws/")) {
                System.err.println("   ❌ Error in filter chain for: " + path);
                System.err.println("   Error: " + e.getMessage());
                e.printStackTrace();
            }
            throw e;
        }
    }
}
