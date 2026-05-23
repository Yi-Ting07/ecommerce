package com.ecommerce.backend.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 驗證碼服務
 * - 產生帶有數字的圖像驗證碼
 * - 以 UUID 儲存驗證碼，設定 5 分鐘有效期
 * - 一次性使用：驗證成功或失敗後均從 Map 移除
 */
@Service
public class CaptchaService {

    private static final int CAPTCHA_EXPIRY_MINUTES = 5;
    private static final int CAPTCHA_LENGTH = 4;
    private static final int IMAGE_WIDTH = 130;
    private static final int IMAGE_HEIGHT = 44;

    private final Map<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public record CaptchaResult(String captchaId, String captchaImage) {}

    private record CaptchaEntry(String code, LocalDateTime expiresAt) {}

    /**
     * 產生新驗證碼，回傳 UUID 與 base64 圖像
     */
    public CaptchaResult generateCaptcha() {
        // 產生 4 位數字驗證碼
        int number = secureRandom.nextInt((int) Math.pow(10, CAPTCHA_LENGTH));
        String code = String.format("%0" + CAPTCHA_LENGTH + "d", number);

        String captchaId = UUID.randomUUID().toString();
        captchaStore.put(captchaId,
                new CaptchaEntry(code, LocalDateTime.now().plusMinutes(CAPTCHA_EXPIRY_MINUTES)));

        // 定期清理過期項目
        cleanExpired();

        String base64Image = generateImage(code);
        return new CaptchaResult(captchaId, base64Image);
    }

    /**
     * 驗證使用者輸入的驗證碼（一次性，驗證後自動移除）
     */
    public boolean validateCaptcha(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) return false;

        CaptchaEntry entry = captchaStore.remove(captchaId);
        if (entry == null) return false;

        if (LocalDateTime.now().isAfter(entry.expiresAt())) return false;

        return entry.code().equals(userInput.trim());
    }

    /**
     * 以 BufferedImage + Graphics2D 產生含干擾線與雜點的驗證碼圖片
     */
    private String generateImage(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 開啟抗鋸齒
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 白色背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 灰色干擾線（5 條）
        g.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(180 + secureRandom.nextInt(60),
                                 180 + secureRandom.nextInt(60),
                                 180 + secureRandom.nextInt(60)));
            g.drawLine(secureRandom.nextInt(IMAGE_WIDTH), secureRandom.nextInt(IMAGE_HEIGHT),
                       secureRandom.nextInt(IMAGE_WIDTH), secureRandom.nextInt(IMAGE_HEIGHT));
        }

        // 數字顏色組
        int[] colors = {0x1a73e8, 0xe53935, 0x43a047, 0xfb8c00, 0x8e24aa};

        // 繪製每個數字（帶輕微隨機旋轉）
        Font font = new Font("Arial", Font.BOLD, 26);
        g.setFont(font);

        int charSpacing = (IMAGE_WIDTH - 20) / CAPTCHA_LENGTH;
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(colors[i % colors.length]));
            int x = 10 + i * charSpacing + secureRandom.nextInt(4);
            int y = IMAGE_HEIGHT - 8;
            double angle = (secureRandom.nextDouble() - 0.5) * 0.5;
            g.rotate(angle, x + 8, y - 10);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-angle, x + 8, y - 10);
        }

        // 雜點（40 個）
        for (int i = 0; i < 40; i++) {
            g.setColor(new Color(secureRandom.nextInt(180),
                                 secureRandom.nextInt(180),
                                 secureRandom.nextInt(180)));
            g.fillOval(secureRandom.nextInt(IMAGE_WIDTH), secureRandom.nextInt(IMAGE_HEIGHT), 2, 2);
        }

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (Exception e) {
            throw new RuntimeException("驗證碼圖片產生失敗", e);
        }

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private void cleanExpired() {
        LocalDateTime now = LocalDateTime.now();
        captchaStore.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }
}
