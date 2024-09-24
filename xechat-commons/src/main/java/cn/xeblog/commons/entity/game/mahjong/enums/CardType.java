package cn.xeblog.commons.entity.game.mahjong.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 019599743
 */

@AllArgsConstructor
public enum CardType {

    /**
     * 万
     */
    WAN("万"),
    /**
     * 条
     */
    TIAO("条"),
    /**
     * 筒
     */
    TONG("筒"),
    /**
     * 待定
     */
    WAIT("定缺中");

    @Getter
    private String name;

}
