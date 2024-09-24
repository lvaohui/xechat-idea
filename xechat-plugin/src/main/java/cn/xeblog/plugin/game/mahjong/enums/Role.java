package cn.xeblog.plugin.game.mahjong.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Role {

    /* 庄 */
    ZHUANG("庄", 2),
    /* 闲 */
    XIAN("闲", 1),
    /* 待定 */
    WAITING("待定", 0);

    /**
     * 名字
     */
    @Getter
    private final String name;
    /**
     * 倍数
     */
    @Getter
    private final int base;

}
