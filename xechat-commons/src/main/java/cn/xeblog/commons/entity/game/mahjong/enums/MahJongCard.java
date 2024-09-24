package cn.xeblog.commons.entity.game.mahjong.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 麻将牌
 *
 * @author 019599743
 */
@AllArgsConstructor
public enum MahJongCard {

    /* 🀇 */
    WAN_1(1, "\ud83c\udc07", CardType.WAN, "一万"),
    /* 🀈 */
    WAN_2(2, "\ud83c\udc08", CardType.WAN, "二万"),
    /* 🀉 */
    WAN_3(3, "\ud83c\udc09", CardType.WAN, "三万"),
    /* 🀊 */
    WAN_4(4, "\ud83c\udc0A", CardType.WAN, "四万"),
    /* 🀋 */
    WAN_5(5, "\ud83c\udc0B", CardType.WAN, "五万"),
    /* 🀈 */
    WAN_6(6, "\ud83c\udc0C", CardType.WAN, "六万"),
    /* 🀈 */
    WAN_7(7, "\ud83c\udc0D", CardType.WAN, "七万"),
    /* 🀎 */
    WAN_8(8, "\ud83c\udc0E", CardType.WAN, "八万"),
    /* 🀏 */
    WAN_9(9, "\ud83c\udc0F", CardType.WAN, "九万"),

    /* 🀐 */
    TIAO_1(21, "\ud83c\udc10", CardType.TIAO, "一条"),
    /* 🀑 */
    TIAO_2(22, "\ud83c\udc11", CardType.TIAO, "二条"),
    /* 🀒 */
    TIAO_3(23, "\ud83c\udc12", CardType.TIAO, "三条"),
    /* 🀓 */
    TIAO_4(24, "\ud83c\udc13", CardType.TIAO, "四条"),
    /* 🀔 */
    TIAO_5(25, "\ud83c\udc14", CardType.TIAO, "五条"),
    /* 🀕 */
    TIAO_6(26, "\ud83c\udc15", CardType.TIAO, "六条"),
    /* 🀖 */
    TIAO_7(27, "\ud83c\udc16", CardType.TIAO, "七条"),
    /* 🀗 */
    TIAO_8(28, "\ud83c\udc17", CardType.TIAO, "八条"),
    /* 🀘 */
    TIAO_9(29, "\ud83c\udc18", CardType.TIAO, "九条"),

    /* 🀙 */
    TONG_1(41, "\ud83c\udc19", CardType.TONG, "一筒"),
    /* 🀚 */
    TONG_2(42, "\ud83c\udc1A", CardType.TONG, "二筒"),
    /* 🀛 */
    TONG_3(43, "\ud83c\udc1B", CardType.TONG, "三筒"),
    /* 🀜 */
    TONG_4(44, "\ud83c\udc1C", CardType.TONG, "四筒"),
    /* 🀝 */
    TONG_5(45, "\ud83c\udc1D", CardType.TONG, "五筒"),
    /* 🀞 */
    TONG_6(46, "\ud83c\udc1E", CardType.TONG, "六筒"),
    /* 🀟 */
    TONG_7(47, "\ud83c\udc1F", CardType.TONG, "七筒"),
    /* 🀠 */
    TONG_8(48, "\ud83c\udc20", CardType.TONG, "八筒"),
    /* 🀡 */
    TONG_9(49, "\ud83c\udc21", CardType.TONG, "九筒"),

    /* 背面🀫 */
    BACK(99, "\ud83c\udc2B", null, "背面"),
    /* 背面🀫 */
    SPACE(98, " ", null, "间隔"),

    ;

    /**
     * 编码
     */
    @Getter
    private int id;
    @Getter
    private String code;
    @Getter
    private CardType type;
    @Getter
    private String desc;

    /**
     * 取值
     *
     * @param id
     * @return 麻将牌
     */
    public static MahJongCard valueOf(int id) {
        for (MahJongCard mahJongCard : MahJongCard.values()) {
            if (mahJongCard.getId() == id) {
                return mahJongCard;
            }
        }
        return MahJongCard.BACK;
    }

}
