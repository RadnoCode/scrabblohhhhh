package com.kotva.presentation.fx;

import com.kotva.launcher.AppContext;
import com.kotva.presentation.controller.GameController;
import com.kotva.presentation.controller.HelpController;
import com.kotva.presentation.controller.HomeController;
import com.kotva.presentation.controller.LocalAiSetupController;
import com.kotva.presentation.controller.LocalMultiplayerSetupController;
import com.kotva.presentation.controller.ModeSelectController;
import com.kotva.presentation.controller.OnlineSetupController;
import com.kotva.presentation.controller.RoomCreateController;
import com.kotva.presentation.controller.RoomSearchController;
import com.kotva.presentation.controller.RoomWaitingController;
import com.kotva.presentation.controller.SettingsController;
import com.kotva.presentation.scene.HelpScene;
import com.kotva.presentation.scene.HomeScene;
import com.kotva.presentation.scene.GameScene;
import com.kotva.presentation.scene.LocalAiSetupScene;
import com.kotva.presentation.scene.LocalMultiplayerSetupScene;
import com.kotva.presentation.scene.ModeSelectScene;
import com.kotva.presentation.scene.OnlineSetupScene;
import com.kotva.presentation.scene.RoomCreateScene;
import com.kotva.presentation.scene.RoomSearchScene;
import com.kotva.presentation.scene.RoomWaitingScene;
import com.kotva.presentation.scene.SettingsScene;
import com.kotva.presentation.viewmodel.GameLaunchContext;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * SceneNavigator centralizes page switching logic.
 * Controllers only need to ask the navigator to show a target page,
 * and do not need to know how scenes are created.
 */
public class SceneNavigator {
    private final Stage stage;
    private final AppContext appContext;
    private final Deque<PageType> history;
    private GameLaunchContext gameLaunchContext;
    private RoomWaitingContext roomWaitingContext;
    private PageType currentPage;
    private GameController gameController;
    private RoomWaitingController roomWaitingController;

    public SceneNavigator(Stage stage, AppContext appContext) {
        this.stage = java.util.Objects.requireNonNull(stage, "stage cannot be null.");
        this.appContext = java.util.Objects.requireNonNull(appContext, "appContext cannot be null.");
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
        showPage(PageType.GAME, true);
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

    public void goBack() {
        if (history.isEmpty()) {
            showPage(PageType.HOME, false);
            return;
        }

        PageType previousPage = history.pop();
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
            case ROOM_SEARCH -> showRoomSearchScene();
            case ROOM_CREATE -> showRoomCreateScene();
            case ROOM_WAITING -> showRoomWaitingScene();
        }
    }

    private void showHomeScene() {
        HomeController controller = new HomeController(this);
        HomeScene scene = new HomeScene(controller);
        stage.setTitle("Scrabble Front-End");
        stage.setScene(scene);
        stage.show();
    }

    private void showGameSettingScene() {
        ModeSelectController controller = new ModeSelectController(this);
        ModeSelectScene scene = new ModeSelectScene(controller);
        stage.setTitle("Scrabble Game Setting");
        stage.setScene(scene);
        stage.show();
    }

    private void showGameScene() {
        GameController controller = new GameController(this, gameLaunchContext != null
                ? gameLaunchContext
                : GameLaunchContext.defaultContext());
        gameController = controller;
        GameScene scene = new GameScene(controller);
        stage.setTitle("Scrabble Game");
        stage.setScene(scene);
        stage.show();
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
        stage.setTitle("Scrabble Settings");
        stage.setScene(scene);
        stage.show();
    }

    private void showHelpScene() {
        HelpController controller = new HelpController(this);
        HelpScene scene = new HelpScene(controller);
        stage.setTitle("Scrabble Help");
        stage.setScene(scene);
        stage.show();
    }

    private void showLocalMultiplayerSetupScene() {
        LocalMultiplayerSetupController controller = new LocalMultiplayerSetupController(this);
        LocalMultiplayerSetupScene scene = new LocalMultiplayerSetupScene(controller);
        stage.setTitle("Scrabble Local Multiplayer Setup");
        stage.setScene(scene);
        stage.show();
    }

    private void showLocalAiSetupScene() {
        LocalAiSetupController controller = new LocalAiSetupController(this);
        LocalAiSetupScene scene = new LocalAiSetupScene(controller);
        stage.setTitle("Scrabble Local AI Setup");
        stage.setScene(scene);
        stage.show();
    }

    private void showOnlineSetupScene() {
        OnlineSetupController controller = new OnlineSetupController(this);
        OnlineSetupScene scene = new OnlineSetupScene(controller);
        stage.setTitle("Scrabble Online Setup");
        stage.setScene(scene);
        stage.show();
    }

    private void showRoomSearchScene() {
        RoomSearchController controller = new RoomSearchController(this);
        RoomSearchScene scene = new RoomSearchScene(controller);
        stage.setTitle("Scrabble Room Search");
        stage.setScene(scene);
        stage.show();
    }

    private void showRoomCreateScene() {
        RoomCreateController controller = new RoomCreateController(this);
        RoomCreateScene scene = new RoomCreateScene(controller);
        stage.setTitle("Scrabble Create Room");
        stage.setScene(scene);
        stage.show();
    }

    private void showRoomWaitingScene() {
        RoomWaitingController controller = new RoomWaitingController(this);
        roomWaitingController = controller;
        RoomWaitingScene scene = new RoomWaitingScene(controller);
        stage.setTitle("Scrabble Room Waiting");
        stage.setScene(scene);
        stage.show();
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
        ROOM_SEARCH,
        ROOM_CREATE,
        ROOM_WAITING
    }
}
