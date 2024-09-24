package cn.xeblog.plugin.game.mahjong.domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.xeblog.commons.entity.game.mahjong.enums.CardType;
import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;
import cn.xeblog.plugin.game.mahjong.enums.Role;
import cn.xeblog.plugin.game.mahjong.ui.component.CardLabel;
import cn.xeblog.plugin.game.mahjong.utils.MahJongUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class PlayerNode {

    /**
     * 其他玩家
     */
    Map<String, PlayerNode> otherPlayerNodeMap;
    /**
     * 玩家姓名
     */
    private String name;
    /**
     * 方向
     */
    private String direct;
    /**
     * 玩家分数
     */
    private AtomicInteger score;
    /**
     * 玩家角色
     */
    private Role role;
    /**
     * 缺
     */
    private CardType notNeedType;
    /**
     * 准备
     */
    private boolean ready;
    /**
     * 是否可出牌
     */
    private boolean canOut;
    /**
     * 是否胡了
     */
    private boolean isHu;
    /**
     * 是否听牌
     */
    private boolean isTing;
    /**
     * 最后一个出牌的
     */
    private boolean lastOut;
    /**
     * 是否刚杠完
     */
    private boolean recentGang;
    /**
     * 暗杠的牌
     */
    private List<MahJongCard> darkGangCardList;
    /**
     * 杠的牌
     */
    private Map<String, List<MahJongCard>> gangCardListMap;
    /**
     * 碰的牌
     */
    private Map<String, List<MahJongCard>> pendCardListMap;
    /**
     * 普通牌
     */
    private List<MahJongCard> normalCardList;
    /**
     * 缺牌
     */
    private List<MahJongCard> notNeedCardList;
    /**
     * 打出去的牌
     */
    private List<MahJongCard> outCardList;
    /**
     * 选中的牌
     */
    private List<CardLabel> choiceCardLabelList;
    /**
     * 刚抓的牌
     */
    private MahJongCard lastCard;
    /**
     * 胡的牌
     */
    private MahJongCard huCard;
    /**
     * 碰牌数据
     */
    private PengBO pengBO;
    /**
     * 杠牌数据
     */
    private GangBO gangBO;
    /**
     * 听牌数据
     */
    private TingBO tingBO;
    /**
     * 胡牌数据
     */
    private HuBO huBO;


    public PlayerNode(String name, String direct) {
        this.otherPlayerNodeMap = new ConcurrentHashMap<>();
        this.name = name;
        this.direct = direct;
        this.score = new AtomicInteger();
        this.ready = false;
        this.start();
    }

    public void addOtherPlayerNode(PlayerNode playerNode) {
        this.otherPlayerNodeMap.put(playerNode.getName(), playerNode);
    }

    /**
     * 新一局，清空牌
     */
    public void start() {
        this.role = Role.WAITING;
        this.notNeedType = CardType.WAIT;
        this.canOut = false;
        this.isHu = false;
        this.isTing = false;
        this.lastOut = false;
        this.recentGang = false;
        this.darkGangCardList = new Vector<>();
        this.gangCardListMap = new ConcurrentHashMap<>();
        this.pendCardListMap = new ConcurrentHashMap<>();
        this.normalCardList = new Vector<>();
        this.notNeedCardList = new Vector<>();
        this.outCardList = new Vector<>();
        this.choiceCardLabelList = new Vector<>();
        this.lastCard = null;
        this.huCard = null;
        this.pengBO = new PengBO();
        this.gangBO = new GangBO();
        this.tingBO = new TingBO();
        this.huBO = new HuBO();
    }

    /**
     * 准备
     */
    public void readyPlay() {
        this.ready = true;
    }

    /**
     * 发牌
     *
     * @param mahJongCards
     */
    public void allocCard(List<MahJongCard> mahJongCards) {
        this.ready = false;
        CollUtil.addAll(this.normalCardList, mahJongCards);
        if (CollUtil.size(mahJongCards) == 14) {
            this.role = Role.ZHUANG;
        } else {
            this.role = Role.XIAN;
        }
    }

    /**
     * 换出去三张
     *
     * @param mahJongCards
     */
    public void swapOutThreeCard(List<MahJongCard> mahJongCards) {
        for (MahJongCard card : mahJongCards) {
            MahJongUtil.removeCardFromList(this.normalCardList, card, 1);
        }
    }

    /**
     * 换进来三张
     *
     * @param mahJongCards
     */
    public void swapInThreeCard(List<MahJongCard> mahJongCards) {
        for (MahJongCard card : mahJongCards) {
            MahJongUtil.addCardToList(this.normalCardList, card, 1);
        }
        this.choiceCardLabelList.clear();
    }

    /**
     * 定缺
     *
     * @param cardType
     */
    public void setNotNeedType(CardType cardType) {
        this.notNeedType = cardType;
        for (int i = this.normalCardList.size() - 1; i >= 0; i--) {
            MahJongCard card = this.normalCardList.get(i);
            if (ObjectUtil.equal(card.getType(), cardType)) {
                MahJongUtil.addCardToList(this.notNeedCardList, card, 1);
                this.normalCardList.remove(i);
            }
        }
        /* 庄家拿出一张牌到最后抓到的牌列表 */
        if (ObjectUtil.equal(this.role, Role.ZHUANG)) {
            /* 缺牌不空，从缺牌拿 */
            if (CollUtil.isNotEmpty(this.notNeedCardList)) {
                this.lastCard = CollUtil.getLast(this.notNeedCardList);
                MahJongUtil.removeCardFromList(this.notNeedCardList, this.lastCard, 1);
            } else {
                this.lastCard = CollUtil.getLast(this.normalCardList);
                MahJongUtil.removeCardFromList(this.normalCardList, this.lastCard, 1);
            }
        }
    }

    /**
     * 抓牌
     *
     * @param card
     */
    public void catchCard(MahJongCard card) {
        /* 如果最后一张牌不空，则添加到普通牌里 */
        if (ObjectUtil.isNotNull(this.lastCard)) {
            if (ObjectUtil.equal(this.lastCard.getType(), this.notNeedType)) {
                this.notNeedCardList.add(this.lastCard);
            } else {
                this.normalCardList.add(this.lastCard);
            }
        }
        this.lastCard = card;
        this.canOut = true;
        this.resetStatus();
    }

    /**
     * 出牌
     */
    public void outCard(MahJongCard card) {
        this.lastOut = true;
        this.canOut = false;
        this.recentGang = false;
        this.resetStatus();
        MahJongUtil.addCardToList(this.outCardList, card, 1);
        /* 出的最后抓的一张牌 */
        if (ObjectUtil.equal(card, this.lastCard)) {
            this.lastCard = null;
            /* 其他玩家最后出牌置为false */
            this.otherPlayerNodeMap.forEach((k, v) -> v.otherOut());
            return;
        }
        /* 最后抓的一张不空且出的不是最后抓的一张，把最后一张牌加到列表 */
        if (ObjectUtil.isNotNull(this.lastCard)) {
            if (ObjectUtil.equal(this.lastCard.getType(), this.notNeedType)) {
                MahJongUtil.addCardToList(this.notNeedCardList, lastCard, 1);
            } else {
                MahJongUtil.addCardToList(this.normalCardList, lastCard, 1);
            }
            this.lastCard = null;
        }
        /* 打的缺牌 */
        if (ObjectUtil.equal(card.getType(), this.notNeedType)) {
            MahJongUtil.removeCardFromList(this.notNeedCardList, card, 1);
            /* 打的普通牌 */
        } else {
            MahJongUtil.removeCardFromList(this.normalCardList, card, 1);
        }
        /* 其他玩家最后出牌置为false */
        this.otherPlayerNodeMap.forEach((k, v) -> v.otherOut());
    }

    /**
     * 听+出牌
     *
     * @param card
     */
    public void tingCard(MahJongCard card) {
        this.isTing = true;
        this.outCard(card);
    }

    /**
     * 碰牌
     *
     * @param card
     */
    public void pengCard(MahJongCard card, String player) {
        this.resetStatus();
        MahJongUtil.removeCardFromList(this.normalCardList, card, 2);
        this.addCardToMap(this.pendCardListMap, player, card, 3);
        /* 碰掉了出牌玩家的牌 */
        PlayerNode playerNode = this.otherPlayerNodeMap.get(player);
        if (ObjectUtil.isNotNull(playerNode)) {
            playerNode.removeLastOutCard(card);
        }
        this.canOut = true;
    }

    /**
     * @param card
     * @param player
     */
    public void gangCard(MahJongCard card, String player) {
        this.resetStatus();
        this.recentGang = true;
        /* 暗杠 */
        if (this.isMe(player)) {
            if (ObjectUtil.equal(this.lastCard, card)) {
                this.lastCard = null;
            }
            MahJongUtil.removeCardFromList(this.normalCardList, card, 4);
            MahJongUtil.addCardToList(this.darkGangCardList, card, 4);
            /* 暗杆，其他所有没胡的玩家减分 */
            this.otherPlayerNodeMap.forEach((k, v) -> {
                if (!v.isHu()) {
                    v.addScore(-2);
                    this.addScore(2);
                }
            });
            return;
        }
        /* 明杠 */
        /* 自己手中有3张，则是他人出牌的杠 */
        if (MahJongUtil.statCardNum(this.normalCardList, card) == 3) {
            MahJongUtil.removeCardFromList(this.normalCardList, card, 3);
            /* 杠掉了出牌玩家的牌 */
            PlayerNode playerNode = this.otherPlayerNodeMap.get(player);
            if (ObjectUtil.isNotNull(playerNode)) {
                playerNode.removeLastOutCard(card);
            }
            /* 否则则是自己手中的牌和已经碰的牌来杠，去掉自己手中的牌 */
        } else {
            /* 如果最后抓的一张是要杠的牌则去掉最后一张 */
            if (ObjectUtil.equal(this.lastCard, card)) {
                this.lastCard = null;
                /* 否则从手牌中去掉 */
            } else {
                MahJongUtil.removeCardFromList(this.normalCardList, card, 1);
            }
            /* 从碰牌中去掉 */
            this.removeCardFromMap(this.pendCardListMap, player, card, 3);
        }
        /* 添加到杠牌中 */
        this.addCardToMap(this.gangCardListMap, player, card, 4);
        this.addScore(1);
        /* 杠的人得分-1 */
        PlayerNode playerNode = this.otherPlayerNodeMap.get(player);
        if (ObjectUtil.isNotNull(playerNode)) {
            playerNode.addScore(-1);
        }
    }

    /**
     * 胡牌
     *
     * @param card
     * @param player
     * @param huScore 胡的分数
     */
    public void huCard(MahJongCard card, String player, int huScore) {
        this.isHu = true;
        this.canOut = false;
        this.huCard = card;
        this.resetStatus();
        if (this.isMe(player)) {
            this.lastCard = null;
            /* 自摸，其他所有没胡的玩家减分 */
            this.otherPlayerNodeMap.forEach((k, v) -> {
                if (!v.isHu()) {
                    v.addScore(-huScore);
                    this.addScore(huScore);
                }
            });
        } else {
            this.addScore(huScore);
            /* 杠掉了出牌玩家的牌 */
            PlayerNode playerNode = this.otherPlayerNodeMap.get(player);
            if (ObjectUtil.isNotNull(playerNode)) {
                playerNode.removeLastOutCard(card);
                /* 点炮的玩家减分 */
                playerNode.addScore(-huScore);
            }
        }
    }

    /**
     * 增加分数
     */
    public void addScore(int n) {
        this.score.addAndGet(n);
    }

    /**
     *
     */
    private void addCardToMap(Map<String, List<MahJongCard>> map, String player, MahJongCard card, int count) {
        if (ObjectUtil.isNull(map.get(player))) {
            map.put(player, new Vector<>());
        }
        MahJongUtil.addCardToList(map.get(player), card, count);
    }

    /**
     * @param map
     * @param player
     * @param card
     * @param count
     */
    private void removeCardFromMap(Map<String, List<MahJongCard>> map, String player, MahJongCard card, int count) {
        if (ObjectUtil.isNull(map.get(player))) {
            return;
        }
        MahJongUtil.removeCardFromList(map.get(player), card, count);
        if (CollUtil.isEmpty(map.get(player))) {
            map.remove(player);
        }
    }

    /**
     *
     */
    private void removeLastOutCard(MahJongCard card) {
        if (ObjectUtil.notEqual(CollUtil.getLast(this.outCardList), card)) {
            return;
        }
        MahJongUtil.removeCardFromList(this.outCardList, card, 1);
        this.lastOut = false;
    }

    private void otherOut() {
        this.lastOut = false;
    }

    /**
     * 判断别人出牌时自己是否能过牌
     *
     * @param card   牌
     * @param player 出牌玩家
     * @return
     */
    public boolean canPass(MahJongCard card, String player) {
        /* 已经胡牌 */
        if (this.isHu) {
            return true;
        }
        this.resetStatus();
        this.judgePeng(card, player);
        this.judgeGang(card, player);
        this.judgeHu(card, player);
        return !(this.pengBO.getEnable() || this.huBO.getEnable() || this.gangBO.getEnable());
    }

    /**
     * 别人出牌是否能碰
     *
     * @param card
     */
    public void judgePeng(MahJongCard card, String player) {
        if (this.isTing) {
            this.pengBO.setEnable(false);
            return;
        }
        int count = MahJongUtil.statCardNum(this.normalCardList, card);
        this.pengBO = PengBO.builder().enable(count >= 2).oppPlayer(player).card(card).build();
    }

    /**
     * 别人出牌是否能杠
     */
    public void judgeGang(MahJongCard card, String player) {
        this.gangBO.setEnable(false);
        int count = MahJongUtil.statCardNum(this.normalCardList, card);
        /* 如果听牌状态下，且可以杠牌，如果杠牌后不能继续听则不能杠 */
        if (this.isTing && count == 3 && !this.canTingAfterGang(card)) {
            return;
        }
        this.gangBO = GangBO.builder().enable(count == 3).oppPlayer(player).card(card).build();
    }

    /**
     * 自己抓到牌判断杠
     */
    public void judgeGang() {
        /* 先置为false */
        this.gangBO.setEnable(false);
        MahJongCard newCard = this.isLastValid() ? this.lastCard : null;
        MahJongCard gangCard = MahJongUtil.canGangCard(this.normalCardList, newCard);
        if (ObjectUtil.isNotNull(gangCard)) {
            this.gangBO.setEnable(!this.isTing || this.canTingAfterGang(gangCard));
            this.gangBO = GangBO.builder().enable(true).oppPlayer(this.name).card(gangCard).build();
        }
        /* 如果能暗杠先暗杠 */
        if (this.gangBO.getEnable()) {
            return;
        }
        /* 从碰牌中杠 */
        for (Map.Entry<String, List<MahJongCard>> entry : this.pendCardListMap.entrySet()) {
            /* 最后一张牌能不能杠 */
            if (entry.getValue().stream().anyMatch(card -> ObjectUtil.equal(card, this.lastCard))) {
                this.gangBO = GangBO.builder().enable(true).oppPlayer(entry.getKey()).card(this.lastCard).build();
                return;
            }
            /* 普通牌中能不能和碰牌中的牌杠 */
            for (MahJongCard normalCard : this.normalCardList.stream().distinct().collect(Collectors.toList())) {
                if (entry.getValue().stream().anyMatch(card -> ObjectUtil.equal(card, normalCard))) {
                    this.gangBO = GangBO.builder().enable(true).oppPlayer(entry.getKey()).card(normalCard).build();
                    return;
                }
            }
        }

    }

    /**
     * 判断听牌
     */
    public void judgeTing() {
        /* 已经听牌状态 */
        if (isTing) {
            this.tingBO.setEnable(false);
            return;
        }
        /* 有1张以上缺牌不能听 */
        int cnt = this.notNeedCnt();
        if (cnt > 1) {
            this.tingBO.setEnable(false);
            return;
        }
        /* 有一张缺牌，去掉缺牌后可听就能听 */
        if (cnt == 1) {
            /* 缺牌 */
            MahJongCard card = CollUtil.isEmpty(this.notNeedCardList) ? this.lastCard : CollUtil.getFirst(this.notNeedCardList);
            /* 剩余牌 */
            List<MahJongCard> allCardList = new Vector<>(this.normalCardList);
            /* 最后一张牌不空且不是缺牌 */
            if (ObjectUtil.isNotNull(this.lastCard) && ObjectUtil.notEqual(this.lastCard.getType(), this.notNeedType)) {
                allCardList.add(this.lastCard);
            }
            if (!MahJongUtil.canTing(allCardList)) {
                this.tingBO.setEnable(false);
                return;
            }
            this.tingBO = TingBO.builder().enable(true).cardList(new Vector<>(Collections.singleton(card))).build();
        } else {
            List<MahJongCard> allCardList = new Vector<>(this.normalCardList);
            if (ObjectUtil.isNotNull(this.lastCard)) {
                allCardList.add(this.lastCard);
            }
            List<MahJongCard> tingCardList = MahJongUtil.calcTingCardList(allCardList);
            if (CollUtil.isEmpty(tingCardList)) {
                this.tingBO.setEnable(false);
                return;
            }
            this.tingBO = TingBO.builder().enable(true).cardList(tingCardList).build();
        }
    }

    /**
     * 判断他人出牌自己能不能胡，能胡计算倍数
     *
     * @param card
     */
    public void judgeHu(MahJongCard card, String player) {
        /* 没听不能胡，已经胡了不能再胡 */
        if (!this.isTing || this.isHu) {
            this.huBO.setEnable(false);
            this.recentGang = false;
            return;
        }
        /* 未出完缺牌，不能胡 */
        if (CollUtil.isNotEmpty(this.notNeedCardList) || ObjectUtil.equal(card.getType(), this.notNeedType)) {
            this.huBO.setEnable(false);
            this.recentGang = false;
            return;
        }
        int score = MahJongUtil.calcHu(this.normalCardList, card);
        if (score == 0) {
            this.huBO.setEnable(false);
            this.recentGang = false;
            return;
        }
        /* 计算杠的个数用来翻倍 */
        AtomicInteger cnt = new AtomicInteger(this.darkGangCardList.size() / 4);
        this.gangCardListMap.forEach((k, v) -> {
            cnt.addAndGet((v.size() / 4));
        });
        while (cnt.addAndGet(-1) >= 0) {
            score *= 2;
        }
        /* 庄翻倍 */
        score *= this.role.getBase();
        /* 自摸翻倍 */
        score *= (StrUtil.equals(player, this.name) ? 2 : 1);
        /* 杠开翻倍 */
        score *= (this.recentGang ? 2 : 1);
        this.huBO = HuBO.builder().enable(true).score(score).card(card).oppPlayer(player).build();
        this.recentGang = false;
    }

    /**
     * 自摸判断是否能胡
     */
    public void judgeHu() {
        this.judgeHu(this.lastCard, this.name);
    }

    /**
     * 是否自己
     *
     * @param player
     * @return
     */
    private boolean isMe(String player) {
        return StrUtil.equals(player, this.name);
    }

    /**
     * 判断是否有缺牌
     *
     * @return
     */
    private int notNeedCnt() {
        return CollUtil.size(this.notNeedCardList) +
                ((ObjectUtil.isNotNull(this.lastCard) && ObjectUtil.equal(this.lastCard.getType(), this.notNeedType)) ? 1 : 0);
    }


    /**
     * 杠牌后是否可以继续听
     *
     * @param card
     * @return
     */
    private boolean canTingAfterGang(MahJongCard card) {
        /* 在听牌状态，刚抓了缺牌，且正常牌里有4个可以杠，那就不能杠，否则就会多张缺牌导致不是听的状态 */
        if (ObjectUtil.isNotNull(this.lastCard) && ObjectUtil.equal(this.lastCard.getType(), this.notNeedType)) {
            return false;
        }
        List<MahJongCard> afterCardList =
                this.normalCardList.stream().filter(card1 -> ObjectUtil.notEqual(card1, card)).collect(Collectors.toList());
        if (this.isLastValid() && ObjectUtil.notEqual(card, this.lastCard)) {
            afterCardList.add(this.lastCard);
        }
        return MahJongUtil.canTing(afterCardList);
    }

    public void pass() {
        this.resetStatus();
    }

    /**
     * 重置状态
     */
    public void resetStatus() {
        this.pengBO.setEnable(false);
        this.gangBO.setEnable(false);
        this.tingBO.setEnable(false);
        this.huBO.setEnable(false);
    }

    /**
     * 最后抓到的牌是否有效
     */
    private boolean isLastValid() {
        if (ObjectUtil.isNull(this.lastCard)) {
            return false;
        }
        return !ObjectUtil.equal(this.lastCard.getType(), this.notNeedType);
    }

}
