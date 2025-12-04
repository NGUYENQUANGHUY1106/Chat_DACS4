package UI_ChatClient.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * TextField bo tròn hiện đại
 */
public class RoundTextField extends JTextField {
    private Shape shape;
    
    public RoundTextField(int size) {
        super(size);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
        g2.dispose();
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(209, 213, 219));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
        g2.dispose();
    }
    
    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 24, 24);
        }
        return shape.contains(x, y);
    }
}
