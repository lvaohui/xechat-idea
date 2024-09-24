package cn.xeblog.plugin.game.mahjong.ui.panel;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.xeblog.plugin.game.mahjong.domain.PlayerNode;
import cn.xeblog.plugin.game.mahjong.ui.MahJongPanel;
import cn.xeblog.plugin.game.mahjong.ui.config.Config;
import cn.xeblog.plugin.game.mahjong.ui.enums.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 中心显示位置和剩余牌数的面板
 */
public class CenterPanel extends BasicJPanel {

    /**
     * 剩余牌数
     */
    @Getter
    private AtomicInteger num;
    private String text;
    private JLabel label;
    private List<PlayerNode> playerNodeList;
    private List<JLabel> labelList;

    public CenterPanel(MahJongPanel mahJongPanel) {
        super(mahJongPanel);
        this.num = new AtomicInteger(108);
        this.text = "";
        this.setLayout(new BorderLayout());

        this.label = new JLabel();
        setLabelLayout(label);
        this.add(this.label, BorderLayout.CENTER);

        playerNodeList = new Vector<>(ListUtil.toList(null, null, null, null));
        labelList = new Vector<>();

        JLabel label1 = new JLabel();
        setLabelLayout(label1);
        labelList.add(label1);
        this.add(label1, BorderLayout.SOUTH);

        label1 = new JLabel();
        setLabelLayout(label1);
        labelList.add(label1);
        this.add(label1, BorderLayout.EAST);

        label1 = new JLabel();
        setLabelLayout(label1);
        labelList.add(label1);
        this.add(label1, BorderLayout.NORTH);

        label1 = new JLabel();
        setLabelLayout(label1);
        labelList.add(label1);
        this.add(label1, BorderLayout.WEST);

    }

    /**
     * 重新更新界面展示
     */
    @Override
    public synchronized void updateShowUi() {
        this.label.setText(text);
        boolean over = this.getMahJongPanel().getOver().get();
        for (int i = 0; i < playerNodeList.size(); ++i) {
            PlayerNode playerNode = playerNodeList.get(i);
            if (ObjectUtil.isNull(playerNode)) {
                this.labelList.get(i).setBorder(Config.NORMAL_BORDER);
                continue;
            }
            this.labelList.get(i).setText(playerNode.getDirect());
            if (over) {
                this.labelList.get(i).setBorder(Config.NORMAL_BORDER);
            } else if (playerNode.isCanOut()) {
                this.labelList.get(i).setBorder(Config.CENTER_OUT_BORDER);
            } else {
                this.labelList.get(i).setBorder(Config.NORMAL_BORDER);
            }
        }
        this.updateUI();
    }

    public void updateCardNum(int increase) {
        this.text = StrUtil.format("{}", this.num.addAndGet(increase));
        this.updateShowUi();
    }

    public void showTips(String text) {
        this.text = text;
        this.updateShowUi();
    }

    public void addPlayerNode(PlayerNode playerNode, Position position) {
        switch (position) {
            case BOTTOM: {
                playerNodeList.set(0, playerNode);
                break;
            }
            case RIGHT: {
                playerNodeList.set(1, playerNode);
                break;
            }
            case UP: {
                playerNodeList.set(2, playerNode);
                break;
            }
            case LEFT: {
                playerNodeList.set(3, playerNode);
                break;
            }
            default: {

            }
        }
    }

    private void setLabelLayout(JLabel label) {
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.CENTER);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setFont(DEFAUT_FONT);
    }

}
