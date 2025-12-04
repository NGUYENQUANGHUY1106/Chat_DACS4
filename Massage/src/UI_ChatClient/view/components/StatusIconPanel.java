package UI_ChatClient.view.components;

import javax.swing.*;
import java.awt.*;
import UI_ChatClient.model.Constants;

/**
 * Panel vẽ chấm trạng thái Online/Offline
 */
public class StatusIconPanel extends JPanel {
    private boolean isOnline = false;
    
    public StatusIconPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(12, 12));
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
        repaint();
    }
    
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int diameter = 10;
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;
        
        if (isOnline) {
            // Vẽ hiệu ứng glow
            g2.setColor(new Color(34, 197, 94, 60));
            g2.fillOval(x - 2, y - 2, diameter + 4, diameter + 4);
            g2.setColor(Constants.ONLINE_COLOR);
        } else {
            g2.setColor(new Color(156, 163, 175));
        }
        g2.fillOval(x, y, diameter, diameter);
        
        g2.dispose();
    }
}
