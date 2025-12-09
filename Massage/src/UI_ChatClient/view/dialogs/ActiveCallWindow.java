package UI_ChatClient.view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import UI_ChatClient.view.utils.IconUtils;
import UI_ChatClient.controller.ScreenCaptureController;
import UI_ChatClient.controller.NetworkController;

/**
 * Cửa sổ hiển thị khi đang trong cuộc gọi
 */
public class ActiveCallWindow extends JFrame {
    private boolean isMicMuted = false;
    private boolean isCamOff = false;
    private boolean isScreenSharing = false;
    
    private JButton btnMute;
    private JButton btnCam;
    private JButton btnScreenShare;
    private JButton btnEnd;
    private JPanel videoContainer;
    private JLabel lblCallTime;
    private JLabel lblScreenShareDisplay;
    private ScreenCaptureController screenCaptureController;
    private NetworkController networkController;
    private javax.swing.Timer callTimer;
    private int callSeconds = 0;
    private String partnerDisplayName;
    private boolean isVideo;
    
    // Callback để thông báo kết thúc cuộc gọi
    private Runnable onEndCall;
    
    // Màu sắc theme xanh ngọc nhạt
    private final Color darkBgColor = new Color(240, 253, 250);         // Nền trắng xanh ngọc
    private final Color cardBgColor = new Color(255, 255, 255);         // Trắng tiền khiết
    private final Color accentGreen = new Color(16, 185, 129);          // Xanh ngọc accent
    private final Color accentRed = new Color(239, 68, 68);             // Đỏ
    private final Color textPrimary = new Color(19, 78, 74);            // Xanh đen
    private final Color textSecondary = new Color(94, 234, 212);        // Xanh ngọc nhạt
    
    public ActiveCallWindow(String partnerName, boolean isVideoCall, Runnable onEndCall, NetworkController networkController) {
        this.partnerDisplayName = partnerName;
        this.isVideo = isVideoCall;
        this.onEndCall = onEndCall;
        this.networkController = networkController;
        this.screenCaptureController = new ScreenCaptureController();
        
        setTitle("Cuộc gọi với " + partnerName);
        setSize(640, 720);
        setMinimumSize(new Dimension(480, 560));
        setResizable(true);
        setBackground(darkBgColor);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Panel chính với gradient trắng xanh ngọc
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, getHeight(), new Color(204, 251, 241)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        
        // === HEADER ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(15, 20, 10, 20));
        
        JButton btnMinimize = createIconButton("▼", textSecondary);
        btnMinimize.addActionListener(e -> setState(JFrame.ICONIFIED));
        
        JLabel lblCallType = new JLabel(isVideoCall ? "📹 Video Call" : "📞 Voice Call");
        lblCallType.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblCallType.setForeground(textSecondary);
        lblCallType.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblCallTime = new JLabel("00:00");
        lblCallTime.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCallTime.setForeground(textPrimary);
        
        headerPanel.add(btnMinimize, BorderLayout.WEST);
        headerPanel.add(lblCallType, BorderLayout.CENTER);
        headerPanel.add(lblCallTime, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // === PHẦN CHÍNH ===
        videoContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!isVideoCall) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2 - 30;
                    
                    // Vòng tròn xanh ngọc gradient
                    for (int i = 3; i >= 1; i--) {
                        int alpha = 20 + (3 - i) * 15;
                        g2.setColor(new Color(94, 234, 212, alpha));
                        int radius = 80 + i * 25;
                        g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                    }
                    
                    g2.dispose();
                }
            }
        };
        videoContainer.setOpaque(false);
        
        if (!isVideoCall) {
            // Voice call - Hiển thị avatar
            JPanel avatarPanel = new JPanel();
            avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
            avatarPanel.setOpaque(false);
            avatarPanel.setBorder(new javax.swing.border.EmptyBorder(50, 0, 30, 0));
            
            JPanel avatarContainer = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    GradientPaint borderGradient = new GradientPaint(
                        0, 0, new Color(94, 234, 212),
                        getWidth(), getHeight(), new Color(45, 212, 191)
                    );
                    g2.setPaint(borderGradient);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    
                    g2.setColor(cardBgColor);
                    g2.fillOval(4, 4, getWidth() - 8, getHeight() - 8);
                    
                    g2.dispose();
                }
            };
            avatarContainer.setOpaque(false);
            avatarContainer.setPreferredSize(new Dimension(140, 140));
            avatarContainer.setMaximumSize(new Dimension(140, 140));
            avatarContainer.setLayout(new GridBagLayout());
            avatarContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel lblAvatar = new JLabel(IconUtils.loadAndScaleIcon("avatar.jpg", 120, 120));
            avatarContainer.add(lblAvatar);
            
            JLabel lblName = new JLabel(partnerName);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 26));
            lblName.setForeground(textPrimary);
            lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblName.setBorder(new javax.swing.border.EmptyBorder(25, 0, 8, 0));
            
            avatarPanel.add(Box.createVerticalGlue());
            avatarPanel.add(avatarContainer);
            avatarPanel.add(lblName);
            avatarPanel.add(Box.createVerticalGlue());
            
            videoContainer.add(avatarPanel, BorderLayout.CENTER);
        }
        // Video call: videoContainer chỉ hiển thị camera, thời gian đã ở header
        
        mainPanel.add(videoContainer, BorderLayout.CENTER);
        
        // === THANH ĐIỀU KHIỂN ===
        JPanel controlWrapper = new JPanel(new BorderLayout());
        controlWrapper.setOpaque(false);
        controlWrapper.setBorder(new javax.swing.border.EmptyBorder(15, 20, 25, 20));
        
        JPanel controlPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(94, 234, 212, 50));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
                g2.dispose();
            }
        };
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 18, 20));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new javax.swing.border.EmptyBorder(10, 15, 10, 15));
        
        btnMute = createControlButton("mic.png", "Tắt tiếng", new Color(204, 251, 241));
        btnMute.addActionListener(e -> toggleMic());
        
        btnScreenShare = createControlButton("screen_share.png", "Chia sẻ màn hình", new Color(204, 251, 241));
        btnScreenShare.addActionListener(e -> toggleScreenShare());
        
        btnCam = createControlButton("video.png", "Camera", new Color(204, 251, 241));
        btnCam.addActionListener(e -> toggleCam());
        if (!isVideoCall) btnCam.setVisible(false);
        
        btnEnd = createControlButton("hangup.png", "Kết thúc", accentRed);
        btnEnd.setPreferredSize(new Dimension(70, 70));
        btnEnd.addActionListener(e -> {
            if (onEndCall != null) onEndCall.run();
        });
        
        controlPanel.add(btnMute);
        controlPanel.add(btnScreenShare);
        if (isVideoCall) controlPanel.add(btnCam);
        controlPanel.add(btnEnd);
        
        controlWrapper.add(controlPanel, BorderLayout.CENTER);
        mainPanel.add(controlWrapper, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        startCallTimer();
        enableWindowDrag(mainPanel);
    }
    
    private JButton createIconButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(color);
        btn.setBackground(null);
        btn.setBorder(null);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JButton createControlButton(String iconName, String tooltip, Color bgColor) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        btn.setIcon(IconUtils.loadAndScaleIcon(iconName, 26, 26));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(58, 58));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void startCallTimer() {
        callTimer = new javax.swing.Timer(1000, e -> {
            callSeconds++;
            int mins = callSeconds / 60;
            int secs = callSeconds % 60;
            String timeStr = String.format("%02d:%02d", mins, secs);
            if (lblCallTime != null) {
                lblCallTime.setText(timeStr);
            }
        });
        callTimer.start();
    }
    
    private void enableWindowDrag(JPanel panel) {
        final int[] mouseX = {0};
        final int[] mouseY = {0};
        
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX[0] = e.getX();
                mouseY[0] = e.getY();
            }
        });
        
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen() - mouseX[0];
                int y = e.getYOnScreen() - mouseY[0];
                setLocation(x, y);
            }
        });
    }
    
    public JPanel getVideoPanel() {
        return videoContainer;
    }

    private void toggleMic() {
        isMicMuted = !isMicMuted;
        if (isMicMuted) {
            btnMute.setIcon(IconUtils.loadAndScaleIcon("mute.png", 26, 26));
            btnMute.setToolTipText("Bật tiếng");
        } else {
            btnMute.setIcon(IconUtils.loadAndScaleIcon("mic.png", 26, 26));
            btnMute.setToolTipText("Tắt tiếng");
        }
    }
    
    private void toggleScreenShare() {
        if (!isScreenSharing) {
            // Hiển thị dialog chọn nguồn
            ScreenShareDialog dialog = new ScreenShareDialog(this);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                isScreenSharing = true;
                btnScreenShare.setIcon(IconUtils.loadAndScaleIcon("stop.png", 26, 26));
                btnScreenShare.setToolTipText("Dừng chia sẻ màn hình");
                
                // Tạo label để hiển thị màn hình được share
                if (lblScreenShareDisplay == null) {
                    lblScreenShareDisplay = new JLabel("Đang khởi động chia sẻ màn hình...", SwingConstants.CENTER);
                    lblScreenShareDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    lblScreenShareDisplay.setForeground(textSecondary);
                }
                
                // Clear videoContainer và thêm screen share display
                videoContainer.removeAll();
                videoContainer.add(lblScreenShareDisplay, BorderLayout.CENTER);
                videoContainer.revalidate();
                videoContainer.repaint();
                
                // Set listener để gửi frames qua network
                if (networkController != null) {
                    screenCaptureController.setListener(frame -> {
                        networkController.sendScreenShareFrame(frame);
                    });
                }
                
                // Bắt đầu capture
                if (dialog.isShareFullScreen()) {
                    GraphicsDevice screen = dialog.getSelectedScreen();
                    screenCaptureController.startFullScreenCapture(screen, lblScreenShareDisplay);
                    System.out.println("Bắt đầu chia sẻ toàn màn hình");
                } else {
                    Window window = dialog.getSelectedWindow();
                    screenCaptureController.startWindowCapture(window, lblScreenShareDisplay);
                    System.out.println("Bắt đầu chia sẻ cửa sổ: " + 
                        (window instanceof Frame ? ((Frame)window).getTitle() : "Unknown"));
                }
            }
        } else {
            // Dừng chia sẻ
            isScreenSharing = false;
            btnScreenShare.setIcon(IconUtils.loadAndScaleIcon("screen_share.png", 26, 26));
            btnScreenShare.setToolTipText("Chia sẻ màn hình");
            
            screenCaptureController.stopCapture();
            System.out.println("Dừng chia sẻ màn hình");
            
            // Restore video container về trạng thái ban đầu
            restoreVideoContainer();
        }
    }
    
    private void toggleCam() {
        isCamOff = !isCamOff;
        if (isCamOff) {
            btnCam.setIcon(IconUtils.loadAndScaleIcon("video_off.png", 26, 26));
            btnCam.setToolTipText("Bật camera");
        } else {
            btnCam.setIcon(IconUtils.loadAndScaleIcon("video.png", 26, 26));
            btnCam.setToolTipText("Tắt camera");
        }
    }
    
    private void restoreVideoContainer() {
        videoContainer.removeAll();
        
        if (!isVideo) {
            // Voice call - Hiển thị lại avatar
            JPanel avatarPanel = new JPanel();
            avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
            avatarPanel.setOpaque(false);
            avatarPanel.setBorder(new javax.swing.border.EmptyBorder(50, 0, 30, 0));
            
            JPanel avatarContainer = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    GradientPaint borderGradient = new GradientPaint(
                        0, 0, new Color(94, 234, 212),
                        getWidth(), getHeight(), new Color(45, 212, 191)
                    );
                    g2.setPaint(borderGradient);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    
                    g2.setColor(cardBgColor);
                    g2.fillOval(4, 4, getWidth() - 8, getHeight() - 8);
                    
                    g2.dispose();
                }
            };
            avatarContainer.setOpaque(false);
            avatarContainer.setPreferredSize(new Dimension(140, 140));
            avatarContainer.setMaximumSize(new Dimension(140, 140));
            avatarContainer.setLayout(new GridBagLayout());
            avatarContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel lblAvatar = new JLabel(IconUtils.loadAndScaleIcon("avatar.jpg", 120, 120));
            avatarContainer.add(lblAvatar);
            
            JLabel lblName = new JLabel(partnerDisplayName);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 26));
            lblName.setForeground(textPrimary);
            lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblName.setBorder(new javax.swing.border.EmptyBorder(25, 0, 8, 0));
            
            avatarPanel.add(Box.createVerticalGlue());
            avatarPanel.add(avatarContainer);
            avatarPanel.add(lblName);
            avatarPanel.add(Box.createVerticalGlue());
            
            videoContainer.add(avatarPanel, BorderLayout.CENTER);
        }
        // Video call: có thể thêm logic restore video feed ở đây
        
        videoContainer.revalidate();
        videoContainer.repaint();
    }
    
    public boolean isMicMuted() { return isMicMuted; }
    public boolean isCamOff() { return isCamOff; }
    public boolean isScreenSharing() { return isScreenSharing; }
    
    public ScreenCaptureController getScreenCaptureController() {
        return screenCaptureController;
    }
    
    public void close() {
        if (callTimer != null) callTimer.stop();
        if (screenCaptureController != null && isScreenSharing) {
            screenCaptureController.stopCapture();
        }
        this.dispose();
    }
}
