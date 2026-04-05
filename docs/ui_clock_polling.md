# UI 轮询接入说明

这份文档给 UI 同学说明当前版本应该如何读取对局状态，尤其是玩家时钟。

当前结论很简单：

- UI 不直接读 `PlayerClock` 这类 domain 可变对象
- UI 持有 `GameSession`
- UI 通过 `GameApplicationService` 轮询拿 `GameSessionSnapshot`
- 如果这一帧需要推进时钟，用 `tickClock(session, elapsedMillis)`
- 如果这一帧只想刷新展示，不想推进时钟，用 `getSessionSnapshot(session)`

## 1. 你现在能用的接口

建局：

```java
GameSession session = appContext.getGameSetupService().startNewGame(request);
```

轮询：

```java
GameSessionSnapshot snapshot =
        appContext.getGameApplicationService().tickClock(session, elapsedMillis);
```

只读快照：

```java
GameSessionSnapshot snapshot =
        appContext.getGameApplicationService().getSessionSnapshot(session);
```

相关类：

- `GameSetupService`
- `GameApplicationService`
- `GameSession`
- `GameSessionSnapshot`
- `PlayerClockSnapshot`

## 2. 推荐轮询方式

推荐 UI 自己开一个定时器，每 `100ms ~ 250ms` 轮询一次。

不要写死“每次减 1000ms”这种逻辑。正确做法是：

1. UI 启动页面时记录一次本地时间戳
2. 下一次定时器触发时，计算真实经过的毫秒数 `elapsedMillis`
3. 把这个 `elapsedMillis` 传给 `tickClock(session, elapsedMillis)`
4. 用返回的 `GameSessionSnapshot` 直接刷新 UI

伪代码：

```java
GameSession session = appContext.getGameSetupService().startNewGame(request);
GameApplicationService gameApp = appContext.getGameApplicationService();

GameSessionSnapshot firstSnapshot = gameApp.getSessionSnapshot(session);
render(firstSnapshot);

long lastTickNanos = System.nanoTime();

// 这里用你们 UI 框架自己的定时器即可
onEvery200ms(() -> {
    if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
        return;
    }

    long now = System.nanoTime();
    long elapsedMillis = (now - lastTickNanos) / 1_000_000L;
    lastTickNanos = now;

    GameSessionSnapshot snapshot = gameApp.tickClock(session, elapsedMillis);
    render(snapshot);
});
```

## 3. 什么时候调用 `tickClock`

应该调用 `tickClock(session, elapsedMillis)` 的场景：

- 游戏页面已经进入
- 对局状态是 `IN_PROGRESS`
- 你希望当前玩家时钟继续走

应该调用 `getSessionSnapshot(session)` 的场景：

- 页面第一次打开，要先渲染一次
- 只是想刷新展示，不想推进时钟
- 非计时局，只想拿当前玩家和所有玩家信息

简单规则：

- 有计时需求时，主循环里用 `tickClock`
- 非主循环场景下，读一次状态就用 `getSessionSnapshot`

## 4. UI 从 Snapshot 里读什么

`GameSessionSnapshot` 当前可直接给 UI 用的字段：

- `sessionStatus`
- `currentPlayerId`
- `currentPlayerName`
- `currentPlayerMainTimeRemainingMillis`
- `currentPlayerByoYomiRemainingMillis`
- `currentPlayerClockPhase`
- `playerClockSnapshots`

`PlayerClockSnapshot` 当前可直接给 UI 用的字段：

- `playerId`
- `playerName`
- `mainTimeRemainingMillis`
- `byoYomiRemainingMillis`
- `phase`
- `active`

推荐渲染方式：

- 页面顶部的“当前行动玩家”直接用 `currentPlayerName`
- 当前玩家的大时钟直接用 `currentPlayerMainTimeRemainingMillis`
- 如果 `currentPlayerClockPhase == BYO_YOMI`，再显示读秒 `currentPlayerByoYomiRemainingMillis`
- 侧边栏玩家列表用 `playerClockSnapshots`
- 如果 `active == false`，UI 显示为已淘汰/已超时

## 5. Phase 应该怎么显示

当前 `ClockPhase` 有这些值：

- `DISABLED`
- `MAIN_TIME`
- `BYO_YOMI`
- `TIMEOUT`

建议 UI 语义：

- `DISABLED`
  说明这局不限时，可以直接隐藏时钟或显示 `--:--`
- `MAIN_TIME`
  说明还在消耗主时间
- `BYO_YOMI`
  说明主时间已用完，当前显示每手读秒
- `TIMEOUT`
  说明该玩家已经超时

## 6. 页面更新建议

每次拿到新的 `GameSessionSnapshot`，UI 至少更新这几块：

- 当前玩家名字
- 当前玩家主时间
- 当前玩家读秒
- 玩家列表中的每个玩家时钟
- 玩家 active / timeout 状态
- sessionStatus

如果 `sessionStatus == COMPLETED`：

- 停掉 UI 定时器
- 不要再继续调用 `tickClock`
- 后续是否切结果页，由 UI 层自己决定

## 7. 两个容易踩坑的点

### 7.1 不要自己维护一份“剩余时间”

UI 不要本地再存一套剩余时间然后自己减。

正确做法是：

- UI 只负责算“这一帧经过了多少毫秒”
- 真正扣时由 `tickClock` 完成
- UI 始终以返回的 `GameSessionSnapshot` 为准

### 7.2 定时器切后台后要按真实时间补差

如果页面卡顿、窗口切后台、或者定时器不准，下一次回来时 `elapsedMillis` 可能大于 200ms。

这是正常的，不要强行截成固定值。

否则 UI 看到的时间会慢于真实时间。

## 8. 当前版本的边界

当前这套接口只解决：

- 新局启动后 UI 怎么拿到 `GameSession`
- UI 怎么轮询读取当前玩家时钟
- UI 怎么读取所有玩家时钟快照

当前还没完成的部分：

- `placeDraftTile`
- `moveDraftTile`
- `removeDraftTile`
- `recallAllDraftTiles`
- `submitDraft`
- `passTurn`

所以当前文档只覆盖“轮询和展示时钟/当前玩家”这一块。

## 9. 一句话接入流程

UI 接入时按这个顺序做就行：

1. `startNewGame(request)` 拿到 `GameSession`
2. 先 `getSessionSnapshot(session)` 做首帧渲染
3. 开一个 `100ms ~ 250ms` 的 UI 定时器
4. 每次触发时计算真实 `elapsedMillis`
5. 调 `tickClock(session, elapsedMillis)`
6. 用返回的 `GameSessionSnapshot` 全量刷新时钟区
7. `sessionStatus == COMPLETED` 后停掉定时器
