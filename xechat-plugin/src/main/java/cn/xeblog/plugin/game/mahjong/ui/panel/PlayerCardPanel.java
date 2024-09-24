package cn.xeblog.plugin.game.mahjong.ui.panel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import cn.xeblog.plugin.game.mahjong.domain.PlayerNode;
import cn.xeblog.plugin.game.mahjong.ui.MahJongPanel;
import cn.xeblog.plugin.game.mahjong.ui.component.CardLabel;
import cn.xeblog.plugin.game.mahjong.ui.config.Config;
import cn.xeblog.plugin.game.mahjong.ui.enums.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;

/**
 * 玩家麻将牌面板
 */
public class PlayerCardPanel extends BasicJPanel {

    private PlayerNode playerNode;
    /**
     * 可滑动显示
     */
    private JScrollPane jScrollPane;

    public PlayerCardPanel(MahJongPanel mahJongPanel, PlayerNode playerNode) {
        super(mahJongPanel);
        this.playerNode = playerNode;
        FlowLayout f = (FlowLayout) getLayout();
        f.setHgap(0);
        f.setVgap(0);
        f.setAlignment(FlowLayout.CENTER);

        this.jScrollPane = new JScrollPane(this);
        this.jScrollPane.setBorder(null);
        this.jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }

    /**
     * 重新更新界面展示
     */
    @Override
    public synchronized void updateShowUi() {
        this.removeAll();
        /* 出牌后可听的牌 */
        boolean ting = this.playerNode.getTingBO().getEnable();
        List<MahJongCard> tingCardList = this.playerNode.getTingBO().getCardList();
        /* 当前玩家大号的牌，其他玩家小号 */
        CardLabel.Size size = CardLabel.Size.BIG;
        Position position = Position.BOTTOM;
        /* 暗杠 */
        ListUtil.sort(this.playerNode.getDarkGangCardList(), Comparator.comparingInt(MahJongCard::getId)).forEach(card -> {
            this.add(new CardLabel(card, position, size, false));
        });
        /* 杠 */
        this.playerNode.getGangCardListMap().forEach((k, v) -> {
            ListUtil.sort(v, Comparator.comparingInt(MahJongCard::getId)).forEach(card -> {
                this.add(new CardLabel(card, position, size, false));
            });
        });
        /* 碰 */
        this.playerNode.getPendCardListMap().forEach((k, v) -> {
            ListUtil.sort(v, Comparator.comparingInt(MahJongCard::getId)).forEach(card -> {
                this.add(new CardLabel(card, position, size, false));
            });
        });
        /* 间隔 */
        this.add(new CardLabel(MahJongCard.SPACE, position, size, false));
        /* 添加普通牌 */
        ListUtil.sort(this.playerNode.getNormalCardList(), Comparator.comparingInt(MahJongCard::getId)).forEach(card -> {
            CardLabel cardLabel = (ting && tingCardList.stream().anyMatch(card1 -> ObjectUtil.equal(card1, card)))
                    ? new CardLabel(card, position, size, Config.CARD_TING_BORDER) : new CardLabel(card, position, size, true);
            /* 没听才可以出其他牌 */
            if (!playerNode.isTing()) {
                this.addListener(cardLabel);
            }
            this.add(cardLabel);
        });
        /* 添加缺牌 */
        if (CollUtil.isNotEmpty(this.playerNode.getNotNeedCardList())) {
            ListUtil.sort(this.playerNode.getNotNeedCardList(), Comparator.comparingInt(MahJongCard::getId)).forEach(card -> {
                CardLabel cardLabel = new CardLabel(card, position, size, Config.CARD_NOT_NEED_BORDER);
                this.addListener(cardLabel);
                this.add(cardLabel);
            });
        }
        /* 刚抓的牌 */
        if (ObjectUtil.isNotNull(this.playerNode.getLastCard())) {
            /* 间隔 */
            this.add(new CardLabel(MahJongCard.SPACE, position, size, false));
            CardLabel cardLabel = (ting && tingCardList.stream().anyMatch(card1 -> ObjectUtil.equal(card1, playerNode.getLastCard())))
                    ? new CardLabel(this.playerNode.getLastCard(), position, size, Config.CARD_TING_BORDER)
                    : (ObjectUtil.equals(this.playerNode.getLastCard().getType(), this.playerNode.getNotNeedType()) ?
                    new CardLabel(this.playerNode.getLastCard(), position, size, Config.CARD_NOT_NEED_BORDER)
                    : new CardLabel(this.playerNode.getLastCard(), position, size, true));
            this.addListener(cardLabel);
            this.add(cardLabel);
        }
        /* 胡的牌 */
        if (ObjectUtil.isNotNull(this.playerNode.getHuCard())) {
            /* 间隔 */
            this.add(new CardLabel(MahJongCard.SPACE, position, size, false));
            CardLabel cardLabel = new CardLabel(this.playerNode.getHuCard(), position, size, false);
            this.add(cardLabel);
        }
        this.updateUI();
    }

    private void addListener(CardLabel cardLabel) {
        cardLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() != 2) {
                    return;
                }
                /* 双击出牌 */
                CardLabel label = (CardLabel) e.getComponent();
                getMahJongPanel().cardDoubleClicked(label);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                CardLabel label = (CardLabel) e.getComponent();
                getMahJongPanel().cardClicked(label);
            }
        });
    }

    @Override
    public void setBounds(double width, double height) {
        this.jScrollPane.setBounds((int) (width * xRate), (int) (height * yRate),
                (int) (width * widthRate), (int) (height * heightRate));
    }

    @Override
    public JComponent getMainPanel() {
        return this.jScrollPane;
    }
}
