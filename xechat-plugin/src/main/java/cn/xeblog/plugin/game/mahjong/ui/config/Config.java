package cn.xeblog.plugin.game.mahjong.ui.config;

import javax.swing.border.LineBorder;
import java.awt.*;

public class Config {

    /**
     * 麻将牌普通大小 其他玩家手牌区域
     */
    public final static Font NORMAL_FONT = new Font("Segoe UI Symbol", Font.PLAIN, 26);
    /**
     * 麻将牌大字体 当前玩家手牌区域
     */
    public final static Font BIG_FONT = new Font("Segoe UI Symbol", Font.PLAIN, 28);
    /**
     * 麻将牌小字体 出牌区域
     */
    public final static Font SMALL_FONT = new Font("Segoe UI Symbol", Font.PLAIN, 25);

    /**
     * 普通边框
     */
    public final static LineBorder NORMAL_BORDER = new LineBorder(new Color(0, 0, 0));
    /**
     * 麻将牌选中边框
     */
    public final static LineBorder CARD_CHOICE_BORDER = new LineBorder(new Color(255, 0, 0));
    /**
     * 麻将牌 可听牌边框
     */
    public final static LineBorder CARD_TING_BORDER = new LineBorder(new Color(234, 232, 232));
    /**
     * 麻将牌 缺牌
     */
    public final static LineBorder CARD_NOT_NEED_BORDER = new LineBorder(new Color(79, 75, 75));
    /**
     * 麻将牌 最后出牌边框
     */
    public final static LineBorder CARD_LAST_OUT_BORDER = new LineBorder(new Color(126, 123, 123));

    /**
     * 中间边框 轮到哪个玩家出牌的颜色
     */
    public final static LineBorder CENTER_OUT_BORDER = new LineBorder(new Color(136, 94, 18));
    /**
     * 中间边框 准备好颜色
     */
    public final static LineBorder CENTER_READY_BORDER = new LineBorder(new Color(0, 255, 0));
    /**
     * 中间边框 胡牌后的颜色
     */
    public final static LineBorder CENTER_HU_BORDER = new LineBorder(new Color(19, 19, 19));

    /**
     * 中间边框 听牌后的颜色
     */
    public final static LineBorder CENTER_TING_BORDER = new LineBorder(new Color(234, 232, 232));

}
