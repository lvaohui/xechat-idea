package cn.xeblog.plugin.game.mahjong.domain;

import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GangBO {

    /**
     * 是否可杠
     */
    private Boolean enable;
    /**
     * 对方
     */
    private String oppPlayer;
    /**
     * 杠的牌
     */
    private MahJongCard card;

    public GangBO() {
        this.enable = false;
    }

}
