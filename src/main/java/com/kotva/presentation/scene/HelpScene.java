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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

        // Create ImageView for the lightbulb icon
        javafx.scene.Node helpIconNode;
        try {
            ImageView helpImageView = new ImageView();
            Image helpImage = new Image(HelpScene.class.getResourceAsStream("/images/help/lightbulb.png"));
            helpImageView.setImage(helpImage);
            helpImageView.setPreserveRatio(true);
            helpImageView.setFitWidth(280);
            helpImageView.setFitHeight(280);
            helpIconNode = helpImageView;
        } catch (Exception e) {
            // Fallback to the original HelpIconView if image not found
            System.err.println("Help image not found, using default icon: " + e.getMessage());
            HelpIconView helpIconView = new HelpIconView();
            helpIconView.setPrefSize(280, 280);
            helpIconNode = helpIconView;
        }

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
        helpPanel.setPrefSize(590, 230);  // Changed from 330 to 230 (reduced by 100px)

        Region spacer = new Region();
        spacer.setMinWidth(156);  // Changed from 56 to 156 (increased by 100px to move scrollpane right)

        HBox contentBox = new HBox(helpIconNode, spacer, helpPanel);
        contentBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(contentBox, new Insets(58, 100, 48, 100));  // Changed top margin from 8 to 108 (increased by 100px)
        root.setCenter(contentBox);

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(50, 0, 0, 20));

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
