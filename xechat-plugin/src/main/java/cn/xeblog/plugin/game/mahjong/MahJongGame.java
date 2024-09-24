package cn.xeblog.plugin.game.mahjong;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.xeblog.commons.entity.User;
import cn.xeblog.commons.entity.game.GameRoom;
import cn.xeblog.commons.entity.game.mahjong.MahJongGameDTO;
import cn.xeblog.commons.enums.Game;
import cn.xeblog.plugin.action.GameAction;
import cn.xeblog.plugin.annotation.DoGame;
import cn.xeblog.plugin.cache.DataCache;
import cn.xeblog.plugin.game.AbstractGame;
import cn.xeblog.plugin.game.mahjong.action.AiPlayerAction;
import cn.xeblog.plugin.game.mahjong.domain.PlayerNode;
import cn.xeblog.plugin.game.mahjong.server.MahJongServer;
import cn.xeblog.plugin.game.mahjong.ui.MahJongPanel;
import cn.xeblog.plugin.game.mahjong.ui.enums.Position;
import com.intellij.openapi.ui.ComboBox;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@DoGame(Game.MAHJONG)
public class MahJongGame extends AbstractGame<MahJongGameDTO> {

    /**
     * 方向
     */
    private final static List<String> DIRECTION_LIST = ListUtil.toList("东", "北", "西", "南");
    private final static List<Position> POSITION_LIST = ListUtil.toList(Position.BOTTOM, Position.RIGHT, Position.UP, Position.LEFT);
    private static List<String> AI_PLAYER_LIST = ListUtil.toList("AI1", "AI2", "AI3");
    private AtomicInteger restartCounter;
    private JPanel mainPanel;
    private JPanel startPanel;
    private JButton backButton;
    private JButton restartButton;
    private JButton gameOverButton;
    /**
     * 游戏面板
     */
    private MahJongPanel mahJongPanel;

    /**
     * 房间玩家
     */
    @Getter
    private List<String> playerList;
    /**
     * 玩家节点列表
     */
    private List<PlayerNode> playerNodeList;
    /**
     * 当前玩家下标
     */
    private int playerIndex;
    /**
     * 玩家节点列表
     */
    private Map<String, AiPlayerAction> aiPlayerActionMap;
    /**
     * Server
     */
    private MahJongServer server;

    /**
     * 当前游戏模式
     */
    private MahJongGame.GameMode gameMode;

    @AllArgsConstructor
    @Getter
    private enum GameMode {
        XUE_ZHAN("血战到底"),
        XUE_LIU("血流成河");

        private String name;

        public static MahJongGame.GameMode getMode(String name) {
            for (MahJongGame.GameMode model : values()) {
                if (model.name.equals(name)) {
                    return model;
                }
            }

            return GameMode.XUE_ZHAN;
        }
    }

    /**
     * 初始化
     */
    @Override
    protected void init() {
        if (restartCounter != null) {
            restartCounter.incrementAndGet();
        }

        if (mainPanel == null) {
            mainPanel = new JPanel();
        }

        mainPanel.removeAll();
        mainPanel.setLayout(null);
        mainPanel.setEnabled(true);
        mainPanel.setVisible(true);
        mainPanel.setMinimumSize(new Dimension(150, 260));
        startPanel = new JPanel();
        startPanel.setBounds(10, 10, 120, 260);
        mainPanel.add(startPanel);

        JLabel title = new JLabel("四川麻将！");
        title.setFont(new Font("", 1, 14));
        startPanel.add(title);

        Box vBox = Box.createVerticalBox();
        startPanel.add(vBox);

        vBox.add(Box.createVerticalStrut(20));
        JLabel modelLabel = new JLabel("游戏模式：");
        modelLabel.setFont(new Font("", 1, 13));
        vBox.add(modelLabel);

        vBox.add(Box.createVerticalStrut(5));
        ComboBox gameModeBox = new ComboBox();
        gameModeBox.setPreferredSize(new Dimension(40, 30));
        for (MahJongGame.GameMode value : MahJongGame.GameMode.values()) {
            gameModeBox.addItem(value.getName());
        }
        gameMode = MahJongGame.GameMode.XUE_ZHAN;
        gameModeBox.setSelectedItem(gameMode.getName());
        gameModeBox.addActionListener(l -> {
            MahJongGame.GameMode selectedGameMode = MahJongGame.GameMode.getMode(gameModeBox.getSelectedItem().toString());
            if (selectedGameMode != null) {
                gameMode = selectedGameMode;
            }
        });
        vBox.add(gameModeBox);

        vBox.add(Box.createVerticalStrut(20));
        vBox.add(getStartGameButton());

        if (DataCache.isOnline) {
            java.util.List<Integer> numsList = new ArrayList();
            numsList.add(4);
            numsList.add(3);
            numsList.add(2);
            List<String> gameModeList = new ArrayList<>();
            for (MahJongGame.GameMode mode : MahJongGame.GameMode.values()) {
                gameModeList.add(mode.getName());
            }

            vBox.add(getCreateRoomButton(numsList, gameModeList));
        }
        vBox.add(getExitButton());

        mainPanel.updateUI();
    }

    @Override
    protected JPanel getComponent() {
        return mainPanel;
    }

    /**
     * 主要逻辑，处理各种消息
     *
     * @param mahJongGameDTO
     */
    @Override
    public void handle(MahJongGameDTO mahJongGameDTO) {
        switch (mahJongGameDTO.getMsgType()) {
            case JOIN_ROBOTS: {
                List<String> robotList = (List<String>) mahJongGameDTO.getData();
                playerList.addAll(robotList);
                this.preparePlayerNode();
                this.showGamePanel();
                this.showTips("等待发牌...");
                if (isHomeowner()) {
                    if (ObjectUtil.isNull(server)) {
                        server = new MahJongServer(this);
                    }
                    invoke(() -> server.allocCard(), 500);
                }
                break;
            }
            default: {
                /* 传给界面处理 */
                invoke(() -> {
                    this.mahJongPanel.handle(mahJongGameDTO);
                    if (isHomeowner()) {
                        /* AI处理 */
                        this.aiPlayerActionMap.forEach((k, v) -> v.handle(mahJongGameDTO));
                        /* Server */
                        this.server.handle(mahJongGameDTO);
                    }
                }, 100);
            }
        }
    }

    private void initValue() {
        this.playerList = new Vector<>();
        this.playerIndex = 0;
        this.playerNodeList = new Vector<>();
        this.aiPlayerActionMap = new ConcurrentHashMap<>();
    }

    /**
     * 游戏开始
     */
    @Override
    protected void start() {
        if (restartCounter == null) {
            restartCounter = new AtomicInteger();
        }
        restartCounter.incrementAndGet();

        initValue();

        GameRoom gameRoom = getRoom();
        if (gameRoom != null) {
            gameMode = MahJongGame.GameMode.getMode(gameRoom.getGameMode());
            playerList.addAll(gameRoom.getUsers().keySet());
        } else {
            playerList.add(GameAction.getNickname());
        }

        this.playerIndex = findPlayerIndex(GameAction.getNickname());

        preparePlayerNode();
        showGamePanel();

        if (playerList.size() < 4) {
            showTips("正在加入机器人...");
        } else {
            showTips("等待发牌...");
        }

        if (gameRoom == null) {
            allPlayersGameStarted();
        }
    }

    /**
     * 创建玩家节点
     */
    private void preparePlayerNode() {
        this.playerNodeList.clear();
        for (int i = 0; i < playerList.size(); ++i) {
            this.playerNodeList.add(new PlayerNode(playerList.get(i), DIRECTION_LIST.get(i)));
        }
        for (PlayerNode current : this.playerNodeList) {
            for (PlayerNode other : this.playerNodeList) {
                if (StrUtil.equals(current.getName(), other.getName())) {
                    continue;
                }
                current.addOtherPlayerNode(other);
            }
        }
    }

    @Override
    protected void allPlayersGameStarted() {
        if (!isHomeowner()) {
            return;
        }
        server = new MahJongServer(this);
        int usersTotal = playerList.size();
        int nums = 4 - usersTotal;
        invoke(() -> {
            if (nums > 0) {
                List<String> joinedAIList = new ArrayList<>(AI_PLAYER_LIST);
                joinedAIList.removeAll(playerList);
                List<String> aiList = joinedAIList.subList(0, Math.min(joinedAIList.size(), nums));
                aiList.forEach(ai -> this.aiPlayerActionMap.put(ai, new AiPlayerAction(this, ai)));
                MahJongGameDTO dto = this.sendMsg(MahJongGameDTO.MsgType.JOIN_ROBOTS, new ArrayList<>(aiList));
                this.handle(dto);
            } else {
                server.allocCard();
            }
        }, 500);
    }

    /**
     * 游戏主界面
     */
    private void showGamePanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
        }
        mainPanel.removeAll();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setMinimumSize(new Dimension(560, 430));

        mahJongPanel = new MahJongPanel(this);
        int n = this.playerNodeList.size();
        for (int i = 0; i < n; ++i) {
            mahJongPanel.addPlayer(this.playerNodeList.get((this.playerIndex + i) % n), POSITION_LIST.get(i));
        }
        mahJongPanel.setPreferredSize(new Dimension(560, 400));

        JPanel mainBottomPanel = new JPanel();
        if (getRoom() == null) {
            backButton = getBackButton();
            mainBottomPanel.add(backButton);
        } else {
            gameOverButton = getGameOverButton();
            gameOverButton.setText("结束");
            mainBottomPanel.add(gameOverButton);
        }

        mainPanel.add(mahJongPanel, BorderLayout.CENTER);
        mainPanel.add(mainBottomPanel, BorderLayout.SOUTH);
        mainPanel.updateUI();
    }

    private JButton getStartGameButton() {
        JButton button = new JButton("开始游戏");
        button.addActionListener(e -> {
            button.setEnabled(false);
            invoke(() -> {
                setHomeowner(true);
                start();
                button.setEnabled(true);
            }, 100);
        });
        return button;
    }

    @Override
    public void playerLeft(User player) {
        super.playerLeft(player);
        String msg = "游戏结束！" + player.getUsername() + "逃跑了~";
        String tips = "溜了~";
        showTips(msg);
        PlayerNode playerNode = playerNodeList.get(findPlayerIndex(player.getUsername()));
        if (playerNode != null) {
            playerNode.setName(tips);
        }
        if (ObjectUtil.isNotNull(this.mahJongPanel)) {
            mahJongPanel.getOver().set(true);
            mahJongPanel.updateShowUi();
        }
    }

    public MahJongGameDTO sendMsg(MahJongGameDTO.MsgType msgType, Object data) {
        return sendMsg(msgType, playerList.get(playerIndex), data, null);
    }

    public MahJongGameDTO sendMsg(MahJongGameDTO.MsgType msgType, Object data, String otherPlayer) {
        return sendMsg(msgType, playerList.get(playerIndex), data, otherPlayer);
    }

    public MahJongGameDTO sendMsg(MahJongGameDTO.MsgType msgType, String player, Object data, String otherPlayer) {
        MahJongGameDTO mahJongGameDTO = new MahJongGameDTO();
        mahJongGameDTO.setMsgType(msgType);
        mahJongGameDTO.setData(data);
        mahJongGameDTO.setPlayer(player);
        mahJongGameDTO.setOtherPlayer(otherPlayer);
        sendMsgImpl(mahJongGameDTO);
        return mahJongGameDTO;
    }

    public void sendMsgImpl(MahJongGameDTO dto) {
        if (getRoom() != null) {
            sendMsg(dto);
        }
        if (isHomeowner()) {
            invoke(() -> {
                /* 房主界面发过来的消息传给AI处理 */
                this.aiPlayerActionMap.forEach((k, v) -> v.handle(dto));
                /* 房主界面发过来的特定消息发给Server处理 */
                switch (dto.getMsgType()) {
                    case TRY:
                    case PASS:
                    case READY:
                    case SWAP_OUT:
                    case MANUAL_GANG_CARD:
                    case MANUAL_HU_CARD:
                    case CONFIRM_SWAP_DIRECTION: {
                        server.handle(dto);
                        break;
                    }
                    default: {
                    }
                }
            }, 100);

        }
    }

    /**
     * ai发的消息发给所有其他玩家
     *
     * @param mahJongGameDTO
     */
    public void aiSendMsgImpl(MahJongGameDTO mahJongGameDTO) {
        if (getRoom() != null) {
            sendMsg(mahJongGameDTO);
        }
        invoke(() -> handle(mahJongGameDTO), 200);
    }

    /**
     * server发的消息发给所有其他玩家
     *
     * @param mahJongGameDTO
     */
    public void serverSendMsgImpl(MahJongGameDTO mahJongGameDTO) {
        if (getRoom() != null) {
            sendMsg(mahJongGameDTO);
        }
        invoke(() -> handle(mahJongGameDTO), 200);
    }

    private void showTips(String tips) {
        if (ObjectUtil.isNull(this.mahJongPanel)) {
            return;
        }
        this.mahJongPanel.showTips(tips);
    }

    /**
     * 查证玩家索引
     *
     * @param player
     * @return
     */
    public int findPlayerIndex(String player) {
        return this.playerList.indexOf(player);
    }

    private JButton getBackButton() {
        JButton button = new JButton("返回");
        button.addActionListener(e -> init());
        return button;
    }

}
