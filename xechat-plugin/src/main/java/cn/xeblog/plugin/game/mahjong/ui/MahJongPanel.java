package cn.xeblog.plugin.game.mahjong.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.xeblog.commons.entity.game.mahjong.MahJongGameDTO;
import cn.xeblog.commons.entity.game.mahjong.enums.CardType;
import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import cn.xeblog.plugin.game.mahjong.MahJongGame;
import cn.xeblog.plugin.game.mahjong.domain.PlayerNode;
import cn.xeblog.plugin.game.mahjong.enums.Role;
import cn.xeblog.plugin.game.mahjong.ui.component.CardLabel;
import cn.xeblog.plugin.game.mahjong.ui.component.MyButton;
import cn.xeblog.plugin.game.mahjong.ui.enums.Position;
import cn.xeblog.plugin.game.mahjong.ui.panel.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class MahJongPanel extends JPanel {

    /**
     * 用来发送信息
     */
    private MahJongGame game;
    /**
     * 中心剩余牌数位置
     */
    private CenterPanel centerPanel;
    /**
     * 控制按钮的位置
     */
    private ControlPanel controlPanel;
    /**
     * 游戏组件列表
     */
    private Map<String, List<BasicJPanel>> panelListMap;
    /**
     * 当前玩家节点
     */
    private PlayerNode currentPlayerNode;
    /**
     * 当前玩家节点
     */
    private Map<String, PlayerNode> playerNodeMap;
    /**
     * 游戏状态
     */
    private AtomicBoolean over;

    public MahJongPanel() {
        this(null);
    }

    /**
     * 构造函数
     */
    public MahJongPanel(MahJongGame mahJongGame) {
        this.game = mahJongGame;
        this.panelListMap = new ConcurrentHashMap<>();
        this.currentPlayerNode = null;
        this.playerNodeMap = new ConcurrentHashMap<>();
        this.over = new AtomicBoolean(false);
        this.setLayout(null);
        /* 监听大小改变事件，改变内部所有组件位置和大小 */
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                Dimension dimension = e.getComponent().getSize();
                double width = dimension.getWidth() / 26f;
                double height = dimension.getHeight() / 21f;
                panelListMap.forEach((k, v) -> v.forEach(p -> p.setBounds(width, height)));
            }
        });
        init();
    }

    public void init() {
        /* 中心剩余牌数位置 */
        centerPanel = new CenterPanel(this);
        /* 控制按钮的位置 */
        controlPanel = new ControlPanel(this);

        centerPanel.setFixedPosition(7, 7, 12, 4);
        controlPanel.setFixedPosition(3, 15, 20, 3);

        this.addPanel("control-panel", controlPanel);
        this.addPanel("center-panel", centerPanel);
    }

    /**
     * 添加玩家
     *
     * @param playerNode
     * @param position
     */
    public void addPlayer(PlayerNode playerNode, Position position) {
        this.playerNodeMap.put(playerNode.getName(), playerNode);
        /* 添加当前玩家节点 */
        if (ObjectUtil.equal(position, Position.BOTTOM)) {
            this.currentPlayerNode = playerNode;
            this.controlPanel.setPlayerNode(playerNode);
        }
        this.centerPanel.addPlayerNode(playerNode, position);
        /* 玩家信息显示区域 */
        InfoPanel infoPanel = new InfoPanel(this, playerNode, position);
        /* 玩家手牌显示区域 */
        BasicJPanel playerPanel = ObjectUtil.equal(position, Position.BOTTOM)
                ? new PlayerCardPanel(this, playerNode) : new OtherPlayerCardPanel(this, playerNode, position);
        /* 玩家出牌显示区域 */
        OutCardPanel outCardPanel = new OutCardPanel(this, playerNode, position);
        switch (position) {
            case BOTTOM:
                infoPanel.setFixedPosition(23, 18, 3, 3);
                playerPanel.setFixedPosition(3, 18, 20, 3);
                outCardPanel.setFixedPosition(7, 11, 12, 4);
                break;
            case UP:
                infoPanel.setFixedPosition(0, 0, 3, 3);
                playerPanel.setFixedPosition(3, 0, 20, 3);
                outCardPanel.setFixedPosition(7, 3, 12, 4);
                break;
            case LEFT:
                infoPanel.setFixedPosition(0, 18, 3, 3);
                playerPanel.setFixedPosition(0, 3, 3, 15);
                outCardPanel.setFixedPosition(3, 3, 4, 12);
                break;
            case RIGHT:
                infoPanel.setFixedPosition(23, 0, 3, 3);
                playerPanel.setFixedPosition(23, 3, 3, 15);
                outCardPanel.setFixedPosition(19, 3, 4, 12);
                break;
            default:
        }
        this.addPanel(playerNode.getName(), playerPanel);
        this.addPanel(playerNode.getName(), outCardPanel);
        this.addPanel(playerNode.getName(), infoPanel);
    }


    /**
     * 处理外部消息，消息到来时每个组件的handle都被调用，如果消息和该组件有关则更新该组件中的内容
     *
     * @param mahJongGameDTO
     */
    public synchronized void handle(MahJongGameDTO mahJongGameDTO) {
        if (ObjectUtil.equal(mahJongGameDTO.getMsgType(), MahJongGameDTO.MsgType.OVER)) {
            this.over.compareAndSet(false, true);
            this.controlPanel.getReadyBtn().setVisible(true);
            this.updateShowUi();
            return;
        }
        String player = mahJongGameDTO.getPlayer();
        PlayerNode playerNode = this.playerNodeMap.get(player);
        if (ObjectUtil.isNull(playerNode)) {
            return;
        }
        boolean isMe = StrUtil.equals(player, currentPlayerNode.getName());
        switch (mahJongGameDTO.getMsgType()) {
            case READY: {
                playerNode.readyPlay();
                break;
            }
            case ALLOC_CARD: {
                if (this.over.compareAndSet(true, false)) {
                    this.centerPanel.getNum().set(108);
                }
                List<MahJongCard> cardList = (List<MahJongCard>) mahJongGameDTO.getData();
                playerNode.allocCard(cardList);
                if (isMe) {
                    this.controlPanel.showSwapBtn();
                }
                this.centerPanel.updateCardNum(-cardList.size());
                this.updateShowUi(player);
                return;
            }
            case SWAP_OUT: {
                playerNode.swapOutThreeCard((List<MahJongCard>) mahJongGameDTO.getData());
                this.updateShowUi(player);
                return;
            }
            case SWAP_IN: {
                playerNode.swapInThreeCard((List<MahJongCard>) mahJongGameDTO.getData());
                if (isMe) {
                    this.controlPanel.showNotNeedBtn();
                }
                break;
            }
            case CONFIRM_DONT_NEED_CARD: {
                playerNode.setNotNeedType((CardType) mahJongGameDTO.getData());
                this.updateShowUi(player);
                return;
            }
            case START: {
                this.currentPlayerNode.getChoiceCardLabelList().clear();
                /* 如果当前玩家是庄则判断 */
                if (ObjectUtil.equal(currentPlayerNode.getRole(), Role.ZHUANG)) {
                    this.currentPlayerNode.judgeGang();
                    this.currentPlayerNode.judgeTing();
                    this.currentPlayerNode.judgeHu();
                }
                playerNode.setCanOut(true);
                break;
            }
            case CATCH_CARD: {
                MahJongCard card = (MahJongCard) mahJongGameDTO.getData();
                this.centerPanel.updateCardNum(-1);
                playerNode.catchCard(card);
                /* 自己抓牌，判断是否能杠、听、胡 */
                if (isMe) {
                    this.currentPlayerNode.judgeGang();
                    this.currentPlayerNode.judgeTing();
                    this.currentPlayerNode.judgeHu();
                    /* 听牌状态 且不能胡 且不能杠，直接出刚抓的 */
                    if (this.currentPlayerNode.isTing() && !this.currentPlayerNode.getHuBO().getEnable()
                            && !this.currentPlayerNode.getGangBO().getEnable()) {
                        MahJongCard lastCard = this.currentPlayerNode.getLastCard();
                        this.currentPlayerNode.outCard(lastCard);
                        this.currentPlayerNode.getChoiceCardLabelList().clear();
                        this.game.sendMsg(MahJongGameDTO.MsgType.OUT_CARD, lastCard);
                    }
                }
                break;
            }
            case OUT_CARD:
            case TING_CARD: {
                MahJongCard card = (MahJongCard) mahJongGameDTO.getData();
                if (ObjectUtil.equal(mahJongGameDTO.getMsgType(), MahJongGameDTO.MsgType.TING_CARD)) {
                    playerNode.tingCard(card);
                } else {
                    playerNode.outCard(card);
                }
                /* 他人出牌，判断是否能过牌 */
                if (currentPlayerNode.canPass(card, mahJongGameDTO.getPlayer())) {
                    this.game.sendMsg(MahJongGameDTO.MsgType.PASS, null);
                }
                break;
            }
            case PEND_CARD: {
                playerNode.pengCard((MahJongCard) mahJongGameDTO.getData(), mahJongGameDTO.getOtherPlayer());
                if (isMe) {
                    this.currentPlayerNode.judgeGang();
                    this.currentPlayerNode.judgeTing();
                }
                break;
            }
            case GANG_CARD:
            case MANUAL_GANG_CARD: {
                playerNode.gangCard((MahJongCard) mahJongGameDTO.getData(), mahJongGameDTO.getOtherPlayer());
                break;
            }
            case HU_CARD:
            case MANUAL_HU_CARD: {
                playerNode.huCard((MahJongCard) mahJongGameDTO.getData(), mahJongGameDTO.getOtherPlayer(), mahJongGameDTO.getScore());
                break;
            }
            default: {
                return;
            }
        }
        this.updateShowUi();
    }

    /**
     * 麻将牌点击
     *
     * @param label 点击的麻将牌
     */
    public void cardClicked(CardLabel label) {
        /* 选中状态，这次点击是取消 */
        if (label.isChoiced()) {
            label.click();
            this.currentPlayerNode.getChoiceCardLabelList().remove(label);
            this.controlPanel.getTingBtn().setVisible(false);
            return;
        }
        /* 游戏未开始，换三张阶段 */
        if (ObjectUtil.isNull(this.currentPlayerNode.getNotNeedType())
                || ObjectUtil.equal(this.currentPlayerNode.getNotNeedType(), CardType.WAIT)) {
            if (this.currentPlayerNode.getChoiceCardLabelList().size() == 3) {
                return;
            }
            label.click();
            this.currentPlayerNode.getChoiceCardLabelList().add(label);
            return;
        }
        /* 如果已经选了，取消之前选的 */
        if (this.currentPlayerNode.getChoiceCardLabelList().size() > 0) {
            this.currentPlayerNode.getChoiceCardLabelList().forEach(CardLabel::click);
            this.currentPlayerNode.getChoiceCardLabelList().clear();
        }
        label.click();
        this.currentPlayerNode.getChoiceCardLabelList().add(label);
        /* 如果选中了可以听的牌展示听牌按钮 */
        this.controlPanel.updateShowUi();
    }

    /**
     * 换三张按钮点击
     */
    public void swapThreeCardBtnClicked(JButton button) {
        /* 判断是否3张 */
        if (this.currentPlayerNode.getChoiceCardLabelList().size() != 3) {
            return;
        }
        /* 判断类型是否一样 */
        List<MahJongCard> mahJongCardList = this.currentPlayerNode.getChoiceCardLabelList()
                .stream().map(CardLabel::getCard).collect(Collectors.toList());
        mahJongCardList.sort(Comparator.comparingInt(MahJongCard::getId));
        /* 判断类型是否一样 */
        if (ObjectUtil.notEqual(CollUtil.getFirst(mahJongCardList).getType(), CollUtil.getLast(mahJongCardList).getType())) {
            return;
        }
        button.setVisible(false);
        this.currentPlayerNode.swapOutThreeCard(mahJongCardList);
        this.currentPlayerNode.getChoiceCardLabelList().clear();
        /* 如果是庄则需要确定交换方向 */
        if (ObjectUtil.equal(this.currentPlayerNode.getRole(), Role.ZHUANG)) {
            this.controlPanel.showSwapDirectionBtn();
        }
        this.updateShowUi();
        /* 发送消息 */
        this.game.sendMsg(MahJongGameDTO.MsgType.SWAP_OUT, new ArrayList<>(mahJongCardList));
    }

    /**
     * 交换方向
     *
     * @param button
     */
    public void swapDirectionBtnClick(MyButton button) {
        this.controlPanel.hideSwapDirectionBtn();
        this.game.sendMsg(MahJongGameDTO.MsgType.CONFIRM_SWAP_DIRECTION, button.getDirection());
        this.updateShowUi();
    }

    /**
     * 定缺按钮点击
     *
     * @param button 点击的按钮
     */
    public void confirmNotNeedBtnClicked(MyButton button) {
        this.controlPanel.hideNotNeedBtn();
        MahJongGameDTO mahJongGameDTO = MahJongGameDTO.builder()
                .msgType(MahJongGameDTO.MsgType.CONFIRM_DONT_NEED_CARD)
                .player(this.currentPlayerNode.getName())
                .data(button.getCardType())
                .build();
        this.updateShowUi();
        /* 尝试定缺 */
        this.game.sendMsg(MahJongGameDTO.MsgType.TRY, mahJongGameDTO);
    }


    /**
     * 双击出牌
     *
     * @param cardLabel 双击的牌
     */
    public void cardDoubleClicked(CardLabel cardLabel) {
        if (!this.currentPlayerNode.isCanOut()) {
            return;
        }
        this.currentPlayerNode.outCard(cardLabel.getCard());
        this.currentPlayerNode.getChoiceCardLabelList().clear();
        this.updateShowUi();
        this.game.sendMsg(MahJongGameDTO.MsgType.OUT_CARD, cardLabel.getCard());
    }

    /**
     * 听牌按钮点击
     */
    public void tingBtnClicked() {
        if (!currentPlayerNode.getTingBO().getEnable()) {
            return;
        }
        if (CollUtil.isEmpty(this.currentPlayerNode.getChoiceCardLabelList())) {
            return;
        }
        controlPanel.getTingBtn().setVisible(false);
        MahJongCard card = CollUtil.getLast(this.currentPlayerNode.getChoiceCardLabelList()).getCard();
        currentPlayerNode.tingCard(card);
        currentPlayerNode.getChoiceCardLabelList().clear();
        this.updateShowUi();
        this.game.sendMsg(MahJongGameDTO.MsgType.TING_CARD, card);

    }

    /**
     * 出牌按钮点击
     */
    public void outBtnClicked() {
        if (!this.currentPlayerNode.isCanOut()) {
            return;
        }
        if (CollUtil.isEmpty(this.currentPlayerNode.getChoiceCardLabelList())) {
            return;
        }
        controlPanel.getOutBtn().setVisible(false);
        MahJongCard card = CollUtil.getLast(this.currentPlayerNode.getChoiceCardLabelList()).getCard();
        currentPlayerNode.outCard(card);
        currentPlayerNode.getChoiceCardLabelList().clear();
        this.updateShowUi();
        this.game.sendMsg(MahJongGameDTO.MsgType.OUT_CARD, card);

    }

    /**
     * 碰牌按钮点击
     */
    public void pengBtnClicked() {
        if (!currentPlayerNode.getPengBO().getEnable()) {
            return;
        }
        currentPlayerNode.resetStatus();
        MahJongGameDTO mahJongGameDTO = MahJongGameDTO.builder()
                .msgType(MahJongGameDTO.MsgType.PEND_CARD)
                .player(this.currentPlayerNode.getName())
                .data(currentPlayerNode.getPengBO().getCard())
                .otherPlayer(currentPlayerNode.getPengBO().getOppPlayer())
                .build();
        this.updateShowUi();
        this.game.sendMsg(MahJongGameDTO.MsgType.TRY, mahJongGameDTO);
//        currentPlayerNode.pengCard(currentPlayerNode.getPengBO().getCard(), currentPlayerNode.getPengBO().getOppPlayer());
//        this.updateShowUi();
//        this.game.sendMsg(MahJongGameDTO.MsgType.PEND_CARD, currentPlayerNode.getPengBO().getCard(), currentPlayerNode.getPengBO().getOppPlayer());
    }

    /**
     * 杠牌按钮点击
     */
    public void gangBtnClicked() {
        if (!currentPlayerNode.getGangBO().getEnable()) {
            return;
        }
        /* 牌权在自己，直接可杠，不用try */
        if (currentPlayerNode.isCanOut()) {
            currentPlayerNode.gangCard(currentPlayerNode.getGangBO().getCard(), currentPlayerNode.getGangBO().getOppPlayer());
            this.updateShowUi();
            this.game.sendMsg(MahJongGameDTO.MsgType.MANUAL_GANG_CARD, currentPlayerNode.getGangBO().getCard(), currentPlayerNode.getGangBO().getOppPlayer());
        } else {
            currentPlayerNode.resetStatus();
            MahJongGameDTO mahJongGameDTO = MahJongGameDTO.builder()
                    .msgType(MahJongGameDTO.MsgType.GANG_CARD)
                    .player(this.currentPlayerNode.getName())
                    .data(currentPlayerNode.getGangBO().getCard())
                    .otherPlayer(currentPlayerNode.getGangBO().getOppPlayer())
                    .build();
            this.updateShowUi();
            this.game.sendMsg(MahJongGameDTO.MsgType.TRY, mahJongGameDTO);
        }
//        currentPlayerNode.gangCard(currentPlayerNode.getGangBO().getCard(), currentPlayerNode.getGangBO().getOppPlayer());
//        this.updateShowUi();
//        this.game.sendMsg(MahJongGameDTO.MsgType.GANG_CARD, currentPlayerNode.getGangBO().getCard(), currentPlayerNode.getGangBO().getOppPlayer());
    }

    /**
     * 胡牌按钮点击
     */
    public void huBtnClicked() {
        if (!currentPlayerNode.getHuBO().getEnable()) {
            return;
        }
        /* 牌权在自己，直接胡 */
        if (currentPlayerNode.isCanOut()) {
            currentPlayerNode.huCard(currentPlayerNode.getHuBO().getCard(),
                    currentPlayerNode.getHuBO().getOppPlayer(), currentPlayerNode.getHuBO().getScore());
            this.updateShowUi();
            this.game.sendMsgImpl(MahJongGameDTO.builder()
                    .msgType(MahJongGameDTO.MsgType.MANUAL_HU_CARD)
                    .data(currentPlayerNode.getHuBO().getCard())
                    .player(this.currentPlayerNode.getName())
                    .otherPlayer(currentPlayerNode.getHuBO().getOppPlayer())
                    .score(currentPlayerNode.getHuBO().getScore()).build());
        } else {
            currentPlayerNode.resetStatus();
            MahJongGameDTO mahJongGameDTO = MahJongGameDTO.builder()
                    .msgType(MahJongGameDTO.MsgType.HU_CARD)
                    .data(currentPlayerNode.getHuBO().getCard())
                    .player(this.currentPlayerNode.getName())
                    .otherPlayer(currentPlayerNode.getHuBO().getOppPlayer())
                    .score(currentPlayerNode.getHuBO().getScore())
                    .build();
            this.updateShowUi();
            this.game.sendMsg(MahJongGameDTO.MsgType.TRY, mahJongGameDTO);
        }
    }

    /**
     * 过牌按钮点击
     */
    public void passBtnClicked() {
        this.currentPlayerNode.pass();
        this.updateShowUi();
        this.game.sendMsg(MahJongGameDTO.MsgType.PASS, null);
    }

    /**
     * 准备按钮点击
     */
    public void readyBtnClicked() {
        this.playerNodeMap.forEach((k, v) -> {
            v.start();
        });
        this.currentPlayerNode.readyPlay();
        this.updateShowUi();
        this.game.sendMsg(MahJongGameDTO.MsgType.READY, null);
    }

    public void addPanel(String player, BasicJPanel comp) {
        this.panelListMap.putIfAbsent(player, new Vector<>());
        this.panelListMap.get(player).add(comp);
        super.add(comp);
    }

    /**
     * 更新状态
     */
    public synchronized void updateShowUi() {
        this.panelListMap.forEach((k, v) -> v.forEach(BasicJPanel::updateShowUi));
        this.updateUI();
    }

    /**
     * 更新相应玩家对应界面
     *
     * @param player
     */
    public synchronized void updateShowUi(String player) {
        Optional.ofNullable(this.panelListMap.get(player)).orElse(ListUtil.empty())
                .forEach(BasicJPanel::updateShowUi);
    }

    public void showTips(String text) {
        this.centerPanel.showTips(text);
    }

}
