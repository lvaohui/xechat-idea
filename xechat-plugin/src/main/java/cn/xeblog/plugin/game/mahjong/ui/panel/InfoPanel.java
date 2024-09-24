package cn.xeblog.plugin.game.mahjong.ui.panel;

import cn.hutool.core.util.StrUtil;
import cn.xeblog.plugin.game.mahjong.domain.PlayerNode;
import cn.xeblog.plugin.game.mahjong.ui.MahJongPanel;
import cn.xeblog.plugin.game.mahjong.ui.config.Config;
import cn.xeblog.plugin.game.mahjong.ui.enums.Position;

import javax.swing.*;
import java.awt.*;

/**
 * 玩家信息显示区域
 */
public class InfoPanel extends BasicJPanel {

    private Position position;
    private PlayerNode playerNode;
    private JLabel label1;
    private JLabel label2;

    public InfoPanel(MahJongPanel mahJongPanel, PlayerNode playerNode, Position position) {
        super(mahJongPanel);
        this.position = position;
        this.playerNode = playerNode;
        this.setLayout(new BorderLayout());
        label1 = new JLabel();
        label2 = new JLabel();
        this.setStyle(label1);
        this.setStyle(label2);
        this.add(label1, BorderLayout.NORTH);
//        this.add(label2, BorderLayout.CENTER);
        this.add(label2, BorderLayout.CENTER);
        this.updateNameAndInfo();
    }

    /**
     * 重新更新界面展示
     */
    @Override
    public synchronized void updateShowUi() {
        this.updateNameAndInfo();
        boolean over = this.getMahJongPanel().getOver().get();
        if (over && playerNode.isReady()) {
            this.setBorder(Config.CENTER_READY_BORDER);
        }  else if (playerNode.isHu()) {
            this.setBorder(Config.CENTER_HU_BORDER);
        }else if (playerNode.isTing()) {
            this.setBorder(Config.CENTER_TING_BORDER);
        } else if (playerNode.isCanOut()) {
            this.setBorder(Config.CENTER_OUT_BORDER);
        } else {
            this.setBorder(null);
        }
        this.updateUI();
    }

    private void setStyle(JLabel label) {
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.CENTER);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.setFont(DEFAUT_FONT);
    }

    /**
     * 更新名字和信息
     */
    private void updateNameAndInfo() {
//        nameLable.setText(this.name);
        String arrow = "";
        String label1Txt = "";
        String label2Txt = "";
        switch (this.position) {
            case BOTTOM:{
                label1Txt = StrUtil.format("←{}·{}", this.playerNode.getName(), this.playerNode.getRole().getName());
                label2Txt = StrUtil.format("{} {}", this.playerNode.getNotNeedType().getName(), this.playerNode.getScore().get());
                break;
            }
            case RIGHT: {
                label2Txt = StrUtil.format("↓{}·{}", this.playerNode.getName(), this.playerNode.getRole().getName());
                label1Txt = StrUtil.format("{} {}", this.playerNode.getNotNeedType().getName(), this.playerNode.getScore().get());
                break;
            }
            case UP:{
                arrow = "→";
                label1Txt = StrUtil.format("{}·{}→", this.playerNode.getName(), this.playerNode.getRole().getName());
                label2Txt = StrUtil.format("{} {}", this.playerNode.getNotNeedType().getName(), this.playerNode.getScore().get());
                break;
            }
            case LEFT: {
                label1Txt = StrUtil.format("↑{}·{}", this.playerNode.getName(), this.playerNode.getRole().getName());
                label2Txt = StrUtil.format("{} {}", this.playerNode.getNotNeedType().getName(), this.playerNode.getScore().get());
                break;
            }
            default:
        }
        label1.setText(label1Txt);
        label2.setText(label2Txt);
    }

}
