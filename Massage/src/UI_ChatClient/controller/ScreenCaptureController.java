package UI_ChatClient.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller để capture và stream màn hình
 */
public class ScreenCaptureController {
    private Robot robot;
    private Rectangle captureArea;
    private boolean isCapturing = false;
    private ScheduledExecutorService executor;
    private JLabel displayLabel; // Label để hiển thị màn hình được capture
    private ScreenCaptureListener listener;
    
    // FPS cho screen capture (mặc định 15 fps)
    private static final int CAPTURE_FPS = 60;
    private static final int CAPTURE_DELAY = 1000 / CAPTURE_FPS;
    
    public interface ScreenCaptureListener {
        void onFrameCaptured(BufferedImage frame);
    }
    
    public ScreenCaptureController() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Không thể khởi tạo screen capture: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Bắt đầu capture toàn màn hình
     */
    public void startFullScreenCapture(GraphicsDevice screen, JLabel displayLabel) {
        if (robot == null) return;
        
        DisplayMode mode = screen.getDisplayMode();
        GraphicsConfiguration config = screen.getDefaultConfiguration();
        Rectangle bounds = config.getBounds();
        
        this.captureArea = new Rectangle(
            bounds.x, bounds.y,
            mode.getWidth(), mode.getHeight()
        );
        this.displayLabel = displayLabel;
        
        startCapture();
    }
    
    /**
     * Bắt đầu capture một cửa sổ cụ thể
     */
    public void startWindowCapture(Window window, JLabel displayLabel) {
        if (robot == null || window == null) return;
        
        Rectangle bounds = window.getBounds();
        this.captureArea = bounds;
        this.displayLabel = displayLabel;
        
        startCapture();
    }
    
    /**
     * Bắt đầu quá trình capture
     */
    private void startCapture() {
        if (isCapturing) {
            stopCapture();
        }
        
        isCapturing = true;
        executor = Executors.newSingleThreadScheduledExecutor();
        
        executor.scheduleAtFixedRate(() -> {
            try {
                if (!isCapturing) return;
                
                BufferedImage screenCapture = robot.createScreenCapture(captureArea);
                
                // Hiển thị lên UI
                if (displayLabel != null) {
                    SwingUtilities.invokeLater(() -> {
                        ImageIcon icon = new ImageIcon(scaleImage(screenCapture, 
                            displayLabel.getWidth(), 
                            displayLabel.getHeight()));
                        displayLabel.setIcon(icon);
                    });
                }
                
                // Callback để gửi qua network
                if (listener != null) {
                    listener.onFrameCaptured(screenCapture);
                }
                
            } catch (Exception e) {
                System.err.println("Lỗi khi capture màn hình: " + e.getMessage());
            }
        }, 0, CAPTURE_DELAY, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Dừng capture màn hình
     */
    public void stopCapture() {
        isCapturing = false;
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            executor = null;
        }
        
        // Clear display
        if (displayLabel != null) {
            SwingUtilities.invokeLater(() -> {
                displayLabel.setIcon(null);
                displayLabel.setText("Đã dừng chia sẻ màn hình");
            });
        }
    }
    
    /**
     * Scale image để fit vào label
     */
    private BufferedImage scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        if (maxWidth <= 0 || maxHeight <= 0) {
            return original;
        }
        
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // Tính toán tỷ lệ
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int scaledWidth = (int) (originalWidth * ratio);
        int scaledHeight = (int) (originalHeight * ratio);
        
        // Tạo image mới với kích thước scaled
        BufferedImage scaledImage = new BufferedImage(
            scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        return scaledImage;
    }
    
    /**
     * Capture một frame đơn lẻ (không liên tục)
     */
    public BufferedImage captureFrame() {
        if (robot == null || captureArea == null) return null;
        return robot.createScreenCapture(captureArea);
    }
    
    // Getters and Setters
    public boolean isCapturing() {
        return isCapturing;
    }
    
    public void setListener(ScreenCaptureListener listener) {
        this.listener = listener;
    }
    
    public Rectangle getCaptureArea() {
        return captureArea;
    }
}
