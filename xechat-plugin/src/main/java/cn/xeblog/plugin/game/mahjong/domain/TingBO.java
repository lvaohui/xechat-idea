package cn.xeblog.plugin.game.mahjong.domain;

import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Vector;

@Data
@Builder
@AllArgsConstructor
public class TingBO {

    /**
     * 是否可听
     */
    private Boolean enable;
    /**
     * 可听的牌
     */
    private List<MahJongCard> cardList;

    public TingBO() {
        this.enable = false;
        this.cardList = new Vector<>();
    }

}
