# 当前代码库差距分析：离“可运行的 Scrabble 游戏”还差什么

## 范围与结论

本文完全以当前代码为准，不以 `docs/` 中的理想架构为准。

结论先行：

1. 当前仓库还不是一个“能玩起来”的 Scrabble 游戏。
2. 更严格地说，当前 `src/main/java` 甚至还不能完整通过主源码编译。
3. 就算先修掉编译错误，当前也仍然缺少启动入口、UI、草稿编辑、预览、提交落子、得分结算、补牌、回合切换接线，因此依然不能开始一盘真实对局。
4. 目前最完整、最接近“可运行链路”的部分是：
   - 开局配置与创建 `GameSession`
   - 牌袋、棋盘、玩家、时钟、字典加载
   - 超时淘汰
   - 赛后结算快照
5. 当前代码更像是“领域对象 + 若干应用层骨架 + 若干未汇总的原型实现”，而不是一条已打通的游戏主流程。

一句话概括现状：

> 现在能“在内存里创建一局游戏对象并推进时钟”，但还不能“启动界面、拖牌、预览、提交、计分并完成一整局 Scrabble”。

---

## 1. 当前代码真实具备的能力

### 1.1 启动层

- `MainApp` 只是一个普通 `main` 方法，负责 new `AppContext` 和 `AppLauncher`。
  - 代码位置：`src/main/java/com/kotva/launcher/MainApp.java:3-10`
- `AppContext` 能创建几个核心服务对象：
  - `GameSetupService`
  - `GameApplicationService`
  - `ClockService`
  - `SettlementService`
  - `DictionaryRepository`
  - `SettingsRepository`
  - 代码位置：`src/main/java/com/kotva/launcher/AppContext.java:16-74`

这说明“依赖装配容器”是有雏形的。

### 1.2 开局配置与会话创建

`GameSetupServiceImpl` 是当前最完整的一段应用层代码。

它已经能做到：

- 校验 `NewGameRequest`
- 只允许 `HOT_SEAT`
- 限制 2 到 4 人
- 校验玩家名非空且不重复
- 加载字典
- 创建 `Player`
- 给每个玩家挂 `PlayerController`
- 创建和初始化 `PlayerClock`
- 随机打乱玩家顺序
- 创建 `GameState`
- 初始摸 7 张牌
- 创建 `GameSession`
- 如果有时间控制，则启动当前玩家时钟

代码位置：

- `src/main/java/com/kotva/application/service/GameSetupServiceImpl.java:25-141`

所以如果只从纯内存对象角度看，“开一局局面”已经基本成立。

### 1.3 领域模型基础

当前已经具备以下核心领域对象：

- `Board`
  - 15x15 棋盘与奖励格布局
  - `src/main/java/com/kotva/domain/model/Board.java:12-122`
- `Cell`
  - 单格和已放置 tile
  - `src/main/java/com/kotva/domain/model/Cell.java`
- `TileBag`
  - 实际上是有限牌袋，不是无限牌袋
  - `src/main/java/com/kotva/domain/model/TileBag.java:17-111`
- `Tile`
  - 含 blank tile 支持字段
  - `src/main/java/com/kotva/domain/model/Tile.java`
- `Rack` / `RackSlot`
  - 玩家牌架
  - `src/main/java/com/kotva/domain/model/Rack.java:12-39`
  - `src/main/java/com/kotva/domain/model/RackSlot.java`
- `Player`
  - 玩家身份、分数、牌架、控制器、时钟
  - `src/main/java/com/kotva/domain/model/Player.java`
- `GameState`
  - 棋盘、牌袋、玩家、当前玩家、游戏结束状态
  - `src/main/java/com/kotva/domain/model/GameState.java:14-157`

这部分说明：领域建模已经有基础，不是从零开始。

### 1.4 时钟与超时淘汰

`ClockServiceImpl` 当前完成度也比较高。

已具备：

- 启动当前玩家时钟
- 主时间耗尽后进入读秒
- 读秒归零后触发超时
- 超时后将当前玩家淘汰
- 只剩 1 名活跃玩家时结束游戏并结算
- 多人局时推进到下一名活跃玩家并开启其时钟

代码位置：

- `src/main/java/com/kotva/application/service/ClockServiceImpl.java:12-103`

对应地，`GameApplicationServiceImpl` 里只有两个真正可用的方法：

- `tickClock`
- `getSessionSnapshot`

代码位置：

- `src/main/java/com/kotva/application/service/GameApplicationServiceImpl.java:57-92`

### 1.5 结算

`SettlementServiceImpl` 能生成：

- 排名
- 结算文字
- 棋盘快照

代码位置：

- `src/main/java/com/kotva/application/service/SettlementServiceImpl.java:20-127`

`SettlementResult`、`PlayerSettlement`、`BoardSnapshot`、`BoardCellSnapshot` 这些 DTO 也都已经存在。

### 1.6 字典加载与设置读写

- `DictionaryRepository` 能加载词库并校验单词存在性
  - `src/main/java/com/kotva/infrastructure/dictionary/DictionaryRepository.java`
- `SettingsRepository` 能落盘和读取音量设置
  - `src/main/java/com/kotva/infrastructure/settings/SettingsRepository.java`

这说明基础设施层不全是空壳。

---

## 2. 当前最关键的阻断：主源码无法完整编译

我直接用 `javac --release 25` 编译了 `src/main/java`，结果失败。

真实错误：

- `src/main/java/com/kotva/application/service/SettlementServiceImpl.java:99`
- `src/main/java/com/kotva/application/service/SettlementServiceImpl.java:101`

原因：

- `SettlementServiceImpl` 的 `switch (endReason)` 使用了不存在的枚举值：
  - `NO_LEGAL_PLACEMENT_AVAILABLE`
  - `NORMAL_FINISH`
- 但 `GameEndReason` 当前只有：
  - `ALL_PLAYERS_PASSED`
  - `ONLY_ONE_PLAYER_REMAINING`
  - `TILE_BAG_EMPTY_AND_PLAYER_FINISHED`
  - `BOARD_FULL`
  - `TARGET_SCORE_REACHED`

对应源码：

- `src/main/java/com/kotva/application/service/SettlementServiceImpl.java:92-102`
- `src/main/java/com/kotva/application/result/GameEndReason.java:3-8`

这意味着：

- 当前仓库首先不是“能玩”
- 连“主源码完整可编译”都还没到

这是第一优先级阻断项。

---

## 3. 从“能跑一个 Scrabble 游戏”角度看，还缺哪些能力

下面按真实运行链路拆。

### 3.1 启动与导航链路没有打通

当前实际启动链：

`MainApp.main()`  
→ `new AppContext()`  
→ `new AppLauncher(appContext)`  
→ `appLauncher.launch()`

但是 `AppLauncher.launch()` 是空的。

代码位置：

- `src/main/java/com/kotva/launcher/MainApp.java:5-9`
- `src/main/java/com/kotva/launcher/AppLauncher.java:3-11`

这意味着当前缺少：

- 主窗口创建
- 场景切换
- 主菜单显示
- 开局页显示
- 游戏页显示
- 结算页显示

更直接地说：

> 当前没有任何“游戏真的启动起来”的外层承载物。

### 3.2 表现层几乎不存在

表现层当前只有一个空控制器：

- `src/main/java/com/kotva/presentation/controller/MainMenuController.java:1-5`

并且仓库内没有：

- FXML 文件
- CSS
- 游戏控制器
- 棋盘组件
- 牌架组件
- 热座切换遮罩
- 结果页控制器

另外，`pom.xml` 里也没有 JavaFX 相关依赖，只有 JUnit。

代码位置：

- `pom.xml:20-27`

因此从真实代码看，目前没有 UI，不是“UI 没接上服务”这么简单，而是：

> UI 本身基本还没开始。

### 3.3 交互主服务 `GameApplicationServiceImpl` 核心方法全未实现

这是当前第二大阻断。

以下方法全部直接 `throw new UnsupportedOperationException(...)`：

- `placeDraftTile`
- `moveDraftTile`
- `removeDraftTile`
- `recallAllDraftTiles`
- `submitDraft`
- `passTurn`

代码位置：

- `src/main/java/com/kotva/application/service/GameApplicationServiceImpl.java:22-50`

这意味着当前缺少全部实际操作入口：

- 拖牌到棋盘
- 在棋盘上移动草稿牌
- 从棋盘撤回一张草稿牌
- 一键撤回本回合所有草稿牌
- 提交草稿
- 跳过回合

也就是说，虽然已经有 `GameSession`、`TurnDraft`、`PreviewResult` 等对象，但用户无法通过应用层真正操作它们。

### 3.4 草稿编辑链路没有完成

虽然有 `TurnDraft` 和 `DraftPlacement`：

- `src/main/java/com/kotva/application/draft/TurnDraft.java:17-50`
- `src/main/java/com/kotva/application/draft/DraftPlacement.java:10-34`

但缺少一个完整可用的草稿编辑器。

`DraftManager` 当前只有一个空壳方法，而且还导入了另一套 `Position` 类型：

- `src/main/java/com/kotva/application/draft/DraftManager.java:3-13`
- 导入的是 `com.kotva.domain.Position`
- 而其余大部分代码使用的是 `com.kotva.domain.model.Position`

当前仓库里确实存在两套 `Position`：

- `src/main/java/com/kotva/domain/Position.java:1-10`
- `src/main/java/com/kotva/domain/model/Position.java:1-20`

这会带来两个问题：

1. 草稿编辑层与领域层的位置对象不统一。
2. 后续一旦真正接线，`DraftManager` 很容易成为类型不匹配源头。

### 3.5 预览链路缺接口实现，也缺结果对象落地能力

预览设计上有接口：

- `MovePreviewService`
  - `src/main/java/com/kotva/application/service/MovePreviewService.java:1-8`

但没有任何实现类。

另外，预览 DTO 也不完整：

- `PreviewResult` 有字段和 getter，可以用
  - `src/main/java/com/kotva/application/preview/PreviewResult.java:5-36`
- `PreviewWord` 只有字段和 getter，没有构造器、没有 setter
  - `src/main/java/com/kotva/application/preview/PreviewWord.java:6-24`
- `BoardHighlight` 完全是空类
  - `src/main/java/com/kotva/application/preview/BoardHighlight.java:1-5`

这意味着：

- 预览服务本身不存在
- 预览结果即使想返回，也没有完整数据结构承载高亮细节
- UI 侧也没有任何消费这些预览结果的控制器或组件

### 3.6 提交落子链路没有打通

从“玩家提交一手牌”到“正式改盘面”的主链，当前是断的。

理想上应该是：

`submitDraft(session)`  
→ 读取 `TurnDraft`  
→ 校验规则  
→ 提取单词  
→ 检查词典  
→ 计算分数  
→ 正式写入棋盘  
→ 从牌架移除已出牌  
→ 给玩家加分  
→ 补牌  
→ 判断是否结束  
→ 切换回合  
→ 返回结果给 UI

当前实际情况：

- `GameApplicationServiceImpl.submitDraft()` 未实现
  - `src/main/java/com/kotva/application/service/GameApplicationServiceImpl.java:42-45`
- `TurnCoordinator.applyAction()` 在 `PLACE_TILE` 分支里什么都没做
  - `src/main/java/com/kotva/application/TurnCoordinator.java:89-106`
- `RuleEngine` 虽然存在，但没人调用
  - `src/main/java/com/kotva/domain/RuleEngine.java:25-145`
- `ScoreCalculator` 虽然存在，但没人调用
  - `src/main/java/com/kotva/domain/utils/ScoreCalculator.java:19-105`
- `WordExtractor` 虽然存在，但只被 `RuleEngine` 内部用到
  - `src/main/java/com/kotva/domain/utils/WordExtractor.java:17-178`

换句话说：

> 规则工具已经零散存在，但“提交动作”这条主流程还没有组装。

### 3.7 玩家得分不会增长

虽然 `Player` 有 `addScore(int points)`：

- `src/main/java/com/kotva/domain/model/Player.java`

但在主代码里没有任何地方调用它。

搜索结果表明：

- `addScore(...)` 只出现在测试里
- 主代码没有一次真实加分

这意味着当前游戏即使强行提交落子，也不会更新比分。

### 3.8 不会补牌

`RuleEngine.apply()` 当前会把已落子的牌从 `Rack` 删除：

- `src/main/java/com/kotva/domain/RuleEngine.java:111-126`

但不会在落子后从牌袋补回到 7 张。

所以当前缺少标准 Scrabble 的核心行为之一：

- 落子后补牌

没有这个，游戏只能越下手牌越少，且多数后续逻辑会很快失真。

### 3.9 Blank tile 只有字段，没有完整交互闭环

当前代码里 blank tile 相关能力只有一半：

- `Tile` 有 `assignedLetter`
- `TilePlacement` 也有 `assignedLetter`
- `WordExtractor` 和结算快照会读取 assigned letter

但缺失点是：

- `DraftPlacement` 不带 `assignedLetter`
- 没有 UI 选择 blank 字母的入口
- 主代码里没有任何地方调用 `Tile.setAssignedLetter(...)`

实际搜索结果显示：

- `setAssignedLetter(...)` 没有主流程调用者

这意味着 blank tile 在当前代码里是“字段存在，但玩法没打通”。

### 3.10 热座交接状态没有真正实现

当前只保留了一个空方法：

- `GameApplicationServiceImpl.confirmHotSeatHandoff(...)`
  - `src/main/java/com/kotva/application/service/GameApplicationServiceImpl.java:52-55`

但会话状态枚举只有：

- `WAITING_FOR_PLAYERS`
- `IN_PROGRESS`
- `COMPLETED`

代码位置：

- `src/main/java/com/kotva/policy/SessionStatus.java`

这说明当前没有以下机制：

- 本回合结束后隐藏上家牌架
- 显示“轮到下一位玩家”
- 等下一位确认后再展示其牌架

对于 `HOT_SEAT` 模式，这其实是必须补齐的交互层能力。

---

## 4. 已有组件之间，哪些地方没接上

这一节专门回答“各个组件之间哪里还需要链接”。

## 4.1 启动容器和会话没有连到任何 UI

当前已经有：

- `AppContext`
- `GameSetupService`
- `GameApplicationService`
- `SettlementService`

但缺少：

- `AppLauncher` 调起页面
- 控制器调用 `GameSetupService.startNewGame(...)`
- 控制器持有 `GameSession`
- 控制器把事件转到 `GameApplicationService`

所以第一条接线是：

`AppLauncher`  
→ `MainMenuController` / `GameSetupController` / `GameController` / `ResultController`

而这几个控制器里，除 `MainMenuController` 外都还不存在。

## 4.2 `GameSetupService` 和 `GameSession` 已接上，但 `GameSession` 又把服务重新 new 了一遍

这是一个很关键但容易忽略的问题。

`AppContext` 已经创建了一个 `SettlementService`：

- `src/main/java/com/kotva/launcher/AppContext.java:24-50`

但 `GameSetupServiceImpl.createSession(...)` 创建 `GameSession` 时，没有把这个 `SettlementService` 注进去：

- `src/main/java/com/kotva/application/service/GameSetupServiceImpl.java:87-101`

`GameSession` 内部又默认 new 了新的 `SettlementServiceImpl()`：

- `src/main/java/com/kotva/application/session/GameSession.java:21-38`

而 `SettlementServiceImpl` 默认又会挂一个 `NoOpSettlementNavigationPort`：

- `src/main/java/com/kotva/application/service/SettlementServiceImpl.java:23-31`
- `src/main/java/com/kotva/application/service/NoOpSettlementNavigationPort.java`

这意味着：

1. `AppContext` 里那份 `SettlementService` 实际没有进入会话。
2. 真实游戏结束时，就算结算被触发，默认也不会导航到结果页。
3. 会话对象与应用容器对象之间存在“服务实例分叉”。

这是一个明确的接线缺口。

## 4.3 `GameApplicationServiceImpl` 没有拿到它真正需要的依赖

当前它只注入了一个 `ClockService`：

- `src/main/java/com/kotva/application/service/GameApplicationServiceImpl.java:15-20`

但如果它要完成真实游戏交互，至少还需要：

- `MovePreviewService`
- `RuleEngine`
- `SettlementService`
- 可能还需要 `DraftManager`
- 可能还需要访问 `DictionaryRepository` 或由 `RuleEngine` 内部封装

所以当前不仅方法未实现，连依赖注入图都没搭完。

## 4.4 `TurnCoordinator` 和 `GameApplicationService` 是两条并行方案，没有汇总

当前仓库里存在两套回合推进思路：

### 方案 A：UI 驱动的 `GameApplicationService`

接口定义看起来像拖放式 UI：

- 放一张草稿牌
- 移动草稿牌
- 撤回草稿牌
- 提交草稿
- pass

代码位置：

- `src/main/java/com/kotva/application/service/GameApplicationService.java`

### 方案 B：阻塞式 `TurnCoordinator + PlayerController Queue`

`TurnCoordinator.startTurn()` 会直接调用：

- 当前玩家的 `PlayerController.requestAction()`
- 该方法内部会 `BlockingQueue.take()`

代码位置：

- `src/main/java/com/kotva/application/TurnCoordinator.java:33-58`
- `src/main/java/com/kotva/mode/PlayerController.java:42-50`

这套模型更像：

- 本地控制器/AI/网络控制器往队列塞动作
- 协调器阻塞等待动作

问题在于：

1. `GameApplicationServiceImpl` 没有调用 `TurnCoordinator`
2. `TurnCoordinator` 也不读取 `GameSession.turnDraft`
3. `TurnCoordinator` 的 `PLACE_TILE` 分支没有真正接规则引擎
4. 如果 UI 线程直接调用 `startTurn()`，会被 `take()` 阻塞

所以目前是“两套思路并存，但没有决定谁是主链”。

这是全局最重要的架构断点之一。

## 4.5 规则层存在，但完全没进入应用主流程

`RuleEngine` 当前实现了两件事：

- `validateMove(...)`
- `apply(...)`

代码位置：

- `src/main/java/com/kotva/domain/RuleEngine.java:40-145`

它内部确实会用到：

- `MoveValidator`
- `WordExtractor`
- `DictionaryRepository`

但现在 repo 里没有任何地方 new `RuleEngine`，也没有任何地方调用它。

搜索结果显示：

- `RuleEngine` 只有定义，没有调用者

所以当前规则链路是“存在但悬空”的状态。

## 4.6 Pass / 终局判定有三套并行机制

当前仓库里至少有三套“pass / 终局”状态来源：

### 第一套：`RoundTracker`

- `src/main/java/com/kotva/application/RoundTracker.java:3-52`
- 被 `TurnCoordinator` 使用

### 第二套：`RoundPassTracker`

- `src/main/java/com/kotva/application/session/RoundPassTracker.java:6-20`
- 被 `GameSession` 持有
- 但全仓库没人真正调用它的 `markPassed/reset/hasPassed`

### 第三套：`GameState.consecutivePasses + EndEvaluator`

- `src/main/java/com/kotva/domain/model/GameState.java:137-156`
- `src/main/java/com/kotva/domain/utils/EndEvaluator.java:26-52`

`EndEvaluator` 会基于：

- 目标分
- 棋盘满
- 连续 pass
- 只剩 1 人

来判终局。

但它当前没有任何调用者。

这三个系统没有统一，意味着：

- 回合统计
- 全员 pass 结束
- 游戏结束原因

目前没有唯一真相源。

## 4.7 时钟链和回合链没有合并

时钟服务会在超时后：

- 淘汰玩家
- 必要时结算并设置 `SessionStatus.COMPLETED`
- 或推进到下一个活跃玩家

代码位置：

- `src/main/java/com/kotva/application/service/ClockServiceImpl.java:72-97`

但是：

- `ClockServiceImpl` 不更新 `TurnCoordinator` 内部的 `gameEnded`
- `TurnCoordinator` 也不调用 `ClockService.startTurnClock/stopTurnClock`
- `GameApplicationService.passTurn/submitDraft` 都还没实现

所以当前“计时推进”和“回合推进”是两条分离链。

现实后果是：

- 你很难保证超时、手动提交、pass、lose 使用的是同一套状态机。

## 4.8 结算结果能生成，但没有导航出口

当前结算链是：

- `SettlementServiceImpl.settle(...)`
- 内部调用 `settlementNavigationPort.showSettlement(result)`

代码位置：

- `src/main/java/com/kotva/application/service/SettlementServiceImpl.java:33-45`

但默认导航端口是：

- `NoOpSettlementNavigationPort`

代码位置：

- `src/main/java/com/kotva/application/service/NoOpSettlementNavigationPort.java`

因此现在的结算实际上只能生成内存结果对象，不能驱动任何 UI 页面跳转。

---

## 5. 即使接上线，当前规则实现本身也还不够构成完整 Scrabble

这一节不是“没接线”，而是“底层逻辑本身还没做完”。

### 5.1 缺少落子连续性规则

`MoveValidator.isStraightLine(...)` 只检查是否同一行或同一列：

- `src/main/java/com/kotva/domain/utils/MoveValidator.java:21-44`

它没有检查：

- 新放的 tile 是否连续
- 如果中间有空位，是否由旧牌桥接

这意味着当前可能接受“同一行但隔很远”的非法落子。

### 5.2 第一手单字母和主词提取逻辑不可靠

`WordExtractor.extract(...)` 的提词逻辑依赖“周围有邻居”才收集单词：

- `src/main/java/com/kotva/domain/utils/WordExtractor.java:35-58`

这会导致一些边界问题：

- 第一手如果提交单字母，它不会形成 candidate word
- 验证阶段可能因此绕过词典校验
- 计分阶段也可能得到 0 分

当前没有一个“必须产出 1 个主词”的保证。

### 5.3 计分没有接入玩家分数，也缺 7 张满贯加成

`ScoreCalculator` 只负责计算数值：

- `src/main/java/com/kotva/domain/utils/ScoreCalculator.java:33-104`

但当前缺少：

- 调用它的应用层主流程
- 把分数加到 `Player`
- Bingo / 50 分奖励

所以离真实 Scrabble 计分闭环还差一步甚至几步。

### 5.4 落子后不会补牌

这是最明显的游戏性缺失之一。

当前没有任何地方在成功提交后：

- 从牌袋抽新牌
- 填回空的 `RackSlot`

没有补牌，就不是一局正常 Scrabble。

### 5.5 Blank tile 玩法未闭环

如上所述，blank tile 只建了字段，没有完成以下流程：

- 玩家选择 blank 代表哪个字母
- 草稿对象记录这个选择
- 提交时应用到真实 tile
- 预览与计分统一使用该 assigned letter

### 5.6 缺少换牌（exchange）功能

当前动作类型只有：

- `PLACE_TILE`
- `PASS_TURN`
- `LOSE`

代码位置：

- `src/main/java/com/kotva/policy/ActionType.java`

标准 Scrabble 的“换牌”当前完全没有建模。

如果你们的目标是最小可玩 hot-seat 版本，换牌不是第一优先级；
但如果目标是“像 Scrabble 的完整对局”，这是缺失项。

### 5.7 AI / LAN 模式只是枚举存在

`GameMode` 有：

- `HOT_SEAT`
- `HUMAN_VS_AI`
- `LAN_MULTIPLAYER`

但 `GameSetupServiceImpl` 明确拒绝除 `HOT_SEAT` 之外的模式：

- `src/main/java/com/kotva/application/service/GameSetupServiceImpl.java:44-47`

`PlayerController` 里 AI / LAN 控制器也只有 TODO：

- `src/main/java/com/kotva/mode/PlayerController.java:60-73`

所以当前真正能朝“可玩”推进的模式只有 hot-seat，本地对战。

---

## 6. 资源、构建与分发层面的实际问题

### 6.1 字典资源路径不符合常规打包方式

当前字典文件放在：

- `src/resources/Dicts/British/CSW19.txt`
- `src/resources/Dicts/North-America/NWL2018.txt`

但 Maven 默认资源目录是 `src/main/resources`。

而 `pom.xml` 没有额外配置 `<resources>`：

- `pom.xml:29-73`

再加上 `DictionaryLoader` 用的是文件系统路径：

- `src/main/java/com/kotva/infrastructure/dictionary/DictionaryLoader.java:35-39`

这带来两个后果：

1. 代码在仓库工作目录里运行时，也许还能找到字典文件。
2. 一旦打 jar 或换工作目录，词典路径很可能失效。

所以词典加载当前更像“开发目录运行可用”，不是“可分发运行”。

### 6.2 `README.md` 是空的

- 仓库根目录 `README.md` 长度为 0

这不是游戏逻辑问题，但它意味着：

- 没有运行说明
- 没有构建说明
- 没有玩法说明
- 没有模块说明

对“可交付运行项目”来说，这也是明显缺口。

### 6.3 没有 Maven Wrapper

仓库内没有 `mvnw` / `mvnw.cmd`。

这意味着：

- 项目构建依赖开发者本机自行安装 Maven
- 可复现构建体验较差

这不是核心玩法缺失，但属于工程完成度缺失。

---

## 7. 测试覆盖告诉了我们什么

当前测试主要覆盖四类：

1. `GameSetupServiceImplTest`
2. `ClockServiceImplTest`
3. `SettlementServiceImplTest`
4. `TurnCoordinatorTest`
5. `DictionaryRepositorySmokeTest`
6. 一个模板 `AppTest`

这说明团队当前主要在验证：

- 能否开局
- 时钟是否走
- 结算是否能出
- `TurnCoordinator` 的局部骨架行为

但当前明显没有测试覆盖：

- UI 启动
- 拖牌与草稿编辑
- 预览生成
- 真实单词校验闭环
- 真实计分闭环
- 落子后补牌
- blank tile
- 多回合完整对局
- 打包后词典加载

特别注意：

`TurnCoordinatorTest` 测的是一个“骨架版 turn loop”，不是“真实 Scrabble 提交流程”。

例如：

- 测试里没有真正提交有效 `DraftPlacement`
- 没有真正写棋盘
- 没有真正计分

所以当前测试通过，并不代表游戏能玩。

---

## 8. 当前代码更接近哪一种完成度

我会把当前完成度分成三层：

### 8.1 已经比较实的部分

- `GameSetupServiceImpl`
- `ClockServiceImpl`
- `DictionaryRepository`
- `SettingsRepository`
- `Board` / `GameState` / `Player` / `TileBag`
- `SettlementServiceImpl`

### 8.2 有骨架但未集成的部分

- `RuleEngine`
- `MoveValidator`
- `WordExtractor`
- `ScoreCalculator`
- `TurnCoordinator`
- `TurnDraft`
- `PreviewResult`

### 8.3 仍属于占位符/草稿的部分

- `AppLauncher`
- `MainMenuController`
- `GameApplicationServiceImpl` 的大多数方法
- `MovePreviewService` 实现
- `DraftManager`
- `BoardHighlight`
- `SubmitDraftResult`
- `TurnTransitionResult`
- AI / LAN 控制器
- `AudioManager`

所以当前项目不能理解成“只差 UI”。

更准确地说，它现在处于：

> 游戏核心对象已经成型，但交互主流程和若干关键规则闭环还没完成。

---

## 9. 如果目标是“先做出一个最小可玩的 Hot-seat Scrabble”，最少还要补哪些东西

这一节只谈“最小可玩”，不包含 AI / LAN / 豪华 UI。

### 第一阶段：先让代码重新可编译

必须先做：

1. 修正 `SettlementServiceImpl` 与 `GameEndReason` 的枚举不一致。
2. 决定终局原因集合到底以哪份为准。

### 第二阶段：确定唯一主流程

必须做一个架构选择：

1. 要么以 `GameApplicationService + TurnDraft + UI 拖放` 为主
2. 要么以 `TurnCoordinator + PlayerController Queue` 为主

我基于当前代码更建议：

1. 把 `GameApplicationService` 作为唯一 UI 入口
2. 让 `TurnCoordinator` 退化为内部回合切换器，或者直接被 `GameApplicationService` 吸收
3. 不要把阻塞式 `requestAction()` 作为主 UI 方案

原因很简单：

- 现在你们已经有 `TurnDraft`
- 也已经有 `PreviewResult`
- 这更适合图形界面拖放玩法

### 第三阶段：补齐草稿编辑与预览

至少需要：

1. 统一只保留一套 `Position`
2. 实现 `DraftManager` 或直接在 `GameApplicationServiceImpl` 中维护 `TurnDraft`
3. 实现 `MovePreviewServiceImpl`
4. 补齐 `PreviewWord` / `BoardHighlight`
5. 让以下方法可用：
   - `placeDraftTile`
   - `moveDraftTile`
   - `removeDraftTile`
   - `recallAllDraftTiles`

### 第四阶段：补齐“提交一手牌”闭环

至少需要：

1. `submitDraft(session)` 读取草稿
2. 调 `RuleEngine.validateMove(...)`
3. 调 `WordExtractor`
4. 调 `ScoreCalculator`
5. 正式写入 `Board`
6. 从 `Rack` 移除用掉的 tile
7. 给当前玩家 `addScore(...)`
8. 从 `TileBag` 补牌
9. 清空当前 `TurnDraft`
10. 判断终局
11. 切换到下一位玩家
12. 启动下一位玩家时钟
13. 返回一个真正有内容的 `SubmitDraftResult`

### 第五阶段：补齐 pass / hot-seat / 结算切页

至少需要：

1. `passTurn(session)` 真正记录 pass
2. 统一只保留一套 pass 统计机制
3. 为 hot-seat 加交接状态
4. 让 `SettlementService` 使用真正的导航端口，而不是 `NoOpSettlementNavigationPort`
5. `SubmitDraftResult` / `TurnTransitionResult` 能携带：
   - 是否结束
   - 当前玩家是谁
   - 下一个玩家是谁
   - 结算结果
   - 是否需要热座遮罩

### 第六阶段：补最小 UI

最小可玩并不需要华丽页面，但至少要有：

1. 开局页
2. 游戏页
3. 结果页
4. 棋盘显示
5. 牌架显示
6. 当前玩家信息
7. 分数显示
8. 时钟显示
9. 提交 / 撤回 / pass 按钮
10. 热座切换遮罩

---

## 10. 如果不做 AI/LAN，只冲“最小可玩本地热座”，我建议的真实接线顺序

下面是按当前代码最顺的施工顺序，不是理想化顺序。

1. 修编译错误：统一 `GameEndReason`。
2. 删除或冻结第二套方案：决定 `GameApplicationService` 才是唯一入口。
3. 统一位置对象：删掉 `com.kotva.domain.Position` 或至少停止使用它。
4. 实现 `MovePreviewServiceImpl`，把 `RuleEngine/WordExtractor/ScoreCalculator` 接进去。
5. 实现 `GameApplicationServiceImpl` 的 6 个核心方法。
6. 在提交成功后补齐：
   - 写盘
   - 加分
   - 补牌
   - 清草稿
   - 切玩家
   - 切时钟
7. 统一 pass/终局机制，只保留一套。
8. 改 `GameSession`，不要内部偷 new `SettlementServiceImpl()`，而是从 `AppContext` 注入。
9. 实现最小 `AppLauncher + GameController + ResultController`。
10. 把字典移动到 `src/main/resources` 或明确配置 Maven resources。

---

## 11. 我对“当前离可运行 Scrabble 还差多少”的判断

如果以 100% 代表“玩家可以启动程序，开一局 hot-seat Scrabble，拖牌、预览、提交、计分、补牌、pass、超时、结算，全流程能跑”，那我对当前代码的判断大概是：

- 领域对象与部分服务骨架：40%
- 真正打通的用户流程：10% 以下
- 整体项目到“最小可玩 hot-seat”：还差主要工程量

更具体一点：

- “能创建一局内存状态”这一块已经有了。
- “能真正玩一局”这一块目前还没有形成主链。

所以答案不是“只差几根线”。

更准确地说是：

> 现在已经有不少零件，但主传动轴、仪表盘和点火系统都还没装完。

---

## 12. 最终判断

### 当前已经完成的核心

- 局面初始化
- 玩家与牌架建模
- 棋盘建模
- 牌袋建模
- 字典读取
- 时钟推进与超时淘汰
- 结算结果对象

### 当前最关键的阻断

- 主源码编译失败
- 启动器为空
- UI 基本不存在
- `GameApplicationServiceImpl` 核心操作未实现
- 规则链路没接入主流程
- 提交后不计分、不补牌、不切回合
- 结算导航未打通
- pass / 终局状态机有三套并行实现

### 团队最需要先统一的事

不是先写更多类，而是先统一这三个问题：

1. 以哪条主流程为准：`GameApplicationService` 还是 `TurnCoordinator`
2. 以哪套终局/回合统计为准：`RoundTracker`、`RoundPassTracker` 还是 `EndEvaluator`
3. 以哪套数据投影给 UI：直接读 `GameSession/GameState`，还是扩展 snapshot/result DTO

如果这三个问题不先统一，继续往下写代码会越来越散。

---

## 附：本次检查中最关键的源码锚点

- 启动但不真正 launch：`src/main/java/com/kotva/launcher/AppLauncher.java:3-11`
- 容器有服务，但没有 UI 注入出口：`src/main/java/com/kotva/launcher/AppContext.java:16-74`
- 真正可用的开局服务：`src/main/java/com/kotva/application/service/GameSetupServiceImpl.java:25-141`
- 交互主服务核心方法全未实现：`src/main/java/com/kotva/application/service/GameApplicationServiceImpl.java:22-50`
- `GameSession` 内部自建 `SettlementServiceImpl`：`src/main/java/com/kotva/application/session/GameSession.java:21-56`
- `TurnCoordinator` 的 `PLACE_TILE` 分支未接规则引擎：`src/main/java/com/kotva/application/TurnCoordinator.java:89-106`
- 规则引擎存在但无调用者：`src/main/java/com/kotva/domain/RuleEngine.java:25-145`
- 三套 pass / 终局机制：
  - `src/main/java/com/kotva/application/RoundTracker.java:3-52`
  - `src/main/java/com/kotva/application/session/RoundPassTracker.java:6-20`
  - `src/main/java/com/kotva/domain/utils/EndEvaluator.java:12-53`
- 编译阻断枚举不一致：
  - `src/main/java/com/kotva/application/service/SettlementServiceImpl.java:92-102`
  - `src/main/java/com/kotva/application/result/GameEndReason.java:3-8`
- 字典路径依赖源码目录：
  - `src/main/java/com/kotva/infrastructure/dictionary/DictionaryLoader.java:19-39`
- 表现层基本空缺：
  - `src/main/java/com/kotva/presentation/controller/MainMenuController.java:1-5`
  - 仓库内无 FXML
- `pom.xml` 只有 JUnit，无 UI 运行时依赖：`pom.xml:20-27`

