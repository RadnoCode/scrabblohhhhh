package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.TextInputLimiter;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.component.ViceTitleBanner;
import com.kotva.presentation.controller.PlayerNameSetupController;
import com.kotva.presentation.fx.PlayerNameSetupContext;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PlayerNameSetupScene extends Scene {
    private static final int MAX_NAME_CODE_POINTS = 8;
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final String NICKNAME_STAMP_IMAGE_PATH = "/images/vice-title/nickname.png";
    private static final String GO_BUTTON_IMAGE_PATH = "/images/player-name/bottom-go.png";
    private static final String[] PLAYER_BUTTON_IMAGE_PATHS = {
        "/images/player-name/player1.png",
        "/images/player-name/player2.png",
        "/images/player-name/player3.png",
        "/images/player-name/player4.png"
    };
    private static final double NICKNAME_STAMP_WIDTH = 180;
    private static final double NICKNAME_STAMP_HEIGHT = 90;
    private static final double BUTTON_SCALE = 0.8;
    private static final double STANDARD_INPUT_BUTTON_WIDTH = 420 * BUTTON_SCALE;
    private static final double STANDARD_INPUT_BUTTON_HEIGHT = (420.0 / (1301.0 / 262.0)) * BUTTON_SCALE;
    private static final double STANDARD_INPUT_FIELD_WIDTH = 236 * BUTTON_SCALE;
    private static final double STANDARD_INPUT_FIELD_HEIGHT = 40 * BUTTON_SCALE;
    private static final double GO_BUTTON_WIDTH = STANDARD_INPUT_BUTTON_HEIGHT * 591.0 / 238.0;
    private static final double COLUMN_OFFSET_Y = 50;
    private static final Insets CONTENT_MARGIN = new Insets(8, 100, 48, 100);
    private static final Insets MESSAGE_MARGIN = new Insets(172, 0, 0, 0);

    public PlayerNameSetupScene(PlayerNameSetupController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(PlayerNameSetupController controller) {
        StackPane sceneRoot = new StackPane();
        PlayerNameSetupContext context = controller.getContext();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("mode-root");

        TitleBanner titleBanner = new TitleBanner(context.getTitleText());
        TransientMessageView messageView = new TransientMessageView();
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(360, 270);
        cardStackIconView.installPlayBeforeButtonActions(sceneRoot);

        ViceTitleBanner viceTitleBanner = new ViceTitleBanner(
                context.getViceTitleText(),
                NICKNAME_STAMP_IMAGE_PATH);
        viceTitleBanner.setPrefSize(NICKNAME_STAMP_WIDTH, NICKNAME_STAMP_HEIGHT);
        viceTitleBanner.setMinSize(NICKNAME_STAMP_WIDTH, NICKNAME_STAMP_HEIGHT);
        viceTitleBanner.setMaxSize(NICKNAME_STAMP_WIDTH, NICKNAME_STAMP_HEIGHT);
        StackPane viceTitleBox = new StackPane(viceTitleBanner);
        viceTitleBox.setAlignment(Pos.CENTER);
        viceTitleBox.setPrefWidth(STANDARD_INPUT_BUTTON_WIDTH);
        viceTitleBox.setMinWidth(STANDARD_INPUT_BUTTON_WIDTH);
        viceTitleBox.setMaxWidth(STANDARD_INPUT_BUTTON_WIDTH);

        Label roomTitleLabel = new Label(context.getRoomTitle());
        roomTitleLabel.getStyleClass().add("player-name-room-label");
        boolean hasRoomTitle = !context.getRoomTitle().isBlank();
        roomTitleLabel.setVisible(hasRoomTitle);
        roomTitleLabel.setManaged(hasRoomTitle);

        int activePlayerCount = context.getActivePlayerCount();
        int displayedPlayerCount = context.getFlow() == PlayerNameSetupContext.Flow.HOT_SEAT
                ? 4
                : activePlayerCount;

        List<InputButton> playerNameButtons = new ArrayList<>();
        for (int index = 0; index < displayedPlayerCount; index++) {
            String playerTitle = resolvePlayerTitle(context, index);
            String defaultPlayerName = resolveDefaultPlayerName(context, index);

            InputButton playerNameButton = new InputButton(playerTitle);
            playerNameButton.getStyleClass().add("player-name-input-button");
            playerNameButton.applyTemplateSize(STANDARD_INPUT_BUTTON_WIDTH);
            playerNameButton.setCustomBackgroundImage(resolvePlayerButtonImagePath(index));
            playerNameButton.setInputFieldSize(STANDARD_INPUT_FIELD_WIDTH, STANDARD_INPUT_FIELD_HEIGHT);
            playerNameButton.setInputText(defaultPlayerName);
            playerNameButton.getTextField().setPromptText("UTF-8 nickname");
            TextInputLimiter.limitCodePoints(playerNameButton.getTextField(), MAX_NAME_CODE_POINTS);
            if (index >= activePlayerCount) {
                playerNameButton.setDisable(true);
                playerNameButton.setOpacity(0.45);
                playerNameButton.getTextField().setEditable(false);
            } else {
                playerNameButton.setDisable(false);
                playerNameButton.setOpacity(1.0);
                playerNameButton.getTextField().setEditable(true);
            }
            playerNameButtons.add(playerNameButton);
        }

        VBox cardsBox = new VBox(5);
        cardsBox.getStyleClass().add("player-name-input-box");
        cardsBox.setAlignment(Pos.TOP_CENTER);
        cardsBox.getChildren().addAll(playerNameButtons);

        CommonButton primaryButton = new CommonButton(context.getConfirmButtonText());
        primaryButton.getStyleClass().add("player-name-go-button");
        primaryButton.setCustomBackgroundImage(GO_BUTTON_IMAGE_PATH);
        primaryButton.applyButtonSize(GO_BUTTON_WIDTH, STANDARD_INPUT_BUTTON_HEIGHT);
        controller.bindPrimaryAction(primaryButton, playerNameButtons, messageView);
        StackPane primaryButtonBox = new StackPane(primaryButton);
        primaryButtonBox.setAlignment(Pos.CENTER);
        primaryButtonBox.setPrefWidth(STANDARD_INPUT_BUTTON_WIDTH);
        primaryButtonBox.setMinWidth(STANDARD_INPUT_BUTTON_WIDTH);
        primaryButtonBox.setMaxWidth(STANDARD_INPUT_BUTTON_WIDTH);

        VBox rightColumn = new VBox(5, viceTitleBox, roomTitleLabel, cardsBox, primaryButtonBox);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setPrefWidth(440);
        rightColumn.setTranslateY(COLUMN_OFFSET_Y);

        Region spacer = new Region();
        spacer.setMinWidth(56);

        HBox contentBox = new HBox(cardStackIconView, spacer, rightColumn);
        contentBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(contentBox, CONTENT_MARGIN);
        root.setCenter(contentBox);

        new OptionSceneEntranceAnimationManager(
                sceneRoot,
                titleBanner,
                cardStackIconView,
                List.of(viceTitleBox, roomTitleLabel, cardsBox, primaryButtonBox))
                .install();

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(50, 0, 0, 20));

        StackPane.setAlignment(messageView, Pos.TOP_CENTER);
        StackPane.setMargin(messageView, MESSAGE_MARGIN);

        sceneRoot.getChildren().addAll(SceneBackgroundLayer.createFor(sceneRoot), root, messageView, backButton);
        return sceneRoot;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/mode.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/player-name-setup.css").toExternalForm());
    }

    private static String resolvePlayerTitle(PlayerNameSetupContext context, int index) {
        if (index < context.getCardTitles().size()) {
            return context.getCardTitles().get(index);
        }
        return "Player " + (index + 1);
    }

    private static String resolveDefaultPlayerName(PlayerNameSetupContext context, int index) {
        if (index < context.getDefaultNames().size()) {
            return context.getDefaultNames().get(index);
        }
        return "Player " + (index + 1);
    }

    private static String resolvePlayerButtonImagePath(int index) {
        if (index >= 0 && index < PLAYER_BUTTON_IMAGE_PATHS.length) {
            return PLAYER_BUTTON_IMAGE_PATHS[index];
        }
        return PLAYER_BUTTON_IMAGE_PATHS[0];
    }
}
