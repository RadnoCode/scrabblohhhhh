# 第一层：应用级流程

[Presentation]
MainMenuController
    ↓  (用户选择)
[Application]
GameSetupService
    ↓  GameConfig
[Application]
GameSetupService
    ↓  GameSession
[Presentation]
GameController

（游戏结束后）

[Application]
SettlementService
    ↓  SettlementResult
[Presentation]
ResultController
    ↓  (用户操作)
[Presentation]
MainMenuController

# 第二层：对局回合推进（Game Loop）

[Application]
TurnCoordinator
    ↓  当前玩家ID
[Application]
GameSession
    ↓  GameState

（玩家动作进入）

[Application]
GameApplicationService
    ↓  Action（Place / Pass / Timeout）

[Application]
TurnCoordinator
    ↓  TurnTransitionResult

→ 分支：

① 继续游戏
    ↓  更新 currentPlayerIndex
    ↓  新回合开始

② 游戏结束
    ↓  GameEndReason
    ↓  SettlementService

# 第三层：落子与规则逻辑

## 3.1 预览流（不修改状态）

[Presentation]
GameController
    ↓  (tile placement)
[Application]
GameApplicationService
    ↓  TurnDraft
[Application]
MovePreviewService
    ↓  (GameState + TurnDraft)
[Domain]
RuleEngine
    ↓  PreviewResult
[Presentation]
GameUiState

## 3.2 提交流（修改状态）

[Application]
GameApplicationService
    ↓  PlaceTilesAction
[Domain]
RuleEngine
    ↓  ValidationResult
    ↓  ScoreResult
    ↓  GameState（new）
[Application]
TurnCoordinator