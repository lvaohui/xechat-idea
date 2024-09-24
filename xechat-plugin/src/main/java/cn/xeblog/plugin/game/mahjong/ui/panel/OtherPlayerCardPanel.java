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

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * 其他玩家麻将牌面板
 */
public class OtherPlayerCardPanel extends BasicJPanel {

    /**
     * 显示位置
     */
    private Position position;
    /**
     *
     */
    private PlayerNode playerNode;

    public OtherPlayerCardPanel(MahJongPanel mahJongPanel, PlayerNode playerNode, Position position) {
        super(mahJongPanel);
        this.playerNode = playerNode;
        this.position = position;
        FlowLayout f = (FlowLayout) getLayout();
        f.setHgap(0);
        f.setVgap(0);
        f.setAlignment(FlowLayout.CENTER);
    }

    /**
     * 重新更新界面展示
     */
    @Override
    public synchronized void updateShowUi() {
        this.removeAll();
        /* 当前玩家大号的牌，其他玩家小号 */
        CardLabel.Size size = CardLabel.Size.NORMAL;
        /* 是否结束，结束了显示其他玩家的牌 */
        boolean over = this.getMahJongPanel().getOver().get();
        /* 取所有牌 */
        List<MahJongCard> cardList = new Vector<>(this.playerNode.getNormalCardList());
        cardList.addAll(this.playerNode.getNotNeedCardList());
        if (ObjectUtil.isNotNull(this.playerNode.getLastCard())) {
            cardList.add(this.playerNode.getLastCard());
        }
        cardList.sort(Comparator.comparingInt(MahJongCard::getId));
        /* 左先加碰过的牌 */
        if (position.equals(Position.LEFT)) {
            /* 暗杠牌 */
            this.playerNode.getDarkGangCardList().forEach(card -> this.add(new CardLabel(over ? card : MahJongCard.BACK, this.position, size, false)));
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
            this.add(new CardLabel(MahJongCard.SPACE, this.position, size, false));
            /* 添加普通牌 */
            int n = cardList.size();
            for (int i = 0; i < n - 1; ++i) {
                this.add(new CardLabel(over ? cardList.get(i) : MahJongCard.BACK, this.position, size, false));
            }
            /* 牌权在手，间隔显示最后一张 */
            if (n % 3 == 2) {
                this.add(new CardLabel(MahJongCard.SPACE, this.position, size, false));
            }
            if (n > 0) {
                this.add(new CardLabel(over ? CollUtil.getLast(cardList) : MahJongCard.BACK, this.position, size, false));
            }
            /* 如果有胡的牌则添加 */
            if (ObjectUtil.isNotNull(this.playerNode.getHuCard())) {
                this.add(new CardLabel(MahJongCard.SPACE, this.position, size, false));
                this.add(new CardLabel(this.playerNode.getHuCard(), this.position, size, false));
            }
            this.updateUI();
            return;
        }
        /* 右和上反过来添加 */
        /* 如果有胡的牌则添加 */
        if (ObjectUtil.isNotNull(this.playerNode.getHuCard())) {
            this.add(new CardLabel(this.playerNode.getHuCard(), this.position, size, false));
            this.add(new CardLabel(MahJongCard.SPACE, this.position, size, false));
        }
        /* 添加普通牌 */
        int n = cardList.size();
        /* 最后一张 */
        if (n > 0) {
            this.add(new CardLabel(over ? CollUtil.getLast(cardList) : MahJongCard.BACK, this.position, size, false));
        }
        /* 牌权在手，间隔显示 */
        if (n % 3 == 2) {
            this.add(new CardLabel(MahJongCard.SPACE, this.position, size, false));
        }
        for (int i = n - 2; i >= 0; i--) {
            this.add(new CardLabel(over ? cardList.get(i) : MahJongCard.BACK, this.position, size, false));
        }
        /* 间隔 */
        this.add(new CardLabel(MahJongCard.SPACE, this.position, size, false));
        /* 碰 */
        this.playerNode.getPendCardListMap().forEach((k, v) -> {
            ListUtil.sort(v, Comparator.comparingInt(MahJongCard::getId)).forEach(card -> {
                this.add(new CardLabel(card, position, size, false));
            });
        });
        /* 杠 */
        this.playerNode.getGangCardListMap().forEach((k, v) -> {
            ListUtil.sort(v, Comparator.comparingInt(MahJongCard::getId)).forEach(card -> {
                this.add(new CardLabel(card, position, size, false));
            });
        });
        /* 暗杠牌 */
        this.playerNode.getDarkGangCardList().forEach(card -> this.add(new CardLabel(over ? card : MahJongCard.BACK, this.position, size, false)));
        this.updateUI();
    }

}
