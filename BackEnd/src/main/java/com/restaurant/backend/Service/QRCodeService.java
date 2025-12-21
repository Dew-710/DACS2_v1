package com.restaurant.backend.Service;

import java.awt.image.BufferedImage;

public interface QRCodeService {
    BufferedImage generateQRCodeImage(String text, int width, int height);
    byte[] generateQRCodeImageBytes(String text, int width, int height);
}

