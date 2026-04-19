package com.kotva.presentation.scene;

import com.kotva.presentation.component.EnvelopeIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.controller.HomeController;
import com.kotva.presentation.viewmodel.HomeViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * HomeScene builds the current home page layout.
 * The structure is kept simple on purpose:
 * top title area + center content area with envelope on the left and buttons on the right.
 */
public class HomeScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    public HomeScene(HomeController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(HomeController controller) {
        HomeViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("home-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        BorderPane.setAlignment(titleBanner, Pos.CENTER);
        BorderPane.setMargin(titleBanner, new Insets(60, 110, 30, 110));
        root.setTop(titleBanner);

        EnvelopeIconView envelopeIconView = new EnvelopeIconView();
        envelopeIconView.setPrefSize(380, 270);

        CommonButton playButton = new CommonButton(viewModel.getPlayText());
        CommonButton settingsButton = new CommonButton(viewModel.getSettingsText());
        CommonButton helpButton = new CommonButton(viewModel.getHelpText());

        controller.bindActions(playButton, settingsButton, helpButton, envelopeIconView);

        VBox buttonColumn = new VBox(26);
        buttonColumn.setAlignment(Pos.CENTER_LEFT);
        buttonColumn.getStyleClass().add("home-button-column");
        buttonColumn.getChildren().addAll(playButton, settingsButton, helpButton);

        Region contentSpacer = new Region();
        contentSpacer.setMinWidth(70);

        HBox contentBox = new HBox();
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getStyleClass().add("home-content-box");
        contentBox.getChildren().addAll(envelopeIconView, contentSpacer, buttonColumn);

        BorderPane.setMargin(contentBox, new Insets(20, 110, 90, 110));
        root.setCenter(contentBox);

        return root;
    }

    /**
     * Load CSS files for the page and shared components.
     * The files live under src/main/resources.
     */
    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());
    }
}
