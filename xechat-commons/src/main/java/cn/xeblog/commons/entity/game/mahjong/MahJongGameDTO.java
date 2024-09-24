package cn.xeblog.commons.entity.game.mahjong;

import cn.xeblog.commons.entity.game.GameDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author anlingyi
 * @date 2022/6/2 1:14 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MahJongGameDTO extends GameDTO {

    /**
     * 消息类型
     */
    private MsgType msgType;

    /**
     * 玩家昵称
     */
    private String player;
    /**
     * 数据内容
     */
    private Object data;
    /**
     * 关联的其他玩家（碰，胡，杠时用到）
     */
    private String otherPlayer;
    /**
     * 胡牌时分数
     */
    private Integer score;

    public enum MsgType {

        /**
         * 准备
         */
        READY,
        /**
         * 可开始出牌
         */
        START,
        /**
         * 结束
         */
        OVER,
        /**
         * 加入机器人
         */
        JOIN_ROBOTS,
        /**
         * 被人出牌后尝试碰、杠、胡
         * 有可能被别人胡不能碰和杠，所以使用TRY
         */
        TRY,
        /**
         * 被人出牌后过
         */
        PASS,
        /**
         * 最开始的发牌
         */
        ALLOC_CARD,
        /**
         * 确定交换方向
         */
        CONFIRM_SWAP_DIRECTION,
        /**
         * 换出三张
         */
        SWAP_OUT,
        /**
         * 换进
         */
        SWAP_IN,
        /**
         * 定缺
         */
        CONFIRM_DONT_NEED_CARD,
        /**
         * 打牌
         */
        OUT_CARD,
        /**
         * 抓牌
         */
        CATCH_CARD,
        /**
         * 碰
         */
        PEND_CARD,
        /**
         * 杠
         */
        GANG_CARD,
        /**
         * 牌权在自己的杠
         */
        MANUAL_GANG_CARD,
        /**
         * 听
         */
        TING_CARD,
        /**
         * 胡牌
         */
        HU_CARD,
        /**
         * 牌权在自己的胡
         */
        MANUAL_HU_CARD,

        ;
    }

}
