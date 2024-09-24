package cn.xeblog.plugin.game.mahjong.ui.panel;

import cn.xeblog.plugin.game.mahjong.ui.MahJongPanel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
@ToString
public abstract class BasicJPanel extends JPanel {

    /**
     * 默认字体
     */
    public static final Font DEFAUT_FONT = new Font(null, Font.PLAIN, 13);
    /**
     * 每个组件保留游戏主面板，通过主面板传递消息
     */
    private MahJongPanel mahJongPanel;
    /**
     * x坐标比例
     */
    protected int xRate;
    /**
     * y坐标比例
     */
    protected int yRate;
    /**
     * 宽度比例
     */
    protected int widthRate;
    /**
     * 高度比例
     */
    protected int heightRate;

    public BasicJPanel(MahJongPanel mahJongPanel) {
        this.mahJongPanel = mahJongPanel;
        this.xRate = 0;
        this.yRate = 0;
        this.widthRate = 20;
        this.heightRate = 20;
    }

    public void setFixedPosition(int x, int y, int width, int height) {
        this.xRate = x;
        this.yRate = y;
        this.widthRate = width;
        this.heightRate = height;
        this.setBounds(20, 20);
    }

    /**
     * 设置位置，传入一格的宽度和高度
     *
     * @param width  一格宽度
     * @param height 一格高度
     */
    public void setBounds(double width, double height) {
        super.setBounds((int) (width * xRate), (int) (height * yRate), (int) (width * widthRate), (int) (height * heightRate));
    }

    /**
     * 取该组件的主面板
     */
    public JComponent getMainPanel() {
        return this;
    }

    /**
     * 重新更新界面展示
     */
    public abstract void updateShowUi();

}
