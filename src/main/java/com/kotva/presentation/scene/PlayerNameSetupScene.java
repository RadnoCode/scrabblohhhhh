package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.PlayerNameCardView;
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
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final String NICKNAME_STAMP_IMAGE_PATH = "/images/vice-title/nickname.png";
    private static final double NICKNAME_STAMP_WIDTH = 180;
    private static final double NICKNAME_STAMP_HEIGHT = 90;
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
        HBox viceTitleBox = new HBox(viceTitleBanner);
        viceTitleBox.setAlignment(Pos.CENTER);
        viceTitleBox.setPrefWidth(420);
        viceTitleBox.setMinWidth(420);
        viceTitleBox.setMaxWidth(420);

        Label roomTitleLabel = new Label(context.getRoomTitle());
        roomTitleLabel.getStyleClass().add("player-name-room-label");
        boolean hasRoomTitle = !context.getRoomTitle().isBlank();
        roomTitleLabel.setVisible(hasRoomTitle);
        roomTitleLabel.setManaged(hasRoomTitle);

        Label summaryLabel = new Label(context.getSummaryText());
        summaryLabel.getStyleClass().add("player-name-summary-label");

        Label hintLabel = new Label(context.getHintText());
        hintLabel.getStyleClass().add("player-name-hint-label");

        List<PlayerNameCardView> playerCards = new ArrayList<>();
        for (int index = 0; index < context.getCardTitles().size(); index++) {
            playerCards.add(
                    new PlayerNameCardView(
                            context.getCardTitles().get(index),
                            context.getDefaultNames().get(index)));
        }

        VBox cardsBox = new VBox(14);
        cardsBox.getStyleClass().add("player-name-card-box");
        cardsBox.setAlignment(Pos.TOP_CENTER);
        cardsBox.getChildren().addAll(playerCards);

        CommonButton primaryButton = new CommonButton(context.getConfirmButtonText());
        primaryButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_2);
        controller.bindPrimaryAction(primaryButton, playerCards, messageView);

        VBox rightColumn = new VBox(14, viceTitleBox, roomTitleLabel, summaryLabel, hintLabel, cardsBox, primaryButton);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setPrefWidth(440);

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
                List.of(viceTitleBox, roomTitleLabel, summaryLabel, hintLabel, cardsBox, primaryButton))
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
}
