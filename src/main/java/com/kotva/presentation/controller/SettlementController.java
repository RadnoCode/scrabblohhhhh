package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.SettlementViewModel;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

public class SettlementController {
    private static final DateTimeFormatter EXPORT_TIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final SceneNavigator navigator;
    private final SettlementViewModel viewModel;

    public SettlementController(SceneNavigator navigator) {
        this.navigator = Objects.requireNonNull(navigator, "navigator cannot be null.");
        this.viewModel = SettlementViewModel.fromResult(navigator.getSettlementResult());
    }

    public SettlementViewModel getViewModel() {
        return viewModel;
    }

    public void bindActions(
        CommonButton homeButton,
        CommonButton exportButton,
        Parent captureRoot,
        TransientMessageView messageView) {
        Objects.requireNonNull(homeButton, "homeButton cannot be null.");
        Objects.requireNonNull(exportButton, "exportButton cannot be null.");
        Objects.requireNonNull(captureRoot, "captureRoot cannot be null.");
        Objects.requireNonNull(messageView, "messageView cannot be null.");

        homeButton.setOnAction(event -> navigator.showHome());
        exportButton.setOnAction(event -> exportSnapshot(captureRoot, exportButton, messageView));
    }

    private void exportSnapshot(
        Parent captureRoot,
        CommonButton exportButton,
        TransientMessageView messageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Settlement Screenshot");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        fileChooser.setInitialFileName(
            "scrabble-settlement-" + EXPORT_TIME_FORMAT.format(LocalDateTime.now()) + ".png");

        File selectedFile = fileChooser.showSaveDialog(
            exportButton.getScene() == null ? null : exportButton.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        File normalizedFile = ensurePngExtension(selectedFile);
        WritableImage snapshot = captureRoot.snapshot(new SnapshotParameters(), null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", normalizedFile);
            messageView.showMessage("Saved screenshot: " + normalizedFile.getName());
        } catch (IOException exception) {
            messageView.showMessage("Failed to save screenshot.");
        }
    }

    private File ensurePngExtension(File selectedFile) {
        String fileName = selectedFile.getName().toLowerCase();
        if (fileName.endsWith(".png")) {
            return selectedFile;
        }
        return new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
    }
}
