package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.LockedButton;
import com.kotva.presentation.component.SettingsGearIconView;
import com.kotva.presentation.component.SliderButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.controller.SettingsController;
import com.kotva.presentation.viewmodel.SettingsViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SettingsScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    public SettingsScene(SettingsController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(SettingsController controller) {
        StackPane sceneRoot = new StackPane();

        SettingsViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("settings-root");

        TitleBanner titleBanner = new TitleBanner("SCRABBLE");
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        SettingsGearIconView settingsGearIconView = new SettingsGearIconView();
        settingsGearIconView.setPrefSize(300, 300);

        InputButton nameButton = new InputButton("Name");
        SliderButton musicButton = new SliderButton("Music");
        LockedButton lockedButton = new LockedButton("ID", viewModel.getUserId());
        nameButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_1);
        musicButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_2);
        lockedButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_3);

        controller.bindControls(nameButton, musicButton);

        VBox settingColumn = new VBox(20);
        settingColumn.setAlignment(Pos.CENTER_LEFT);
        settingColumn.getChildren().addAll(nameButton, musicButton, lockedButton);

        Region spacer = new Region();
        spacer.setMinWidth(60);

        HBox contentBox = new HBox(settingsGearIconView, spacer, settingColumn);
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
        getStylesheets().add(getClass().getResource("/css/settings.css").toExternalForm());
    }
}
