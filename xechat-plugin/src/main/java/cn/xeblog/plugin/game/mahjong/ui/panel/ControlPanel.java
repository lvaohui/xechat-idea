package cn.xeblog.plugin.game.mahjong.ui.panel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.xeblog.commons.entity.game.mahjong.enums.CardType;
import cn.xeblog.plugin.game.mahjong.domain.PlayerNode;
import cn.xeblog.plugin.game.mahjong.ui.MahJongPanel;
import cn.xeblog.plugin.game.mahjong.ui.component.MyButton;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ControlPanel extends BasicJPanel {

    @Setter
    private PlayerNode playerNode;
    /**
     * 换三张按钮
     */
    private final JButton swapBtn;
    /**
     * 换三张方向按钮
     */
    private final List<MyButton> swapDirectionBtnList;
    private final List<MyButton> notNeedBtnList;
    private final JButton passBtn;
    private final JButton pengBtn;
    private final JButton gangBtn;
    @Getter
    private final JButton tingBtn;
    @Getter
    private final JButton outBtn;
    private final JButton huBtn;
    @Getter
    private final JButton readyBtn;
    /**
     * 原子操作保证一次只能有一个按钮点击
     */
    private final AtomicBoolean mutex;

    public ControlPanel(MahJongPanel mahJongPanel) {
        super(mahJongPanel);
        /* 交换方向按钮 */
        swapDirectionBtnList = new Vector<>();
        mutex = new AtomicBoolean(false);
        for (Pair<Integer, String> pair : ListUtil.toList(new Pair<>(-1, "↺"), new Pair<>(0, "⇅"), new Pair<>(1, "↻"))) {
            MyButton btn = new MyButton(pair.getValue(), pair.getKey());
            this.addListener(btn, b -> this.getMahJongPanel().swapDirectionBtnClick((MyButton) b));
            btn.setVisible(false);
            this.add(btn);
            swapDirectionBtnList.add(btn);
        }
        /* 定缺按钮 */
        notNeedBtnList = new Vector<>();
        for (CardType cardType : ListUtil.toList(CardType.WAN, CardType.TIAO, CardType.TONG)) {
            MyButton btn = new MyButton(cardType.getName(), cardType);
            this.addListener(btn, b -> this.getMahJongPanel().confirmNotNeedBtnClicked((MyButton) b));
            btn.setVisible(false);
            this.add(btn);
            notNeedBtnList.add(btn);
        }
        /* 其他按钮 */
        swapBtn = new JButton("确定换三张");
        passBtn = new JButton("过");
        pengBtn = new JButton("碰");
        gangBtn = new JButton("杠");
        outBtn = new JButton("出");
        tingBtn = new JButton("听");
        huBtn = new JButton("胡");
        readyBtn = new JButton("准备");
        /* 添加事件 */
        this.addListener(swapBtn, b -> this.getMahJongPanel().swapThreeCardBtnClicked(b));
        this.addListener(passBtn, () -> this.getMahJongPanel().passBtnClicked());
        this.addListener(pengBtn, () -> this.getMahJongPanel().pengBtnClicked());
        this.addListener(gangBtn, () -> this.getMahJongPanel().gangBtnClicked());
        this.addListener(outBtn, ()-> this.getMahJongPanel().outBtnClicked());
        this.addListener(tingBtn, () -> this.getMahJongPanel().tingBtnClicked());
        this.addListener(huBtn, () -> this.getMahJongPanel().huBtnClicked());
        this.addListener(readyBtn, () -> {
            readyBtn.setVisible(false);
            this.getMahJongPanel().readyBtnClicked();
        });

        /* 设置不显示 */
        swapBtn.setVisible(false);
        passBtn.setVisible(false);
        pengBtn.setVisible(false);
        gangBtn.setVisible(false);
        outBtn.setVisible(false);
        tingBtn.setVisible(false);
        huBtn.setVisible(false);
        readyBtn.setVisible(false);
        /* 添加组件 */
        this.add(swapBtn);
        this.add(passBtn);
        this.add(pengBtn);
        this.add(gangBtn);
        this.add(outBtn);
        this.add(tingBtn);
        this.add(huBtn);
        this.add(readyBtn);
    }

    /**
     * 重新更新界面展示
     */
    @Override
    public synchronized void updateShowUi() {
        if (ObjectUtil.isNull(this.playerNode)) {
            return;
        }
        /* 是否其他人出牌 */
        boolean isOtherOut = !this.playerNode.isCanOut();
        /* 他人出牌且能碰杠胡时才显示过 */
        this.passBtn.setVisible(isOtherOut && (this.playerNode.getHuBO().getEnable()
                || this.playerNode.getPengBO().getEnable() || this.playerNode.getGangBO().getEnable()));
        this.pengBtn.setVisible(this.playerNode.getPengBO().getEnable());
        this.gangBtn.setVisible(this.playerNode.getGangBO().getEnable());
        this.outBtn.setVisible(this.playerNode.isCanOut());
        this.tingBtn.setVisible(this.playerNode.getTingBO().getEnable()
                && CollUtil.isNotEmpty(this.playerNode.getChoiceCardLabelList()) && this.playerNode.getTingBO().getCardList().stream().anyMatch(
                card -> ObjectUtil.equal(card, CollUtil.getFirst(this.playerNode.getChoiceCardLabelList()).getCard())));
        this.huBtn.setVisible(this.playerNode.getHuBO().getEnable());
        this.updateUI();
    }

    /**
     * 显示换三张按钮
     */
    public void showSwapBtn() {
        this.swapBtn.setVisible(true);
        this.updateShowUi();
    }

    /**
     * 显示交换方向按钮
     */
    public void showSwapDirectionBtn() {
        for (MyButton button : this.swapDirectionBtnList) {
            button.setVisible(true);
        }
//        this.updateUI();
    }

    /**
     * 取消交换方向按钮
     */
    public void hideSwapDirectionBtn() {
        for (MyButton button : this.swapDirectionBtnList) {
            button.setVisible(false);
        }
//        this.updateUI();
    }

    /**
     * 显示定缺按钮
     */
    public void showNotNeedBtn() {
        for (MyButton button : this.notNeedBtnList) {
            button.setVisible(true);
        }
//        this.updateUI();
    }

    /**
     * 显示定缺按钮
     */
    public void hideNotNeedBtn() {
        for (MyButton button : this.notNeedBtnList) {
            button.setVisible(false);
        }
//        this.updateUI();
    }

    private void addListener(JButton btn, Consumer<JButton> func) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (mutex.compareAndSet(false, true)) {
                    func.accept((JButton) e.getComponent());
                    mutex.set(false);
                }
            }
        });
    }

    private void addListener(JButton btn, Runnable runnable) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (mutex.compareAndSet(false, true)) {
                    runnable.run();
                    mutex.set(false);
                }
            }
        });
    }

}
