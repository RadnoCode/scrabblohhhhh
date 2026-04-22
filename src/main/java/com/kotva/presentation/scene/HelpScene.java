package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.HelpIconView;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.controller.HelpController;
import com.kotva.presentation.viewmodel.HelpViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class HelpScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    public HelpScene(HelpController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(HelpController controller) {
        StackPane sceneRoot = new StackPane();

        HelpViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("help-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        HelpIconView helpIconView = new HelpIconView();
        helpIconView.setPrefSize(280, 280);

        Label helpTextLabel = new Label(viewModel.getHelpText());
        helpTextLabel.setWrapText(true);
        helpTextLabel.getStyleClass().add("help-text-label");

        ScrollPane helpScrollPane = new ScrollPane(helpTextLabel);
        helpScrollPane.getStyleClass().add("help-scroll-pane");
        helpScrollPane.setFitToWidth(true);
        helpScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        helpScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        StackPane helpPanel = new StackPane(helpScrollPane);
        helpPanel.getStyleClass().add("help-panel");
        helpPanel.setPrefSize(590, 330);

        Region spacer = new Region();
        spacer.setMinWidth(56);

        HBox contentBox = new HBox(helpIconView, spacer, helpPanel);
        contentBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(contentBox, new Insets(8, 100, 48, 100));
        root.setCenter(contentBox);

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(10, 0, 0, 30));

        sceneRoot.getChildren().addAll(SceneBackgroundLayer.createFor(sceneRoot), root, backButton);
        return sceneRoot;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/help.css").toExternalForm());
    }
}
