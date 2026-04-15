# 项目方法级 UML 关系图

本文档基于当前仓库 `src/main/java/com/kotva` 的实际代码整理，目标是把主链路画到“方法级”。

说明：
- 这里优先覆盖真正驱动项目运行的主链路，而不是把全部 192 个 Java 文件都塞进一张无法阅读的大图。
- 图中会同时标出：
  - 类的所属层与职责
  - 方法之间的调用关系
  - 方法之间传输的数据对象
  - 关键拥有关系（owns / creates / reads / returns）

---

## 1. 分层与所有权总图

```mermaid
classDiagram
direction LR

class AppContext {
  +AppContext()
  +ClockService getClockService()
  +SettlementService getSettlementService()
  +DictionaryRepository getDictionaryRepository()
  +SettingsRepository getSettingsRepository()
  +GameRuntimeFactory getGameRuntimeFactory()
}

class SceneNavigator {
  -AppContext appContext
  -GameLaunchContext gameLaunchContext
  -GameController gameController
  +getAppContext() AppContext
  +showHome()
  +showGame(GameLaunchContext)
  +goBack()
  -showPage(PageType, boolean)
  -showGameScene()
  -releaseCurrentPage()
}

class GameScene {
  +GameScene(GameController)
  -createRoot(GameController) Parent
  -loadStyleSheets()
}

class GameController {
  -GameRuntimeFactory gameRuntimeFactory
  -GameLaunchContext launchContext
  -GameViewModel viewModel
  -GameDraftState draftState
  -GameRuntime gameRuntime
  -GameRenderer renderer
  +getViewModel() GameViewModel
  +getDraftState() GameDraftState
  +getSession() GameSession
  +hasSession() boolean
  +refreshFromCurrentSession()
  +bind(GameRenderer, GameInteractionCoordinator)
  -startGameFromLaunchContext()
  -pollSnapshot()
  -renderSnapshot(GameSessionSnapshot)
  -buildPlayerCards(GameSessionSnapshot) List~PlayerCardModel~
  -buildRackTiles(GameSessionSnapshot) List~TileModel~
  -buildBoardTiles(GameSessionSnapshot) List~BoardTileModel~
  -resolveTotalTimerText(GameSessionSnapshot) String
  -resolveStepTimerText(GameSessionSnapshot) String
  -formatDuration(long) String
  -stopPolling()
  +shutdown()
  -refreshSnapshotAfterAction()
  -tickClockBeforeActionIfNeeded()
  -shutdownRuntime()
  -syncAiTurn(GameSessionSnapshot)
  -handleAiMoveCompleted(TurnCompletion)
  +isInteractionLocked() boolean
  +onDraftTilePlaced(String, Position)
  +onDraftTileMoved(String, Position)
  +onDraftTileRemoved(String)
  +onRecallAllDraftTilesRequested()
  +onSubmitDraftRequested()
  +onSkipTurnRequested()
  +onRearrangeRequested()
  +onResignRequested()
}

class GameInteractionCoordinator {
  -BoardView boardView
  -RackView rackView
  -ActionPanelView actionPanelView
  -GameDraftState draftState
  -PreviewRenderer previewRenderer
  -GameRenderer gameRenderer
  -GameActionPort actionPort
  +attach()
  -bindRackInteractions()
  -bindBoardInteractions()
  -bindWorkbenchButtons()
  -handleRackPressed(int, MouseEvent)
  -handleBoardPressed(BoardCoordinate, MouseEvent)
  -handleSceneMouseDragged(MouseEvent)
  -handleSceneMouseReleased(MouseEvent)
  -recallAllDraftTiles()
  -submitDraft()
}

class GameRenderer {
  -BoardRenderer boardRenderer
  -RackRenderer rackRenderer
  -GameViewModel lastViewModel
  +render(GameViewModel)
  +refresh()
  -applyRender(GameViewModel)
}

class GameRuntimeFactory {
  -GameSetupService gameSetupService
  -GameApplicationService gameApplicationService
  +create(NewGameRequest) GameRuntime
}

class GameRuntime {
  <<interface>>
  +start(NewGameRequest)
  +hasSession() boolean
  +getSession() GameSession
  +hasTimeControl() boolean
  +isSessionInProgress() boolean
  +getSessionSnapshot() GameSessionSnapshot
  +tickClock(long) GameSessionSnapshot
  +placeDraftTile(String, Position)
  +moveDraftTile(String, Position)
  +removeDraftTile(String)
  +recallAllDraftTiles()
  +submitDraft()
  +passTurn()
  +shutdown()
}

class AbstractLocalGameRuntime {
  -GameSetupService gameSetupService
  #GameApplicationService gameApplicationService
  -GameSession session
  +start(NewGameRequest)
  +getSessionSnapshot() GameSessionSnapshot
  +tickClock(long) GameSessionSnapshot
  +placeDraftTile(String, Position)
  +moveDraftTile(String, Position)
  +removeDraftTile(String)
  +recallAllDraftTiles()
  +submitDraft()
  +passTurn()
  +shutdown()
  #afterSessionStarted()
  #decorateSnapshot(GameSessionSnapshot) GameSessionSnapshot
}

class LocalAiGameRuntime {
  -AiTurnRuntime aiTurnRuntime
  -AiRuntimeSnapshot aiRuntimeSnapshot
  +hasAutomatedTurnSupport() boolean
  +isCurrentTurnAutomated() boolean
  +requestAutomatedTurnIfIdle(Consumer)
  +matchesAutomatedTurn(TurnCompletion) boolean
  +applyAutomatedTurn(TurnCompletion)
  +disableAutomatedTurnSupport()
  +shutdown()
  #afterSessionStarted()
  #decorateSnapshot(GameSessionSnapshot) GameSessionSnapshot
}

class GameSetupServiceImpl {
  -DictionaryRepository dictionaryRepository
  -ClockService clockService
  -Random random
  +buildConfig(NewGameRequest) GameConfig
  +startNewGame(NewGameRequest) GameSession
  -createSession(GameConfig) GameSession
  -createPlayers(GameConfig) List~Player~
  -createPlayerClock(TimeControlConfig) PlayerClock
  -normalizePlayerName(String) String
  -resolvePlayerType(GameMode, int) PlayerType
}

class GameApplicationServiceImpl {
  -ClockService clockService
  -DraftManager draftManager
  -MovePreviewService movePreviewService
  -DictionaryRepository dictionaryRepository
  +assignLettertoBlank(GameSession, String, char)
  +placeDraftTile(GameSession, String, Position) PreviewResult
  +moveDraftTile(GameSession, String, Position) PreviewResult
  +removeDraftTile(GameSession, String) PreviewResult
  +recallAllDraftTiles(GameSession) PreviewResult
  +submitDraft(GameSession) SubmitDraftResult
  +passTurn(GameSession) TurnTransitionResult
  +tickClock(GameSession, long) GameSessionSnapshot
  +getSessionSnapshot(GameSession) GameSessionSnapshot
  -refreshPreview(GameSession) PreviewResult
  -executeAction(GameSession, PlayerAction) ActionDispatchResult
  -executePlace(GameSession, Player, PlayerAction) ActionDispatchResult
  -executePass(GameSession, Player, PlayerAction) ActionDispatchResult
  -executeLose(GameSession, Player, PlayerAction) ActionDispatchResult
}

class GameSession {
  -String sessionId
  -GameConfig config
  -GameState gameState
  -TurnDraft turnDraft
  -SessionStatus sessionStatus
  -TurnCoordinator turnCoordinator
  +getSessionId() String
  +getConfig() GameConfig
  +getGameState() GameState
  +getTurnDraft() TurnDraft
  +resetTurnDraft()
  +getSessionStatus() SessionStatus
  +setSessionStatus(SessionStatus)
  +getTurnCoordinator() TurnCoordinator
}

class GameSessionSnapshotFactory {
  +fromSession(GameSession) GameSessionSnapshot
  +fromSession(GameSession, AiRuntimeSnapshot) GameSessionSnapshot
  -buildCurrentRackTiles(Player) List~RackTileSnapshot~
  -buildBoardCells(GameSession, Player, PreviewSnapshot) List~BoardCellRenderSnapshot~
  -buildDraftPlacements(GameSession) List~DraftPlacementSnapshot~
  -buildPreviewSnapshot(GameSession) PreviewSnapshot
}

class RuleEngine {
  -DictionaryRepository dictionaryRepository
  +validateMove(GameState, PlayerAction) String
  +apply(GameState, PlayerAction)
}

AppContext --> GameRuntimeFactory : owns
SceneNavigator --> AppContext : reads
SceneNavigator --> GameController : creates / releases
GameScene --> GameController : binds
GameController --> GameRuntimeFactory : create()
GameController --> GameRuntime : owns current runtime
GameController --> GameRenderer : owns
GameController --> GameDraftState : owns
GameInteractionCoordinator --> GameActionPort : dispatches UI action
GameRenderer --> GameDraftState : reads merged UI state
GameRuntimeFactory --> AbstractLocalGameRuntime : creates
AbstractLocalGameRuntime ..|> GameRuntime
LocalAiGameRuntime --|> AbstractLocalGameRuntime
AbstractLocalGameRuntime --> GameSetupServiceImpl : startNewGame()
AbstractLocalGameRuntime --> GameApplicationServiceImpl : gameplay actions
AbstractLocalGameRuntime --> GameSession : owns current session
GameSetupServiceImpl --> GameSession : creates
GameApplicationServiceImpl --> GameSession : mutates / snapshots
GameApplicationServiceImpl --> RuleEngine : validates / applies
GameApplicationServiceImpl --> GameSessionSnapshotFactory : builds snapshot
GameSession --> TurnCoordinator : owns
GameSessionSnapshotFactory --> GameSession : reads
```

---

## 2. 主链路一：启动、建局、进入游戏页

### 2.1 方法级时序图

```mermaid
sequenceDiagram
autonumber
actor User as 用户
participant Setup as SetupController / GameLaunchContext
participant Nav as SceneNavigator
participant Ctx as AppContext
participant GC as GameController
participant RF as GameRuntimeFactory
participant RT as GameRuntime
participant GS as GameSetupServiceImpl
participant Sess as GameSession
participant Snap as GameSessionSnapshotFactory
participant Rend as GameRenderer

User->>Setup: 选择模式/人数/词典/时间
Setup->>Setup: GameLaunchContext.forLocalMultiplayer(...) / forLocalAi(...)
Setup->>Nav: showGame(GameLaunchContext)
Nav->>Nav: showPage(PageType.GAME, true)
Nav->>Nav: showGameScene()
Nav->>GC: new GameController(this, launchContext)
Nav->>Rend: GameScene.createRoot(controller) 内创建 GameRenderer / InteractionCoordinator
Rend->>GC: bind(renderer, interactionCoordinator)
GC->>GC: startGameFromLaunchContext()
GC->>Ctx: getGameRuntimeFactory()
GC->>RF: create(NewGameRequest)
RF-->>GC: HotSeatGameRuntime / LocalAiGameRuntime
GC->>RT: start(NewGameRequest)
RT->>GS: startNewGame(request)
GS->>GS: buildConfig(request)
GS->>GS: createPlayers(config)
GS->>GS: createSession(config)
GS-->>RT: GameSession
GC->>RT: getSessionSnapshot()
RT->>Snap: fromSession(session)
Snap-->>RT: GameSessionSnapshot
RT-->>GC: GameSessionSnapshot
GC->>GC: renderSnapshot(snapshot)
GC->>Rend: render(viewModel)
```

### 2.2 这一段的数据传输

```mermaid
classDiagram
direction TB

class GameLaunchContext {
  +NewGameRequest getRequest()
  +String getModeLabel()
  +String getGameTimeLabel()
  +String getLanguageLabel()
  +String getPlayerCountLabel()
  +String getDifficultyLabel()
}

class NewGameRequest {
  +GameMode getGameMode()
  +int getPlayerCount()
  +List~String~ getPlayerNames()
  +DictionaryType getDictionaryType()
  +TimeControlConfig getTimeControlConfig()
  +AiDifficulty getAiDifficulty()
}

class GameConfig {
  <<value>>
}

class GameSession {
  +GameConfig getConfig()
  +GameState getGameState()
  +TurnDraft getTurnDraft()
  +TurnCoordinator getTurnCoordinator()
}

class GameSessionSnapshot {
  +SessionStatus getSessionStatus()
  +List~GamePlayerSnapshot~ getPlayers()
  +List~BoardCellRenderSnapshot~ getBoardCells()
  +List~RackTileSnapshot~ getCurrentRackTiles()
  +PreviewSnapshot getPreview()
  +SettlementResult getSettlementResult()
}

GameLaunchContext --> NewGameRequest : wraps
NewGameRequest --> GameConfig : buildConfig()
GameConfig --> GameSession : createSession()
GameSession --> GameSessionSnapshot : fromSession()
```

---

## 3. 主链路二：拖拽、预览、提交落子

### 3.1 类与方法关系图

```mermaid
classDiagram
direction LR

class GameActionPort {
  <<interface>>
  +isInteractionLocked() boolean
  +onDraftTilePlaced(String, Position)
  +onDraftTileMoved(String, Position)
  +onDraftTileRemoved(String)
  +onRecallAllDraftTilesRequested()
  +onSubmitDraftRequested()
  +onSkipTurnRequested()
  +onRearrangeRequested()
  +onResignRequested()
}

class GameInteractionCoordinator {
  +attach()
  -handleRackPressed(int, MouseEvent)
  -handleBoardPressed(BoardCoordinate, MouseEvent)
  -handleSceneMouseDragged(MouseEvent)
  -handleSceneMouseReleased(MouseEvent)
  -recallAllDraftTiles()
  -submitDraft()
}

class PreviewRenderer {
  +beginRackDrag(int, TileModel, double, double)
  +beginBoardDrag(BoardCoordinate, TileModel, double, double)
  +update(double, double)
  +clear()
  +hasActiveDrag() boolean
  +isDraggingFromRack() boolean
  +isDraggingFromBoard() boolean
  +getDraggedTileId() String
  +getSuppressedRackIndex() Integer
  +getSuppressedBoardCoordinate() BoardCoordinate
  +getHoveredCoordinate() BoardCoordinate
}

class GameDraftState {
  +syncSnapshot(List~TileModel~, List~BoardTileModel~)
  +getRenderedRackTiles(Integer) List~TileModel~
  +getRenderedBoardTiles(BoardCoordinate) List~BoardTileModel~
  +getRackTileAt(int) TileModel
  +getDraftTileAt(BoardCoordinate) DraftPlacementModel
  +hasDraftPlacements() boolean
  +isCellOccupied(BoardCoordinate, String) boolean
}

class GameController {
  +onDraftTilePlaced(String, Position)
  +onDraftTileMoved(String, Position)
  +onDraftTileRemoved(String)
  +onRecallAllDraftTilesRequested()
  +onSubmitDraftRequested()
  +onSkipTurnRequested()
  -tickClockBeforeActionIfNeeded()
  -refreshSnapshotAfterAction()
}

class AbstractLocalGameRuntime {
  +placeDraftTile(String, Position)
  +moveDraftTile(String, Position)
  +removeDraftTile(String)
  +recallAllDraftTiles()
  +submitDraft()
  +passTurn()
}

class PlayerController {
  +assignLettertoBlank(GameApplicationService, GameSession, String, char)
  +placeDraftTile(GameApplicationService, GameSession, String, Position) PreviewResult
  +moveDraftTile(GameApplicationService, GameSession, String, Position) PreviewResult
  +removeDraftTile(GameApplicationService, GameSession, String) PreviewResult
  +recallAllDraftTiles(GameApplicationService, GameSession) PreviewResult
  +submitDraft(GameApplicationService, GameSession) SubmitDraftResult
  +passTurn(GameApplicationService, GameSession) TurnTransitionResult
}

class GameApplicationServiceImpl {
  +assignLettertoBlank(GameSession, String, char)
  +placeDraftTile(GameSession, String, Position) PreviewResult
  +moveDraftTile(GameSession, String, Position) PreviewResult
  +removeDraftTile(GameSession, String) PreviewResult
  +recallAllDraftTiles(GameSession) PreviewResult
  +submitDraft(GameSession) SubmitDraftResult
  +passTurn(GameSession) TurnTransitionResult
  -refreshPreview(GameSession) PreviewResult
  -executeAction(GameSession, PlayerAction) ActionDispatchResult
}

class DraftManager {
  +placeTile(TurnDraft, String, Position)
  +moveTile(TurnDraft, String, Position)
  +removeTile(TurnDraft, String)
  +recallAllTiles(TurnDraft)
  +findPlacementByTileId(TurnDraft, String) DraftPlacement
}

class MovePreviewServiceImpl {
  +preview(GameSession) PreviewResult
  -validateSafely(GameState, PlayerAction) String
  -extractCandidateWords(GameState, PlayerAction) List~CandidateWord~
  -buildPreviewWords(GameState, PlayerAction, List~CandidateWord~) List~PreviewWord~
}

class TurnDraftActionMapper {
  +toPlaceAction(String, TurnDraft) PlayerAction
}

class RuleEngine {
  +validateMove(GameState, PlayerAction) String
  +apply(GameState, PlayerAction)
}

GameInteractionCoordinator --> GameActionPort : dispatches
GameInteractionCoordinator --> PreviewRenderer : drives drag preview
GameInteractionCoordinator --> GameDraftState : reads local visual state
GameController ..|> GameActionPort
GameController --> AbstractLocalGameRuntime : forwards user intent
AbstractLocalGameRuntime --> PlayerController : requireCurrentPlayerController()
PlayerController --> GameApplicationServiceImpl : delegates
GameApplicationServiceImpl --> DraftManager : edit TurnDraft
GameApplicationServiceImpl --> MovePreviewServiceImpl : recompute preview
GameApplicationServiceImpl --> TurnDraftActionMapper : draft -> action
GameApplicationServiceImpl --> RuleEngine : validate / apply
```

### 3.2 方法级时序图：从拖拽到预览

```mermaid
sequenceDiagram
autonumber
actor User as 用户
participant IC as GameInteractionCoordinator
participant PR as PreviewRenderer
participant DS as GameDraftState
participant GC as GameController
participant RT as GameRuntime
participant PC as PlayerController
participant AS as GameApplicationServiceImpl
participant DM as DraftManager
participant MPS as MovePreviewServiceImpl
participant TD as TurnDraft

User->>IC: 在 rack 按下 tile
IC->>DS: getRackTileAt(rackIndex)
DS-->>IC: TileModel
IC->>PR: beginRackDrag(rackIndex, tileModel, sceneX, sceneY)

User->>IC: 拖动鼠标
IC->>PR: update(sceneX, sceneY)
PR->>PR: boardView.resolveCoordinate(...)

User->>IC: 在棋盘释放
IC->>PR: getHoveredCoordinate()
IC->>PR: getDraggedTileId()
IC->>DS: isCellOccupied(coord, tileId)
IC->>GC: onDraftTilePlaced(tileId, coord.toPosition())

GC->>GC: tickClockBeforeActionIfNeeded()
GC->>RT: placeDraftTile(tileId, position)
RT->>PC: placeDraftTile(gameApplicationService, session, tileId, position)
PC->>AS: placeDraftTile(session, tileId, position)
AS->>DM: placeTile(session.getTurnDraft(), tileId, position)
AS->>AS: refreshPreview(session)
AS->>MPS: preview(session)
MPS->>TD: read placements
MPS->>MPS: TurnDraftActionMapper.toPlaceAction(...)
MPS->>MPS: RuleEngine.validateMove(...)
MPS-->>AS: PreviewResult
AS->>TD: setPreviewResult(previewResult)
AS-->>PC: PreviewResult
PC-->>RT: PreviewResult
RT-->>GC: void
GC->>GC: refreshSnapshotAfterAction()
GC->>RT: getSessionSnapshot()
```

### 3.3 方法级时序图：提交落子

```mermaid
sequenceDiagram
autonumber
actor User as 用户
participant IC as GameInteractionCoordinator
participant GC as GameController
participant RT as GameRuntime
participant PC as PlayerController
participant AS as GameApplicationServiceImpl
participant Map as TurnDraftActionMapper
participant RE as RuleEngine
participant TC as TurnCoordinator
participant CS as ClockServiceImpl
participant Sess as GameSession

User->>IC: 点击 Submit
IC->>GC: onSubmitDraftRequested()
GC->>GC: tickClockBeforeActionIfNeeded()
GC->>RT: submitDraft()
RT->>PC: submitDraft(gameApplicationService, session)
PC->>AS: submitDraft(session)
AS->>Map: toPlaceAction(currentPlayerId, session.getTurnDraft())
Map-->>AS: PlayerAction(type=PLACE_TILE, placements)
AS->>RE: validateMove(session.getGameState(), action)
alt 合法
  AS->>RE: apply(session.getGameState(), action)
  AS->>Sess: resetTurnDraft()
  AS->>CS: stopTurnClock(session)
  AS->>TC: onActionApplied(action)
  TC-->>AS: SettlementResult? / next player advanced
  AS-->>PC: SubmitDraftResult(success, message, awardedScore, nextPlayerId, gameEnded, settlementResult)
else 不合法
  AS-->>PC: SubmitDraftResult(false, validationMessage, 0, currentPlayerId, false, null)
end
PC-->>RT: SubmitDraftResult
RT-->>GC: void
GC->>GC: refreshSnapshotAfterAction()
```

---

## 4. 主链路三：快照构造与 UI 回写

### 4.1 类图

```mermaid
classDiagram
direction LR

class GameController {
  -renderSnapshot(GameSessionSnapshot)
  -buildPlayerCards(GameSessionSnapshot) List~PlayerCardModel~
  -buildRackTiles(GameSessionSnapshot) List~TileModel~
  -buildBoardTiles(GameSessionSnapshot) List~BoardTileModel~
}

class GameSessionSnapshotFactory {
  +fromSession(GameSession) GameSessionSnapshot
  -buildCurrentRackTiles(Player) List~RackTileSnapshot~
  -buildBoardCells(GameSession, Player, PreviewSnapshot) List~BoardCellRenderSnapshot~
  -buildDraftPlacements(GameSession) List~DraftPlacementSnapshot~
  -buildPreviewSnapshot(GameSession) PreviewSnapshot
}

class GameSessionSnapshot {
  +getPlayers() List~GamePlayerSnapshot~
  +getBoardCells() List~BoardCellRenderSnapshot~
  +getCurrentRackTiles() List~RackTileSnapshot~
  +getPreview() PreviewSnapshot
  +getSettlementResult() SettlementResult
  +getAiRuntimeSnapshot() AiRuntimeSnapshot
}

class GameViewModel {
  +setPlayerCards(List~PlayerCardModel~)
  +setBoardTiles(List~BoardTileModel~)
  +setRackTiles(List~TileModel~)
  +setStepTimerText(String)
  +setTotalTimerText(String)
  +setInteractionLocked(boolean)
  +setAiErrorSummary(String)
  +setAiErrorDetails(String)
}

class GameDraftState {
  +syncSnapshot(List~TileModel~, List~BoardTileModel~)
}

class GameRenderer {
  +render(GameViewModel)
  -applyRender(GameViewModel)
}

class BoardRenderer {
  +render()
}

class RackRenderer {
  +render()
}

class BoardView {
  +setTiles(List~BoardTileModel~)
  +setHoveredCell(BoardCoordinate)
}

class RackView {
  +setTiles(List~TileModel~)
}

GameSessionSnapshotFactory --> GameSessionSnapshot : creates
GameController --> GameSessionSnapshot : reads
GameController --> GameViewModel : writes
GameController --> GameDraftState : syncSnapshot()
GameController --> GameRenderer : render()
GameRenderer --> BoardRenderer : render()
GameRenderer --> RackRenderer : render()
BoardRenderer --> GameDraftState : getRenderedBoardTiles()
RackRenderer --> GameDraftState : getRenderedRackTiles()
BoardRenderer --> BoardView : setTiles()
RackRenderer --> RackView : setTiles()
```

### 4.2 数据对象关系图

```mermaid
classDiagram
direction TB

class GameSessionSnapshot {
  +List~GamePlayerSnapshot~ players
  +List~BoardCellRenderSnapshot~ boardCells
  +List~RackTileSnapshot~ currentRackTiles
  +PreviewSnapshot preview
  +SettlementResult settlementResult
}

class GamePlayerSnapshot {
  <<DTO>>
}

class BoardCellRenderSnapshot {
  +int getRow()
  +int getCol()
  +BonusType getBonusType()
  +String getTileId()
  +Character getDisplayLetter()
  +int getScore()
  +boolean isDraft()
  +boolean isPreviewValid()
  +boolean isPreviewInvalid()
  +boolean isMainWordHighlighted()
  +boolean isCrossWordHighlighted()
}

class RackTileSnapshot {
  <<DTO>>
}

class PreviewSnapshot {
  <<DTO>>
}

class SettlementResult {
  +GameEndReason getEndReason()
  +List~PlayerSettlement~ getRankings()
  +List~String~ getSummaryMessages()
  +BoardSnapshot getBoardSnapshot()
}

GameSessionSnapshot --> GamePlayerSnapshot : contains
GameSessionSnapshot --> BoardCellRenderSnapshot : contains
GameSessionSnapshot --> RackTileSnapshot : contains
GameSessionSnapshot --> PreviewSnapshot : contains
GameSessionSnapshot --> SettlementResult : contains
```

---

## 5. 主链路四：计时、回合推进、终局结算

```mermaid
sequenceDiagram
autonumber
participant GC as GameController
participant RT as GameRuntime
participant AS as GameApplicationServiceImpl
participant CS as ClockServiceImpl
participant Sess as GameSession
participant TC as TurnCoordinator
participant Snap as GameSessionSnapshotFactory

loop 每 100ms 轮询
  GC->>GC: pollSnapshot()
  GC->>RT: tickClock(elapsedMillis)
  RT->>AS: tickClock(session, elapsedMillis)
  AS->>CS: tick(session, elapsedMillis)
  alt 当前玩家超时
    AS->>AS: handleTimeoutIfNeeded(session)
    AS->>TC: onActionApplied(PlayerAction.lose(currentPlayerId))
  end
  AS->>Snap: fromSession(session)
  Snap-->>AS: GameSessionSnapshot
  AS-->>RT: GameSessionSnapshot
  RT-->>GC: GameSessionSnapshot
  GC->>GC: renderSnapshot(snapshot)
end
```

### 5.1 关键类关系

```mermaid
classDiagram
direction LR

class ClockServiceImpl {
  +startTurnClock(GameSession)
  +stopTurnClock(GameSession)
  +tick(GameSession, long)
  +handleTimeout(GameSession)
}

class TurnCoordinator {
  -GameState gameState
  -SettlementService settlementService
  -RoundTracker roundTracker
  -EndGameChecker endGameChecker
  -SettlementResult settlementResult
  +onActionApplied(PlayerAction) SettlementResult
  +getNextPlayer() Player
  +getTurnNumber() int
  +isGameEnded() boolean
  +getSettlementResult() SettlementResult
  -endGame(GameEndReason)
}

class SettlementResult {
  +getEndReason() GameEndReason
  +getRankings() List~PlayerSettlement~
  +getSummaryMessages() List~String~
  +getBoardSnapshot() BoardSnapshot
}

class GameApplicationServiceImpl {
  +tickClock(GameSession, long) GameSessionSnapshot
  +passTurn(GameSession) TurnTransitionResult
  +submitDraft(GameSession) SubmitDraftResult
  -executePass(GameSession, Player, PlayerAction) ActionDispatchResult
  -executePlace(GameSession, Player, PlayerAction) ActionDispatchResult
  -executeLose(GameSession, Player, PlayerAction) ActionDispatchResult
}

GameApplicationServiceImpl --> ClockServiceImpl : tick / stopTurnClock
GameApplicationServiceImpl --> TurnCoordinator : onActionApplied()
TurnCoordinator --> SettlementResult : creates when endGame()
```

---

## 6. AI 分支链路

```mermaid
sequenceDiagram
autonumber
participant GC as GameController
participant RT as LocalAiGameRuntime
participant AIR as AiTurnRuntime
participant PC as PlayerController(AI)
participant AS as GameApplicationServiceImpl

GC->>GC: syncAiTurn(snapshot)
alt 当前轮到 AI
  GC->>RT: requestAutomatedTurnIfIdle(completionConsumer)
  RT->>AIR: requestTurnIfIdle(session, controller, completionConsumer)
  AIR-->>RT: TurnCompletion
  GC->>RT: matchesAutomatedTurn(completion)
  GC->>RT: applyAutomatedTurn(completion)
  RT->>PC: applyAutomatedTurn(aiTurnCoordinator, gameApplicationService, session, move)
  PC->>AS: placeDraftTile / submitDraft / passTurn
  AS-->>PC: PreviewResult / SubmitDraftResult / TurnTransitionResult
  RT-->>GC: AI state updated
  GC->>GC: refreshSnapshotAfterAction()
end
```

---

## 7. 类与方法职责索引

| 类 / 方法 | 所属层 | 职责 |
|---|---|---|
| `AppContext` | launcher | 装配服务与运行时工厂 |
| `SceneNavigator.showGame()` | presentation.fx | 切页，并把 `GameLaunchContext` 带入游戏页 |
| `GameController.bind()` | presentation.controller | 绑定渲染与交互，并真正启动对局 |
| `GameController.renderSnapshot()` | presentation.controller | 把 `GameSessionSnapshot` 转成 `GameViewModel` |
| `GameInteractionCoordinator.handleSceneMouseReleased()` | presentation.interaction | 决定释放时是“落到棋盘”还是“取消预览” |
| `PreviewRenderer.beginRackDrag()/beginBoardDrag()` | presentation.renderer | 创建跟手拖拽的视觉层 |
| `GameDraftState.syncSnapshot()` | presentation.interaction | 把 snapshot 投影成 UI 本地可命中的只读状态 |
| `GameRuntimeFactory.create()` | application.runtime | 按模式选择 HotSeat / LocalAi runtime |
| `AbstractLocalGameRuntime.start()` | application.runtime | 调用 `GameSetupService` 启动会话 |
| `AbstractLocalGameRuntime.placeDraftTile()` | application.runtime | 把运行时动作转交给当前玩家控制器 |
| `PlayerController.placeDraftTile()` | mode | 统一本地玩家 / AI 的动作适配入口 |
| `GameApplicationServiceImpl.placeDraftTile()` | application.service | 修改 `TurnDraft`，并刷新预览 |
| `GameApplicationServiceImpl.submitDraft()` | application.service | `TurnDraft -> PlayerAction -> RuleEngine -> TurnCoordinator` |
| `DraftManager` | application.draft | 维护本回合草稿 placements |
| `TurnDraftActionMapper.toPlaceAction()` | application.draft | 把草稿转换成正式领域动作 |
| `MovePreviewServiceImpl.preview()` | application.service | 生成合法性、分数、主词/副词、高亮和消息 |
| `RuleEngine.validateMove()` | domain | 校验一手棋是否合法 |
| `RuleEngine.apply()` | domain | 把合法动作真正写回 `GameState` |
| `ClockServiceImpl.tick()` | application.service | 推进主时间 / 读秒，并在超时时标记 TIMEOUT |
| `TurnCoordinator.onActionApplied()` | application | 推进轮次、换手、判定终局、生成结算 |
| `GameSessionSnapshotFactory.fromSession()` | application.session | 把可变会话压平成 UI 只读快照 |
| `GameRenderer.applyRender()` | presentation.renderer | 把 `GameViewModel` 回写到 `BoardView/RackView/PlayerCard` |

---

## 8. 读图顺序建议

1. 先看“分层与所有权总图”，理解谁拥有谁。
2. 再看“主链路一”，理解建局是怎么进入 `GameSession` 的。
3. 然后看“主链路二”，这是交互最密集、最容易出 bug 的地方。
4. 接着看“主链路三”，理解为什么 UI 不直接读 `GameState`。
5. 最后看“主链路四”和“AI 分支链路”，理解计时、终局、AI 的特殊状态推进。

---

## 9. 文档边界

本文档当前精确覆盖的是“项目主运行链路”的方法级关系，已经足够支撑你排查大多数问题，例如：
- 拖拽为什么没有回 rack
- preview 为什么没有显示
- submit 为什么没有错误提示
- bonus 为什么没渲染
- 计时为什么在 skip/终局后异常

如果你还要，我下一步可以继续补两份更细的文件：
- `docs/uml-presentation-only.md`
  只画 `presentation.*`，适合前端同学
- `docs/uml-domain-rules.md`
  只画 `RuleEngine / MoveValidator / WordExtractor / ScoreCalculator / TurnCoordinator`
