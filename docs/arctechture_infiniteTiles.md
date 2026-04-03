可以。下面我给你一版简化后的完整层级架构图 + 详细类设计。
这一版是专门为你们当前阶段收缩过的：
•   保留核心架构思想
•   去掉过度分层
•   方便给组员讲解
•   也方便直接开始建项目

我会按这几个部分来给：

       1.  简化后的总层级架构图
                  2.  简化后的包结构
                             3.  每层职责
                                        4.  详细类设计
                                                   5.  关键调用链
                                                              6.  最后给一个“组会讲解版速记图”

⸻

一、简化后的总层级架构图

┌──────────────────────────────────────────────────────────────┐
│                        Presentation                          │
│  FXML Pages / Controllers / UI Components / GameUiState     │
│  main-menu.fxml / game-setup.fxml / game.fxml / result.fxml │
└──────────────────────────────────────────────────────────────┘
│
▼
┌──────────────────────────────────────────────────────────────┐
│                        Application                           │
│  GameSetupService                                            │
│  GameApplicationService                                      │
│  MovePreviewService                                          │
│  TurnCoordinator                                             │
│  ClockService                                                │
│  SettlementService                                           │
│  GameSession / TurnDraft / PreviewResult / GameConfig        │
└──────────────────────────────────────────────────────────────┘
│                            │
▼                            ▼
┌───────────────────────────────┐   ┌───────────────────────────┐
│            Domain             │   │       Mode / Policy       │
│  GameState / Board / Player   │   │ PlayerController          │
│  RuleEngine / Validator       │   │ PlayerControlType         │
│  WordExtractor / ScoreCalc    │   │ DictionaryType            │
│  EndEvaluator                 │   │ GameMode / EndReason      │
└───────────────────────────────┘   └───────────────────────────┘
│
▼
┌──────────────────────────────────────────────────────────────┐
│                      Infrastructure                          │
│  DictionaryRepository / SettingsRepository / AudioManager    │
│  NetworkTransport / Logger                                   │
└──────────────────────────────────────────────────────────────┘


⸻

二、简化后的生命周期架构图

App启动
→ MainApp / AppLauncher / AppContext
→ 主菜单

主菜单
→ 游戏设定页
→ 生成 GameConfig
→ 创建 GameSession
→ 进入游戏页

游戏页
→ 玩家拖拽 tile
→ 修改 TurnDraft
→ MovePreviewService 生成 PreviewResult
→ 提交 / 跳过 / 超时
→ TurnCoordinator 切换回合或结束游戏

游戏结束
→ SettlementService 生成 SettlementResult
→ 进入结算页


⸻

三、简化后的包结构

这一版比完整版收缩了 UI 层，保留最必要的东西。

com.yourteam.scrabble
├─ app
│  ├─ MainApp
│  ├─ AppLauncher
│  └─ AppContext
│
├─ presentation
│  ├─ fxml
│  │  ├─ main-menu.fxml
│  │  ├─ game-setup.fxml
│  │  ├─ game.fxml
│  │  ├─ settings.fxml
│  │  └─ result.fxml
│  ├─ controller
│  │  ├─ MainMenuController
│  │  ├─ GameSetupController
│  │  ├─ GameController
│  │  ├─ SettingsController
│  │  └─ ResultController
│  ├─ component
│  │  ├─ BoardView
│  │  ├─ RackView
│  │  ├─ PreviewPanel
│  │  ├─ PlayerClockView
│  │  └─ HotSeatOverlay
│  ├─ uistate
│  │  └─ GameUiState
│  └─ fx
│     └─ SceneNavigator
│
├─ application
│  ├─ GameSetupService
│  ├─ GameApplicationService
│  ├─ MovePreviewService
│  ├─ TurnCoordinator
│  ├─ ClockService
│  ├─ SettlementService
│  ├─ GameSession
│  ├─ SessionStatus
│  ├─ TurnDraft
│  ├─ DraftPlacement
│  ├─ PreviewResult
│  ├─ PreviewWord
│  ├─ BoardHighlight
│  ├─ GameConfig
│  ├─ PlayerConfig
│  ├─ TimeControlConfig
│  ├─ PlayerClock
│  ├─ RoundPassTracker
│  ├─ SettlementResult
│  ├─ PlayerSettlement
│  └─ NewGameRequest
│
├─ domain
│  ├─ GameState
│  ├─ Board
│  ├─ Cell
│  ├─ Position
│  ├─ Tile
│  ├─ TileBag
│  ├─ Rack
│  ├─ RackSlot
│  ├─ PlayerState
│  ├─ PlayerStatus
│  ├─ PlayerAction
│  ├─ PlaceTilesAction
│  ├─ PassAction
│  ├─ TilePlacement
│  ├─ RuleEngine
│  ├─ MoveValidator
│  ├─ WordExtractor
│  ├─ ExtractedWord
│  ├─ WordValidator
│  ├─ ScoreCalculator
│  ├─ ScoreResult
│  ├─ EndEvaluator
│  ├─ TurnRecord
│  └─ WordRecord
│
├─ mode
│  ├─ GameMode
│  ├─ PlayerController
│  ├─ PlayerControlType
│  ├─ LocalHumanPlayerController
│  ├─ AIPlayerController
│  └─ RemotePlayerController
│
├─ policy
│  ├─ DictionaryType
│  ├─ ClockPhase
│  ├─ WordType
│  ├─ HighlightType
│  ├─ BonusType
│  └─ GameEndReason
│
└─ infrastructure
├─ DictionaryRepository
├─ SettingsRepository
├─ AppSettings
├─ AudioManager
├─ NetworkTransport
└─ GameLogger


⸻

四、每层职责

1. app 层

负责整个程序启动与全局依赖初始化。

核心类
•   MainApp
•   AppLauncher
•   AppContext

作用
•   启动 JavaFX
•   初始化全局 service
•   初始化设置、词典、音频
•   打开主菜单

⸻

2. presentation 层

负责所有界面和交互。

组成
•   fxml：页面布局
•   controller：事件处理
•   component：棋盘、rack 等组件
•   uistate：只保留 GameUiState
•   fx：页面切换

说明

这一层不算规则，不改正式棋盘，只负责显示和交互。

⸻

3. application 层

负责流程控制。

作用
•   创建一局游戏
•   管理当前回合草稿
•   生成预览
•   提交动作
•   跳过回合
•   计时
•   热座交接
•   生成结算

⸻

4. domain 层

负责 Scrabble 正式规则。

作用
•   棋盘和玩家状态
•   落子是否合法
•   提取主词和交叉词
•   计分
•   游戏结束规则

⸻

5. mode / policy 层

负责模式和枚举策略。

说明

这个层很轻，主要为了避免这些配置和枚举散落各处。

⸻

6. infrastructure 层

负责词典、设置、音频、网络这些外部资源。

⸻

五、简化后的详细类设计

下面我按“最重要的一批类”给你完整说明。

⸻

六、App 层详细类

1. MainApp

作用

JavaFX 启动类。

主要职责
•   启动程序
•   创建主窗口
•   调用 AppLauncher
•   进入主菜单

简化结构

class MainApp extends Application {
@Override
public void start(Stage primaryStage) {
AppLauncher launcher = new AppLauncher();
AppContext context = launcher.launch(primaryStage);
}
}


⸻

2. AppLauncher

作用

初始化应用所需全局对象。

职责
•   加载设置
•   初始化词典仓库
•   初始化音频
•   初始化服务
•   创建 AppContext

⸻

3. AppContext

作用

全局依赖容器。

字段建议

class AppContext {
private SceneNavigator sceneNavigator;
private GameSetupService gameSetupService;
private GameApplicationService gameApplicationService;
private ClockService clockService;
private SettlementService settlementService;
private DictionaryRepository dictionaryRepository;
private SettingsRepository settingsRepository;
private AudioManager audioManager;
}


⸻

七、Application 层详细类

1. GameConfig

作用

本局游戏的设定。

字段

class GameConfig {
private GameMode gameMode;
private int playerCount;
private List<PlayerConfig> players;
private DictionaryType dictionaryType;
private boolean timeControlEnabled;
private TimeControlConfig timeControlConfig;
}


⸻

2. PlayerConfig

作用

单个玩家的开局信息。

字段

class PlayerConfig {
private String playerId;
private String playerName;
private PlayerControlType controlType;
}


⸻

3. TimeControlConfig

作用

时间规则配置。

字段

class TimeControlConfig {
private long mainTimeMillis;
private long overtimeMillis;
}


⸻

4. GameSession

作用

一局游戏运行时的大容器。

字段

class GameSession {
private String sessionId;
private GameConfig config;
private GameState gameState;
private TurnDraft currentDraft;
private SessionStatus sessionStatus;
private List<PlayerController> playerControllers;
private RoundPassTracker roundPassTracker;
}

解释
•   GameState 是正式状态
•   TurnDraft 是当前回合草稿
•   sessionStatus 控制热座遮罩、结算等状态

⸻

5. SessionStatus

enum SessionStatus {
IN_PROGRESS,
WAITING_FOR_HOTSEAT_HANDOFF,
GAME_OVER,
PAUSED
}


⸻

6. TurnDraft

作用

当前回合还没提交的临时落子。

字段

class TurnDraft {
private List<DraftPlacement> placements;
private Map<String, Integer> originalRackSlots;
private String draggingTileId;
private PreviewResult previewResult;
}


⸻

7. DraftPlacement

作用

单个未提交 tile 的临时摆放。

字段

class DraftPlacement {
private String tileId;
private Position position;
}


⸻

8. PreviewResult

作用

给 UI 显示当前预览。

字段

class PreviewResult {
private boolean valid;
private int estimatedScore;
private List<PreviewWord> words;
private List<BoardHighlight> highlights;
private List<String> messages;
}


⸻

9. PreviewWord

字段

class PreviewWord {
private String word;
private boolean valid;
private int scoreContribution;
private List<Position> coveredPositions;
private WordType wordType;
}


⸻

10. BoardHighlight

字段

class BoardHighlight {
private Position position;
private HighlightType type;
}


⸻

11. PlayerClock

作用

玩家个人时钟状态。

字段

class PlayerClock {
private long mainTimeRemainingMillis;
private long overtimeRemainingMillis;
private ClockPhase phase;
}


⸻

12. RoundPassTracker

作用

记录当前轮哪些活跃玩家选择了跳过。

字段

class RoundPassTracker {
private Set<String> passedPlayerIds;
}

规则
•   玩家 pass 时记录
•   有人成功落子后清空
•   所有活跃玩家都在集合中则结束游戏

⸻

13. SettlementResult

作用

结算页面的数据模型。

字段

class SettlementResult {
private GameEndReason endReason;
private List<PlayerSettlement> rankings;
private List<String> summaryMessages;
}


⸻

14. PlayerSettlement

字段

class PlayerSettlement {
private String playerId;
private String playerName;
private int finalScore;
private PlayerStatus status;
private int rank;
}


⸻

八、Application 服务类

1. GameSetupService

作用

从设定页创建一局游戏。

方法

interface GameSetupService {
GameConfig buildConfig(NewGameRequest request);
GameSession createSession(GameConfig config);
}


⸻

2. GameApplicationService

作用

游戏页主要调用的总入口。

方法

interface GameApplicationService {
PreviewResult placeDraftTile(GameSession session, String tileId, Position position);
PreviewResult moveDraftTile(GameSession session, String tileId, Position newPosition);
PreviewResult removeDraftTile(GameSession session, String tileId);
PreviewResult recallAllDraftTiles(GameSession session);
SubmitDraftResult submitDraft(GameSession session);
TurnTransitionResult passTurn(GameSession session);
void confirmHotSeatHandoff(GameSession session);
}


⸻

3. MovePreviewService

作用

根据当前草稿生成预览。

方法

interface MovePreviewService {
PreviewResult preview(GameSession session);
}


⸻

4. TurnCoordinator

作用

管理回合推进。

方法

interface TurnCoordinator {
TurnTransitionResult onDraftSubmitted(GameSession session);
TurnTransitionResult onPass(GameSession session);
TurnTransitionResult onTimeout(GameSession session);
void confirmHotSeatHandoff(GameSession session);
}


⸻

5. ClockService

作用

管理当前玩家总时间与倒计时。

方法

interface ClockService {
void startTurnClock(GameSession session);
void stopTurnClock(GameSession session);
void tick(GameSession session, long elapsedMillis);
boolean isCurrentPlayerTimeout(GameSession session);
void handleTimeout(GameSession session);
}


⸻

6. SettlementService

作用

生成结算结果。

方法

interface SettlementService {
SettlementResult settle(GameSession session, GameEndReason endReason);
}


⸻

九、Domain 层详细类

1. GameState

作用

正式已提交游戏状态。

字段

class GameState {
private Board board;
private List<PlayerState> players;
private int currentPlayerIndex;
private TileBag tileBag;
private boolean gameOver;
private List<TurnRecord> history;
}


⸻

2. PlayerState

作用

单个玩家正式状态。

字段

class PlayerState {
private String playerId;
private String playerName;
private Rack rack;
private int score;
private PlayerStatus status;
private PlayerClock clock;
}


⸻

3. Board

作用

正式棋盘，15×15。

字段

class Board {
private Cell[][] cells;
}


⸻

4. Cell

作用

棋盘上的一个格子。

字段

class Cell {
private Position position;
private Tile placedTile;
private BonusType bonusType;
}


⸻

5. Position

作用

棋盘位置。

字段

class Position {
private int row;
private int col;
}


⸻

6. Tile

作用

单张字母牌。

字段

class Tile {
private String tileId;
private char letter;
private int score;
private boolean blank;
private Character assignedLetter;
}


⸻

7. Rack

作用

玩家手牌区。

字段

class Rack {
private List<RackSlot> slots;
}


⸻

8. RackSlot

作用

rack 的一个槽位。

字段

class RackSlot {
private int index;
private Tile tile;
}


⸻

9. TileBag

作用
无尽字母池（每次抽牌随机生成新 Tile，永不枯竭）。

字段
class TileBag {
// 移除 remainingTiles 集合，改为无限生成逻辑
private Random random;
}


⸻

10. PlayerAction

interface PlayerAction {
String getPlayerId();
}


⸻

11. PlaceTilesAction

作用

正式提交的落子动作。

字段

class PlaceTilesAction implements PlayerAction {
private String playerId;
private List<TilePlacement> placements;
}


⸻

12. TilePlacement

字段

class TilePlacement {
private String tileId;
private Position position;
private Character assignedLetter;
}


⸻

13. PassAction

class PassAction implements PlayerAction {
private String playerId;
}


⸻

十、规则相关类

1. RuleEngine

作用

规则总入口。

方法

interface RuleEngine {
ValidationResult validate(GameState state, PlaceTilesAction action);
ScoreResult calculateScore(GameState state, PlaceTilesAction action);
GameState apply(GameState state, PlaceTilesAction action);
}


⸻

2. MoveValidator

作用

检查落子是否合法。

负责检查
•   同一行或同一列
•   是否连续
•   首手是否过中心
•   是否连接已有词
•   是否覆盖已有格
•   是否满足结构规则

⸻

3. WordExtractor

作用

提取这次动作形成的所有词。

方法

interface WordExtractor {
List<ExtractedWord> extractWords(GameState state, PlaceTilesAction action);
}

关键规则

一次落子只会形成：
•   1 个主词
•   0 到多个交叉词

⸻

4. ExtractedWord

作用

中间提取结果对象。

字段

class ExtractedWord {
private String word;
private List<Position> positions;
private WordType wordType;
}


⸻

5. WordValidator

作用

检查词是否在当前词典中。

方法

interface WordValidator {
boolean isValid(String word, DictionaryType dictionaryType);
}


⸻

6. ScoreCalculator

作用

计算本次落子的得分。

方法

interface ScoreCalculator {
ScoreResult calculate(GameState state, PlaceTilesAction action);
}


⸻

7. EndEvaluator

作用
判断是否结束游戏。

结束条件
•	有任何一名玩家达到目标分数（如 100 分）
•	所有活跃玩家都连续跳过（Pass）了一整轮
•	因超时或认输导致只剩一名活跃玩家

⸻

十一、Mode / Policy 层

1. GameMode

enum GameMode {
HOT_SEAT,
HUMAN_VS_AI,
LAN_MULTIPLAYER
}


⸻

2. PlayerController

interface PlayerController {
String getPlayerId();
PlayerControlType getType();
}


⸻

3. PlayerControlType

enum PlayerControlType {
LOCAL_HUMAN,
AI,
REMOTE
}


⸻

4. DictionaryType

enum DictionaryType {
AMERICAN,
BRITISH
}


⸻

5. ClockPhase

enum ClockPhase {
MAIN_TIME,
OVERTIME,
TIMEOUT
}


⸻

6. WordType

enum WordType {
MAIN_WORD,
CROSS_WORD
}


⸻

7. HighlightType

enum HighlightType {
DRAFT_TILE,
MAIN_WORD,
CROSS_WORD,
VALID_DROP_TARGET,
INVALID_DROP_TARGET,
CONFLICT
}


⸻

8. BonusType

enum BonusType {
NONE,
DOUBLE_LETTER,
TRIPLE_LETTER,
DOUBLE_WORD,
TRIPLE_WORD
}


⸻

9. GameEndReason

enum GameEndReason {
TARGET_SCORE_REACHED,          // 达到目标分数（如100分）
ALL_PLAYERS_PASSED,            // 所有玩家跳过一轮
ONLY_ONE_PLAYER_REMAINING,     // 只剩一名活跃玩家
BOARD_FULL                     // 棋盘全满（理论边界保护）
}


⸻

十二、Presentation 层详细类

1. GameController

作用

游戏页控制器。

职责
•   响应拖拽
•   响应提交、撤回、跳过
•   调用 GameApplicationService
•   刷新 GameUiState
•   控制热座遮罩

⸻

2. GameUiState

作用

游戏主页面的 UI 状态对象。

字段建议

class GameUiState {
private String currentPlayerName;
private int estimatedScore;
private boolean submitEnabled;
private boolean hotSeatOverlayVisible;
private String hotSeatOverlayText;
private List<BoardCellUiData> boardCells;
private List<RackTileUiData> rackTiles;
private List<PreviewWordUiData> previewWords;
}

说明

只保留这个 UI 状态对象，不给所有页面都配 ViewModel。

⸻

3. GameSetupController

作用

游戏设定页控制器。

职责
•   读取玩家输入
•   构造 NewGameRequest
•   调用 GameSetupService
•   切到游戏页

⸻

4. ResultController

作用

结算页控制器。

职责
•   展示 SettlementResult
•   返回主菜单
•   重新开始

⸻

5. SettingsController

作用

设置页控制器。

职责
•   音量调节
•   保存 App 设置

⸻

6. BoardView

作用

棋盘组件。

职责
•   显示正式棋盘
•   显示草稿 tile
•   显示高亮
•   支持拖放反馈

⸻

7. RackView

作用

手牌组件。

职责
•   显示当前玩家 rack
•   拖出 tile
•   接收拖回的 tile

⸻

8. PreviewPanel

作用

显示预览词和预计得分。

⸻

9. PlayerClockView

作用

显示玩家时间状态。

⸻

10. HotSeatOverlay

作用

热座交接遮罩。

功能
•   隐藏 rack
•   提示“轮到 Player X”
•   点击开始回合

⸻

十三、Infrastructure 层详细类

1. DictionaryRepository

interface DictionaryRepository {
boolean containsWord(DictionaryType type, String word);
}


⸻

2. SettingsRepository

interface SettingsRepository {
AppSettings load();
void save(AppSettings settings);
}


⸻

3. AppSettings

class AppSettings {
private double musicVolume;
private double sfxVolume;
}


⸻

4. AudioManager

class AudioManager {
void setMusicVolume(double volume);
void setSfxVolume(double volume);
void playClick();
void playPlaceTile();
}


⸻

5. NetworkTransport

interface NetworkTransport {
void send(Message message);
void start();
void stop();
}


⸻

6. GameLogger

作用

记录对局日志与调试信息。

⸻

十四、关键依赖方向

必须守住这个方向，不然架构会长歪：

Presentation → Application → Domain
Application → Infrastructure
Domain 不依赖 Presentation
Domain 不依赖具体 Infrastructure


⸻

十五、关键调用链

1. 拖拽预览

GameController
→ GameApplicationService.placeDraftTile(...)
→ TurnDraft 更新
→ MovePreviewService.preview(...)
→ PreviewResult 返回
→ GameUiState 更新
→ BoardView / PreviewPanel 刷新


⸻

2. 提交落子

GameController
→ GameApplicationService.submitDraft(...)
→ TurnDraft 转 PlaceTilesAction
→ RuleEngine.validate(...)
→ WordExtractor 提取主词和交叉词
→ ScoreCalculator.calculate(...)
→ GameState 更新
→ TurnCoordinator.onDraftSubmitted(...)


⸻

3. 跳过

GameController
→ GameApplicationService.passTurn(...)
→ RoundPassTracker 记录
→ TurnCoordinator.onPass(...)
→ 若全员跳过则结束


⸻

4. 超时

ClockService.tick(...)
→ 当前玩家主时间耗尽进入 OVERTIME
→ overtime 归零
→ handleTimeout(...)
→ PlayerStatus = ELIMINATED_TIMEOUT
→ TurnCoordinator.onTimeout(...)
→ 若只剩一名活跃玩家则结束


⸻

5. 结算

TurnCoordinator 判定结束
→ SettlementService.settle(...)
→ 生成 SettlementResult
→ ResultController 展示


⸻

十六、最值得你们先实现的 12 个类

这 12 个类是最小骨架主梁：

       1.  MainApp
                  2.  AppContext
                             3.  GameConfig
                                        4.  GameSession
                                                   5.  GameState
                                                              6.  Board
                                                                         7.  PlayerState
                                                                                    8.  TurnDraft
                                                                                               9.  GameApplicationService
                                                                                                          10. MovePreviewService
                                                                                                                     11. RuleEngine
                                                                                                                                12. GameController

这些先立起来，你们项目就不是草图，而是已经有了骨架。

⸻

十七、给组员讲的简化版总图

最后给你一版最适合口头讲的图：

App入口
→ MainApp / AppContext

UI层
→ 页面 + 控制器 + 棋盘/手牌组件 + GameUiState

应用层
→ 创建游戏
→ 管理草稿
→ 预览
→ 提交/跳过
→ 计时
→ 结算

规则层
→ 棋盘
→ 玩家
→ 规则验证
→ 提词
→ 计分
→ 结束判定

基础设施层
→ 词典
→ 设置
→ 音频
→ 网络

如果你愿意，我下一条可以继续把这版简化架构整理成 UML 风格文本类图，这样你拿去给组员讲会更丝滑。