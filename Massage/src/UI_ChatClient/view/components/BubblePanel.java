package UI_ChatClient.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import UI_ChatClient.model.Constants;

/**
 * Panel tin nhắn dạng bong bóng với shadow
 */
public class BubblePanel extends JPanel {
    private final String message;
    private final Color bgColor;
    private Color textColor = Color.BLACK;
    private boolean isMyMessage = false;
    
    public BubblePanel(String message, Color bgColor) {
        this.message = message;
        this.bgColor = bgColor;
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel label = new JLabel("<html><body style='padding: 10px 14px;'>" + 
            message.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>") + "</body></html>");
        label.setFont(Constants.UI_FONT);
        label.setForeground(textColor);
        add(label);
    }
    
    public void setTextColor(Color color) {
        this.textColor = color;
        if (getComponentCount() > 0 && getComponent(0) instanceof JLabel) {
            ((JLabel) getComponent(0)).setForeground(this.textColor);
        }
    }
    
    public void setIsMyMessage(boolean isMyMessage) {
        this.isMyMessage = isMyMessage;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ shadow nhẹ hơn
        g2.setColor(new Color(94, 234, 212, 25));
        g2.fill(new RoundRectangle2D.Float(2, 2, getWidth()-2, getHeight()-2, 18, 18));
        
        // Vẽ nền chính với gradient cho tin nhắn của mình
        if (isMyMessage) {
            GradientPaint gradient = new GradientPaint(
                0, 0, Constants.MY_MESSAGE_COLOR,
                getWidth(), 0, Constants.PRIMARY_DARK
            );
            g2.setPaint(gradient);
        } else {
            g2.setColor(bgColor);
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-2, getHeight()-2, 18, 18));
        g2.dispose();
        super.paintComponent(g);
    }
}
