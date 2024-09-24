package cn.xeblog.plugin.game.mahjong.ui.panel;

import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import cn.xeblog.plugin.game.mahjong.domain.PlayerNode;
import cn.xeblog.plugin.game.mahjong.ui.MahJongPanel;
import cn.xeblog.plugin.game.mahjong.ui.component.CardLabel;
import cn.xeblog.plugin.game.mahjong.ui.config.Config;
import cn.xeblog.plugin.game.mahjong.ui.enums.Position;

import java.awt.*;

/**
 * 显示出牌的面板
 */
public class OutCardPanel extends BasicJPanel {

    /**
     * 位置
     */
    private Position position;
    private PlayerNode playerNode;

    public OutCardPanel(MahJongPanel mahJongPanel, PlayerNode playerNode, Position position) {
        super(mahJongPanel);
        this.position = position;
        this.playerNode = playerNode;
        FlowLayout f = (FlowLayout) getLayout();
        f.setHgap(0);
        f.setVgap(0);
        f.setAlignment(FlowLayout.LEFT);
    }

    /**
     * 重新更新界面展示
     */
    @Override
    public synchronized void updateShowUi() {
        this.removeAll();
        int size = this.playerNode.getOutCardList().size();
        for (int i = 0; i < size; ++i) {
            MahJongCard card = this.playerNode.getOutCardList().get(i);
            this.add(new CardLabel(card, this.position, CardLabel.Size.SMALL,
                    ((i == size - 1) && this.playerNode.isLastOut()) ? Config.CARD_LAST_OUT_BORDER : null));
        }
        this.updateUI();
    }
}
