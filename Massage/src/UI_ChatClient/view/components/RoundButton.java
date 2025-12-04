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
        
        if (getModel().isArmed()) {
            g2.setColor(getBackground().darker());
        } else if (getModel().isRollover() && hoverColor != null) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(getBackground());
        }
        g2.fillOval(0, 0, getWidth(), getHeight());
        g2.dispose();
        
        super.paintComponent(g);
    }
}
