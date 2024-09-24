package cn.xeblog.plugin.game.mahjong.domain;

import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PengBO {

    /**
     * 是否可碰
     */
    private Boolean enable;
    /**
     * 对方
     */
    private String oppPlayer;
    /**
     * 碰的牌
     */
    private MahJongCard card;

    public PengBO() {
        this.enable = false;
    }

}
