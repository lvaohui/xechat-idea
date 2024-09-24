package cn.xeblog.plugin.game.mahjong.server;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.xeblog.commons.entity.game.mahjong.MahJongGameDTO;
import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import cn.xeblog.commons.util.ThreadUtils;
import cn.xeblog.plugin.game.mahjong.MahJongGame;
import cn.xeblog.plugin.game.mahjong.utils.MahJongUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MahJongServer {

    /**
     * 房间
     */
    private MahJongGame game;
    /**
     * 庄家索引
     */
    private int zhuangIndex = 0;
    /**
     * 当前牌权玩家
     */
    private AtomicInteger currentPlayerIndex;
    /**
     * 麻将牌
     */
    private List<MahJongCard> mahJongCardList;
    /**
     * 麻将索引
     */
    private AtomicInteger mahJongIndex;
    /**
     * 玩家换出的牌
     */
    private List<List<MahJongCard>> playerSwapCardList;
    /**
     * 交换方向
     */
    private int swapDirection;
    /**
     * 交换牌的计数，达到5代表所有数据全了
     */
    private AtomicInteger swapCounter;
    /**
     * 别人出牌后接受到的过牌或try的计数
     */
    private AtomicInteger receiveCounter;
    /**
     * 尝试发送的消息
     */
    private Vector<MahJongGameDTO> tryDTOVector;
    /**
     * 等待胡牌的计数
     */
    private AtomicInteger huCouter;
    /**
     * 已经胡牌的玩家
     */
    private Set<Integer> huPlayerSet;
    /**
     * 第一个胡的玩家，用来判断是否连庄
     */
    private AtomicInteger firstHuPlayer;

    /**
     * 构造函数
     *
     * @param game
     */
    public MahJongServer(MahJongGame game) {
        this.game = game;
    }

    public void handle(MahJongGameDTO mahJongGameDTO) {
        String player = mahJongGameDTO.getPlayer();
        int index = this.game.findPlayerIndex(player);
        switch (mahJongGameDTO.getMsgType()) {
            case READY: {
                if (receiveCounter.incrementAndGet() == 4) {
                    /* 第一个胡的不是庄，轮庄 */
                    if (firstHuPlayer.get() != zhuangIndex) {
                        zhuangIndex = (zhuangIndex + 1) % 4;
                    }
                    allocCard();
                }
                break;
            }
            case SWAP_OUT: {
                playerSwapCardList.get(index).addAll((List<MahJongCard>) mahJongGameDTO.getData());
                if (swapCounter.incrementAndGet() == 5) {
                    this.swap();
                }
                break;
            }
            case CONFIRM_SWAP_DIRECTION: {
                swapDirection = (int) mahJongGameDTO.getData();
                if (swapCounter.incrementAndGet() == 5) {
                    this.swap();
                }
                break;
            }
            case TRY:
            case PASS: {
                if (ObjectUtil.isNotNull(mahJongGameDTO.getData())) {
                    MahJongGameDTO gameDTO = (MahJongGameDTO) mahJongGameDTO.getData();
                    tryDTOVector.add(gameDTO);
                    /* 定缺消息 */
                    if (ObjectUtil.equal(gameDTO.getMsgType(), MahJongGameDTO.MsgType.CONFIRM_DONT_NEED_CARD)) {
                        if (receiveCounter.incrementAndGet() == 4) {
                            receiveCounter.set(0);
                            /* 发送定缺消息 */
                            tryDTOVector.forEach(this::sendMsg);
                            tryDTOVector.clear();
                            ThreadUtils.spinMoment(500);
                            this.sendMsg(MahJongGameDTO.MsgType.START, game.getPlayerList().get(zhuangIndex), null, null);
                        }
                        break;
                    }
                }
                /* 另外3个人都回应了 */
                if (receiveCounter.incrementAndGet() == 3) {
                    /* 清空计数器 */
                    receiveCounter.set(0);
                    /* 如果没有消息则都过了，发牌 */
                    if (tryDTOVector.size() == 0) {
                        this.sendCard(true);
                        break;
                    }
                    huCouter.set(0);
                    tryDTOVector.forEach(gameDto -> {
                        if (ObjectUtil.equal(gameDto.getMsgType(), MahJongGameDTO.MsgType.HU_CARD)) {
                            huCouter.incrementAndGet();
                        }
                    });
                    int huCnt = 0;
                    /* 先发送胡的消息 */
                    for (MahJongGameDTO gameDTO : tryDTOVector) {
                        if (ObjectUtil.notEqual(gameDTO.getMsgType(), MahJongGameDTO.MsgType.HU_CARD)) {
                            continue;
                        }
                        huCnt++;
                        this.sendMsg(gameDTO);
                        ThreadUtils.spinMoment(500);
                    }
                    if (huCnt > 0) {
                        tryDTOVector.clear();
                        break;
                    }
                    /* 再发送碰杠消息 */
                    for (MahJongGameDTO gameDTO : tryDTOVector) {
                        if (ObjectUtil.equal(gameDTO.getMsgType(), MahJongGameDTO.MsgType.HU_CARD)) {
                            continue;
                        }
                        this.sendMsg(gameDTO);
                    }
                    tryDTOVector.clear();
                }
                break;
            }
            case PEND_CARD: {
                /* 牌权设置为当前玩家 */
                this.currentPlayerIndex.set(index);
                break;
            }
            case GANG_CARD:
            case MANUAL_GANG_CARD: {
                this.currentPlayerIndex.set(index);
                this.sendCard(false);
                break;
            }
            case HU_CARD:
            case MANUAL_HU_CARD: {
                firstHuPlayer.compareAndSet(-1, index);
                huPlayerSet.add(index);
                /* 所有try的人都胡了才发牌 */
                if (huCouter.decrementAndGet() <= 0) {
                    huCouter.set(0);
                    this.currentPlayerIndex.set(index);
                    this.sendCard(true);
                }
                break;
            }
            default: {
                return;
            }
        }
    }

    /**
     * 结束
     */
    private void sendOver() {
        receiveCounter.set(0);
        sendMsg(MahJongGameDTO.MsgType.OVER, null, null, null);
    }

    /**
     * 单个发牌
     */
    private void sendCard(boolean next) {
        int index = mahJongIndex.getAndIncrement();
        if (index >= mahJongCardList.size() || huPlayerSet.size() >= 3) {
            sendOver();
            return;
        }
        int pidx = next ? currentPlayerIndex.incrementAndGet() : currentPlayerIndex.get();
        /* 发给没胡的 */
        while (huPlayerSet.contains(pidx % 4)) {
            pidx = currentPlayerIndex.incrementAndGet();
        }
        /* 发给下一个玩家 */
        sendMsg(MahJongGameDTO.MsgType.CATCH_CARD,
                game.getPlayerList().get(pidx % 4),
                mahJongCardList.get(index), null);
    }

    /**
     * 换三张
     */
    private void swap() {
        /* 换三张开始前计数器归0 */
        receiveCounter.set(0);
        for (int i = 0; i < this.game.getPlayerList().size(); ++i) {
            this.sendMsg(MahJongGameDTO.MsgType.SWAP_IN,
                    this.game.getPlayerList().get(i),
                    playerSwapCardList.get((i + (swapDirection == 0 ? 2 : swapDirection) + 4) % 4),
                    null);
        }
    }

    /**
     * 初始发牌
     */
    public void allocCard() {
        /* 发牌前初始化 */
        this.reset();
        /* 发牌 */
        for (int i = 0; i < this.game.getPlayerList().size(); ++i) {
            int cnt = (i == zhuangIndex ? 14 : 13);
            int st = mahJongIndex.getAndAdd(cnt);
            this.sendMsg(MahJongGameDTO.MsgType.ALLOC_CARD,
                    this.game.getPlayerList().get(i), CollUtil.sub(mahJongCardList, st, st + cnt), null);
        }
    }

    /**
     * 初始化
     */
    private void reset() {
        currentPlayerIndex = new AtomicInteger(zhuangIndex);
        mahJongCardList = MahJongUtil.randomCardList();
        mahJongIndex = new AtomicInteger(0);
        playerSwapCardList = Collections.synchronizedList(ListUtil.toList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        swapCounter = new AtomicInteger(0);
        /* 碰杠胡拦截 */
        tryDTOVector = new Vector<>();
        receiveCounter = new AtomicInteger(0);
        huCouter = new AtomicInteger(0);
        huPlayerSet = new ConcurrentHashSet<>();
        firstHuPlayer = new AtomicInteger(-1);
    }

    private void sendMsg(MahJongGameDTO.MsgType msgType, String player, Object data, String otherPlayer) {
        MahJongGameDTO mahJongGameDTO = new MahJongGameDTO();
        mahJongGameDTO.setMsgType(msgType);
        mahJongGameDTO.setData(data);
        mahJongGameDTO.setPlayer(player);
        mahJongGameDTO.setOtherPlayer(otherPlayer);
        this.sendMsg(mahJongGameDTO);
    }

    private void sendMsg(MahJongGameDTO mahJongGameDTO) {
        this.game.serverSendMsgImpl(mahJongGameDTO);
    }

}
