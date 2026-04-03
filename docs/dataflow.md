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
TurnCoordinator.startTurn()
    ↓  getNextPlayer()（跳过 inactive player）
[Domain]
Player
    ↓  PlayerController.requestAction()（阻塞等待动作）

（玩家动作进入）

[Presentation / AI / LAN]
PlayerController.onSubmit / onPass / onLose
    ↓  PlayerAction（PLACE_TILE / PASS_TURN / LOSE）入队

[Application]
TurnCoordinator
    ↓  validateActionOwner()
    ↓  applyAction()
    ↓  RoundTracker.recordTurn(isPass)
    ↓  finalizeRound()

→ 分支：

① 本轮未完成
    ↓  返回本次 PlayerAction
    ↓  下次继续 startTurn()

② 本轮完成且（全员 PASS 或 activePlayerCount = 0）
    ↓  gameEnded = true
    ↓  SettlementService

③ 本轮完成但未终局
    ↓  RoundTracker.startNewRound(activePlayerCount)
    ↓  下一轮 startTurn()

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
