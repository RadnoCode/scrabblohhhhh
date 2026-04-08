# UI 集成说明：如何使用 `GameSessionSnapshot`

本文面向 UI 同学，说明当前 non-UI 对局内核如何调用，以及界面应该如何取数和刷新。

## 1. 先说结论

当前 UI 不应该直接读取或持有 `GameState`、`Player`、`Board` 这些 domain 对象。

UI 应该只做两件事：

1. 持有 `GameSession`
2. 通过 `GameApplicationService` 取 `GameSessionSnapshot`

也就是说：

- `GameSession` 是对局句柄
- `GameSessionSnapshot` 是 UI 渲染数据

## 2. 你们要用的核心接口

### 开局

使用：

- `GameSetupService.startNewGame(NewGameRequest request)`

返回：

- `GameSession`

这个 `GameSession` 需要由 UI 层保存，后续所有操作都要把它传回 application service。

### 游戏内操作

使用：

- `GameApplicationService.placeDraftTile(...)`
- `GameApplicationService.moveDraftTile(...)`
- `GameApplicationService.removeDraftTile(...)`
- `GameApplicationService.recallAllDraftTiles(...)`
- `GameApplicationService.submitDraft(...)`
- `GameApplicationService.passTurn(...)`
- `GameApplicationService.tickClock(...)`
- `GameApplicationService.getSessionSnapshot(...)`

## 3. 推荐调用模型

推荐把 `GameSessionSnapshot` 当作 UI 的唯一渲染真相。

也就是：

- 每次动作后，重新取一份 snapshot
- UI 不自己推演棋盘、牌架、当前玩家

推荐模式如下：

1. `startNewGame(...)` 拿到 `GameSession`
2. 立即调用 `getSessionSnapshot(session)` 渲染首屏
3. 用户每次编辑 draft：
   - 调 `place/move/remove/recall`
   - 然后调 `getSessionSnapshot(session)`
   - 用新的 snapshot 重绘
4. 用户提交：
   - 调 `submitDraft(session)`
   - 再调 `getSessionSnapshot(session)`
   - 如果 `SubmitDraftResult.isGameEnded()` 为 `true`，直接切结算页
5. 用户 pass：
   - 调 `passTurn(session)`
   - 再调 `getSessionSnapshot(session)`
6. 时钟轮询：
   - 周期性调 `tickClock(session, elapsedMillis)`
   - 直接使用返回的 `GameSessionSnapshot`

## 4. 为什么不要直接用 `GameState`

原因很简单：

- `GameState` 是内部可变对象
- UI 直接拿 domain state，容易越层读写
- 后续 hot-seat、AI、LAN、可见性裁剪都更适合在 snapshot 层做
- `GameSessionSnapshot` 已经是面向界面的只读投影

所以：

- `state` 是内部真相
- `snapshot` 是 UI 真相

## 5. 当前 `GameSessionSnapshot` 有什么

当前 snapshot 已经足够驱动一个基础游戏界面。

### 基本会话信息

- `sessionId`
- `gameMode`
- `sessionStatus`
- `gameEnded`
- `gameEndReason`
- `turnNumber`

### 当前行动玩家

- `currentPlayerId`
- `currentPlayerName`
- `currentPlayerMainTimeRemainingMillis`
- `currentPlayerByoYomiRemainingMillis`
- `currentPlayerClockPhase`

### 全体玩家信息

- `players`

每个 `GamePlayerSnapshot` 包含：

- `playerId`
- `playerName`
- `playerType`
- `score`
- `active`
- `currentTurn`
- `rackTileCount`
- `clockSnapshot`

### 棋盘

- `boardSnapshot`

`BoardSnapshot` 里有 225 个 `BoardCellSnapshot`，每个 cell 包含：

- `row`
- `col`
- `bonusType`
- `letter`
- `blank`

注意：

- 这里只包含已经正式落盘的内容
- draft 中临时摆上去的 tile 不会写进 `boardSnapshot`

### 当前玩家牌架

- `currentRackTiles`

每个 `RackTileSnapshot` 包含：

- `slotIndex`
- `tileId`
- `letter`
- `score`
- `blank`
- `assignedLetter`

注意：

- 当前只暴露当前玩家牌架
- 其他玩家只给 `rackTileCount`

### 当前 draft

- `draftPlacements`

每个 `DraftPlacementSnapshot` 包含：

- `tileId`
- `row`
- `col`

UI 应该把它作为“浮层 / 待提交棋子”渲染，而不是写进正式棋盘层。

### 当前 preview

- `preview`

当前 `PreviewSnapshot` 包含：

- `valid`
- `estimatedScore`
- `messages`

当前版本还没有把 preview words / highlights 做完整，所以这些先不要假设存在。

### 终局结算

- `settlementResult`

只有在游戏结束后才会非空。

## 6. UI 怎么渲染一帧

推荐把画面拆成 4 层：

1. 棋盘底图层
   - 用 `boardSnapshot.cells`
2. draft 浮层
   - 用 `draftPlacements`
3. 当前玩家牌架
   - 用 `currentRackTiles`
4. 侧边栏 / 顶栏
   - 用 `players`
   - 用 `preview`
   - 用 `sessionStatus`
   - 用 `turnNumber`

## 7. 典型调用流程

### 7.1 开局进入游戏页

```java
GameSession session = gameSetupService.startNewGame(request);
GameSessionSnapshot snapshot = gameApplicationService.getSessionSnapshot(session);
render(snapshot);
```

### 7.2 放一个 draft tile

```java
gameApplicationService.placeDraftTile(session, tileId, new Position(row, col));
GameSessionSnapshot snapshot = gameApplicationService.getSessionSnapshot(session);
render(snapshot);
```

### 7.3 拖动 draft tile

```java
gameApplicationService.moveDraftTile(session, tileId, new Position(row, col));
GameSessionSnapshot snapshot = gameApplicationService.getSessionSnapshot(session);
render(snapshot);
```

### 7.4 撤回 draft

```java
gameApplicationService.recallAllDraftTiles(session);
GameSessionSnapshot snapshot = gameApplicationService.getSessionSnapshot(session);
render(snapshot);
```

### 7.5 提交

```java
SubmitDraftResult result = gameApplicationService.submitDraft(session);
GameSessionSnapshot snapshot = gameApplicationService.getSessionSnapshot(session);
render(snapshot);

if (result.isGameEnded()) {
    showSettlement(result.getSettlementResult());
}
```

### 7.6 Pass

```java
TurnTransitionResult result = gameApplicationService.passTurn(session);
GameSessionSnapshot snapshot = gameApplicationService.getSessionSnapshot(session);
render(snapshot);

if (result.isGameEnded()) {
    showSettlement(result.getSettlementResult());
}
```

### 7.7 时钟轮询

```java
GameSessionSnapshot snapshot = gameApplicationService.tickClock(session, elapsedMillis);
render(snapshot);

if (snapshot.isGameEnded() && snapshot.getSettlementResult() != null) {
    showSettlement(snapshot.getSettlementResult());
}
```

## 8. 哪些返回值该怎么用

### `PreviewResult`

用途：

- 可做交互即时反馈

但不建议把它当 UI 的唯一真相，因为：

- 它不包含棋盘
- 不包含牌架
- 不包含玩家列表
- 不包含 draft 完整信息

推荐：

- 用它做即时 toast / message
- 但最终仍以 `GameSessionSnapshot` 重新渲染

### `SubmitDraftResult` / `TurnTransitionResult`

用途：

- 看动作是否成功
- 看是否终局
- 读 `nextPlayerId`
- 读 `settlementResult`

但它们也不是完整界面数据，不应单独驱动重绘。

推荐：

- 动作成功或失败后，始终再取一次 `GameSessionSnapshot`

## 9. 当前已知限制

UI 同学需要知道当前后端内核还有这些现实限制：

- `confirmHotSeatHandoff(session)` 目前还是 no-op
- preview 只有 `valid / estimatedScore / messages`
- 其他玩家的 rack 内容不会暴露
- `boardSnapshot` 只反映正式落盘，不包含 draft 浮层
- AI / LAN 还没有实际行为

## 10. 当前最适合 UI 的落地方式

建议当前界面层先这样做：

- 启动后只做 `HOT_SEAT`
- 一页主游戏界面
- 每次用户动作后都重新调用 `getSessionSnapshot(session)`
- 用 `draftPlacements` 单独画半透明待提交层
- 用 `preview.messages` 和 `preview.estimatedScore` 做提示
- 游戏结束时直接读 `settlementResult`

这样可以和当前内核保持最稳定的边界，后续就算 domain 继续重构，UI 也只需要适配 snapshot。
