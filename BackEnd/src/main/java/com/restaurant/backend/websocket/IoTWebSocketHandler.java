package com.restaurant.backend.websocket;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IoTWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> readySessions = ConcurrentHashMap.newKeySet();
    private final Set<WebSocketSession> kitchenSessions = ConcurrentHashMap.newKeySet();
    private final Set<WebSocketSession> staffSessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Determine client type based on query parameters
        String clientType = getClientType(session);

        switch (clientType) {
            case "esp32":
                readySessions.add(session);
                System.out.println("🔗 ESP32 connected: " + session.getId());
                break;
            case "kitchen":
                kitchenSessions.add(session);
                System.out.println("👨‍🍳 Kitchen display connected: " + session.getId());
                break;
            case "staff":
                staffSessions.add(session);
                System.out.println("👤 Staff app connected: " + session.getId());
                break;
            default:
                readySessions.add(session); // Default to ESP32
                System.out.println("🔗 Unknown client connected: " + session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        readySessions.remove(session);
        kitchenSessions.remove(session);
        staffSessions.remove(session);
        System.out.println("🔌 Client disconnected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        String clientType = getClientType(session);

        System.out.println("📩 From " + clientType + ": " + payload);

        if ("ESP32 ready!".equals(payload)) {
            try {
                session.sendMessage(new TextMessage("Server received: ESP32 ready!"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if ("IMAGE_OK".equals(payload)) {
            System.out.println("✅ ESP32 confirmed image reception");
        }
    }

    /**
     * Send notification to kitchen displays
     */
    public void notifyKitchen(String message) {
        notifyClients(kitchenSessions, "KITCHEN:" + message);
    }

    /**
     * Send notification to staff apps
     */
    public void notifyStaff(String message) {
        notifyClients(staffSessions, "STAFF:" + message);
    }

    /**
     * Send notification to ESP32 devices
     */
    public void notifyEsp32(String message) {
        notifyClients(readySessions, "ESP32:" + message);
    }

    /**
     * Notify new order to kitchen and staff
     */
    public void notifyNewOrder(String tableName, String orderDetails) {
        String message = "NEW_ORDER|" + tableName + "|" + orderDetails;
        notifyKitchen(message);
        notifyStaff(message);
    }

    /**
     * Notify order status update
     */
    public void notifyOrderStatusUpdate(String tableName, String status) {
        String message = "ORDER_UPDATE|" + tableName + "|" + status;
        notifyKitchen(message);
        notifyStaff(message);
    }

    private void notifyClients(Set<WebSocketSession> sessions, String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("Failed to send message to session " + session.getId());
                }
            }
        }
    }

    private String getClientType(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("client=kitchen")) {
            return "kitchen";
        } else if (query != null && query.contains("client=staff")) {
            return "staff";
        } else if (query != null && query.contains("client=esp32")) {
            return "esp32";
        }
        return "unknown";
    }

    /** Gửi ảnh JPEG tới tất cả ESP32 đang kết nối */
    public void broadcastImageBytes(byte[] jpgData) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(jpgData));
            if (img == null) {
                System.err.println("❌ Cannot decode image (null)");
                return;
            }

            System.out.println("📷 Original image: " + img.getWidth() + "x" + img.getHeight());

            // Resize cho ST7735S (128x160)
            // Resize ảnh không bị méo – giữ nguyên tỷ lệ
            int lcdW = 160;
            int lcdH = 128;

// Tính tỷ lệ ảnh gốc
            double imgRatio = (double) img.getWidth() / img.getHeight();
            double lcdRatio = (double) lcdW / lcdH;

            int newW, newH;

// Nếu ảnh rộng hơn so với màn
            if (imgRatio > lcdRatio) {
                newW = lcdW;
                newH = (int) (lcdW / imgRatio);
            } else {
                newH = lcdH;
                newW = (int) (lcdH * imgRatio);
            }

// Tạo ảnh scale đúng tỷ lệ
            Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);

// Tạo canvas LCD
            BufferedImage canvas = new BufferedImage(lcdW, lcdH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = canvas.createGraphics();

// Nền đen (hoặc đổi màu nếu muốn)
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, lcdW, lcdH);

// Căn giữa ảnh
            int x = (lcdW - newW) / 2;
            int y = (lcdH - newH) / 2;

            g.drawImage(scaled, x, y, null);
            g.dispose();

// Gán lại để encode JPEG
            BufferedImage resized = canvas;


            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.drawImage(scaled, 0, 0, null);
            g.dispose();

            ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();

            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(0.85f);
            }

            ImageOutputStream ios = ImageIO.createImageOutputStream(jpegOut);
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(resized, null, null), writeParam);
            writer.dispose();
            ios.close();

            byte[] resizedJpeg = jpegOut.toByteArray();
            System.out.println("📐 Resized to 128x160, JPEG size: " + resizedJpeg.length + " bytes");

            // Gửi theo chunk
            String base64 = Base64.getEncoder().encodeToString(resizedJpeg);
            int chunkSize = 4000;
            int total = (int) Math.ceil((double) base64.length() / chunkSize);

            // 🧩 Debug: lưu base64 ra file để kiểm tra hoặc decode offline
            try {
                String debugPath = "debug_base64_" + System.currentTimeMillis() + ".txt";
                java.nio.file.Files.write(java.nio.file.Paths.get(debugPath), base64.getBytes());
                System.out.println("🪶 Base64 debug saved to: " + debugPath);
                System.out.println("📏 Base64 length: " + base64.length() + " chars");
            } catch (IOException e) {
                System.err.println("⚠️ Failed to save base64 debug file: " + e.getMessage());
            }


            System.out.println("📤 Sending " + total + " chunks to ESP32...");

            for (int i = 0; i < total; i++) {
                int start = i * chunkSize;
                int end = Math.min(base64.length(), start + chunkSize);
                String part = base64.substring(start, end);
                String msg = "IMG|" + (i + 1) + "/" + total + "|" + part;

                for (WebSocketSession s : readySessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(msg));
                    }
                }

                Thread.sleep(40); // chờ giữa các gói để tránh nghẽn bộ đệm
            }

            System.out.println("✅ Sent JPEG image to ESP32 (" + total + " chunks, " + resizedJpeg.length + " bytes)");

        } catch (Exception e) {
            System.err.println("❌ Error broadcasting image:");
            e.printStackTrace();
        }
    }
}
