package com.kotva.presentation.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Plays the letter burst animation on the home screen.
 */
final class HomeLetterBurstAnimationManager {
    private static final Duration HOVER_DELAY = Duration.seconds(3);
    private static final double TILE_SIZE = 60;
    private static final double RIGHTWARD_PROBABILITY = 0.60;
    private static final double GRAVITY = 1_280;
    private static final double BOUNCE_DAMPING = 0.58;
    private static final double MIN_HORIZONTAL_SPEED = 140;
    private static final double MAX_HORIZONTAL_SPEED = 380;
    private static final double MIN_VERTICAL_SPEED = 560;
    private static final double MAX_VERTICAL_SPEED = 840;
    private static final List<String> LETTER_IMAGE_PATHS = List.of(
        "/images/home/letters/B.png",
        "/images/home/letters/C.png",
        "/images/home/letters/E.png",
        "/images/home/letters/E-prime.png",
        "/images/home/letters/H.png",
        "/images/home/letters/L.png",
        "/images/home/letters/L-prime.png",
        "/images/home/letters/O.png",
        "/images/home/letters/R.png",
        "/images/home/letters/S.png");

    private final StackPane sceneRoot;
    private final Node envelopeNode;
    private final List<Node> hoverTargets;
    private final List<Image> letterImages;
    private final Random random;
    private PauseTransition hoverDelay;
    private Node activeHoverTarget;
    private long hoverToken;

    HomeLetterBurstAnimationManager(
        StackPane sceneRoot,
        Node envelopeNode,
        List<? extends Node> hoverTargets) {
        this.sceneRoot = Objects.requireNonNull(sceneRoot, "sceneRoot cannot be null.");
        this.envelopeNode = Objects.requireNonNull(envelopeNode, "envelopeNode cannot be null.");
        this.hoverTargets = List.copyOf(Objects.requireNonNull(hoverTargets, "hoverTargets cannot be null."));
        this.letterImages = LETTER_IMAGE_PATHS.stream()
            .map(HomeLetterBurstAnimationManager::loadImage)
            .toList();
        this.random = new Random();
    }

    void install() {
        for (Node hoverTarget : hoverTargets) {
            hoverTarget.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> beginHover(hoverTarget));
            hoverTarget.addEventHandler(MouseEvent.MOUSE_EXITED, event -> cancelHover(hoverTarget));
        }
    }

    private void beginHover(Node hoverTarget) {
        activeHoverTarget = hoverTarget;
        hoverToken++;
        long token = hoverToken;
        if (hoverDelay != null) {
            hoverDelay.stop();
        }
        PauseTransition delay = new PauseTransition(HOVER_DELAY);
        hoverDelay = delay;
        delay.setOnFinished(event -> {
            if (activeHoverTarget == hoverTarget && hoverToken == token) {
                playBurst();
                if (activeHoverTarget == hoverTarget && hoverToken == token && hoverDelay == delay) {
                    delay.playFromStart();
                }
            }
        });
        delay.playFromStart();
    }

    private void cancelHover(Node hoverTarget) {
        if (activeHoverTarget != hoverTarget) {
            return;
        }
        activeHoverTarget = null;
        hoverToken++;
        if (hoverDelay != null) {
            hoverDelay.stop();
        }
    }

    private void playBurst() {
        if (sceneRoot.getScene() == null) {
            return;
        }

        Pane particleLayer = new Pane();
        particleLayer.setPickOnBounds(false);
        particleLayer.setMouseTransparent(true);
        particleLayer.prefWidthProperty().bind(sceneRoot.widthProperty());
        particleLayer.prefHeightProperty().bind(sceneRoot.heightProperty());
        particleLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        sceneRoot.getChildren().add(particleLayer);

        Point2D origin = resolveEnvelopeCenter();
        List<LetterParticle> particles = new ArrayList<>();
        for (Image letterImage : letterImages) {
            ImageView imageView = new ImageView(letterImage);
            imageView.setFitWidth(TILE_SIZE);
            imageView.setFitHeight(TILE_SIZE);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            imageView.setLayoutX(origin.getX() - TILE_SIZE / 2.0);
            imageView.setLayoutY(origin.getY() - TILE_SIZE / 2.0);
            imageView.setRotate(random.nextDouble(-18.0, 18.0));
            particleLayer.getChildren().add(imageView);
            particles.add(createParticle(imageView));
        }

        AnimationTimer timer = new AnimationTimer() {
            private long lastFrameNanos;

            @Override
            public void handle(long now) {
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = now;
                    return;
                }
                double elapsedSeconds = Math.min((now - lastFrameNanos) / 1_000_000_000.0, 0.04);
                lastFrameNanos = now;
                updateParticles(particles, particleLayer, elapsedSeconds);
                if (particles.isEmpty()) {
                    stop();
                    sceneRoot.getChildren().remove(particleLayer);
                }
            }
        };
        timer.start();
    }

    private LetterParticle createParticle(ImageView imageView) {
        boolean rightward = random.nextDouble() < RIGHTWARD_PROBABILITY;
        double horizontalSpeed =
            MIN_HORIZONTAL_SPEED + random.nextDouble() * (MAX_HORIZONTAL_SPEED - MIN_HORIZONTAL_SPEED);
        double verticalSpeed =
            MIN_VERTICAL_SPEED + random.nextDouble() * (MAX_VERTICAL_SPEED - MIN_VERTICAL_SPEED);
        double vx = rightward ? horizontalSpeed : -horizontalSpeed;
        double vy = -verticalSpeed;
        double spin = random.nextDouble(-180.0, 180.0);
        return new LetterParticle(imageView, vx, vy, spin);
    }

    private void updateParticles(List<LetterParticle> particles, Pane particleLayer, double elapsedSeconds) {
        double floorY = Math.max(0, sceneRoot.getHeight() - TILE_SIZE);
        double removeY = sceneRoot.getHeight() + TILE_SIZE * 1.8;
        for (int index = particles.size() - 1; index >= 0; index--) {
            LetterParticle particle = particles.get(index);
            ImageView imageView = particle.imageView();
            particle.setVy(particle.vy() + GRAVITY * elapsedSeconds);
            imageView.setLayoutX(imageView.getLayoutX() + particle.vx() * elapsedSeconds);
            imageView.setLayoutY(imageView.getLayoutY() + particle.vy() * elapsedSeconds);
            imageView.setRotate(imageView.getRotate() + particle.spin() * elapsedSeconds);

            if (!particle.bounced() && imageView.getLayoutY() >= floorY) {
                imageView.setLayoutY(floorY);
                particle.setVy(-Math.abs(particle.vy()) * BOUNCE_DAMPING);
                particle.setBounced(true);
            }

            if (particle.bounced() && imageView.getLayoutY() > removeY) {
                particleLayer.getChildren().remove(imageView);
                particles.remove(index);
            }
        }
    }

    private Point2D resolveEnvelopeCenter() {
        Bounds sceneBounds = envelopeNode.localToScene(envelopeNode.getBoundsInLocal());
        Point2D sceneCenter = new Point2D(
            sceneBounds.getMinX() + sceneBounds.getWidth() / 2.0,
            sceneBounds.getMinY() + sceneBounds.getHeight() / 2.0);
        return sceneRoot.sceneToLocal(sceneCenter);
    }

    private static Image loadImage(String imagePath) {
        return new Image(Objects.requireNonNull(
            HomeLetterBurstAnimationManager.class.getResource(imagePath),
            "Missing home letter image: " + imagePath).toExternalForm());
    }

    private static final class LetterParticle {
        private final ImageView imageView;
        private final double vx;
        private final double spin;
        private double vy;
        private boolean bounced;

        private LetterParticle(ImageView imageView, double vx, double vy, double spin) {
            this.imageView = imageView;
            this.vx = vx;
            this.vy = vy;
            this.spin = spin;
        }

        private ImageView imageView() {
            return imageView;
        }

        private double vx() {
            return vx;
        }

        private double vy() {
            return vy;
        }

        private void setVy(double vy) {
            this.vy = vy;
        }

        private double spin() {
            return spin;
        }

        private boolean bounced() {
            return bounced;
        }

        private void setBounced(boolean bounced) {
            this.bounced = bounced;
        }
    }
}
