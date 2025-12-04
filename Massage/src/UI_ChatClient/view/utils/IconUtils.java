package UI_ChatClient.view.utils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Tiện ích để load và scale icons
 */
public class IconUtils {
    
    private IconUtils() {} // Không cho phép khởi tạo
    
    /**
     * Load và scale icon từ thư mục icons
     */
    public static ImageIcon loadAndScaleIcon(String fileName, int width, int height) {
        URL resourceUrl = IconUtils.class.getResource("/icons/" + fileName);
        if (resourceUrl == null) {
            // Thử tìm trong đường dẫn tương đối
            resourceUrl = IconUtils.class.getResource("../../icons/" + fileName);
        }
        if (resourceUrl == null) {
            System.err.println("Không thể tìm thấy icon: " + fileName);
            return new ImageIcon(new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
        ImageIcon icon = new ImageIcon(resourceUrl);
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }
    
    /**
     * Load và scale icon với đường dẫn tùy chỉnh
     */
    public static ImageIcon loadAndScaleIcon(Class<?> clazz, String relativePath, int width, int height) {
        URL resourceUrl = clazz.getResource(relativePath);
        if (resourceUrl == null) {
            System.err.println("Không thể tìm thấy icon: " + relativePath);
            return new ImageIcon(new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
        ImageIcon icon = new ImageIcon(resourceUrl);
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }
}
