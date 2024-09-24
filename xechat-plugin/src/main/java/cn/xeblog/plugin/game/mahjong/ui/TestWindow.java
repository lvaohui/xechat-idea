package cn.xeblog.plugin.game.mahjong.ui;

import cn.hutool.core.collection.ListUtil;
import cn.xeblog.plugin.game.mahjong.MahJongGame;

import java.util.List;
import java.util.Vector;

public class TestWindow {


    public static void main(String[] args) {

        List<String> playerList = new Vector<>(ListUtil.toList("lv", "p1", "p2", "p3"));

        List<MahJongGame> mahJongGameList = new Vector<>();

//        for (int i = 0; i < playerList.size(); ++i) {
//            if (i == 4) {
//                break;
//            }
////            MahJongGame mahJongGame = new MahJongGame(playerList, i, "lv");
////            mahJongGameList.add(mahJongGame);
//        }
    }

}

