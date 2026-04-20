# Data Flow

这份文档只关注“主要数据如何生成、存放、转换、传播”，不再按页面动作顺序描述 UI 流程。阅读方式建议是：

1. 先看核心数据对象。
2. 再看对象之间的变换关系。
3. 最后看几条最重要的数据通道。

## 1. 核心数据对象

当前项目的主数据不是 Controller，也不是某个单独 Service，而是下面这组对象：

| 对象 | 所在层 | 作用 | 上游来源 | 下游去向 |
| --- | --- | --- | --- | --- |
| `GameLaunchContext` | Presentation/Application 边界 | 承载“从设置页到对局页”的启动参数 | setup controller 的用户输入 | `GameController` / `GameRuntimeFactory` |
| `NewGameRequest` | Application 输入 | 标准化开局请求 | `GameLaunchContext` | `GameSetupServiceImpl` |
| `GameConfig` | Application 会话配置 | 开局后的静态配置 | `GameSetupServiceImpl.buildConfig()` | `GameSession` |
| `GameSession` | Application 核心 | 一局游戏的聚合根 | `GameSetupServiceImpl` | `GameRuntime` / `GameApplicationService` / `GameSessionSnapshotFactory` |
| `GameState` | Domain 核心 | 对局真实状态，含棋盘、玩家、牌袋 | `GameSession` 初始化时创建 | `RuleEngine` / `TurnCoordinator` / `SettlementService` |
| `TurnDraft` | Application 编辑态 | 当前玩家未提交的临时摆放 | `GameSession` 内部持有 | `MovePreviewServiceImpl` / `TurnDraftActionMapper` |
| `PlayerAction` | Domain 命令对象 | 一次有效操作的标准表达 | `TurnDraftActionMapper` 或 pass/lose 构造器 | `RuleEngine` / `TurnCoordinator` |
| `PreviewResult` | Application 派生数据 | 对 `TurnDraft` 的规则校验与估分结果 | `MovePreviewServiceImpl` | `TurnDraft` / `GameSessionSnapshotFactory` |
| `GameActionResult` | Application 动作回执 | 一次提交后的结果摘要 | `GameApplicationServiceImpl.executeAction()` | `GameSession` / `GameController` |
| `GameSessionSnapshot` | Application 读模型 | 给前端渲染的完整快照 | `GameSessionSnapshotFactory` | `GameController` / Renderer |
| `SettlementResult` | Application 结算读模型 | 终局后的排名、摘要、棋盘快照 | `SettlementServiceImpl` | `GameSessionSnapshot` / Settlement UI |
| `AiRuntimeSnapshot` | Application 辅助状态 | AI 运行状态与失败信息 | `LocalAiGameRuntime` | `GameSessionSnapshot` |

## 2. 数据主线

整个项目的主数据链可以压缩成下面这一条：

```text
用户设置
  → GameLaunchContext
  → NewGameRequest
  → GameConfig
  → GameSession
      ├─ GameState
      ├─ TurnDraft
      ├─ latest GameActionResult
      └─ TurnCoordinator / SettlementResult
  → GameSessionSnapshot
  → ViewModel / UI
```

也就是说：

- 写路径主要写入 `GameSession`。
- 读路径主要从 `GameSessionSnapshot` 输出。
- `GameState` 是真实对局状态。
- `TurnDraft` 是未提交编辑态。
- `PreviewResult`、`GameActionResult`、`SettlementResult` 都是围绕 `GameSession` 产生的派生结果。

## 3. 开局阶段的数据变换

### 3.1 从设置项到开局请求

设置页真正产出的核心不是页面跳转，而是 `GameLaunchContext`。

```text
game time / dictionary / player count / difficulty
  ↓
GameLaunchContext
  ↓
NewGameRequest
  - GameMode
  - playerCount
  - playerNames
  - DictionaryType
  - TimeControlConfig
  - AiDifficulty
```

这里完成的转换包括：

- UI label 映射为 `DictionaryType`
- 输入分钟数映射为 `TimeControlConfig`
- 模式映射为 `GameMode`
- 玩家名列表标准化为 `playerNames`

### 3.2 从请求到会话

`GameSetupServiceImpl` 的主要任务是把请求数据展开成完整会话。

```text
NewGameRequest
  ↓ validate + normalize
GameConfig
  ↓
List<Player>
  ↓
GameState
  ↓ initialDraw()
GameSession
```

此阶段的数据增量主要有：

- `playerNames` 变成带 `playerId`、`PlayerType`、`PlayerClock` 的 `Player`
- 配置型数据进入 `GameConfig`
- 玩家列表进入 `GameState`
- `GameState` 再被包装进 `GameSession`
- `GameSession` 自动补齐 `TurnDraft`、`TurnCoordinator`、`sessionId`

## 4. 游戏中的三类数据

游戏运行时最重要的是区分三类数据，它们不能混在一起理解。

### 4.1 真实状态数据

这类数据代表“游戏已经生效”的事实：

- `GameState`
- `Board`
- `Player`
- `Rack`
- `TileBag`
- `PlayerClock`

这部分只应在动作提交成功后被修改。

### 4.2 临时编辑数据

这类数据代表“玩家正在编辑，但还没有提交”：

- `TurnDraft`
- `DraftPlacement`

这部分只影响预览，不直接改动真实棋盘。

### 4.3 面向 UI 的派生数据

这类数据是从真实状态或临时编辑态计算出来的展示模型：

- `PreviewResult`
- `PreviewSnapshot`
- `GameActionResult`
- `GameSessionSnapshot`
- `SettlementResult`
- `AiRuntimeSnapshot`

所以当前系统的核心边界是：

```text
GameState != TurnDraft != GameSessionSnapshot
```

## 5. Draft 数据通道

`TurnDraft` 是当前项目最关键的中间态。

```text
UI drag/drop
  ↓
GameApplicationService
  ↓
DraftManager
  ↓
TurnDraft
```

`TurnDraft` 内部承载的数据本质上是：

- 哪些 tile 被暂时拿出牌架
- 每个 tile 暂时摆在哪个 board position
- 当前这批摆法对应的 `PreviewResult`

它的职责是把“编辑中的一手棋”从 `GameState` 中隔离出来。这样做的结果是：

- 棋盘正式状态保持稳定
- 用户可以反复拖动
- 预览和提交都基于同一个 draft 数据源

## 6. Preview 数据通道

预览流的重点不是“谁调用谁”，而是数据如何被解释。

```text
TurnDraft
  ↓
TurnDraftActionMapper
  ↓
PlayerAction(PLACE_TILE)
  ↓
MovePreviewServiceImpl
  ├─ RuleEngine.validateMove(...)
  ├─ WordExtractor.extract(...)
  └─ ScoreCalculator.calculate(...)
  ↓
PreviewResult
  ↓
TurnDraft.previewResult
  ↓
GameSessionSnapshotFactory
  ↓
PreviewSnapshot
```

这里发生了三层变换：

1. `TurnDraft -> PlayerAction`
   把 UI 临时摆放转成规则层可以理解的动作对象。

2. `PlayerAction -> PreviewResult`
   生成合法性、候选单词、估分、高亮信息。

3. `PreviewResult -> PreviewSnapshot`
   转成前端渲染用的只读结构。

因此预览区显示的并不是 `RuleEngine` 原始输出，而是：

```text
TurnDraft
  → PreviewResult
  → PreviewSnapshot
  → ViewModel
```

## 7. Submit 数据通道

提交时发生的是“临时编辑态向真实状态的折叠”。

```text
TurnDraft
  ↓
TurnDraftActionMapper
  ↓
PlayerAction
  ↓
GameApplicationServiceImpl.executeAction(...)
  ↓
RuleEngine.validateMove(...)
  ↓
RuleEngine.apply(...)
  ↓
GameState mutated
  ↓
TurnCoordinator.onActionApplied(...)
  ↓
GameActionResult
```

提交成功后，主要数据变化有：

| 变化对象 | 变化内容 |
| --- | --- |
| `Board` | 正式写入落子 |
| `Rack` | 对应 tile 被移除 |
| `Player.score` | 加上本次得分 |
| `TileBag` | 用于补牌 |
| `TurnDraft` | 被清空并重建 |
| `GameActionResult` | 记录动作回执 |
| `TurnCoordinator` | 更新回合与终局状态 |

换句话说，submit 的本质不是“按下按钮”，而是：

```text
Draft data
  → Validated command
  → Persistent game state mutation
  → Action result summary
```

## 8. 回合与终局数据通道

回合推进阶段的核心数据并不是 UI，而是 `TurnCoordinator` 内部维护的回合语义。

```text
PlayerAction
  ↓
TurnCoordinator
  ├─ turnNumber
  ├─ RoundTracker state
  ├─ gameEnded
  └─ SettlementResult?
```

`TurnCoordinator` 从动作中提取的不是界面信息，而是：

- 这是否算一手
- 这手是不是 pass
- 本轮是否结束
- 游戏是否结束
- 若结束，对应的 `SettlementResult` 是什么

这里的关键点是：

- `GameState` 保存局面事实
- `TurnCoordinator` 保存局面推进语义

两者不是同一层数据。

## 9. Snapshot 数据通道

当前前端不是直接读取 `GameSession`，而是通过 `GameSessionSnapshotFactory` 组装只读快照。

```text
GameSession
  ├─ GameConfig
  ├─ GameState
  ├─ TurnDraft
  ├─ latestActionResult
  ├─ settlementResult
  └─ aiRuntimeSnapshot
      ↓
GameSessionSnapshotFactory.fromSession(...)
      ↓
GameSessionSnapshot
```

`GameSessionSnapshot` 汇总了多源数据：

| 快照字段来源 | 来源对象 |
| --- | --- |
| 当前玩家、玩家列表、时钟 | `GameState` / `PlayerClock` |
| 棋盘正式落子 | `BoardSnapshotFactory.fromBoard(...)` |
| 棋盘渲染格子 | `Board + TurnDraft + PreviewSnapshot` |
| 当前牌架 | 当前玩家 `Rack` |
| 预览结果 | `TurnDraft.previewResult` |
| 最新动作反馈 | `latestActionResult` |
| 结算信息 | `TurnCoordinator.getSettlementResult()` |
| AI 状态 | `AiRuntimeSnapshot` |

这说明当前 UI 所见数据本质上是一个“聚合读模型”，而不是直接暴露底层对象。

## 10. Settlement 数据通道

终局后，主数据从 `GameState` 转为 `SettlementResult`。

```text
GameState + GameEndReason
  ↓
SettlementServiceImpl.settle(...)
  ↓
SettlementResult
  - rankings
  - summaryMessages
  - BoardSnapshot
```

结算阶段的数据变换重点有三类：

- 排名数据：`List<Player> -> List<PlayerSettlement>`
- 摘要数据：`GameEndReason -> summaryMessages`
- 棋盘数据：`Board -> BoardSnapshot`

因此结算页读到的已经不是原始 `GameState`，而是经过压缩和排序后的结果数据。

## 11. AI 数据通道

AI 模式下，多出来的不是另一套游戏状态，而是一条“候选动作输入通道”。

```text
GameSession
  ↓
AiTurnRuntime
  ↓
AiMoveOptionSet / AiMove
  ↓
AI PlayerController
  ↓
GameApplicationServiceImpl.executeAction(...)
```

AI 数据最终仍会被转回统一的 `PlayerAction -> GameState -> GameActionResult` 链路，因此：

- AI 不拥有独立棋盘状态
- AI 不绕过 `RuleEngine`
- AI 不绕过 `TurnCoordinator`

AI 特有的附加数据只有：

- `AiRuntimeSnapshot`
- AI 候选步与失败信息

这些数据最终也是以附加字段进入 `GameSessionSnapshot`。

## 12. 当前项目最重要的数据边界

如果只保留最关键的边界，当前结构可以总结成四层：

```text
输入层
  GameLaunchContext / NewGameRequest / UI drag-drop

会话层
  GameSession

状态层
  GameState + TurnDraft

输出层
  PreviewResult / GameActionResult / GameSessionSnapshot / SettlementResult
```

其中最重要的规则是：

- 开局输入先进入 `NewGameRequest`
- 游戏编辑先进入 `TurnDraft`
- 正式落子才进入 `GameState`
- UI 永远优先消费 `GameSessionSnapshot`

## 13. 一句话总结

当前项目更准确的数据流表述应该是：

```text
配置数据先生成 GameSession，
运行中的编辑数据先写入 TurnDraft，
提交后才折叠进 GameState，
再由 GameSessionSnapshotFactory 把会话状态重新投影成前端可读快照。
```
