package cn.xeblog.plugin.game.mahjong.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.xeblog.commons.entity.game.mahjong.enums.MahJongCard;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 麻将牌工具类，判断是否可听牌，胡牌
 */
public class MahJongUtil {

    private final static List<MahJongCard> ALL_MAHJONG_CARDS = new Vector<>();

    static {
        Arrays.stream(MahJongCard.values())
                .filter(card -> card.getId() < 50)
                .forEach(card -> CollUtil.addAll(ALL_MAHJONG_CARDS, ListUtil.toList(card, card, card, card)));
    }

    /**
     * 洗牌返回随机的108张
     *
     * @return
     */
    public static List<MahJongCard> randomCardList() {
        List<MahJongCard> mahJongCardList = new Vector<>(ALL_MAHJONG_CARDS);
//        return mahJongCardList;
        Collections.shuffle(mahJongCardList);
        return mahJongCardList;
    }

    /**
     * 计算可以杠的牌
     *
     * @param mahJongCardList
     * @param cardList
     * @return
     */
    public static MahJongCard canGangCard(List<MahJongCard> mahJongCardList, MahJongCard... cardList) {
        Map<MahJongCard, Long> cardCountMap = mahJongCardList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        for (MahJongCard card : cardList) {
            if (ObjectUtil.isNull(card)) {
                continue;
            }
            cardCountMap.put(card, 1L + Optional.ofNullable(cardCountMap.get(card)).orElse(0L));
        }
        for (Map.Entry<MahJongCard, Long> entry : cardCountMap.entrySet()) {
            if (entry.getValue().equals(4L)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static int statCardNum(List<MahJongCard> mahJongCardList, MahJongCard card) {
        int count = 0;
        for (MahJongCard mahJongCard : mahJongCardList) {
            if (ObjectUtil.equal(mahJongCard, card)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 计算出哪张可以听
     *
     * @return
     */
    public static List<MahJongCard> calcTingCardList(List<MahJongCard> mahJongCardList) {
        /* 结果 */
        List<MahJongCard> resCardList = new Vector<>();
        /* 试过的牌 */
        Set<Integer> tryCardIdSet = new HashSet<>();
        for (int i = 0; i < mahJongCardList.size(); ++i) {
            MahJongCard card = mahJongCardList.get(i);
            if (tryCardIdSet.contains(card.getId())) {
                continue;
            }
            /* 在新复制出来的列表上操作 */
            List<MahJongCard> list = new Vector<>(mahJongCardList);
            /* 模拟出牌 */
            list.remove(i);
            if (canTing(list)) {
                resCardList.add(card);
            }
            tryCardIdSet.add(card.getId());
        }
        return resCardList;
    }

    /**
     * 当前13张牌是否可听
     *
     * @param mahJongCardList
     */
    public static boolean canTing(List<MahJongCard> mahJongCardList) {
        if (mahJongCardList.size() % 3 != 1) {
            return false;
        }
        /* 遍历所有牌张，判断是否可胡，有一张可胡就算 */
        for (MahJongCard card : MahJongCard.values()) {
            if (card.getId() > 50) {
                continue;
            }
            if (calcHu(mahJongCardList, card) > 0) {
                return true;
            }
        }
        /* 所有都不能胡 */
        return false;
    }

    /**
     * 计算胡的分数
     *
     * @param mahJongCardList 手牌
     * @param card            刚抓的牌或别人出的牌
     * @return
     */
    public static int calcHu(List<MahJongCard> mahJongCardList, MahJongCard card) {
        if (mahJongCardList.size() % 3 != 1) {
            return 0;
        }
        /* 得分 */
        int score = 0;
        /* 所有手牌 */
        List<MahJongCard> allCardList = new Vector<>(mahJongCardList);
        allCardList.add(card);
        /* 统计牌数 */
        Map<MahJongCard, Long> cardCntMap = allCardList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        /* 遍历所有可能的将牌，去除将牌后判断是否可以组成顺 */
        for (MahJongCard k : cardCntMap.keySet()) {
            if (cardCntMap.get(k) < 2) {
                continue;
            }
            /* 删除将牌 */
            removeCardFromList(allCardList, k, 2);
            score = Math.max(score, is3n(allCardList));
            /* 添加回来 */
            addCardToList(allCardList, k, 2);
            /* 如果2代表碰碰胡，最大的分数，直接结束，否则继续判断是否能碰碰胡 */
            if (score == 2) {
                break;
            }
        }
        if (score == 0) {
            return score;
        }
        /* 判断清一色 */
        allCardList.sort(Comparator.comparingInt(MahJongCard::getId));
        if (ObjectUtil.equal(CollUtil.getFirst(allCardList).getType(), CollUtil.getLast(allCardList).getType())) {
            score *= 2;
        }
        return score;
    }

    /**
     * 是否3n的形式，如果是碰碰返回2，平胡返回1，不是返回0
     *
     * @return
     */
    private static int is3n(List<MahJongCard> mahJongCardList) {
        if (mahJongCardList.size() % 3 != 0) {
            return 0;
        }
        /* 从小到大排序 */
        mahJongCardList.sort(Comparator.comparingInt(MahJongCard::getId));
        /* 判断是否能碰碰胡 */
        boolean pengpeng = true;
        for (int i = 0; i < mahJongCardList.size(); i += 3) {
            if (ObjectUtil.notEqual(mahJongCardList.get(i), mahJongCardList.get(i + 2))) {
                pengpeng = false;
                break;
            }
        }
        /* 碰碰胡直接返回 */
        if (pengpeng) {
            return 2;
        }
        /* 按断点分割的牌列表 111344556889 -> 111, 344556, 889 */
        List<List<MahJongCard>> splitCardList = new Vector<>();
        MahJongCard preCard = null;
        for (MahJongCard card : mahJongCardList) {
            if (ObjectUtil.isNull(preCard) || (card.getId() - preCard.getId() > 1)) {
                splitCardList.add(new Vector<>());
            }
            CollUtil.getLast(splitCardList).add(card);
            preCard = card;
        }
        /* 遍历分割后每一组的牌，如果有一组的牌个数不是3的倍数则不能胡，直接返回0 */
        for (List<MahJongCard> cardGroup : splitCardList) {
            if (cardGroup.size() % 3 != 0) {
                return 0;
            }
        }
        /* 每一组都是3的倍数，判断每组能不能组成顺子 */
        for (List<MahJongCard> cardGroup : splitCardList) {
            /* 转成牌个数列表 344556 -> 1,2,2,1;   44455667 -> 3,2,2,1 */
            List<Integer> cardCntList = new Vector<>();
            int cnt = 1;
            for (int i = 1; i < cardGroup.size(); ++i) {
                if (ObjectUtil.equal(cardGroup.get(i - 1), cardGroup.get(i))) {
                    cnt++;
                } else {
                    cardCntList.add(cnt);
                    cnt = 1;
                }
                if (i == cardGroup.size() - 1) {
                    cardCntList.add(cnt);
                }
            }
            /* 根据个数列表判断是否能组成顺子 */
            for (int i = 0; i < cardCntList.size(); ++i) {
                if (cardCntList.get(i) >= 3) {
                    cardCntList.set(i, cardCntList.get(i) - 3);
                    i -= cardCntList.get(i) == 0 ? 0 : 1;
                } else if (cardCntList.get(i) > 0 && i < cardCntList.size() - 2) {
                    cardCntList.set(i, cardCntList.get(i) - 1);
                    cardCntList.set(i + 1, cardCntList.get(i + 1) - 1);
                    cardCntList.set(i + 2, cardCntList.get(i + 2) - 1);
                    i -= cardCntList.get(i) == 0 ? 0 : 1;
                }
            }
            /* 不能组成顺子，返回0 */
            if (cardCntList.stream().anyMatch(c -> c != 0)) {
                return 0;
            }
        }
        return 1;
    }

    /**
     * 从列表删除指定个数麻将
     *
     * @param list
     * @param mahJongCard
     * @param count
     */
    public static void removeCardFromList(List<MahJongCard> list, MahJongCard mahJongCard, int count) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (ObjectUtil.equal(list.get(i), mahJongCard)) {
                list.remove(i);
                count--;
            }
            if (count == 0) {
                break;
            }
        }
    }

    /**
     * 添加麻将到列表
     *
     * @param list
     * @param mahJongCard
     * @param count
     */
    public static void addCardToList(List<MahJongCard> list, MahJongCard mahJongCard, int count) {
        for (; count > 0; count--) {
            CollUtil.addAll(list, mahJongCard);
        }
    }

    public static void main(String[] args) {
        List<MahJongCard> mahJongCardList = ListUtil.toList(
                MahJongCard.TIAO_1, MahJongCard.TIAO_1, MahJongCard.TIAO_1,
                MahJongCard.TIAO_2, MahJongCard.TIAO_2, MahJongCard.TIAO_2,
                MahJongCard.TIAO_3, MahJongCard.TIAO_3,
//                MahJongCard.TIAO_4,
                MahJongCard.TIAO_5, MahJongCard.TIAO_5
        );
        System.out.println(calcHu(mahJongCardList, MahJongCard.TIAO_3));
    }

}
