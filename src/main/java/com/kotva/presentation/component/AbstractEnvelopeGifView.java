package com.kotva.presentation.component;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

abstract class AbstractEnvelopeGifView extends StackPane implements HomeEnvelopeAnimation {
    private static final double ENVELOPE_WIDTH = 495;
    private static final double ENVELOPE_HEIGHT = 372;

    private final AnimationSequence forwardSequence;
    private final AnimationSequence backwardSequence;
    private final ImageView imageView;
    private Timeline activeTimeline;

    protected AbstractEnvelopeGifView(String forwardResourcePath, String backwardResourcePath) {
        this.forwardSequence = decodeGifData(loadResourceBytes(forwardResourcePath));
        this.backwardSequence = decodeGifData(loadResourceBytes(backwardResourcePath));
        this.imageView = new ImageView();
        initialize();
    }

    private void initialize() {
        getStyleClass().add("home-envelope-gif");
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMouseTransparent(true);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setFitWidth(ENVELOPE_WIDTH);
        imageView.setFitHeight(ENVELOPE_HEIGHT);
        imageView.setMouseTransparent(true);
        imageView.setImage(forwardSequence.getFirstFrame());

        getChildren().add(imageView);
    }

    @Override
    public void playForward(Runnable onFinished) {
        playSequence(forwardSequence, onFinished);
    }

    @Override
    public void playBackward(Runnable onFinished) {
        playSequence(backwardSequence, onFinished);
    }

    @Override
    public void showForwardStartState() {
        stopPlayback();
        toFront();
        imageView.setImage(forwardSequence.getFirstFrame());
    }

    @Override
    public void showForwardEndState() {
        stopPlayback();
        toFront();
        imageView.setImage(forwardSequence.lastFrame);
    }

    public void activate() {
        playForward(null);
    }

    public void deactivate() {
        showForwardEndState();
    }

    private void playSequence(AnimationSequence sequence, Runnable onFinished) {
        stopPlayback();
        toFront();
        imageView.setImage(sequence.getFirstFrame());

        if (sequence.frames.size() <= 1) {
            imageView.setImage(sequence.lastFrame);
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        Timeline timeline = new Timeline();
        Duration elapsed = Duration.ZERO;
        for (int frameIndex = 1; frameIndex < sequence.frames.size(); frameIndex++) {
            elapsed = elapsed.add(sequence.frames.get(frameIndex - 1).duration);
            Image frameImage = sequence.frames.get(frameIndex).image;
            timeline.getKeyFrames().add(new KeyFrame(elapsed, event -> imageView.setImage(frameImage)));
        }

        timeline.getKeyFrames().add(new KeyFrame(sequence.totalDuration, event -> {
            imageView.setImage(sequence.lastFrame);
            if (onFinished != null) {
                onFinished.run();
            }
        }));
        timeline.setCycleCount(1);
        activeTimeline = timeline;
        timeline.playFromStart();
    }

    private void stopPlayback() {
        if (activeTimeline != null) {
            activeTimeline.stop();
            activeTimeline = null;
        }
    }

    private byte[] loadResourceBytes(String resourcePath) {
        try (InputStream inputStream = Objects.requireNonNull(
            getClass().getResourceAsStream(resourcePath),
            "Cannot find envelope gif resource: " + resourcePath)) {
            return inputStream.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read envelope gif resource: " + resourcePath, exception);
        }
    }

    private AnimationSequence decodeGifData(byte[] bytes) {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (imageInputStream == null) {
                return fallbackAnimationSequence(bytes);
            }

            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) {
                return fallbackAnimationSequence(bytes);
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInputStream, false, false);
                int frameCount = reader.getNumImages(true);
                if (frameCount <= 0) {
                    return fallbackAnimationSequence(bytes);
                }

                int canvasWidth = reader.getWidth(0);
                int canvasHeight = reader.getHeight(0);
                BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
                BufferedImage previousCanvasSnapshot = null;
                GifFrameState previousFrameState = null;
                List<AnimationFrame> frames = new ArrayList<>();
                Duration totalDuration = Duration.ZERO;

                for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                    if (previousFrameState != null) {
                        applyDisposal(canvas, previousCanvasSnapshot, previousFrameState);
                    }

                    IIOMetadata metadata = reader.getImageMetadata(frameIndex);
                    GifFrameState currentFrameState = GifFrameState.from(metadata);
                    BufferedImage frameImage = reader.read(frameIndex);
                    previousCanvasSnapshot = currentFrameState.restoreToPrevious ? copyImage(canvas) : null;
                    drawFrame(canvas, frameImage, currentFrameState.left, currentFrameState.top);

                    Image composedFrameImage = convertToFxImage(copyImage(canvas));
                    Duration frameDuration = resolveFrameDuration(metadata);
                    frames.add(new AnimationFrame(composedFrameImage, frameDuration));
                    totalDuration = totalDuration.add(frameDuration);
                    previousFrameState = currentFrameState;
                }

                Image lastFrame = frames.get(frames.size() - 1).image;
                Duration safeDuration = totalDuration.toMillis() > 0 ? totalDuration : Duration.millis(1000);
                return new AnimationSequence(List.copyOf(frames), lastFrame, safeDuration);
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            return fallbackAnimationSequence(bytes);
        }
    }

    private AnimationSequence fallbackAnimationSequence(byte[] bytes) {
        Image fallbackImage = new Image(new ByteArrayInputStream(bytes));
        AnimationFrame frame = new AnimationFrame(fallbackImage, Duration.millis(1000));
        return new AnimationSequence(List.of(frame), fallbackImage, Duration.millis(1000));
    }

    private Duration resolveFrameDuration(IIOMetadata metadata) {
        long delayCentiseconds = extractDelayTime(metadata);
        return Duration.millis(delayCentiseconds > 0 ? delayCentiseconds * 10.0 : 10.0);
    }

    private Image convertToFxImage(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", outputStream);
        return new Image(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    private void drawFrame(BufferedImage canvas, BufferedImage frameImage, int left, int top) {
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.drawImage(frameImage, left, top, null);
        } finally {
            graphics.dispose();
        }
    }

    private void applyDisposal(
        BufferedImage canvas,
        BufferedImage previousCanvasSnapshot,
        GifFrameState previousFrameState) {
        if (previousFrameState.restoreToBackground) {
            Graphics2D graphics = canvas.createGraphics();
            try {
                graphics.setComposite(AlphaComposite.Clear);
                graphics.fillRect(
                    previousFrameState.left,
                    previousFrameState.top,
                    previousFrameState.width,
                    previousFrameState.height);
            } finally {
                graphics.dispose();
            }
            return;
        }

        if (previousFrameState.restoreToPrevious && previousCanvasSnapshot != null) {
            Graphics2D graphics = canvas.createGraphics();
            try {
                graphics.setComposite(AlphaComposite.Src);
                graphics.drawImage(previousCanvasSnapshot, 0, 0, null);
            } finally {
                graphics.dispose();
            }
        }
    }

    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return copy;
    }

    private long extractDelayTime(IIOMetadata metadata) {
        if (metadata == null) {
            return 0;
        }

        String formatName = metadata.getNativeMetadataFormatName();
        if (formatName == null) {
            return 0;
        }

        Node root = metadata.getAsTree(formatName);
        return findDelayTime(root);
    }

    private long findDelayTime(Node node) {
        if (node == null) {
            return 0;
        }

        if ("GraphicControlExtension".equals(node.getNodeName())) {
            NamedNodeMap attributes = node.getAttributes();
            if (attributes == null) {
                return 0;
            }
            Node delayNode = attributes.getNamedItem("delayTime");
            if (delayNode == null) {
                return 0;
            }
            try {
                return Long.parseLong(delayNode.getNodeValue());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        Node child = node.getFirstChild();
        while (child != null) {
            long delayTime = findDelayTime(child);
            if (delayTime > 0) {
                return delayTime;
            }
            child = child.getNextSibling();
        }
        return 0;
    }

    private static final class GifFrameState {
        private final int left;
        private final int top;
        private final int width;
        private final int height;
        private final boolean restoreToBackground;
        private final boolean restoreToPrevious;

        private GifFrameState(
            int left,
            int top,
            int width,
            int height,
            boolean restoreToBackground,
            boolean restoreToPrevious) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
            this.restoreToBackground = restoreToBackground;
            this.restoreToPrevious = restoreToPrevious;
        }

        private static GifFrameState from(IIOMetadata metadata) {
            if (metadata == null) {
                return new GifFrameState(0, 0, 0, 0, false, false);
            }

            String formatName = metadata.getNativeMetadataFormatName();
            if (formatName == null) {
                return new GifFrameState(0, 0, 0, 0, false, false);
            }

            Node root = metadata.getAsTree(formatName);
            Node imageDescriptor = findNode(root, "ImageDescriptor");
            Node graphicControl = findNode(root, "GraphicControlExtension");

            int left = readIntAttribute(imageDescriptor, "imageLeftPosition");
            int top = readIntAttribute(imageDescriptor, "imageTopPosition");
            int width = readIntAttribute(imageDescriptor, "imageWidth");
            int height = readIntAttribute(imageDescriptor, "imageHeight");
            String disposalMethod = readStringAttribute(graphicControl, "disposalMethod");

            return new GifFrameState(
                left,
                top,
                width,
                height,
                "restoreToBackgroundColor".equals(disposalMethod),
                "restoreToPrevious".equals(disposalMethod));
        }

        private static Node findNode(Node node, String nodeName) {
            if (node == null) {
                return null;
            }
            if (nodeName.equals(node.getNodeName())) {
                return node;
            }

            Node child = node.getFirstChild();
            while (child != null) {
                Node found = findNode(child, nodeName);
                if (found != null) {
                    return found;
                }
                child = child.getNextSibling();
            }
            return null;
        }

        private static int readIntAttribute(Node node, String attributeName) {
            String value = readStringAttribute(node, attributeName);
            if (value == null) {
                return 0;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        private static String readStringAttribute(Node node, String attributeName) {
            if (node == null || node.getAttributes() == null) {
                return null;
            }
            Node attribute = node.getAttributes().getNamedItem(attributeName);
            return attribute == null ? null : attribute.getNodeValue();
        }
    }

    private static final class AnimationFrame {
        private final Image image;
        private final Duration duration;

        private AnimationFrame(Image image, Duration duration) {
            this.image = image;
            this.duration = duration;
        }
    }

    private static final class AnimationSequence {
        private final List<AnimationFrame> frames;
        private final Image lastFrame;
        private final Duration totalDuration;

        private AnimationSequence(List<AnimationFrame> frames, Image lastFrame, Duration totalDuration) {
            this.frames = frames;
            this.lastFrame = lastFrame;
            this.totalDuration = totalDuration;
        }

        private Image getFirstFrame() {
            return frames.isEmpty() ? lastFrame : frames.get(0).image;
        }
    }
}
