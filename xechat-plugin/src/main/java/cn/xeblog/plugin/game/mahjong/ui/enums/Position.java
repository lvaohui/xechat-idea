package cn.xeblog.plugin.game.mahjong.ui.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 位置
 */
@AllArgsConstructor
public enum Position {
    LEFT(90),
    RIGHT(-90),
    UP(0),
    BOTTOM(0);

    /**
     * 麻将牌角度
     */
    @Getter
    private final double angle;

}
