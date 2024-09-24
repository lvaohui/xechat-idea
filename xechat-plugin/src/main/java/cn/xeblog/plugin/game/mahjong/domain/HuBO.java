package cn.xeblog.plugin.game.mahjong.domain;

import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HuBO {

    /**
     * 是否可胡
     */
    private Boolean enable;
    /**
     * 对方
     */
    private String oppPlayer;
    /**
     * 胡的牌
     */
    private MahJongCard card;
    /**
     * 胡的分数
     */
    private Integer score;

    public HuBO() {
        this.enable = false;
        this.score = 0;
    }

}
