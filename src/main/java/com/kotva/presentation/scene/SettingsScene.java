package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.SettingsGearIconView;
import com.kotva.presentation.component.SettingsValueButton;
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

/**
 * Shows the settings screen.
 */
public class SettingsScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final double FEATURED_ICON_WIDTH = 270;
    private static final double FEATURED_ICON_HEIGHT = 202.5;
    private static final double SETTINGS_BUTTON_WIDTH = 420;
    private static final String ID_BUTTON_IMAGE_PATH = "/images/settings/buttons/id.png";

    public SettingsScene(SettingsController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(SettingsController controller) {
        StackPane sceneRoot = new StackPane();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("settings-root");

        TitleBanner titleBanner = new TitleBanner("SCRABBLE");
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        SettingsGearIconView settingsGearIconView = new SettingsGearIconView();
        settingsGearIconView.setPrefSize(FEATURED_ICON_WIDTH, FEATURED_ICON_HEIGHT);
        settingsGearIconView.setMinSize(FEATURED_ICON_WIDTH, FEATURED_ICON_HEIGHT);
        settingsGearIconView.setMaxSize(FEATURED_ICON_WIDTH, FEATURED_ICON_HEIGHT);

        SettingsViewModel viewModel = controller.getViewModel();
        SliderButton soundEffectButton = new SliderButton("SoundEffect");
        SettingsValueButton idButton = new SettingsValueButton(viewModel.getUserId());
        soundEffectButton.getStyleClass().add("settings-sound-effect-button");
        soundEffectButton.applyTemplateSize(SETTINGS_BUTTON_WIDTH);
        soundEffectButton.setSliderWidth(104);
        soundEffectButton.setSliderRightOffset(124);
        controller.bindSoundEffectSlider(soundEffectButton);
        configureSettingsButton(idButton, ID_BUTTON_IMAGE_PATH);

        VBox settingColumn = new VBox(20);
        settingColumn.setAlignment(Pos.CENTER_LEFT);
        settingColumn.getChildren().addAll(soundEffectButton, idButton);

        Region spacer = new Region();
        spacer.setMinWidth(60);

        HBox contentBox = new HBox(settingsGearIconView, spacer, settingColumn);
        contentBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(contentBox, new Insets(8, 100, 48, 100));
        root.setCenter(contentBox);

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(50, 0, 0, 20));

        sceneRoot.getChildren().addAll(SceneBackgroundLayer.createFor(sceneRoot), root, backButton);
        return sceneRoot;
    }

    private static void configureSettingsButton(CommonButton button, String imagePath) {
        button.setText(null);
        button.setCustomBackgroundImage(imagePath);
        button.applyTemplateSize(SETTINGS_BUTTON_WIDTH);
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/settings.css").toExternalForm());
    }
}
