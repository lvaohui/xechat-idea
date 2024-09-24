package cn.xeblog.plugin.game.mahjong.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.xeblog.commons.entity.game.mahjong.MahJongGameDTO;
import cn.xeblog.commons.entity.game.mahjong.enums.CardType;
import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import cn.xeblog.plugin.game.mahjong.MahJongGame;
import cn.xeblog.plugin.game.mahjong.domain.PlayerNode;
import cn.xeblog.plugin.game.mahjong.enums.Role;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AiPlayerAction {

    private MahJongGame game;
    /**
     * 当前玩家节点
     */
    private PlayerNode currentPlayerNode;

    public AiPlayerAction(MahJongGame game, String name) {
        this.game = game;
        /* 只用来算牌 */
        this.currentPlayerNode = new PlayerNode(name, "");
    }

    public void handle(MahJongGameDTO mahJongGameDTO) {
        if (ObjectUtil.equal(mahJongGameDTO.getMsgType(), MahJongGameDTO.MsgType.OVER)) {
            /* AI接收到结束直接准备 */
            this.exec(this::readyToPlay);
            return;
        }
        String player = mahJongGameDTO.getPlayer();
        /* TODO AI只用处理其他人的出牌消息 */
        if (!StrUtil.equals(player, currentPlayerNode.getName())) {
            switch (mahJongGameDTO.getMsgType()) {
                case OUT_CARD:
                case TING_CARD: {
                    MahJongCard card = (MahJongCard) mahJongGameDTO.getData();
                    /* 他人出牌，判断是否能过牌 */
                    if (currentPlayerNode.canPass(card, mahJongGameDTO.getPlayer())) {
                        this.sendMsg(MahJongGameDTO.MsgType.PASS, null, null);
                    } else {
                        /* 不能过牌进行回应 */
                        this.exec(this::doAction);
                    }
                    break;
                }
                default:
            }
            return;
        }
        switch (mahJongGameDTO.getMsgType()) {
            case ALLOC_CARD: {
                List<MahJongCard> cardList = (List<MahJongCard>) mahJongGameDTO.getData();
                currentPlayerNode.allocCard(cardList);
                this.exec(this::swapThreeCard);
                break;
            }
            case SWAP_IN: {
                currentPlayerNode.swapInThreeCard((List<MahJongCard>) mahJongGameDTO.getData());
                this.exec(this::confirmNotNeed);
                break;
            }
            case CONFIRM_DONT_NEED_CARD: {
                currentPlayerNode.setNotNeedType((CardType) mahJongGameDTO.getData());
                break;
            }
            case START: {
                /* 如果当前玩家是庄则判断 */
                if (ObjectUtil.equal(currentPlayerNode.getRole(), Role.ZHUANG)) {
                    this.currentPlayerNode.judgeGang();
                    this.currentPlayerNode.judgeTing();
                    this.currentPlayerNode.judgeHu();
                    this.currentPlayerNode.setCanOut(true);
                    /* 进行回应 */
                    this.exec(this::doAction);
                }
                break;
            }
            case CATCH_CARD: {
                MahJongCard card = (MahJongCard) mahJongGameDTO.getData();
                this.currentPlayerNode.catchCard(card);
                this.currentPlayerNode.judgeGang();
                this.currentPlayerNode.judgeTing();
                this.currentPlayerNode.judgeHu();
                /* 进行回应 */
                this.exec(this::doAction);
                break;
            }
            case PEND_CARD: {
                this.currentPlayerNode.pengCard((MahJongCard) mahJongGameDTO.getData(), mahJongGameDTO.getOtherPlayer());
                this.currentPlayerNode.judgeGang();
                this.currentPlayerNode.judgeTing();
                this.exec(this::doAction);
                break;
            }
            case GANG_CARD: {
                this.currentPlayerNode.gangCard((MahJongCard) mahJongGameDTO.getData(), mahJongGameDTO.getOtherPlayer());
                break;
            }
            case HU_CARD: {
                this.currentPlayerNode.huCard((MahJongCard) mahJongGameDTO.getData(), mahJongGameDTO.getOtherPlayer(), mahJongGameDTO.getScore());
                break;
            }
            default: {
            }
        }
    }

    /**
     * 准备
     */
    private void readyToPlay() {
        this.currentPlayerNode.start();
        this.currentPlayerNode.readyPlay();
        this.sendMsg(MahJongGameDTO.MsgType.READY, null, null);
    }

    /**
     * 换三张
     */
    private void swapThreeCard() {
        /* 统计各个类型牌的数量，选择>=3且最小的一种类型随机3张 */
        Map<CardType, List<MahJongCard>> cardTypeMap = new ConcurrentHashMap<>();
        for (MahJongCard card : this.currentPlayerNode.getNormalCardList()) {
            if (ObjectUtil.isNull(cardTypeMap.get(card.getType()))) {
                cardTypeMap.put(card.getType(), new ArrayList<>());
            }
            cardTypeMap.get(card.getType()).add(card);
        }
        int cnt = 15;
        CardType minCardType = null;
        for (Map.Entry<CardType, List<MahJongCard>> entry : cardTypeMap.entrySet()) {
            int size = entry.getValue().size();
            if (size >= 3 && size < cnt) {
                minCardType = entry.getKey();
                cnt = entry.getValue().size();
            }
        }
        /* 打乱 */
        Collections.shuffle(cardTypeMap.get(minCardType));
        /* 随机选3张交换 */
        List<MahJongCard> cardList = cardTypeMap.get(minCardType).subList(0, 3);
        /* 先自己执行 */
        this.currentPlayerNode.swapOutThreeCard(cardList);
        /* 然后发送消息 */
        this.sendMsg(MahJongGameDTO.MsgType.SWAP_OUT, new ArrayList<>(cardList), null);
        /* 如果是庄则需要确定交换方向 */
        if (ObjectUtil.equal(this.currentPlayerNode.getRole(), Role.ZHUANG)) {
            this.swapDirection();
        }
    }

    /**
     * 确定交换方向
     */
    private void swapDirection() {
        /* 随机方向 */
        this.sendMsg(MahJongGameDTO.MsgType.CONFIRM_SWAP_DIRECTION, RandomUtil.randomInt(-1, 2), null);
    }

    /**
     * 定缺
     */
    private void confirmNotNeed() {
        /* 统计各个类型牌的数量，选中最少的定为缺牌 */
        Map<CardType, Integer> cardTypeMap = new ConcurrentHashMap<>();
        for (MahJongCard card : this.currentPlayerNode.getNormalCardList()) {
            cardTypeMap.compute(card.getType(), (k, v) -> ObjectUtil.isNull(v) ? 1 : ++v);
        }
        for (CardType cardType : CardType.values()) {
            if (ObjectUtil.equal(cardType, CardType.WAIT)) {
                continue;
            }
            cardTypeMap.putIfAbsent(cardType, 0);
        }
        CardType cardType = cardTypeMap.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
        MahJongGameDTO mahJongGameDTO = MahJongGameDTO.builder()
                .msgType(MahJongGameDTO.MsgType.CONFIRM_DONT_NEED_CARD)
                .player(this.currentPlayerNode.getName())
                .data(cardType)
                .build();
        /* 尝试定缺 */
        this.sendMsg(MahJongGameDTO.MsgType.TRY, mahJongGameDTO, null);
    }

    /**
     * 进行回应, 按优先级来
     * 胡 -> 听 -> 杠 -> 碰 -> 随机出牌
     */
    private void doAction() {
        /* 能胡就胡 */
        if (this.currentPlayerNode.getHuBO().getEnable()) {
            this.huAction();
            return;
        }
        if (this.currentPlayerNode.getTingBO().getEnable()) {
            this.tingAction();
            return;
        }
        if (this.currentPlayerNode.getGangBO().getEnable()) {
            this.gangAction();
            return;
        }
        if (this.currentPlayerNode.getPengBO().getEnable()) {
            this.pengAction();
            return;
        }
        this.outAction();
    }

    /**
     * 胡
     */
    private void huAction() {
        /* 牌权在自己，直接胡 */
        if (currentPlayerNode.isCanOut()) {
            currentPlayerNode.huCard(currentPlayerNode.getHuBO().getCard(),
                    currentPlayerNode.getHuBO().getOppPlayer(), currentPlayerNode.getHuBO().getScore());
            this.sendMsg(MahJongGameDTO.builder()
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
            this.sendMsg(MahJongGameDTO.MsgType.TRY, mahJongGameDTO, null);
        }
    }

    /**
     * 听
     */
    private void tingAction() {
        /* 从听牌列表中随机选择一个出 */
        Collections.shuffle(this.currentPlayerNode.getTingBO().getCardList());
        MahJongCard card = CollUtil.getFirst(this.currentPlayerNode.getTingBO().getCardList());
        currentPlayerNode.tingCard(card);
        this.sendMsg(MahJongGameDTO.MsgType.TING_CARD, card, null);
    }

    /**
     * 杠
     */
    private void gangAction() {
        /* 牌权在自己，直接可杠，不用try */
        if (currentPlayerNode.isCanOut()) {
            currentPlayerNode.gangCard(currentPlayerNode.getGangBO().getCard(), currentPlayerNode.getGangBO().getOppPlayer());
            this.sendMsg(MahJongGameDTO.MsgType.MANUAL_GANG_CARD, currentPlayerNode.getGangBO().getCard(), currentPlayerNode.getGangBO().getOppPlayer());
        } else {
            currentPlayerNode.resetStatus();
            MahJongGameDTO mahJongGameDTO = MahJongGameDTO.builder()
                    .msgType(MahJongGameDTO.MsgType.GANG_CARD)
                    .player(this.currentPlayerNode.getName())
                    .data(currentPlayerNode.getGangBO().getCard())
                    .otherPlayer(currentPlayerNode.getGangBO().getOppPlayer())
                    .build();
            this.sendMsg(MahJongGameDTO.MsgType.TRY, mahJongGameDTO, null);
        }
    }

    /**
     * 碰
     */
    private void pengAction() {
        currentPlayerNode.resetStatus();
        MahJongGameDTO mahJongGameDTO = MahJongGameDTO.builder()
                .msgType(MahJongGameDTO.MsgType.PEND_CARD)
                .player(this.currentPlayerNode.getName())
                .data(currentPlayerNode.getPengBO().getCard())
                .otherPlayer(currentPlayerNode.getPengBO().getOppPlayer())
                .build();
        this.sendMsg(MahJongGameDTO.MsgType.TRY, mahJongGameDTO, null);
    }

    /**
     * 出牌
     */
    private void outAction() {
        MahJongCard card = null;
        /* 缺牌不空，先出缺牌 */
        if (CollUtil.isNotEmpty(this.currentPlayerNode.getNotNeedCardList())) {
            card = CollUtil.getFirst(this.currentPlayerNode.getNotNeedCardList());
            /* 如果最后一张是缺牌，则出最后一张 */
        } else if (ObjectUtil.isNotNull(this.currentPlayerNode.getLastCard())
                && ObjectUtil.equal(this.currentPlayerNode.getLastCard().getType(), this.currentPlayerNode.getNotNeedType())) {
            card = this.currentPlayerNode.getLastCard();
            /* 如果是听牌状态只能出最后一张 */
        } else if (this.currentPlayerNode.isTing()) {
            card = this.currentPlayerNode.getLastCard();
            /* 其余情况随机出牌 */
        } else {
            List<MahJongCard> mahJongCards = new Vector<>(this.currentPlayerNode.getNormalCardList());
            if (ObjectUtil.isNotNull(this.currentPlayerNode.getLastCard())) {
                mahJongCards.add(this.currentPlayerNode.getLastCard());
            }
            Collections.shuffle(mahJongCards);
            card = CollUtil.getFirst(mahJongCards);
        }
        /* 出牌 */
        this.currentPlayerNode.outCard(card);
        this.sendMsg(MahJongGameDTO.MsgType.OUT_CARD, card, null);
    }

    /**
     * 执行
     *
     * @param runnable
     */
    private void exec(Runnable runnable) {
        this.game.invoke(runnable, 500);
    }

    /**
     * 发送消息
     *
     * @param msgType
     * @param data
     * @param otherPlayer
     */
    public void sendMsg(MahJongGameDTO.MsgType msgType, Object data, String otherPlayer) {
        MahJongGameDTO mahJongGameDTO = new MahJongGameDTO();
        mahJongGameDTO.setMsgType(msgType);
        mahJongGameDTO.setData(data);
        mahJongGameDTO.setPlayer(this.currentPlayerNode.getName());
        mahJongGameDTO.setOtherPlayer(otherPlayer);
        this.sendMsg(mahJongGameDTO);
    }

    /**
     * 发送消息
     *
     * @param mahJongGameDTO
     */
    public void sendMsg(MahJongGameDTO mahJongGameDTO) {
        this.game.aiSendMsgImpl(mahJongGameDTO);
    }

}
