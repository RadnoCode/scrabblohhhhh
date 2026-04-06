# 当前代码库全流程与 Gap Analysis

基于 `2026-04-06` 当前仓库真实代码整理。

本次判断以 `src/main/java`、`src/test/java`、`pom.xml` 为准；架构文档只作为历史参考，不作为现状依据。

## 1. 结论先行

当前代码已经不再是“纯骨架”。

现在已经存在一条真实的应用主链：

- 开局配置与 `GameSession` 创建
- 回合内 draft 编辑
- preview 校验与估分
- `submitDraft / passTurn / timeout`
- domain action 执行
- `TurnCoordinator` 回合推进
- `EndGameChecker` 终局判定
- `SettlementServiceImpl` 结算结果生成

但它仍然**不是一个可以从启动入口直接玩起来的 Scrabble 游戏**。当前最关键的差距不再是“完全没主链”，而是下面这几类：

- 启动层和表现层没有接通，`MainApp -> AppLauncher` 之后没有真正的游戏界面或控制器
- 面向 UI 的会话快照过于贫瘠，拿不到棋盘、牌架、分数、draft、preview
- 规则层仍有关键漏洞，尤其是“tile 所有权 / 连续性 / blank tile / 终局规则完整性”
- `PlayerController` 现在只是转发器，不是真正区分 `LOCAL / AI / LAN` 的动作来源实现
- 热座换手、结果导航、应用级编排还有占位符和断链

如果目标是“最小可玩 hot-seat Scrabble”，现在距离目标已经明显更近，但还没有到“可直接运行并交互”的程度。

## 2. 本次检查的依据

本次检查重点看了这些代码：

- 启动与装配
  - `src/main/java/com/kotva/launcher/MainApp.java`
  - `src/main/java/com/kotva/launcher/AppLauncher.java`
  - `src/main/java/com/kotva/launcher/AppContext.java`
- 开局与会话
  - `src/main/java/com/kotva/application/service/GameSetupServiceImpl.java`
  - `src/main/java/com/kotva/application/session/GameSession.java`
  - `src/main/java/com/kotva/application/session/GameSessionSnapshot.java`
- 回合主链
  - `src/main/java/com/kotva/application/service/GameApplicationServiceImpl.java`
  - `src/main/java/com/kotva/application/TurnCoordinator.java`
  - `src/main/java/com/kotva/application/RoundTracker.java`
- draft / preview
  - `src/main/java/com/kotva/application/draft/DraftManager.java`
  - `src/main/java/com/kotva/application/draft/TurnDraft.java`
  - `src/main/java/com/kotva/application/draft/TurnDraftActionMapper.java`
  - `src/main/java/com/kotva/application/preview/PreviewResult.java`
- domain 规则与终局
  - `src/main/java/com/kotva/domain/RuleEngine.java`
  - `src/main/java/com/kotva/domain/utils/MoveValidator.java`
  - `src/main/java/com/kotva/domain/utils/WordExtractor.java`
  - `src/main/java/com/kotva/domain/utils/ScoreCalculator.java`
  - `src/main/java/com/kotva/domain/endgame/EndGameChecker.java`
- 结算
  - `src/main/java/com/kotva/application/service/SettlementServiceImpl.java`
- 动作来源
  - `src/main/java/com/kotva/mode/PlayerController.java`
- 测试
  - `src/test/java/com/kotva/application/service/GameApplicationServiceImplSubmitDraftTest.java`
  - `src/test/java/com/kotva/application/TurnCoordinatorTest.java`
  - `src/test/java/com/kotva/application/service/ClockServiceImplTest.java`
  - `src/test/java/com/kotva/application/service/GameSetupServiceImplTest.java`
  - `src/test/java/com/kotva/domain/endgame/EndGameCheckerTest.java`

另外我还实际执行了构建验证：

- `mvn test`

结果不是单元测试失败，而是 Maven 当前绑定到了 JDK 17，而 `pom.xml` 要求 `maven.compiler.release=25`，因此编译阶段直接失败。

## 3. 当前真实全流程

### 3.1 启动与装配

当前启动路径是：

`MainApp.main()`  
-> new `AppContext()`  
-> new `AppLauncher(appContext)`  
-> `AppLauncher.launch()`

真实状态：

- `MainApp` 只负责 new context 和调用 launcher
- `AppContext` 会创建：
  - `ClockServiceImpl`
  - `SettlementServiceImpl`
  - `DictionaryRepository`
  - `SettingsRepository`
  - `GameApplicationServiceImpl`
  - `GameSetupServiceImpl`
- `AppLauncher.launch()` 目前是空方法

这意味着：

- 依赖容器已经存在
- 但从启动入口并不会创建对局，也不会打开 UI，也不会进入任何回合主链

### 3.2 开局

当前开局主链在 `GameSetupServiceImpl`。

真实流程：

1. `buildConfig(NewGameRequest)`
2. 校验模式、人数、字典、玩家名
3. 只接受 `GameMode.HOT_SEAT`
4. 把所有玩家都转成 `PlayerType.LOCAL`
5. `startNewGame(request)`
6. `createSession(config)`
7. 加载字典
8. 创建 `Player`
9. 给每个 `Player` 挂一个 `PlayerController`
10. 创建 `PlayerClock`
11. 随机打乱玩家顺序
12. 创建 `GameState`
13. `initialDraw()` 初始抽 7 张
14. 创建 `GameSession`
15. `sessionStatus = IN_PROGRESS`
16. 如果有时控，启动当前玩家时钟

这条链已经是可工作的，不再是空壳。

### 3.3 回合内 draft 编辑

当前 draft 编辑主链是：

`PlayerController`  
-> `GameApplicationServiceImpl.place/move/remove/recall`  
-> `DraftManager`  
-> `refreshPreview(session)`

四个编辑动作都已经实现：

- `placeDraftTile`
- `moveDraftTile`
- `removeDraftTile`
- `recallAllDraftTiles`

`DraftManager` 当前只操作 `TurnDraft`：

- 改 placements
- 清 dragging tile
- 清 preview

它不会直接改：

- `Board`
- `Rack`
- `Score`
- 当前玩家
- 回合状态

这部分边界现在是清楚的。

### 3.4 Preview

当前 preview 并不是独立服务实现，而是内嵌在 `GameApplicationServiceImpl.refreshPreview()`。

真实流程：

1. 当前 `TurnDraft`
2. `TurnDraftActionMapper.toPlaceAction(...)`
3. 生成 domain `PlayerAction`
4. `RuleEngine.validateMove(...)`
5. 如果非法：
   - `PreviewResult(valid=false, estimatedScore=0, messages=[...])`
6. 如果合法：
   - `WordExtractor.extract(...)`
   - `ScoreCalculator.calculate(...)`
   - `PreviewResult(valid=true, estimatedScore=...)`
7. 写回 `session.getTurnDraft().setPreviewResult(...)`

也就是说，preview 和 submit 已经共享同一套 domain 输入模型了，这是这版代码的一个实质进展。

### 3.5 提交、pass、timeout

当前三类动作已经统一为 domain action：

- `PLACE_TILE`
- `PASS_TURN`
- `LOSE`

统一分发入口在 `GameApplicationServiceImpl.executeAction(...)`。

#### 提交 draft

流程：

1. `submitDraft(session)`
2. 取当前 active player
3. `TurnDraft -> PlayerAction.place(...)`
4. `executeAction(session, action)`
5. `executePlace(...)`
6. `RuleEngine.validateMove(...)`
7. `WordExtractor.extract(...)`
8. `ScoreCalculator.calculate(...)`
9. `RuleEngine.apply(...)`
10. `currentPlayer.addScore(...)`
11. `refillRack(...)`
12. `session.resetTurnDraft()`
13. `clockService.stopTurnClock(session)`
14. `session.getTurnCoordinator().onActionApplied(action)`
15. 若未终局，`clockService.startTurnClock(session)`
16. 返回 `SubmitDraftResult`

#### pass

流程：

1. `passTurn(session)`
2. `PlayerAction.pass(...)`
3. `executeAction(session, action)`
4. `executePass(...)`
5. `RuleEngine.apply(...)`
6. 清空 draft
7. 停钟
8. `TurnCoordinator.onActionApplied(action)`
9. 未终局则开下一手钟
10. 返回 `TurnTransitionResult`

#### timeout

流程：

1. `tickClock(session, elapsedMillis)`
2. `clockService.tick(...)`
3. 如果当前玩家 clock phase 变成 `TIMEOUT`
4. `handleTimeoutIfNeeded(...)`
5. `PlayerAction.lose(...)`
6. `executeAction(session, action)`
7. `executeLose(...)`
8. `RuleEngine.apply(...)`
9. 清 draft
10. 停钟
11. `TurnCoordinator.onActionApplied(action)`
12. 未终局则推进下一位

这里最重要的结构性变化是：

- timeout 不再自己在 `ClockServiceImpl` 里直接结算整局
- submit / pass / timeout 都会汇到统一 action 消费主链

### 3.6 回合推进与终局

当前回合推进在 `TurnCoordinator`。

真实逻辑：

1. 每次 action 执行完成后，调用 `onActionApplied(PlayerAction action)`
2. `turnNumber++`
3. `RoundTracker.recordTurn(action.type() == PASS_TURN)`
4. 计算：
   - `roundComplete`
   - `allPassedInRound`
5. 调 `EndGameChecker.evaluate(...)`
6. 若命中终局：
   - `gameState.markGameOver(reason)`
   - `settlementService.settle(gameState, reason)`
7. 若未终局：
   - 如果 round complete，则 `roundTracker.startNewRound(...)`
   - `gameState.advanceToNextActivePlayer()`

当前 live 的终局条件只有 3 条：

- `ALL_PLAYERS_PASSED`
- `ONLY_ONE_PLAYER_REMAINING`
- `TILE_BAG_EMPTY_AND_PLAYER_FINISHED`

### 3.7 结算

当前结算已经是完整可返回对象，不再只是概念接口。

`SettlementServiceImpl.settle(...)` 会生成：

- `SettlementResult`
- 排名
- 终局 summary message
- `BoardSnapshot`

并且还会调用 `SettlementNavigationPort.showSettlement(result)`。

但注意：这条导航口现在默认接的是 `NoOpSettlementNavigationPort`，所以没有实际 UI 跳转。

## 4. 现在已经解决了什么

相比旧版 gap analysis，这些点已经不再属于“没做”：

### 4.1 已经存在一条真实动作主链

当前已经有：

- draft 编辑
- preview
- submit
- pass
- timeout -> lose
- turn transition
- endgame
- settlement

这条链不是伪代码，而是已经写进主服务里的执行路径。

### 4.2 `GameApplicationServiceImpl` 已经不是空壳

之前最核心的缺口就是应用层主服务几乎空着。

现在它已经承担了这些真实职责：

- draft 编辑入口
- preview 更新
- action dispatch
- 提交后计分、补牌、清 draft
- pass
- timeout 转 action
- session snapshot

### 4.3 `TurnCoordinator` 不再是第二套并行队列方案

旧的阻塞式“拉动作”方案已经退出主链。

现在的 `TurnCoordinator` 是：

- 纯事件式
- 只吃已经执行完的 action
- 只负责回合推进和触发终局/结算

### 4.4 终局规则已经下沉到 domain

`EndGameChecker` 已经成为终局条件的真实入口。

虽然规则还不完整，但“谁负责判断终局”这件事已经比之前清楚得多。

### 4.5 测试已经覆盖到部分主链

目前测试覆盖到的真实能力包括：

- setup
- submitDraft
- draft editing
- timeout
- turn coordinator
- endgame checker
- settlement
- dictionary smoke

这说明当前仓库已经不是“完全无法验证”的状态。

## 5. 现在还差什么

下面这些才是当前代码距离“可运行 Scrabble”真正剩下的 gap。

## 5.1 启动层和表现层仍然没有接通

这是当前最大的外层阻断。

### 已有

- `MainApp`
- `AppContext`
- `AppLauncher`
- 一个空的 `MainMenuController`

### 缺失

- 创建新游戏的 UI
- 游戏主界面 controller
- 棋盘与牌架渲染
- 用户输入绑定到 `GameApplicationService`
- 结果页或结算页
- 导航与页面切换

### 影响

即使 service 层能跑，用户也无法从启动入口进入一盘真实对局。

## 5.2 面向 UI 的会话快照严重不足

`GameSessionSnapshot` 目前只提供：

- session status
- 当前玩家 id / name
- 当前玩家时钟
- 各玩家时钟

它没有提供：

- 棋盘状态
- 玩家分数
- 当前玩家牌架
- 其他玩家牌数
- 当前 draft placements
- 当前 preview result
- 当前终局原因
- 当前轮到谁之外的更多对局状态

### 影响

即使现在接一个 UI，也拿不到足够信息去渲染主对局界面。

换句话说，当前 `getSessionSnapshot()` 更像“clock overlay snapshot”，不是“game screen snapshot”。

## 5.3 `PlayerController` 还不是“不同玩家类型的动作来源”

你们前面已经专门讨论过这一点，当前代码确实只做到了“保留这个类型”，但还没有做出不同类型的真实行为。

### 当前状态

- `PlayerController.create(...)` 会分出：
  - `LocalPlayerController`
  - `LANPlayerController`
  - `AIPlayerController`
- 但三个子类都没有额外行为
- 本质上只是同一个转发器的三个名字

### 缺口

- `LOCAL` 没有真正的 UI 绑定层
- `AI` 没有选点 / 搜索 / 提交动作逻辑
- `LAN` 没有网络协议、收发、同步层

### 影响

虽然 `PlayerController` 保留下来了，但“不同玩家类型的动作来源”并没有真正实现。

## 5.4 draft 层仍然缺少关键约束

`DraftManager` 目前只管改 `TurnDraft`，这是合理的；但它没有做任何和真实棋盘交互必须具备的约束。

### 当前缺少的校验

- tile 是否真的属于当前玩家 rack
- 一个 tile 是否已经被放到当前 draft 中
- 两个不同 tile 是否占了同一格
- tile 是否已经在棋盘别处被使用
- tile 是否已经在其他玩家 rack 中
- blank tile 是否指定了字母
- original rack slot 是否记录并回退

### 直接后果

当前 draft 可以构造出很多物理上不成立的状态，而这些状态直到提交时也未必会被挡住。

## 5.5 Preview 仍然是“最小骨架”

preview 主链是通了，但结果对象本身仍然很弱。

### 当前 preview 能给出的内容

- 是否合法
- 估分
- message 列表

### 当前 preview 还给不出的内容

- 形成的单词列表
- 每个单词的分值贡献
- 主词 / 副词区分
- 棋盘高亮
- 错误位置高亮

原因很直接：

- `PreviewResult.words` 目前总是空
- `PreviewResult.highlights` 目前总是空
- `BoardHighlight` 还是空类
- `PreviewWord` 没有真正被构造和填充
- `MovePreviewService` 只有接口没有实现，也没进入主流程

### 影响

当前 preview 只能做“合法/不合法 + 一个估分数字”，还不足以支撑真正的 Scrabble 出牌交互体验。

## 5.6 规则层还存在关键漏洞

这是当前离“真正可玩”最近的一批硬伤。

### 5.6.1 缺少连续性校验

`RuleEngine.validateMove(...)` 当前只检查：

- 同一直线
- 不与现有棋子重叠
- 首手覆盖中心
- 非首手连接已有棋子
- 提取出来的单词是否在字典里

它**没有检查新放下的多个 tile 之间是否连续**。

这意味着像下面这种落子在当前实现里可能会被错误接受：

- `(7,7)` 放 `A`
- `(7,9)` 放 `T`
- `(7,8)` 留空

这是一个关键规则漏洞。

### 5.6.2 可能接受“不形成单词”的提交

当前 `WordExtractor.extract(...)` 是基于相邻 tile 去提词。

因此以下情况很危险：

- 首手只放一个单 tile 在中心
- 新放 tiles 之间不连续
- 最终没有提取出任何 candidate word

在这种情况下，`validateMove(...)` 可能返回 `null`，因为它只会对“提取出来的词”做字典检查，而不会强制要求“必须形成至少一个合法词”。

### 5.6.3 没有校验 tile 所有权

这是另一个非常严重的漏洞。

当前 action 里只有 `tileId`，而 `RuleEngine.apply(...)` 会：

1. 用 `tileBag.getTileById(tileId)` 找到 tile
2. 直接把这个 tile 放到棋盘
3. 再去当前玩家 rack 里尝试删除同 id tile

问题是它没有先验证：

- 这个 tile 是否确实在当前玩家 rack 里

因此如果传入一个合法存在但不属于当前玩家的 `tileId`，当前代码可能仍会把它放到棋盘上。

### 5.6.4 没有校验 tile 唯一使用

`TileBag` 通过 `allTilesById` 保存了对所有生成 tile 的全局引用。

这意味着：

- 即使某 tile 已经被抽走
- 即使某 tile 已经在棋盘上
- 只要知道它的 id，就还能从 `getTileById()` 拿到它

当前没有全局校验去阻止“同一 tile 对象被重复用于多个位置”。

### 5.6.5 没有处理 blank tile 指定字母的提交流程

虽然 `Tile` 支持：

- `isBlank()`
- `assignedLetter`

但当前主链没有：

- draft 阶段指定 blank 字母
- action 中携带 assigned letter
- apply 时写入 assigned letter
- preview / score / word extraction 的完整 blank 交互

### 5.6.6 计分规则不完整

当前 `ScoreCalculator` 还缺少：

- 7 tile bingo / full rack bonus
- 终局时剩余牌罚分与奖励
- 更完整的主词/副词拆分呈现

此外，由于 `WordExtractor` 本身存在提词边界问题，计分的正确性也会受连带影响。

## 5.7 终局规则还不是完整 Scrabble

虽然终局判断已经有真实入口，但 live 规则只有 3 条。

### 当前 live 规则

- 全员 pass
- 只剩 1 个 active player
- 牌袋空且当前行动者清空 rack

### 当前未接入主流程的规则

- `BOARD_FULL`
- `TARGET_SCORE_REACHED`
- `NO_LEGAL_PLACEMENT_AVAILABLE`
- `NORMAL_FINISH`

这些 reason 在枚举或 legacy 逻辑里存在，但没有进入当前主链。

### 额外问题

当前仓库里还同时保留着旧终局机制：

- `GameState.consecutivePasses`
- `EndEvaluator`
- `RoundPassTracker`

现在真正起作用的是：

- `RoundTracker`
- `EndGameChecker`

所以当前终局判定虽然已经有主线，但仓库里仍然存在**遗留并行机制**。

## 5.8 结算仍然是“展示对象完整，规则不完整”

`SettlementServiceImpl` 现在能生成：

- 终局 reason message
- ranking
- board snapshot

这部分已经不是 gap。

但它还不是完整 Scrabble 结算，因为没有：

- 剩余 rack 分值扣减
- 清空 rack 玩家获得他人剩余牌总分
- 终局后得分修正逻辑

当前 ranking 基本就是：

- 按当前 `Player.score` 排序

如果按 Scrabble 规则，这还不够。

## 5.9 hot-seat 交接还没真正实现

`confirmHotSeatHandoff(GameSession session)` 现在是 no-op。

这意味着当前虽然 setup 只支持 `HOT_SEAT`，但 hot-seat 最关键的 UI/状态切换动作其实还没实现。

### 缺少的东西

- 交接前锁屏/遮挡
- 交接确认状态
- 交接后 reveal 当前玩家 rack
- 防止上一位继续操作

### 影响

目前只是“逻辑上轮到下一个人”，还不是完整 hot-seat 体验。

## 5.10 `GameSession` 的 wiring 仍有一个实际断点

`AppContext` 自己持有一个 `SettlementService`。

但 `GameSetupServiceImpl.createSession(...)` 创建 `GameSession` 时，用的是：

- `new GameSession(sessionId, config, gameState)`

而这个 `GameSession` 默认又会：

- `new SettlementServiceImpl()`

这意味着当前 live session **不会继承 `AppContext` 里那份 settlement service / navigation port 配置**。

### 影响

如果你想在 `AppContext` 里注入自定义 `SettlementNavigationPort`，当前对局 session 不会自动用上。

这不是规则问题，而是一个真实 wiring gap。

## 5.11 文档和代码已经再次出现偏差

当前仓库里仍然保留着一些过期结构或说明：

- `docs/dataflow.md` 仍然描述旧的阻塞式 `TurnCoordinator.startTurn()` / `PlayerController.requestAction()` 模型
- `MovePreviewService` 仍是旧分层接口，但当前 preview 已经直接并入 `GameApplicationServiceImpl`
- `domain.Position` 是遗留重复类，主链实际用的是 `domain.model.Position`
- `TilePlacement` 也是遗留模型，主链实际用的是 `ActionPlacement`

这说明当前代码虽然比之前更完整了，但**重构后的结构还没完成彻底收口**。

## 5.12 AI / LAN 仍然只是占位

当前仓库里这些概念都存在：

- `GameMode.HUMAN_VS_AI`
- `GameMode.LAN_MULTIPLAYER`
- `PlayerType.AI`
- `PlayerType.LAN`
- `AIPlayerController`
- `LANPlayerController`

但真实情况是：

- `GameSetupServiceImpl.buildConfig(...)` 直接拒绝非 `HOT_SEAT`
- `AIPlayerController` / `LANPlayerController` 没有独立行为
- 没有 AI 搜索器、网络协议、同步机制、消息收发

所以这两种模式目前仍然是名义支持，不是功能支持。

## 5.13 构建与资源层还有实际问题

### 5.13.1 Maven 现在不能直接构建

我实际运行了：

```powershell
mvn test
```

结果失败于编译插件：

- `pom.xml` 要求 `maven.compiler.release=25`
- 但 Maven 当前绑定到的是 `Java 17`

具体表现是：

- `java -version` 指向 `25.0.2`
- `mvn -version` 显示 Maven runtime 仍在 `C:\DevTools\Scoop\apps\openjdk17\current`

这属于真实的构建阻断。

### 5.13.2 字典资源不是标准 classpath 资源

`DictionaryLoader` 现在直接读：

- `src/resources/Dicts/North-America/NWL2018.txt`
- `src/resources/Dicts/British/CSW19.txt`

而不是从 `src/main/resources` / classpath 读取。

这意味着：

- IDE 内本地运行还能工作
- 打 jar 或换工作目录后，路径很可能失效

### 5.13.3 `README.md` 仍然是空的

这会直接影响：

- 开发者理解入口
- 本地运行指引
- 构建环境说明

## 6. 测试覆盖告诉了我们什么

当前测试说明：

### 已被测试覆盖的

- setup 基本合法性
- new game 初始抽牌
- timed game clock 进入 byo-yomi
- timeout -> lose -> end game
- draft editing 不直接改棋盘
- submit draft 合法/非法基本路径
- pass turn 基本路径
- turn coordinator 的 3 条 live end condition
- settlement 的排名和棋盘快照
- dictionary repository smoke

### 尚未被测试覆盖的

- 启动层与 `AppContext` 装配
- `AppLauncher`
- `MainMenuController`
- hot-seat handoff
- AI / LAN
- 非法 tileId / 非本玩家 rack tile 的作弊路径
- 重复占位 / 重复 tileId
- gapped placement
- blank tile
- 终局计分修正
- session snapshot 是否足够支撑 UI

这意味着现在的测试足以证明“主链部分存在”，但还不足以证明“游戏可玩”。

## 7. 当前代码的完成度判断

### 7.1 已经比较实的部分

- 对局创建
- 初始发牌
- 字典加载
- action 模型
- submit/pass/timeout 统一消费
- turn coordination
- endgame 基础判断
- settlement result 生成

### 7.2 有主链但不够完整的部分

- draft editing
- preview
- score calculation
- time control
- hot-seat 模式

### 7.3 仍然属于占位/未接通的部分

- 启动后可视化对局
- game controller / board UI
- AI
- LAN
- hot-seat handoff
- settlement navigation

## 8. 如果目标是“最小可玩 Hot-seat Scrabble”，现在最少还要补什么

我建议按下面顺序补，不要并行扩散。

### 第一步：先修外层运行入口

目标：

- 从 `MainApp` 真正能进入一盘对局

至少要补：

- game screen controller
- 新游戏入口
- `AppLauncher.launch()` 真正创建并挂载 UI

### 第二步：补完整的 game snapshot / UI 读模型

目标：

- UI 能一次性拿到渲染整局所需的状态

至少要补：

- board snapshot
- player scores
- current rack
- opponents tile counts
- draft placements
- preview result
- current end status

### 第三步：补规则硬伤

目标：

- 防止当前实现接受物理上不成立的动作

优先修：

- tile 必须属于当前玩家 rack
- tile 不能重复使用
- draft 内不能重复占同一格
- placements 必须连续
- 必须形成至少一个合法词
- blank tile 指派字母链路

### 第四步：补 hot-seat 交接

目标：

- 当前唯一支持的模式真的可玩

至少要补：

- `confirmHotSeatHandoff`
- 交接前后 UI 状态
- rack 可见性切换

### 第五步：补结算细则和导航

目标：

- 对局结束后结果正确且能展示

至少要补：

- 终局计分修正
- `SettlementNavigationPort` 真接 UI
- `GameSession` / `AppContext` 的 settlement wiring

## 9. 我对当前距离“可运行 Scrabble”的判断

如果以 0 到 10 粗略估计：

- 之前那版代码大概在 `2/10`
- 当前这版大概已经到 `5.5/10` 到 `6/10`

原因是：

- 内核主链已经成型
- 但外层可玩性和关键规则完整性还没到位

换句话说，当前更像：

- “一个已经有真实回合主链的 Scrabble 引擎雏形”

而不是：

- “一个已经能完整跑起来的 Scrabble 产品”

## 10. 最终判断

### 当前已经完成的核心

- 开局配置与 session 创建
- 玩家、牌架、棋盘、牌袋建模
- 字典加载
- draft -> preview -> action -> apply 的主链
- submit / pass / timeout 三类动作统一消费
- turn coordinator 与基础终局判断
- settlement result 生成

### 当前最关键的阻断

- 没有真正的启动后可交互 UI
- session snapshot 不足以驱动 UI
- 规则层存在关键漏洞，特别是 tile 所有权与连续性
- hot-seat handoff 没有实现
- AI / LAN 仍是占位

### 当前最需要先统一的事

不是再讨论抽象分层，而是明确下面两件事：

1. 先只冲“最小可玩 hot-seat”
2. 先补 UI snapshot 与规则硬伤，再谈 AI / LAN

如果这两个优先级不统一，代码会继续出现“主链能跑一点，但产品形态永远起不来”的状态。

## 11. 本次检查中最关键的源码锚点

- 启动入口
  - `src/main/java/com/kotva/launcher/MainApp.java`
  - `src/main/java/com/kotva/launcher/AppLauncher.java`
- 开局
  - `src/main/java/com/kotva/application/service/GameSetupServiceImpl.java`
- 会话
  - `src/main/java/com/kotva/application/session/GameSession.java`
  - `src/main/java/com/kotva/application/session/GameSessionSnapshot.java`
- 应用主链
  - `src/main/java/com/kotva/application/service/GameApplicationServiceImpl.java`
- 回合协调
  - `src/main/java/com/kotva/application/TurnCoordinator.java`
  - `src/main/java/com/kotva/application/RoundTracker.java`
- draft / preview
  - `src/main/java/com/kotva/application/draft/DraftManager.java`
  - `src/main/java/com/kotva/application/draft/TurnDraft.java`
  - `src/main/java/com/kotva/application/preview/PreviewResult.java`
- domain action / rule
  - `src/main/java/com/kotva/domain/action/PlayerAction.java`
  - `src/main/java/com/kotva/domain/RuleEngine.java`
  - `src/main/java/com/kotva/domain/utils/MoveValidator.java`
  - `src/main/java/com/kotva/domain/utils/WordExtractor.java`
  - `src/main/java/com/kotva/domain/utils/ScoreCalculator.java`
- 终局
  - `src/main/java/com/kotva/domain/endgame/EndGameChecker.java`
- 结算
  - `src/main/java/com/kotva/application/service/SettlementServiceImpl.java`
- 动作来源
  - `src/main/java/com/kotva/mode/PlayerController.java`
- 过期/遗留并行机制
  - `src/main/java/com/kotva/domain/utils/EndEvaluator.java`
  - `src/main/java/com/kotva/application/session/RoundPassTracker.java`
  - `src/main/java/com/kotva/domain/Position.java`
  - `src/main/java/com/kotva/domain/model/TilePlacement.java`
