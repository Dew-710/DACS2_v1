package com.restaurant.backend.Service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.restaurant.backend.Service.QRCodeService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeServiceImpl implements QRCodeService {

    @Override
    public BufferedImage generateQRCodeImage(String text, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage qrImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            qrImage.createGraphics();

            Graphics2D graphics = (Graphics2D) qrImage.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }

            return qrImage;
        } catch (WriterException e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }

    @Override
    public byte[] generateQRCodeImageBytes(String text, int width, int height) {
        try {
            BufferedImage qrImage = generateQRCodeImage(text, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error converting QR code to bytes", e);
        }
    }
}

