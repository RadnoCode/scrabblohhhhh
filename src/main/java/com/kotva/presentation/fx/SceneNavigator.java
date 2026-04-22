package com.kotva.presentation.fx;

import com.kotva.application.result.SettlementResult;
import com.kotva.launcher.AppContext;
import com.kotva.presentation.controller.GameController;
import com.kotva.presentation.controller.HelpController;
import com.kotva.presentation.controller.HomeController;
import com.kotva.presentation.controller.LocalAiSetupController;
import com.kotva.presentation.controller.LocalMultiplayerSetupController;
import com.kotva.presentation.controller.ModeSelectController;
import com.kotva.presentation.controller.OnlineSetupController;
import com.kotva.presentation.controller.PlayerNameSetupController;
import com.kotva.presentation.controller.RoomCreateController;
import com.kotva.presentation.controller.RoomSearchController;
import com.kotva.presentation.controller.RoomWaitingController;
import com.kotva.presentation.controller.SettingsController;
import com.kotva.presentation.controller.SettlementController;
import com.kotva.presentation.scene.GameScene;
import com.kotva.presentation.scene.HelpScene;
import com.kotva.presentation.scene.HomeScene;
import com.kotva.presentation.scene.LocalAiSetupScene;
import com.kotva.presentation.scene.LocalMultiplayerSetupScene;
import com.kotva.presentation.scene.ModeSelectScene;
import com.kotva.presentation.scene.OnlineSetupScene;
import com.kotva.presentation.scene.PlayerNameSetupScene;
import com.kotva.presentation.scene.RoomCreateScene;
import com.kotva.presentation.scene.RoomSearchScene;
import com.kotva.presentation.scene.RoomWaitingScene;
import com.kotva.presentation.scene.SceneTitleEntranceAnimationManager;
import com.kotva.presentation.scene.SettingsScene;
import com.kotva.presentation.scene.SettlementScene;
import com.kotva.presentation.viewmodel.GameLaunchContext;
import com.kotva.tutorial.TutorialScriptId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import javafx.stage.Stage;

public class SceneNavigator {
    private final Stage stage;
    private final AppContext appContext;
    private final Deque<PageType> history;
    private GameLaunchContext gameLaunchContext;
    private PlayerNameSetupContext playerNameSetupContext;
    private RoomWaitingContext roomWaitingContext;
    private SettlementResult settlementResult;
    private PageType currentPage;
    private GameController gameController;
    private RoomWaitingController roomWaitingController;
    private boolean animateNextSceneTitleEntrance;

    public SceneNavigator(Stage stage, AppContext appContext) {
        this.stage = Objects.requireNonNull(stage, "stage cannot be null.");
        this.appContext = Objects.requireNonNull(appContext, "appContext cannot be null.");
        this.history = new ArrayDeque<>();
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public void showHome() {
        showPage(PageType.HOME, true);
    }

    public void showGameSetting() {
        showPage(PageType.GAME_SETTING, true);
    }

    public void showSettings() {
        showPage(PageType.SETTINGS, true);
    }

    public void showHelp() {
        showPage(PageType.HELP, true);
    }

    public void showGame(GameLaunchContext gameLaunchContext) {
        this.gameLaunchContext = gameLaunchContext;
        this.settlementResult = null;
        showPage(PageType.GAME, true);
    }

    public void showTutorial() {
        showGame(GameLaunchContext.forTutorial(TutorialScriptId.BASIC_ONBOARDING));
    }

    public void showLocalMultiplayerSetup() {
        showPage(PageType.LOCAL_MULTIPLAYER_SETUP, true);
    }

    public void showLocalAiSetup() {
        showPage(PageType.LOCAL_AI_SETUP, true);
    }

    public void showOnlineSetup() {
        showPage(PageType.ONLINE_SETUP, true);
    }

    public void showPlayerNameSetup(PlayerNameSetupContext playerNameSetupContext) {
        this.playerNameSetupContext = Objects.requireNonNull(
                playerNameSetupContext,
                "playerNameSetupContext cannot be null.");
        showPage(PageType.PLAYER_NAME_SETUP, true);
    }

    public PlayerNameSetupContext getPlayerNameSetupContext() {
        return playerNameSetupContext;
    }

    public void showRoomSearch() {
        showPage(PageType.ROOM_SEARCH, true);
    }

    public void showRoomCreate() {
        showPage(PageType.ROOM_CREATE, true);
    }

    public void showRoomWaiting() {
        showPage(PageType.ROOM_WAITING, true);
    }

    public void showRoomWaiting(RoomWaitingContext roomWaitingContext) {
        this.roomWaitingContext = roomWaitingContext;
        showPage(PageType.ROOM_WAITING, true);
    }

    public RoomWaitingContext getRoomWaitingContext() {
        return roomWaitingContext;
    }

    public void showSettlement() {
        showSettlement(settlementResult);
    }

    public void showSettlement(SettlementResult settlementResult) {
        this.settlementResult = settlementResult;
        showPage(PageType.SETTLEMENT, false);
    }

    public SettlementResult getSettlementResult() {
        return settlementResult;
    }

    public void requestNextSceneTitleEntranceAnimation() {
        animateNextSceneTitleEntrance = true;
    }

    public void goBack() {
        if (history.isEmpty()) {
            showPage(PageType.HOME, false);
            return;
        }

        PageType previousPage = history.pop();
        if (currentPage == PageType.GAME_SETTING && previousPage == PageType.SETTLEMENT) {
            history.clear();
            showPage(PageType.HOME, false);
            return;
        }

        showPage(previousPage, false);
    }

    private void showPage(PageType pageType, boolean recordHistory) {
        if (recordHistory && currentPage != null) {
            history.push(currentPage);
        }

        releaseCurrentPage();
        currentPage = pageType;

        switch (pageType) {
            case HOME -> showHomeScene();
            case GAME_SETTING -> showGameSettingScene();
            case GAME -> showGameScene();
            case SETTINGS -> showSettingsScene();
            case HELP -> showHelpScene();
            case LOCAL_MULTIPLAYER_SETUP -> showLocalMultiplayerSetupScene();
            case LOCAL_AI_SETUP -> showLocalAiSetupScene();
            case ONLINE_SETUP -> showOnlineSetupScene();
            case PLAYER_NAME_SETUP -> showPlayerNameSetupScene();
            case ROOM_SEARCH -> showRoomSearchScene();
            case ROOM_CREATE -> showRoomCreateScene();
            case ROOM_WAITING -> showRoomWaitingScene();
            case SETTLEMENT -> showSettlementScene();
        }
    }

    private void showHomeScene() {
        HomeController controller = new HomeController(this);
        HomeScene scene = new HomeScene(controller);
        showScene(scene, "Scrabble Front-End");
    }

    private void showGameSettingScene() {
        ModeSelectController controller = new ModeSelectController(this);
        ModeSelectScene scene = new ModeSelectScene(controller);
        showScene(scene, "Scrabble Game Setting");
    }

    private void showGameScene() {
        GameController controller = new GameController(
                this,
                gameLaunchContext != null ? gameLaunchContext : GameLaunchContext.defaultContext());
        gameController = controller;
        GameScene scene = new GameScene(controller);
        showScene(scene, "Scrabble Game");
    }

    private void releaseCurrentPage() {
        if (currentPage == PageType.GAME && gameController != null) {
            gameController.shutdown();
            gameController = null;
        }
        if (currentPage == PageType.ROOM_WAITING && roomWaitingController != null) {
            roomWaitingController.shutdown();
            roomWaitingController = null;
        }
    }

    private void showSettingsScene() {
        SettingsController controller = new SettingsController(this);
        SettingsScene scene = new SettingsScene(controller);
        showScene(scene, "Scrabble Settings");
    }

    private void showHelpScene() {
        HelpController controller = new HelpController(this);
        HelpScene scene = new HelpScene(controller);
        showScene(scene, "Scrabble Help");
    }

    private void showLocalMultiplayerSetupScene() {
        LocalMultiplayerSetupController controller = new LocalMultiplayerSetupController(this);
        LocalMultiplayerSetupScene scene = new LocalMultiplayerSetupScene(controller);
        showScene(scene, "Scrabble Local Multiplayer Setup");
    }

    private void showLocalAiSetupScene() {
        LocalAiSetupController controller = new LocalAiSetupController(this);
        LocalAiSetupScene scene = new LocalAiSetupScene(controller);
        showScene(scene, "Scrabble Local AI Setup");
    }

    private void showOnlineSetupScene() {
        OnlineSetupController controller = new OnlineSetupController(this);
        OnlineSetupScene scene = new OnlineSetupScene(controller);
        showScene(scene, "Scrabble Online Setup");
    }

    private void showPlayerNameSetupScene() {
        PlayerNameSetupController controller = new PlayerNameSetupController(this);
        PlayerNameSetupScene scene = new PlayerNameSetupScene(controller);
        showScene(scene, "Scrabble Player Setup");
    }

    private void showRoomSearchScene() {
        RoomSearchController controller = new RoomSearchController(this);
        RoomSearchScene scene = new RoomSearchScene(controller);
        showScene(scene, "Scrabble Room Search");
    }

    private void showRoomCreateScene() {
        RoomCreateController controller = new RoomCreateController(this);
        RoomCreateScene scene = new RoomCreateScene(controller);
        showScene(scene, "Scrabble Create Room");
    }

    private void showRoomWaitingScene() {
        RoomWaitingController controller = new RoomWaitingController(this);
        roomWaitingController = controller;
        RoomWaitingScene scene = new RoomWaitingScene(controller);
        showScene(scene, "Scrabble Room Waiting");
    }

    private void showSettlementScene() {
        SettlementController controller = new SettlementController(this);
        SettlementScene scene = new SettlementScene(controller);
        showScene(scene, "Scrabble Settlement");
    }

    private void showScene(javafx.scene.Scene scene, String stageTitle) {
        stage.setTitle(stageTitle);
        stage.setScene(scene);
        stage.show();

        if (animateNextSceneTitleEntrance) {
            animateNextSceneTitleEntrance = false;
            SceneTitleEntranceAnimationManager.playEntranceIfPresent(scene.getRoot());
        }
    }

    private enum PageType {
        HOME,
        GAME_SETTING,
        GAME,
        SETTINGS,
        HELP,
        LOCAL_MULTIPLAYER_SETUP,
        LOCAL_AI_SETUP,
        ONLINE_SETUP,
        PLAYER_NAME_SETUP,
        ROOM_SEARCH,
        ROOM_CREATE,
        ROOM_WAITING,
        SETTLEMENT
    }
}
