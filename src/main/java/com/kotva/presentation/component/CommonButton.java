package com.kotva.presentation.component;

import com.kotva.infrastructure.AudioManager;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class CommonButton extends Button {
    private static final double TEMPLATE_ASPECT_RATIO = 1301.0 / 262.0;
    private static final double DEFAULT_BUTTON_WIDTH = 420;
    private static final double DEFAULT_BUTTON_HEIGHT = DEFAULT_BUTTON_WIDTH / TEMPLATE_ASPECT_RATIO;
    private static final double DEFAULT_BUTTON_SCALE = 0.8;
    private static AudioManager audioManager;
    private static final Map<TemplateState, Image> TEMPLATE_IMAGES = createTemplateImages();

    private boolean templateEnabled = true;
    private TemplateState templateState = TemplateState.TEMPLATE_1;
    private final StackPane graphicRoot = new StackPane();
    private final ImageView backgroundView = new ImageView();
    private final StackPane contentHolder = new StackPane();
    private final Label textLabel = new Label();

    public enum TemplateState {
        TEMPLATE_1("/images/buttons/CommonButtonTemplate1.png"),
        TEMPLATE_2("/images/buttons/CommonButtonTemplate2.png"),
        TEMPLATE_3("/images/buttons/CommonButtonTemplate3.png");

        private final String imagePath;

        TemplateState(String imagePath) {
            this.imagePath = imagePath;
        }

        public String getImagePath() {
            return imagePath;
        }
    }

    public CommonButton() {
        initializeButton();
    }

    public CommonButton(String text) {
        super(text);
        initializeButton();
    }

    protected void initializeButton() {
        getStyleClass().add("common-button");

        setFocusTraversable(true);
        setScaleX(DEFAULT_BUTTON_SCALE);
        setScaleY(DEFAULT_BUTTON_SCALE);
        initializeGraphicRoot();
        setTemplateState(TemplateState.TEMPLATE_1);
        applyTemplateSize(DEFAULT_BUTTON_WIDTH);

        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> requestFocus());
        addEventFilter(ActionEvent.ACTION, event -> playClickSound());
    }

    public void setTemplateState(TemplateState templateState) {
        this.templateState = templateState == null ? TemplateState.TEMPLATE_1 : templateState;
        refreshTemplateBackground();
    }

    public TemplateState getTemplateState() {
        return templateState;
    }

    public void setTemplateEnabled(boolean templateEnabled) {
        this.templateEnabled = templateEnabled;
        refreshTemplateBackground();
    }

    public boolean isTemplateEnabled() {
        return templateEnabled;
    }

    public void applyTemplateSize(double width) {
        double height = computeTemplateHeight(width);
        applyFixedSize(width, height);
    }

    public static void setAudioManager(AudioManager manager) {
        audioManager = manager;
    }

    protected void playClickSound() {
        if (audioManager != null) {
            audioManager.playButtonClick();
        }
    }

    protected void setButtonContent(javafx.scene.Node content) {
        contentHolder.getChildren().setAll(content == null ? textLabel : content);
    }

    protected void applyFixedSize(double width, double height) {
        super.setPrefSize(width, height);
        super.setMinSize(width, height);
        super.setMaxSize(width, height);
        graphicRoot.setPrefSize(width, height);
        graphicRoot.setMinSize(width, height);
        graphicRoot.setMaxSize(width, height);
    }

    protected double computeTemplateHeight(double width) {
        return width / TEMPLATE_ASPECT_RATIO;
    }

    private void initializeGraphicRoot() {
        textLabel.textProperty().bind(textProperty());
        textLabel.getStyleClass().add("common-button-label");
        textLabel.setMouseTransparent(true);

        backgroundView.fitWidthProperty().bind(widthProperty());
        backgroundView.fitHeightProperty().bind(heightProperty());
        backgroundView.setPreserveRatio(false);
        backgroundView.setMouseTransparent(true);

        contentHolder.setAlignment(Pos.CENTER_LEFT);
        contentHolder.prefWidthProperty().bind(widthProperty());
        contentHolder.prefHeightProperty().bind(heightProperty());
        setButtonContent(null);

        graphicRoot.setAlignment(Pos.CENTER_LEFT);
        graphicRoot.getChildren().addAll(backgroundView, contentHolder);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(graphicRoot);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        graphicRoot.setClip(clip);
    }

    private void refreshTemplateBackground() {
        backgroundView.setVisible(templateEnabled);
        backgroundView.setManaged(templateEnabled);
        backgroundView.setImage(templateEnabled ? TEMPLATE_IMAGES.get(templateState) : null);
    }

    private static Map<TemplateState, Image> createTemplateImages() {
        EnumMap<TemplateState, Image> images = new EnumMap<>(TemplateState.class);
        for (TemplateState state : TemplateState.values()) {
            images.put(state, loadImage(state.getImagePath()));
        }
        return images;
    }

    private static Image loadImage(String imagePath) {
        return new Image(
            Objects.requireNonNull(
                CommonButton.class.getResource(imagePath),
                "Missing common button template image: " + imagePath)
                .toExternalForm());
    }
}
