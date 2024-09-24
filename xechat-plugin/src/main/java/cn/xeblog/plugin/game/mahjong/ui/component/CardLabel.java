package cn.xeblog.plugin.game.mahjong.ui.component;

import cn.hutool.core.util.ObjectUtil;
import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import cn.xeblog.plugin.game.mahjong.ui.config.Config;
import cn.xeblog.plugin.game.mahjong.ui.enums.Position;
import lombok.Getter;

import javax.swing.border.Border;
import java.awt.*;

/**
 * 麻将牌Label
 */
public class CardLabel extends RotatedJLabel {


    @Getter
    private MahJongCard card;
    private Position position;
    /**
     * 是否有边界
     */
    @Getter
    private boolean haveBorder;
    /**
     * 默认边界
     */
    private Border defaultBorder;
    /**
     * 当前是否选中
     */
    @Getter
    private boolean choiced;

    public CardLabel(MahJongCard mahJongCard, Position position) {
        this(mahJongCard, position, Size.NORMAL, false);
    }

    public CardLabel(MahJongCard mahJongCard, Position position, Size size, boolean haveBorder) {
        super(mahJongCard.getCode(), position.getAngle());
        this.card = mahJongCard;
        this.position = position;
        this.haveBorder = haveBorder;
        this.defaultBorder = Config.NORMAL_BORDER;
        this.choiced = false;
        switch (size) {
            case BIG:
                this.setFont(Config.BIG_FONT);
                break;
            case SMALL:
                this.setFont(Config.SMALL_FONT);
                break;
            default:
                this.setFont(Config.NORMAL_FONT);
        }
        this.calcSize();
        if (haveBorder) {
            this.setBorder(defaultBorder);
        }
    }

    public CardLabel(MahJongCard mahJongCard, Position position, Size size, Border border) {
        this(mahJongCard, position, size, true);
        this.defaultBorder = border;
        this.setBorder(defaultBorder);
    }

    /**
     * 点击
     */
    public void click() {
        this.choiced = !this.choiced;
        if (this.choiced) {
            this.setBorder(Config.CARD_CHOICE_BORDER);
            return;
        }
        if (this.haveBorder) {
            this.setBorder(this.defaultBorder);
            return;
        }
        this.setBorder(null);
        this.updateUI();
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
        /* 如果设置为null则重新计算大小 */
        if (ObjectUtil.isNull(border)) {
            this.haveBorder = false;
            this.calcSize();
        }
    }

    /**
     * 计算大小
     */
    public void calcSize() {
        FontMetrics fm = this.getFontMetrics(this.getFont());
        int width = fm.stringWidth(this.card.getCode()) + (this.haveBorder ? 8 : 0);
        int height = fm.getHeight();
        if (this.position.equals(Position.LEFT) || this.position.equals(Position.RIGHT)) {
            this.setPreferredSize(new Dimension(height, width));
        } else {
            this.setPreferredSize(new Dimension(width, height));
        }
    }

    public enum Size {
        NORMAL,
        BIG,
        SMALL;
    }

}
