package com.kotva.presentation.scene;

import com.kotva.presentation.component.ActionPanelView;
import com.kotva.presentation.component.AiStatusBannerView;
import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.component.PlayerInfoCardView;
import com.kotva.presentation.component.RackView;
import com.kotva.presentation.component.TimerView;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.controller.GameController;
import com.kotva.presentation.interaction.GameInteractionCoordinator;
import com.kotva.presentation.renderer.GameRenderer;
import com.kotva.presentation.renderer.PreviewRenderer;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * GameScene 负责组装游戏页的 JavaFX 视图树。
 * 这里不直接处理业务逻辑，只负责把组件和控制器需要的渲染层接起来。
 */
public class GameScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    public GameScene(GameController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(GameController controller) {
        // 先从控制器拿到页面基础展示数据。
        GameViewModel viewModel = controller.getViewModel();

        // 最外层使用 StackPane，这样主内容层和拖拽浮层可以叠在一起。
        StackPane root = new StackPane();
        root.getStyleClass().add("game-root");

        // contentRoot 负责正常页面排版；dragOverlay 专门承载拖拽中的假 Tile。
        BorderPane contentRoot = new BorderPane();
        Pane dragOverlay = new Pane();
        // 浮层只负责显示，不拦截鼠标事件。
        dragOverlay.setMouseTransparent(true);
        dragOverlay.setPickOnBounds(false);

        // 顶部标题横幅。
        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        BorderPane.setMargin(titleBanner, new Insets(34, 210, 24, 210));
        contentRoot.setTop(titleBanner);

        // 中央棋盘列：上面是棋盘，下面是牌架。
        AiStatusBannerView aiStatusBannerView = new AiStatusBannerView();
        BoardView boardView = new BoardView();
        RackView rackView = new RackView();
        VBox boardColumn = new VBox(18, aiStatusBannerView, boardView, rackView);
        boardColumn.setAlignment(Pos.CENTER);
        boardColumn.getStyleClass().add("game-board-column");

        PlayerInfoCardView leftTopCard = new PlayerInfoCardView();
        PlayerInfoCardView leftBottomCard = new PlayerInfoCardView();
        TimerView stepTimerView = new TimerView("Step Time");
        TimerView totalTimerView = new TimerView("Total Time");
        HBox timerRow = new HBox(18, stepTimerView, totalTimerView);
        timerRow.setAlignment(Pos.CENTER);
        timerRow.getStyleClass().add("game-timer-row");

        // 左侧列固定放两个玩家卡和计时器。
        VBox leftColumn = new VBox(34, leftTopCard, leftBottomCard, timerRow);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.getStyleClass().add("game-side-column");

        PlayerInfoCardView rightTopCard = new PlayerInfoCardView();
        PlayerInfoCardView rightBottomCard = new PlayerInfoCardView();
        ActionPanelView actionPanel = new ActionPanelView();

        // 右侧列固定放两个玩家卡和工作台。
        VBox rightColumn = new VBox(34, rightTopCard, rightBottomCard, actionPanel);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.getStyleClass().add("game-side-column");

        // 三列横向排开，形成完整游戏页主体。
        HBox contentBox = new HBox(42, leftColumn, boardColumn, rightColumn);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getStyleClass().add("game-content-box");
        BorderPane.setMargin(contentBox, new Insets(6, 44, 48, 44));
        contentRoot.setCenter(contentBox);

        // PreviewRenderer 只画“假的拖拽效果”，不记录真实草稿。
        PreviewRenderer previewRenderer = new PreviewRenderer(boardView, dragOverlay);
        // GameRenderer 负责把 ViewModel 投到具体组件上。
        GameRenderer renderer = new GameRenderer(
                boardView,
                rackView,
                actionPanel,
                aiStatusBannerView,
                stepTimerView,
                totalTimerView,
                List.of(leftTopCard, rightTopCard, leftBottomCard, rightBottomCard),
                controller.getDraftState(),
                previewRenderer);
        // 交互协调器负责把 Rack / Board / 工作台事件都接起来。
        GameInteractionCoordinator interactionCoordinator = new GameInteractionCoordinator(
                boardView,
                rackView,
                actionPanel,
                controller.getDraftState(),
                previewRenderer,
                renderer,
                controller);
        // 控制器在这里拿到渲染层和交互层，并正式启动页面。
        controller.bind(renderer, interactionCoordinator);

        // 主内容层在下，拖拽浮层在上。
        root.getChildren().addAll(contentRoot, dragOverlay);

        return root;
    }

    private void loadStyleSheets() {
        // 依次加载基础、主题、组件和游戏页专属样式。
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
    }
}
