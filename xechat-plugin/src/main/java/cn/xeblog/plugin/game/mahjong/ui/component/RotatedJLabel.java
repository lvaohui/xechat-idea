package cn.xeblog.plugin.game.mahjong.ui.component;

import javax.swing.*;
import java.awt.*;

public class RotatedJLabel extends JLabel {
    private double angle;

    public RotatedJLabel(String text, double angle) {
        super(text);
        this.angle = angle;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 获取当前字体的宽度和高度
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(getText());
        int height = fm.getHeight();

        // 计算旋转后的文本位置
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2 + fm.getAscent();

        // 旋转文本
        g2d.rotate(Math.toRadians(angle), getWidth() / 2, getHeight() / 2);
        g2d.drawString(getText(), x, y);

        // 重置旋转状态
        g2d.rotate(-Math.toRadians(angle), getWidth() / 2, getHeight() / 2);
    }

}
