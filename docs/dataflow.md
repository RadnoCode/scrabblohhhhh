# Data Flow

这份文档基于当前最新代码整理，重点描述“核心数据对象如何生成、归属、变换、同步”，尤其覆盖现在已经成型的三类运行模式：

- 本地热座 / 本地 AI
- LAN Host
- LAN Client

当前版本里，项目的数据流已经不再是单一的 `GameSession -> UI`。更准确的说法是：

```text
启动数据
  → RuntimeLaunchSpec / GameLaunchContext
  → GameRuntime

权威状态数据
  → GameSession / GameState

本地编辑态数据
  → TurnDraft

可传输读模型
  → GameSessionSnapshot

附加运行态数据
  → AiRuntimeSnapshot / ClientRuntimeSnapshot
```

## 1. 核心数据对象

| 对象 | 层 | 角色 | 谁拥有它 | 谁消费它 |
| --- | --- | --- | --- | --- |
| `GameLaunchContext` | Presentation/Application 边界 | 场景启动上下文 | setup controller / room controller | `GameController` |
| `RuntimeLaunchSpec` | Application runtime | 标准化运行时启动参数 | `GameLaunchContext` | `GameRuntimeFactory` |
| `NewGameRequest` | Application setup | 开局请求 | `GameLaunchContext` / `RuntimeLaunchSpec` | `GameSetupServiceImpl` |
| `GameRuntime` | Application runtime | 一局游戏在某种运行模式下的外壳 | `GameRuntimeFactory` 或外部直接提供 | `GameController` |
| `GameSession` | Application session | 权威会话聚合根 | 本地 runtime / host runtime / tutorial runtime | `GameApplicationService` / `GameSessionSnapshotFactory` |
| `GameState` | Domain | 权威对局状态 | `GameSession` | `RuleEngine` / `TurnCoordinator` / `SettlementService` |
| `TurnDraft` | Application draft | 本地未提交编辑态 | `GameSession` 或 `ClientDraftService` | preview / submit / snapshot projection |
| `PlayerAction` | Domain command | 标准动作命令 | `TurnDraftActionMapper`、pass、resign、LAN command | `GameApplicationServiceImpl` / host |
| `PreviewResult` | Application preview | draft 的规则校验与估分结果 | `MovePreviewService` / `ClientPreviewService` | `TurnDraft` / snapshot |
| `GameActionResult` | Application result | 一次动作落地后的回执 | `GameApplicationServiceImpl` | `GameSession` / `GameSessionSnapshot` |
| `GameSessionSnapshot` | Application read model | 统一前端读模型，也是 LAN 传输载体 | `GameSessionSnapshotFactory` 或 host 广播 | `GameController` / LAN client |
| `SettlementResult` | Application result | 终局结算读模型 | `SettlementServiceImpl` | `GameSessionSnapshot` / Settlement UI |
| `AiRuntimeSnapshot` | Application session | AI 运行状态 | `LocalAiGameRuntime` | `GameSessionSnapshot` |
| `ClientRuntimeSnapshot` | Application session | LAN 客户端运行状态 | `ClientGameRuntime` / `LanClientService` / host viewer decorator | `GameSessionSnapshot` |
| `ClientGameContext` | Application client | LAN client 的本地上下文 | `ClientGameRuntime` | `LanClientService` / `ClientDraftService` |

## 2. 数据归属边界

当前系统最重要的不是调用关系，而是“哪类数据由谁拥有”。

### 2.1 权威状态

权威状态只存在于能真正推进对局的一侧：

- `GameSession`
- `GameState`
- `TurnCoordinator`
- `SettlementResult`

拥有权：

- 本地热座：本地 runtime
- 本地 AI：本地 runtime
- LAN Host：host runtime
- 教程：tutorial runtime

### 2.2 本地编辑态

编辑态用于“正在摆，但尚未提交”的操作：

- `TurnDraft`
- `PreviewResult`

拥有权：

- 本地模式：`GameSession` 内部持有 `TurnDraft`
- LAN Client：`ClientDraftService` 单独持有本地 `TurnDraft`

### 2.3 只读投影

前端读到的不是权威状态本体，而是投影后的只读模型：

- `GameSessionSnapshot`
- `PreviewSnapshot`
- `BoardCellRenderSnapshot`
- `ClientRuntimeSnapshot`
- `AiRuntimeSnapshot`

所以新版结构下最关键的边界是：

```text
权威状态 != 本地编辑态 != UI 读模型
```

也就是：

```text
GameState != TurnDraft != GameSessionSnapshot
```

## 3. 启动数据流

### 3.1 `GameLaunchContext` 已扩展为多入口

现在启动游戏不再只有 `NewGameRequest` 这一条入口。`GameLaunchContext` 可以承载三类启动数据：

```text
GameLaunchContext
  ├─ request
  ├─ launchSpec
  └─ providedRuntime
```

对应三种来源：

| 启动方式 | 入口数据 |
| --- | --- |
| 本地多人 / 本地 AI | `RuntimeLaunchSpec.forLocal(request)` |
| LAN Host / LAN Client | `RuntimeLaunchSpec.forLanHost(...)` / `RuntimeLaunchSpec.forLanClient(...)` |
| 已建好的运行时直接进入游戏页 | `providedRuntime` |

### 3.2 启动阶段的标准变换

```text
UI setup input
  ↓
GameLaunchContext
  ↓
RuntimeLaunchSpec / NewGameRequest / providedRuntime
  ↓
GameRuntimeFactory.create(...)
  ↓
GameRuntime
```

因此现在“启动数据”这一层已经从旧版本的：

```text
GameLaunchContext -> NewGameRequest
```

扩展成：

```text
GameLaunchContext -> RuntimeLaunchSpec -> GameRuntime
```

## 4. 本地与 Host 的权威建局流

凡是能真正创建 `GameSession` 的运行时，底层仍然走 `GameSetupServiceImpl`。

```text
NewGameRequest
  ↓
GameSetupServiceImpl.buildConfig(...)
  ↓
GameConfig
  ↓
Player + PlayerController + PlayerClock
  ↓
GameState
  ↓ initialDraw()
GameSession
  ├─ TurnDraft
  ├─ TurnCoordinator
  ├─ latestActionResult
  └─ settlementResult
```

这里的主要数据增量：

- 请求型数据被标准化成 `GameConfig`
- 玩家名列表被扩展成 `Player`
- 规则状态被装入 `GameState`
- 会话级控制数据被装入 `GameSession`

目前会产生权威 `GameSession` 的 runtime 包括：

- `HotSeatGameRuntime`
- `LocalAiGameRuntime`
- `HostGameRuntime`
- `LobbyHostGameRuntime`
- `TutorialGameRuntime`

## 5. 客户端模式的数据流

`ClientGameRuntime` 是新版里最重要的结构变化之一，因为它不拥有 `GameSession`。

### 5.1 Client 不持有权威状态

`ClientGameRuntime` 的关键特征是：

- `getSession()` 返回 `null`
- 本地状态基于 `ClientGameContext`
- UI 读取的是客户端生成的 `GameSessionSnapshot`

也就是说 LAN client 的核心不是：

```text
GameSession -> snapshot
```

而是：

```text
authoritative remote snapshot
  → ClientGameContext
  → local TurnDraft overlay
  → UI snapshot
```

### 5.2 Client 的本地数据对象

LAN client 侧主要多出这几个对象：

| 对象 | 作用 |
| --- | --- |
| `ClientGameContext` | 保存最近一次权威快照、本地玩家 id、时钟预测所需信息 |
| `ClientDraftService` | 在客户端维护本地 draft |
| `ClientPreviewService` | 基于快照重建 `GameState` 做本地预览 |
| `LanClientService` | 发送命令、接收 host 快照和命令结果 |
| `ClientRuntimeSnapshot` | 向 UI 暴露“等待 host 确认 / 已断线 / 已锁定”等客户端运行状态 |

### 5.3 Client 的主数据链

```text
remote GameSessionSnapshot
  ↓
ClientGameContext.updateSnapshot(...)
  ↓
ClientDraftService.withLocalDraft(...)
  ↓
GameSessionSnapshotFactory.withLocalDraft(...)
  ↓
UI snapshot
```

这一条链说明：

- host 发来的快照是权威基线
- client 自己的 draft 是本地覆盖层
- UI 最终看到的是“权威快照 + 本地 draft overlay”

## 6. Draft 数据流

新版里 `TurnDraft` 已经分成两种归属方式，但数据形态是一致的。

### 6.1 本地模式

```text
UI drag/drop
  ↓
GameApplicationService
  ↓
DraftManager
  ↓
GameSession.turnDraft
```

### 6.2 LAN Client 模式

```text
UI drag/drop
  ↓
LanClientService
  ↓
ClientDraftService
  ↓
DraftManager
  ↓
local TurnDraft
```

不管在哪种模式，`TurnDraft` 的语义都相同：

- 暂存落子位置
- 暂存从牌架取出的 tile
- 记录该 draft 的 `PreviewResult`

## 7. Preview 数据流

### 7.1 本地 / Host 预览

本地和 host 侧，预览直接基于权威 `GameSession` 生成：

```text
TurnDraft
  ↓
TurnDraftActionMapper
  ↓
PlayerAction
  ↓
MovePreviewService
  ↓
PreviewResult
  ↓
TurnDraft.previewResult
```

预览计算需要的核心输入是：

- 当前 `GameState`
- 当前玩家 id
- 当前 `TurnDraft`
- 当前词典类型

### 7.2 LAN Client 预览

LAN client 不能直接访问 host 的 `GameSession`，所以要先从快照重建预览态：

```text
latest authoritative snapshot
  ↓
ClientPreviewStateFactory.fromSnapshot(...)
  ↓
preview GameState
  ↓
MovePreviewService.preview(...)
  ↓
PreviewResult
  ↓
local TurnDraft.previewResult
```

这个变化非常关键，因为它意味着：

- client 的 preview 是本地计算的
- 但基准局面来自 host 下发的权威快照
- 真正提交时仍需 host 再次验证

## 8. Submit 与命令数据流

### 8.1 本地 / Host 提交

本地与 host 的提交本质上是把 draft 折叠进权威状态：

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

提交后会更新的权威数据包括：

- `Board`
- `Rack`
- `Player.score`
- `TileBag`
- `TurnDraft`
- `latestActionResult`
- `TurnCoordinator`

### 8.2 LAN Client 提交

client 提交不会直接修改权威状态，而是先发命令：

```text
local TurnDraft
  ↓
ClientDraftService.buildSubmitAction()
  ↓
PlayerAction
  ↓
PlayerController.buildLanCommand(...)
  ↓
CommandEnvelope
  ↓
LanClientTransport.sendCommand(...)
```

之后等待 host 返回：

```text
RemoteCommandResult
  ├─ success/failure
  ├─ message
  └─ snapshot
```

如果 host 返回新快照：

- 成功时：客户端清空本地 draft，接受权威新状态
- 失败时：可选择保留本地 draft，继续编辑同一回合

因此 client 的 submit 本质是：

```text
local draft
  → remote command
  → host validation
  → authoritative snapshot replacement
```

## 9. Snapshot 数据流

`GameSessionSnapshot` 现在已经不只是前端 DTO，而是整个系统统一的只读交换格式。

### 9.1 Snapshot 的来源

当前 snapshot 有三种主要生成方式：

```text
GameSessionSnapshotFactory.fromSession(...)
GameSessionSnapshotFactory.fromSessionForViewer(...)
GameSessionSnapshotFactory.withLocalDraft(...)
```

三者含义不同：

| 方法 | 作用 |
| --- | --- |
| `fromSession(...)` | 从权威会话生成标准快照 |
| `fromSessionForViewer(...)` | 按 viewer 身份裁剪可见信息，主要用于 LAN host 向不同玩家广播 |
| `withLocalDraft(...)` | 在基准快照上叠加本地 draft，主要用于 LAN client UI |

### 9.2 Snapshot 的组成

现在的 `GameSessionSnapshot` 已经聚合了：

- `BoardSnapshot`
- `boardCells`
- `currentRackTiles`
- `draftPlacements`
- `PreviewSnapshot`
- `TutorialSnapshot`
- `GameActionResult`
- `SettlementResult`
- `AiRuntimeSnapshot`
- `ClientRuntimeSnapshot`
- `snapshotSentAtEpochMillis`
- `snapshotReceivedAtEpochMillis`

这说明它已经是一个：

```text
状态 + 可见性裁剪 + 运行时附加信息 + 网络时间信息
```

的复合读模型。

### 9.3 Host 侧 viewer snapshot

LAN host 不会把同一份原始快照无差别发给所有人，而是会按 viewer 重建：

```text
GameSession
  ↓
GameSessionSnapshotFactory.fromSessionForViewer(session, viewerPlayerId)
  ↓
viewer-specific GameSessionSnapshot
```

这里最重要的语义是：

- 当前轮到谁，谁才能看到自己的 draft / 当前手信息
- 非当前玩家拿到的是裁剪后的 viewer 快照

## 10. 客户端运行态数据流

新版 `ClientRuntimeSnapshot` 解决的是“客户端 UI 该如何表达非棋盘状态”。

它承载的不是对局规则数据，而是客户端运行状态：

- 是否锁定交互
- 是否存在 pending command
- 当前提示 summary
- 当前详情 details

典型来源有两类：

### 10.1 Client 自己生成

```text
pendingCommandId / disconnected / local status
  ↓
LanClientService.buildRuntimeSnapshot()
  ↓
ClientRuntimeSnapshot
  ↓
GameSessionSnapshotFactory.withClientRuntimeSnapshot(...)
```

### 10.2 Host viewer 注入

host 也会在 viewer 快照上注入客户端运行态提示，例如：

- 当前等待远端玩家行动
- 当前被系统 notice 锁定

所以 `ClientRuntimeSnapshot` 已经不再是“client only”字段，而是统一的前端运行态通道。

## 11. AI 数据流

AI 路径本身没变，但现在它和 `ClientRuntimeSnapshot` 一样，也属于 snapshot 的附加运行态字段。

```text
GameSession
  ↓
AiTurnRuntime
  ↓
AiMoveOptionSet / AiMove
  ↓
GameApplicationServiceImpl.executeAction(...)
  ↓
GameState mutation
  ↓
AiRuntimeSnapshot
  ↓
GameSessionSnapshot
```

因此 AI 和 LAN client 的共同点是：

- 都会在 `GameSessionSnapshot` 上附加运行态信息

不同点是：

- AI 改变动作生产方式
- LAN client 改变状态持有方式

## 12. 结算数据流

结算主线仍然是从权威状态投影到读模型：

```text
GameState + GameEndReason
  ↓
SettlementServiceImpl.settle(...)
  ↓
SettlementResult
  ├─ rankings
  ├─ summaryMessages
  └─ BoardSnapshot
```

之后结算结果被挂到：

- 本地 / host：`GameSession` -> `GameSessionSnapshot`
- client：通过 host 发来的最终 snapshot 同步过来

所以 client 不会独立计算最终结算，它只接收权威结算投影。

## 13. 当前结构下的四条主数据通道

### 13.1 启动通道

```text
UI setup
  → GameLaunchContext
  → RuntimeLaunchSpec / providedRuntime
  → GameRuntime
```

### 13.2 权威状态通道

```text
NewGameRequest
  → GameSession
  → GameState
  → GameActionResult / SettlementResult
```

### 13.3 本地编辑通道

```text
UI interaction
  → TurnDraft
  → PreviewResult
```

### 13.4 同步与展示通道

```text
GameSession or remote snapshot
  → GameSessionSnapshot
  → ClientRuntimeSnapshot / AiRuntimeSnapshot
  → GameController
  → ViewModel / UI
```

## 14. 一句话总结

当前最新版代码下，最准确的 data flow 表述应该是：

```text
RuntimeLaunchSpec 决定运行时类型；
权威对局状态只存在于本地/host 的 GameSession 中；
编辑中的落子先进入 TurnDraft；
LAN client 只持有“权威快照 + 本地 draft 覆盖层”；
最终所有模式都收敛到统一的 GameSessionSnapshot 供 UI 和网络同步使用。
```
