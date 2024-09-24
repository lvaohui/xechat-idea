package cn.xeblog.plugin.game.mahjong.ui.component;

import cn.xeblog.commons.entity.game.mahjong.enums.CardType;
import lombok.*;

import javax.swing.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MyButton extends JButton {

    /**
     * 定缺类型
     */
    private CardType cardType;
    /**
     * 交换反向 -1 逆时针， 0 对家 1 顺时针
     */
    private int direction;

    public MyButton(String text) {
        this(text, null);
    }

    public MyButton(String text, CardType cardType) {
        super(text);
        this.cardType = cardType;
    }

    public MyButton(String text, int direction) {
        super(text);
        this.direction = direction;
    }

}
