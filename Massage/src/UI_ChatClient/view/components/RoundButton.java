package UI_ChatClient.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * Button bo tròn hiện đại
 */
public class RoundButton extends JButton {
    private Color hoverColor;
    
    public RoundButton(Icon icon) {
        super(icon);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(42, 42));
    }
    
    public RoundButton(Icon icon, Color bgColor) {
        this(icon);
        setBackground(bgColor);
        this.hoverColor = bgColor.darker();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color currentColor;
        if (getModel().isArmed()) {
            currentColor = getBackground().darker();
        } else if (getModel().isRollover() && hoverColor != null) {
            currentColor = hoverColor;
        } else {
            currentColor = getBackground();
        }
        
        // Vẽ shadow nhẹ
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillOval(2, 2, getWidth()-2, getHeight()-2);
        
        // Vẽ nền chính
        g2.setColor(currentColor);
        g2.fillOval(0, 0, getWidth()-2, getHeight()-2);
        g2.dispose();
        
        super.paintComponent(g);
    }
}
