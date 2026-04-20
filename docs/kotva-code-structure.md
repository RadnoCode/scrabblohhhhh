# kotva 项目代码结构说明

本文档根据当前源码中的中文注释自动整理，汇总了每个包、类、方法的作用以及它们之间的引用关系。

## 阅读说明

- 主代码位于 `src/main/java/com/kotva`。
- 测试代码位于 `src/test/java/com/kotva`。
- “引用关系”主要对应类注释中的“引用类”说明，用来帮助理解当前类依赖了哪些其他类。

## 包目录

- `com.kotva`：测试根包，负责放置基础测试代码
- `com.kotva.application`：应用层核心包，负责协调玩家动作、轮次推进与回合流程控制
- `com.kotva.application.draft`：应用层草稿包，负责保存和操作玩家当前回合尚未提交的落子草稿
- `com.kotva.application.preview`：应用层预览包，负责承载走子预览、高亮和单词预估结果
- `com.kotva.application.result`：应用层结算结果包，负责封装终局结算、棋盘快照和排名结果
- `com.kotva.application.service`：应用层服务包，负责定义并实现开局、行棋、计时、预览和结算相关服务
- `com.kotva.application.session`：应用层会话包，负责保存对局配置、会话状态与界面快照数据
- `com.kotva.application.setup`：应用层开局请求包，负责承接新对局创建时的输入参数
- `com.kotva.domain`：领域层入口包，负责提供规则引擎和通用领域对象
- `com.kotva.domain.model`：领域模型包，负责定义棋盘、玩家、牌袋、牌架等核心业务实体
- `com.kotva.domain.utils`：领域工具包，负责执行走子校验、单词提取和分数计算等规则辅助逻辑
- `com.kotva.infrastructure`：基础设施包，负责提供音频等外部资源相关能力
- `com.kotva.infrastructure.dictionary`：基础设施词典包，负责加载词典文件并提供单词查询能力
- `com.kotva.infrastructure.settings`：基础设施设置包，负责应用设置对象和持久化存储
- `com.kotva.launcher`：启动包，负责组装应用依赖并启动程序入口
- `com.kotva.mode`：模式与控制器包，负责描述游戏模式和玩家控制器类型
- `com.kotva.policy`：策略枚举包，负责集中定义动作、奖励格、时钟和状态等枚举常量
- `com.kotva.presentation.controller`：表示层控制器包，负责承载界面控制器

## 包：`com.kotva`

- 包作用：测试根包，负责放置基础测试代码
- 包含类：AppTest

### 文件：`src/test/java/com/kotva/AppTest.java`

#### 类：`AppTest`

- 类型：class
- 类作用：基础测试类，用于验证测试框架运行正常
- 包含方法：shouldAnswerWithTrue
- 继承/实现：无
- 引用关系：Test 用于标记测试方法

| 方法 | 作用 |
| --- | --- |
| `shouldAnswerWithTrue` | 未提取到方法说明 |

## 包：`com.kotva.application`

- 包作用：应用层核心包，负责协调玩家动作、轮次推进与回合流程控制
- 包含类：PlayerAction、RoundTracker、TurnCoordinator

### 文件：`src/main/java/com/kotva/application/PlayerAction.java`

#### 类：`PlayerAction`

- 类型：record
- 类作用：记录一次玩家动作，统一封装动作类型、所属玩家和可选的回合草稿
- 包含方法：place、pass、lose
- 继承/实现：无
- 引用关系：ActionType 用于区分动作类型；TurnDraft 用于保存当前回合草稿数据

| 方法 | 作用 |
| --- | --- |
| `place` | 创建一个落子动作记录 |
| `pass` | 创建一个跳过回合动作记录 |
| `lose` | 创建一个认输动作记录 |

### 文件：`src/main/java/com/kotva/application/RoundTracker.java`

#### 类：`RoundTracker`

- 类型：class
- 类作用：跟踪一轮中的完成回合数和 pass 次数，用于判断是否进入新一轮或满足终局条件
- 包含方法：RoundTracker、startNewRound、recordTurn、isRoundComplete、isAllPassedInRound、getFinishedTurnCount、getPassCount、getActivePlayerCountInRound
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `RoundTracker` | 构造方法：初始化 RoundTracker 所需的基础状态 |
| `startNewRound` | 开始一轮新的回合统计 |
| `recordTurn` | 记录当前回合是否为 pass |
| `isRoundComplete` | 判断是否轮次Complete |
| `isAllPassedInRound` | 判断是否AllPassedIn轮次 |
| `getFinishedTurnCount` | 获取已完成Turn数量 |
| `getPassCount` | 获取pass 状态数量 |
| `getActivePlayerCountInRound` | 获取有效状态玩家数量In轮次 |

### 文件：`src/main/java/com/kotva/application/TurnCoordinator.java`

#### 类：`TurnCoordinator`

- 类型：class
- 类作用：协调玩家回合执行、动作处理、轮次结束判断和终局结算触发
- 包含方法：TurnCoordinator、startTurn、getNextPlayer、getTurnNumber、isGameEnded、getSettlementResult、validateActionOwner、applyAction、evaluateImmediateGameEnd、finalizeRound、detectReservedGameEndReason、endGame
- 继承/实现：无
- 引用关系：GameEndReason 用于标识对局结束原因；GameState 用于访问或更新当前对局状态；Player 用于访问玩家对象、分数、行动权或牌架；PlayerController 用于接收并产出玩家动作；ActionType 用于区分动作类型

| 方法 | 作用 |
| --- | --- |
| `TurnCoordinator` | 构造方法：初始化 TurnCoordinator 所需的基础状态 |
| `startTurn` | 启动当前玩家的回合并处理其动作结果 |
| `getNextPlayer` | 获取Next玩家 |
| `getTurnNumber` | 获取TurnNumber |
| `isGameEnded` | 判断是否对局Ended |
| `getSettlementResult` | 获取结算结果 |
| `validateActionOwner` | 校验动作提交者是否为当前玩家 |
| `applyAction` | 把当前玩家动作应用到对局状态 |
| `evaluateImmediateGameEnd` | 检查当前动作是否立即触发终局 |
| `finalizeRound` | 在一轮结束时执行轮次收尾和终局判断 |
| `detectReservedGameEndReason` | 检测延后到轮次边界判断的终局原因 |
| `endGame` | 结束对局并触发结算 |

### 文件：`src/test/java/com/kotva/application/TurnCoordinatorTest.java`

#### 类：`TurnCoordinatorTest`

- 类型：class
- 类作用：测试回合协调器的终局判断与轮次推进逻辑
- 包含方法：loseDoesNotEndGameWhenMultipleActivePlayersRemain、loseEndsGameWhenOnlyOneActivePlayerRemains、allPlayersPassingEndsGameAtRoundBoundary、emptyTileBagAndEmptyRackEndsGameAfterPlaceTileAction、createPlayer
- 继承/实现：无
- 引用关系：TurnDraft 用于保存当前回合草稿数据；GameEndReason 用于标识对局结束原因；GameState 用于访问或更新当前对局状态；Player 用于访问玩家对象、分数、行动权或牌架；Tile 用于访问字牌字母、分值和空白牌状态；PlayerController 用于接收并产出玩家动作；PlayerType 用于区分玩家控制器类型；Test 用于标记测试方法

| 方法 | 作用 |
| --- | --- |
| `loseDoesNotEndGameWhenMultipleActivePlayersRemain` | 未提取到方法说明 |
| `loseEndsGameWhenOnlyOneActivePlayerRemains` | 未提取到方法说明 |
| `allPlayersPassingEndsGameAtRoundBoundary` | 未提取到方法说明 |
| `emptyTileBagAndEmptyRackEndsGameAfterPlaceTileAction` | 未提取到方法说明 |
| `createPlayer` | 创建一个测试用玩家对象 |

#### 类：`RecordingSettlementService`

- 类型：class
- 类作用：测试替身类，用于记录结算服务调用结果
- 包含方法：settle
- 继承/实现：实现 SettlementService
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `settle` | 未提取到方法说明 |

## 包：`com.kotva.application.draft`

- 包作用：应用层草稿包，负责保存和操作玩家当前回合尚未提交的落子草稿
- 包含类：DraftManager、DraftPlacement、TurnDraft

### 文件：`src/main/java/com/kotva/application/draft/DraftManager.java`

#### 类：`DraftManager`

- 类型：class
- 类作用：管理当前回合草稿中的字牌放置操作
- 包含方法：placeTile
- 继承/实现：无
- 引用关系：Position 用于表示棋盘坐标

| 方法 | 作用 |
| --- | --- |
| `placeTile` | 把指定字牌放入当前回合草稿的目标坐标 |

### 文件：`src/main/java/com/kotva/application/draft/DraftPlacement.java`

#### 类：`DraftPlacement`

- 类型：class
- 类作用：表示一枚待提交字牌在草稿中的位置数据
- 包含方法：DraftPlacement、getTileId、getPosition、setTileId、setPosition
- 继承/实现：无
- 引用关系：Position 用于表示棋盘坐标

| 方法 | 作用 |
| --- | --- |
| `DraftPlacement` | 构造方法：初始化 DraftPlacement 所需的基础状态 |
| `getTileId` | 获取字牌标识 |
| `getPosition` | 获取坐标 |
| `setTileId` | 设置字牌标识 |
| `setPosition` | 设置坐标 |

### 文件：`src/main/java/com/kotva/application/draft/TurnDraft.java`

#### 类：`TurnDraft`

- 类型：class
- 类作用：保存当前玩家回合内的暂存落子、原始牌架槽位和预览结果
- 包含方法：TurnDraft、getPlacements、getPreviewResult、getOriginalRackSlots、getDraggingTileId、setDraggingTileId、setPreviewResult
- 继承/实现：无
- 引用关系：PreviewResult 用于返回走子预览结果

| 方法 | 作用 |
| --- | --- |
| `TurnDraft` | 构造方法：初始化 TurnDraft 所需的基础状态 |
| `getPlacements` | 获取Placements |
| `getPreviewResult` | 获取预览结果 |
| `getOriginalRackSlots` | 获取原始牌架槽位列表 |
| `getDraggingTileId` | 获取拖拽字牌字牌标识 |
| `setDraggingTileId` | 设置拖拽字牌字牌标识 |
| `setPreviewResult` | 设置预览结果 |

## 包：`com.kotva.application.preview`

- 包作用：应用层预览包，负责承载走子预览、高亮和单词预估结果
- 包含类：BoardHighlight、PreviewResult、PreviewWord

### 文件：`src/main/java/com/kotva/application/preview/BoardHighlight.java`

#### 类：`BoardHighlight`

- 类型：class
- 类作用：预留的棋盘高亮数据类型，用于承接走子预览中的高亮信息
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/application/preview/PreviewResult.java`

#### 类：`PreviewResult`

- 类型：class
- 类作用：封装一次走子预览的合法性、预估分数、高亮和提示信息
- 包含方法：PreviewResult、isValid、getEstimatedScore、getWordList、getHighlights、getMessages
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `PreviewResult` | 构造方法：初始化 PreviewResult 所需的基础状态 |
| `isValid` | 判断是否是否合法 |
| `getEstimatedScore` | 获取预估分数 |
| `getWordList` | 获取单词List |
| `getHighlights` | 获取高亮列表 |
| `getMessages` | 获取提示信息列表 |

### 文件：`src/main/java/com/kotva/application/preview/PreviewWord.java`

#### 类：`PreviewWord`

- 类型：class
- 类作用：表示预览阶段识别出的单词及其得分贡献与覆盖坐标
- 包含方法：getWordType、getWord、getScoreContribution、getCoveredPositions
- 继承/实现：无
- 引用关系：Position 用于表示棋盘坐标；WordType 用于表示单词类型

| 方法 | 作用 |
| --- | --- |
| `getWordType` | 获取单词类型 |
| `getWord` | 获取单词 |
| `getScoreContribution` | 获取分数Contribution |
| `getCoveredPositions` | 获取覆盖坐标列表 |

## 包：`com.kotva.application.result`

- 包作用：应用层结算结果包，负责封装终局结算、棋盘快照和排名结果
- 包含类：BoardCellSnapshot、BoardSnapshot、GameEndReason、PlayerSettlement、SettlementResult

### 文件：`src/main/java/com/kotva/application/result/BoardCellSnapshot.java`

#### 类：`BoardCellSnapshot`

- 类型：class
- 类作用：封装结算时单个棋盘格子的快照数据
- 包含方法：BoardCellSnapshot、getRow、getCol、getBonusType、getLetter、isBlank
- 继承/实现：无
- 引用关系：BonusType 用于识别奖励格类型

| 方法 | 作用 |
| --- | --- |
| `BoardCellSnapshot` | 构造方法：初始化 BoardCellSnapshot 所需的基础状态 |
| `getRow` | 获取行 |
| `getCol` | 获取列 |
| `getBonusType` | 获取奖励类型 |
| `getLetter` | 获取字母 |
| `isBlank` | 判断是否Blank |

### 文件：`src/main/java/com/kotva/application/result/BoardSnapshot.java`

#### 类：`BoardSnapshot`

- 类型：class
- 类作用：封装结算时整个棋盘的快照数据
- 包含方法：BoardSnapshot、getCells
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `BoardSnapshot` | 构造方法：初始化 BoardSnapshot 所需的基础状态 |
| `getCells` | 获取格子列表 |

### 文件：`src/main/java/com/kotva/application/result/GameEndReason.java`

#### 类：`GameEndReason`

- 类型：enum
- 类作用：枚举对局结束原因
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/application/result/PlayerSettlement.java`

#### 类：`PlayerSettlement`

- 类型：class
- 类作用：封装单个玩家的结算分数和名次
- 包含方法：PlayerSettlement、getPlayerName、getFinalScore、getRank
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `PlayerSettlement` | 构造方法：初始化 PlayerSettlement 所需的基础状态 |
| `getPlayerName` | 获取玩家名称 |
| `getFinalScore` | 获取Final分数 |
| `getRank` | 获取名次 |

### 文件：`src/main/java/com/kotva/application/result/SettlementResult.java`

#### 类：`SettlementResult`

- 类型：class
- 类作用：汇总整局对局的结算原因、排名、摘要和棋盘快照
- 包含方法：SettlementResult、getEndReason、getRankings、getSummaryMessages、getBoardSnapshot
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `SettlementResult` | 构造方法：初始化 SettlementResult 所需的基础状态 |
| `getEndReason` | 获取结束原因 |
| `getRankings` | 获取排名列表 |
| `getSummaryMessages` | 获取摘要提示信息列表 |
| `getBoardSnapshot` | 获取棋盘快照 |

## 包：`com.kotva.application.service`

- 包作用：应用层服务包，负责定义并实现开局、行棋、计时、预览和结算相关服务
- 包含类：ClockService、ClockServiceImpl、GameApplicationService、GameApplicationServiceImpl、GameSetupService、GameSetupServiceImpl、MovePreviewService、NoOpSettlementNavigationPort、SettlementNavigationPort、SettlementService、SettlementServiceImpl、SubmitDraftResult、TurnTransitionResult

### 文件：`src/main/java/com/kotva/application/service/ClockService.java`

#### 类：`ClockService`

- 类型：interface
- 类作用：定义回合计时相关操作接口
- 包含方法：startTurnClock、stopTurnClock、tick、handleTimeout
- 继承/实现：无
- 引用关系：GameSession 用于承载当前会话、配置与对局状态

| 方法 | 作用 |
| --- | --- |
| `startTurnClock` | 启动当前玩家的回合计时 |
| `stopTurnClock` | 停止当前会话的回合计时 |
| `tick` | 按传入的时间增量推进当前计时状态 |
| `handleTimeout` | 处理当前玩家超时后的状态变化 |

### 文件：`src/main/java/com/kotva/application/service/ClockServiceImpl.java`

#### 类：`ClockServiceImpl`

- 类型：class
- 类作用：实现回合计时推进、读秒切换和超时淘汰逻辑
- 包含方法：startTurnClock、stopTurnClock、tick、handleTimeout、requireCurrentPlayer
- 继承/实现：实现 ClockService
- 引用关系：GameEndReason 用于标识对局结束原因；GameSession 用于承载当前会话、配置与对局状态；GameState 用于访问或更新当前对局状态；Player 用于访问玩家对象、分数、行动权或牌架；PlayerClock 用于访问或更新玩家计时状态；ClockPhase 用于区分计时阶段；SessionStatus 用于表示会话状态

| 方法 | 作用 |
| --- | --- |
| `startTurnClock` | 未提取到方法说明 |
| `stopTurnClock` | 未提取到方法说明 |
| `tick` | 未提取到方法说明 |
| `handleTimeout` | 未提取到方法说明 |
| `requireCurrentPlayer` | 获取当前正在行动的玩家 |

### 文件：`src/main/java/com/kotva/application/service/GameApplicationService.java`

#### 类：`GameApplicationService`

- 类型：interface
- 类作用：定义对局进行中的应用层操作接口
- 包含方法：placeDraftTile、moveDraftTile、removeDraftTile、recallAllDraftTiles、submitDraft、passTurn、confirmHotSeatHandoff、tickClock、getSessionSnapshot
- 继承/实现：无
- 引用关系：PreviewResult 用于返回走子预览结果；GameSession 用于承载当前会话、配置与对局状态；GameSessionSnapshot 用于配合当前类完成与 GameSessionSnapshot 相关的处理；Position 用于表示棋盘坐标

| 方法 | 作用 |
| --- | --- |
| `placeDraftTile` | 在当前回合草稿中放置字牌并返回预览结果 |
| `moveDraftTile` | 移动当前回合草稿中的字牌并返回预览结果 |
| `removeDraftTile` | 移除当前回合草稿中的指定字牌并返回预览结果 |
| `recallAllDraftTiles` | 撤回当前回合草稿中的全部字牌并返回预览结果 |
| `submitDraft` | 提交当前回合草稿并返回提交结果 |
| `passTurn` | 处理当前玩家跳过回合并返回切换结果 |
| `confirmHotSeatHandoff` | 确认热座模式下的玩家交接 |
| `tickClock` | 推进会话时钟并返回最新快照 |
| `getSessionSnapshot` | 生成并返回当前会话快照 |

### 文件：`src/main/java/com/kotva/application/service/GameApplicationServiceImpl.java`

#### 类：`GameApplicationServiceImpl`

- 类型：class
- 类作用：实现对局过程中的草稿操作、计时推进和会话快照获取逻辑
- 包含方法：GameApplicationServiceImpl、placeDraftTile、moveDraftTile、removeDraftTile、recallAllDraftTiles、submitDraft、passTurn、confirmHotSeatHandoff、tickClock、getSessionSnapshot
- 继承/实现：实现 GameApplicationService
- 引用关系：PreviewResult 用于返回走子预览结果；GameSession 用于承载当前会话、配置与对局状态；GameSessionSnapshot 用于配合当前类完成与 GameSessionSnapshot 相关的处理；PlayerClockSnapshot 用于配合当前类完成与 PlayerClockSnapshot 相关的处理；Player 用于访问玩家对象、分数、行动权或牌架；PlayerClock 用于访问或更新玩家计时状态；Position 用于表示棋盘坐标；ClockPhase 用于区分计时阶段

| 方法 | 作用 |
| --- | --- |
| `GameApplicationServiceImpl` | 构造方法：初始化 GameApplicationServiceImpl 所需的基础状态 |
| `placeDraftTile` | 未提取到方法说明 |
| `moveDraftTile` | 未提取到方法说明 |
| `removeDraftTile` | 未提取到方法说明 |
| `recallAllDraftTiles` | 未提取到方法说明 |
| `submitDraft` | 未提取到方法说明 |
| `passTurn` | 未提取到方法说明 |
| `confirmHotSeatHandoff` | 未提取到方法说明 |
| `tickClock` | 未提取到方法说明 |
| `getSessionSnapshot` | 未提取到方法说明 |

### 文件：`src/main/java/com/kotva/application/service/GameSetupService.java`

#### 类：`GameSetupService`

- 类型：interface
- 类作用：定义开局配置构建和新对局创建接口
- 包含方法：buildConfig、startNewGame
- 继承/实现：无
- 引用关系：GameConfig 用于提供开局配置；GameSession 用于承载当前会话、配置与对局状态；NewGameRequest 用于承接新对局请求参数

| 方法 | 作用 |
| --- | --- |
| `buildConfig` | 根据请求参数构建合法的对局配置 |
| `startNewGame` | 根据请求参数创建并启动一局新游戏 |

### 文件：`src/main/java/com/kotva/application/service/GameSetupServiceImpl.java`

#### 类：`GameSetupServiceImpl`

- 类型：class
- 类作用：负责校验新对局请求、构建配置、创建玩家并启动新会话
- 包含方法：GameSetupServiceImpl、buildConfig、startNewGame、createSession、createPlayers、createPlayerClock、normalizePlayerName
- 继承/实现：实现 GameSetupService
- 引用关系：GameConfig 用于提供开局配置；GameSession 用于承载当前会话、配置与对局状态；PlayerConfig 用于描述单个玩家配置；TimeControlConfig 用于提供时间控制参数；NewGameRequest 用于承接新对局请求参数；GameState 用于访问或更新当前对局状态；Player 用于访问玩家对象、分数、行动权或牌架；PlayerClock 用于访问或更新玩家计时状态；DictionaryRepository 用于查询词典是否合法；GameMode 用于配合当前类完成与 GameMode 相关的处理；PlayerController 用于接收并产出玩家动作；SessionStatus 用于表示会话状态；PlayerType 用于区分玩家控制器类型

| 方法 | 作用 |
| --- | --- |
| `GameSetupServiceImpl` | 构造方法：初始化 GameSetupServiceImpl 所需的基础状态 |
| `buildConfig` | 未提取到方法说明 |
| `startNewGame` | 未提取到方法说明 |
| `createSession` | 根据配置创建新的对局会话 |
| `createPlayers` | 根据配置创建玩家列表 |
| `createPlayerClock` | 根据时间配置创建玩家时钟 |
| `normalizePlayerName` | 规范化并校验玩家名称 |

### 文件：`src/main/java/com/kotva/application/service/MovePreviewService.java`

#### 类：`MovePreviewService`

- 类型：interface
- 类作用：定义走子预览能力接口
- 包含方法：preview
- 继承/实现：无
- 引用关系：PreviewResult 用于返回走子预览结果；GameSession 用于承载当前会话、配置与对局状态

| 方法 | 作用 |
| --- | --- |
| `preview` | 生成当前草稿的走子预览结果 |

### 文件：`src/main/java/com/kotva/application/service/NoOpSettlementNavigationPort.java`

#### 类：`NoOpSettlementNavigationPort`

- 类型：class
- 类作用：提供一个空实现的结算跳转端口，便于在无界面场景下调用结算
- 包含方法：showSettlement
- 继承/实现：实现 SettlementNavigationPort
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `showSettlement` | 未提取到方法说明 |

### 文件：`src/main/java/com/kotva/application/service/SettlementNavigationPort.java`

#### 类：`SettlementNavigationPort`

- 类型：interface
- 类作用：定义结算结果展示或跳转的端口接口
- 包含方法：showSettlement
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `showSettlement` | 展示或记录结算结果 |

### 文件：`src/main/java/com/kotva/application/service/SettlementService.java`

#### 类：`SettlementService`

- 类型：interface
- 类作用：定义对局结束后的结算接口
- 包含方法：settle
- 继承/实现：无
- 引用关系：GameEndReason 用于标识对局结束原因；GameState 用于访问或更新当前对局状态

| 方法 | 作用 |
| --- | --- |
| `settle` | 根据终局状态生成并返回结算结果 |

### 文件：`src/main/java/com/kotva/application/service/SettlementServiceImpl.java`

#### 类：`SettlementServiceImpl`

- 类型：class
- 类作用：根据终局状态生成排名、摘要消息和棋盘快照，并触发结算展示
- 包含方法：SettlementServiceImpl、SettlementServiceImpl、settle、buildRankings、buildSummaryMessages、buildEndReasonMessage、buildBoardSnapshot
- 继承/实现：实现 SettlementService
- 引用关系：BoardCellSnapshot 用于封装单格快照；BoardSnapshot 用于封装棋盘快照；GameEndReason 用于标识对局结束原因；PlayerSettlement 用于封装玩家结算结果；Board 用于访问棋盘结构；Cell 用于访问单个格子的内容和奖励信息；GameState 用于访问或更新当前对局状态；Player 用于访问玩家对象、分数、行动权或牌架；Position 用于表示棋盘坐标；Tile 用于访问字牌字母、分值和空白牌状态

| 方法 | 作用 |
| --- | --- |
| `SettlementServiceImpl` | 构造方法：初始化 SettlementServiceImpl 所需的基础状态 |
| `SettlementServiceImpl` | 构造方法：初始化 SettlementServiceImpl 所需的基础状态 |
| `settle` | 未提取到方法说明 |
| `buildRankings` | 构建结算排名列表 |
| `buildSummaryMessages` | 构建结算摘要消息列表 |
| `buildEndReasonMessage` | 根据结束原因生成对应的说明文本 |
| `buildBoardSnapshot` | 根据当前棋盘生成结算快照 |

### 文件：`src/main/java/com/kotva/application/service/SubmitDraftResult.java`

#### 类：`SubmitDraftResult`

- 类型：class
- 类作用：预留的草稿提交结果类型
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/application/service/TurnTransitionResult.java`

#### 类：`TurnTransitionResult`

- 类型：class
- 类作用：预留的回合切换结果类型
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/test/java/com/kotva/application/service/ClockServiceImplTest.java`

#### 类：`ClockServiceImplTest`

- 类型：class
- 类作用：测试计时服务实现的关键行为
- 包含方法：tickMovesFromMainTimeIntoByoYomiAndSnapshotReflectsChanges、tickTimeoutEliminatesCurrentPlayerAndEndsTwoPlayerGame、createTimedSession
- 继承/实现：无
- 引用关系：GameEndReason 用于标识对局结束原因；GameSessionSnapshot 用于配合当前类完成与 GameSessionSnapshot 相关的处理；GameSession 用于承载当前会话、配置与对局状态；TimeControlConfig 用于提供时间控制参数；NewGameRequest 用于承接新对局请求参数；Player 用于访问玩家对象、分数、行动权或牌架；TurnDraft 用于保存当前回合草稿数据；DictionaryRepository 用于查询词典是否合法；GameMode 用于配合当前类完成与 GameMode 相关的处理；ClockPhase 用于区分计时阶段；DictionaryType 用于区分词典类型；SessionStatus 用于表示会话状态；Test 用于标记测试方法

| 方法 | 作用 |
| --- | --- |
| `tickMovesFromMainTimeIntoByoYomiAndSnapshotReflectsChanges` | 未提取到方法说明 |
| `tickTimeoutEliminatesCurrentPlayerAndEndsTwoPlayerGame` | 未提取到方法说明 |
| `createTimedSession` | 创建Timed会话 |

#### 类：`StubDictionaryRepository`

- 类型：class
- 类作用：测试替身类，用于在测试中提供可控的词典行为
- 包含方法：loadDictionary、getDictionary、getLoadedDictionaryType、isAccepted
- 继承/实现：继承 DictionaryRepository
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `loadDictionary` | 未提取到方法说明 |
| `getDictionary` | 未提取到方法说明 |
| `getLoadedDictionaryType` | 未提取到方法说明 |
| `isAccepted` | 未提取到方法说明 |

### 文件：`src/test/java/com/kotva/application/service/GameSetupServiceImplTest.java`

#### 类：`GameSetupServiceImplTest`

- 类型：class
- 类作用：测试开局服务实现的配置校验与建局行为
- 包含方法：buildConfigNormalizesHotSeatPlayersAndTimeControl、buildConfigRejectsInvalidSetupInputs、startNewGameInitializesSessionDictionaryOrderAndTiles、startNewGameUsesStableRandomizedPlayerOrder、startNewGameLeavesUnlimitedGamesWithDisabledClocks、extractPlayerNames、countRackTiles
- 继承/实现：无
- 引用关系：GameConfig 用于提供开局配置；GameSession 用于承载当前会话、配置与对局状态；TimeControlConfig 用于提供时间控制参数；NewGameRequest 用于承接新对局请求参数；Player 用于访问玩家对象、分数、行动权或牌架；RackSlot 用于配合当前类完成与 RackSlot 相关的处理；DictionaryRepository 用于查询词典是否合法；GameMode 用于配合当前类完成与 GameMode 相关的处理；ClockPhase 用于区分计时阶段；DictionaryType 用于区分词典类型；PlayerType 用于区分玩家控制器类型；SessionStatus 用于表示会话状态；Test 用于标记测试方法

| 方法 | 作用 |
| --- | --- |
| `buildConfigNormalizesHotSeatPlayersAndTimeControl` | 未提取到方法说明 |
| `buildConfigRejectsInvalidSetupInputs` | 未提取到方法说明 |
| `startNewGameInitializesSessionDictionaryOrderAndTiles` | 未提取到方法说明 |
| `startNewGameUsesStableRandomizedPlayerOrder` | 未提取到方法说明 |
| `startNewGameLeavesUnlimitedGamesWithDisabledClocks` | 未提取到方法说明 |
| `extractPlayerNames` | 提取会话中的玩家名称列表 |
| `countRackTiles` | 统计玩家牌架中的字牌数量 |

#### 类：`StubDictionaryRepository`

- 类型：class
- 类作用：测试替身类，用于在测试中提供可控的词典行为
- 包含方法：loadDictionary、getDictionary、getLoadedDictionaryType、isAccepted
- 继承/实现：继承 DictionaryRepository
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `loadDictionary` | 未提取到方法说明 |
| `getDictionary` | 未提取到方法说明 |
| `getLoadedDictionaryType` | 未提取到方法说明 |
| `isAccepted` | 未提取到方法说明 |

### 文件：`src/test/java/com/kotva/application/service/SettlementServiceImplTest.java`

#### 类：`SettlementServiceImplTest`

- 类型：class
- 类作用：测试结算服务实现的结果生成行为
- 包含方法：settlementIncludesRankingNamesReasonAndBoardSnapshot、createPlayer、assertFalseOrMissingBlank
- 继承/实现：无
- 引用关系：BoardCellSnapshot 用于封装单格快照；GameEndReason 用于标识对局结束原因；PlayerSettlement 用于封装玩家结算结果；GameState 用于访问或更新当前对局状态；Player 用于访问玩家对象、分数、行动权或牌架；Tile 用于访问字牌字母、分值和空白牌状态；PlayerController 用于接收并产出玩家动作；BonusType 用于识别奖励格类型；PlayerType 用于区分玩家控制器类型；Test 用于标记测试方法

| 方法 | 作用 |
| --- | --- |
| `settlementIncludesRankingNamesReasonAndBoardSnapshot` | 未提取到方法说明 |
| `createPlayer` | 创建一个测试用玩家对象 |
| `assertFalseOrMissingBlank` | 校验空白牌标记是否符合预期 |

#### 类：`RecordingNavigationPort`

- 类型：class
- 类作用：测试替身类，用于记录结算展示调用结果
- 包含方法：showSettlement
- 继承/实现：实现 SettlementNavigationPort
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `showSettlement` | 未提取到方法说明 |

## 包：`com.kotva.application.session`

- 包作用：应用层会话包，负责保存对局配置、会话状态与界面快照数据
- 包含类：GameConfig、GameSession、GameSessionSnapshot、PlayerClockSnapshot、PlayerConfig、RoundPassTracker、TimeControlConfig

### 文件：`src/main/java/com/kotva/application/session/GameConfig.java`

#### 类：`GameConfig`

- 类型：class
- 类作用：封装一局游戏的模式、玩家、词典和计时配置
- 包含方法：GameConfig、getGameMode、getPlayers、getPlayerCount、getDictionaryType、getTimeControlConfig、hasTimeControl
- 继承/实现：无
- 引用关系：GameMode 用于配合当前类完成与 GameMode 相关的处理；DictionaryType 用于区分词典类型

| 方法 | 作用 |
| --- | --- |
| `GameConfig` | 构造方法：初始化 GameConfig 所需的基础状态 |
| `getGameMode` | 获取对局模式 |
| `getPlayers` | 获取玩家列表 |
| `getPlayerCount` | 获取玩家数量 |
| `getDictionaryType` | 获取词典类型 |
| `getTimeControlConfig` | 获取时间Control配置 |
| `hasTimeControl` | 判断是否具有时间Control |

### 文件：`src/main/java/com/kotva/application/session/GameSession.java`

#### 类：`GameSession`

- 类型：class
- 类作用：封装一次对局会话，集中保存配置、领域状态、草稿、回合协调器和结算服务
- 包含方法：GameSession、GameSession、GameSession、getSessionId、getConfig、getGameState、getTurnDraft、resetTurnDraft、getSessionStatus、setSessionStatus、getRoundPassTracker、getSettlementService、getTurnCoordinator
- 继承/实现：无
- 引用关系：TurnCoordinator 用于配合当前类完成与 TurnCoordinator 相关的处理；TurnDraft 用于保存当前回合草稿数据；GameState 用于访问或更新当前对局状态；SessionStatus 用于表示会话状态

| 方法 | 作用 |
| --- | --- |
| `GameSession` | 构造方法：初始化 GameSession 所需的基础状态 |
| `GameSession` | 构造方法：初始化 GameSession 所需的基础状态 |
| `GameSession` | 构造方法：初始化 GameSession 所需的基础状态 |
| `getSessionId` | 获取会话标识 |
| `getConfig` | 获取配置 |
| `getGameState` | 获取对局State |
| `getTurnDraft` | 获取Turn草稿 |
| `resetTurnDraft` | 重置当前会话中的回合草稿 |
| `getSessionStatus` | 获取会话Status |
| `setSessionStatus` | 设置会话Status |
| `getRoundPassTracker` | 获取轮次pass 状态Tracker |
| `getSettlementService` | 获取结算Service |
| `getTurnCoordinator` | 获取TurnCoordinator |

### 文件：`src/main/java/com/kotva/application/session/GameSessionSnapshot.java`

#### 类：`GameSessionSnapshot`

- 类型：class
- 类作用：封装供界面读取的会话快照
- 包含方法：GameSessionSnapshot、getSessionStatus、getCurrentPlayerId、getCurrentPlayerName、getCurrentPlayerMainTimeRemainingMillis、getCurrentPlayerByoYomiRemainingMillis、getCurrentPlayerClockPhase、getPlayerClockSnapshots
- 继承/实现：无
- 引用关系：ClockPhase 用于区分计时阶段；SessionStatus 用于表示会话状态

| 方法 | 作用 |
| --- | --- |
| `GameSessionSnapshot` | 构造方法：初始化 GameSessionSnapshot 所需的基础状态 |
| `getSessionStatus` | 获取会话Status |
| `getCurrentPlayerId` | 获取当前玩家标识 |
| `getCurrentPlayerName` | 获取当前玩家名称 |
| `getCurrentPlayerMainTimeRemainingMillis` | 获取当前玩家主时间剩余毫秒数 |
| `getCurrentPlayerByoYomiRemainingMillis` | 获取当前玩家读秒剩余毫秒数 |
| `getCurrentPlayerClockPhase` | 获取当前玩家计时阶段 |
| `getPlayerClockSnapshots` | 获取玩家计时Snapshots |

### 文件：`src/main/java/com/kotva/application/session/PlayerClockSnapshot.java`

#### 类：`PlayerClockSnapshot`

- 类型：class
- 类作用：封装单个玩家的时钟快照
- 包含方法：PlayerClockSnapshot、getPlayerId、getPlayerName、getMainTimeRemainingMillis、getByoYomiRemainingMillis、getPhase、isActive
- 继承/实现：无
- 引用关系：ClockPhase 用于区分计时阶段

| 方法 | 作用 |
| --- | --- |
| `PlayerClockSnapshot` | 构造方法：初始化 PlayerClockSnapshot 所需的基础状态 |
| `getPlayerId` | 获取玩家标识 |
| `getPlayerName` | 获取玩家名称 |
| `getMainTimeRemainingMillis` | 获取主时间剩余毫秒数 |
| `getByoYomiRemainingMillis` | 获取读秒剩余毫秒数 |
| `getPhase` | 获取阶段 |
| `isActive` | 判断是否有效状态 |

### 文件：`src/main/java/com/kotva/application/session/PlayerConfig.java`

#### 类：`PlayerConfig`

- 类型：class
- 类作用：封装单个玩家的开局配置
- 包含方法：PlayerConfig、getPlayerName、getPlayerType
- 继承/实现：无
- 引用关系：PlayerType 用于区分玩家控制器类型

| 方法 | 作用 |
| --- | --- |
| `PlayerConfig` | 构造方法：初始化 PlayerConfig 所需的基础状态 |
| `getPlayerName` | 获取玩家名称 |
| `getPlayerType` | 获取玩家类型 |

### 文件：`src/main/java/com/kotva/application/session/RoundPassTracker.java`

#### 类：`RoundPassTracker`

- 类型：class
- 类作用：记录本轮哪些玩家已经选择 pass
- 包含方法：markPassed、reset、hasPassed
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `markPassed` | 记录指定玩家本轮已经 pass |
| `reset` | 重置当前对象维护的状态 |
| `hasPassed` | 判断是否具有Passed |

### 文件：`src/main/java/com/kotva/application/session/TimeControlConfig.java`

#### 类：`TimeControlConfig`

- 类型：class
- 类作用：封装主时间和读秒参数
- 包含方法：TimeControlConfig、getMainTimeMillis、getByoYomiMillisPerTurn
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `TimeControlConfig` | 构造方法：初始化 TimeControlConfig 所需的基础状态 |
| `getMainTimeMillis` | 获取主时间毫秒数 |
| `getByoYomiMillisPerTurn` | 获取读秒毫秒数PerTurn |

## 包：`com.kotva.application.setup`

- 包作用：应用层开局请求包，负责承接新对局创建时的输入参数
- 包含类：NewGameRequest

### 文件：`src/main/java/com/kotva/application/setup/NewGameRequest.java`

#### 类：`NewGameRequest`

- 类型：class
- 类作用：封装创建新对局时的请求参数
- 包含方法：NewGameRequest、getGameMode、getPlayerCount、getPlayerNames、getDictionaryType、getTimeControlConfig
- 继承/实现：无
- 引用关系：TimeControlConfig 用于提供时间控制参数；GameMode 用于配合当前类完成与 GameMode 相关的处理；DictionaryType 用于区分词典类型

| 方法 | 作用 |
| --- | --- |
| `NewGameRequest` | 构造方法：初始化 NewGameRequest 所需的基础状态 |
| `getGameMode` | 获取对局模式 |
| `getPlayerCount` | 获取玩家数量 |
| `getPlayerNames` | 获取玩家名称列表 |
| `getDictionaryType` | 获取词典类型 |
| `getTimeControlConfig` | 获取时间Control配置 |

## 包：`com.kotva.domain`

- 包作用：领域层入口包，负责提供规则引擎和通用领域对象
- 包含类：Position、RuleEngine

### 文件：`src/main/java/com/kotva/domain/Position.java`

#### 类：`Position`

- 类型：class
- 类作用：表示棋盘中的坐标数据
- 包含方法：Position
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `Position` | 构造方法：初始化 Position 所需的基础状态 |

### 文件：`src/main/java/com/kotva/domain/RuleEngine.java`

#### 类：`RuleEngine`

- 类型：class
- 类作用：组合走子校验、单词提取和词典校验，判断一次落子是否合法
- 包含方法：RuleEngine、validateMove
- 继承/实现：无
- 引用关系：PlayerAction 用于配合当前类完成与 PlayerAction 相关的处理；DraftPlacement 用于描述单个草稿落子位置；TurnDraft 用于保存当前回合草稿数据；Board 用于访问棋盘结构；GameState 用于访问或更新当前对局状态；Position 用于表示棋盘坐标；TileBag 用于抽牌或按编号查询字牌；CandidateWord 用于封装提取出的候选单词；MoveValidator 用于执行走子合法性校验；WordExtractor 用于提取本次落子形成的单词；DictionaryRepository 用于查询词典是否合法

| 方法 | 作用 |
| --- | --- |
| `RuleEngine` | 构造方法：初始化 RuleEngine 所需的基础状态 |
| `validateMove` | 校验当前玩家落子动作是否合法 |

## 包：`com.kotva.domain.model`

- 包作用：领域模型包，负责定义棋盘、玩家、牌袋、牌架等核心业务实体
- 包含类：Board、Cell、GameState、Player、PlayerClock、Position、Rack、RackSlot、Tile、TileBag、TilePlacement

### 文件：`src/main/java/com/kotva/domain/model/Board.java`

#### 类：`Board`

- 类型：class
- 类作用：表示 15x15 棋盘及其奖励格布局，并提供格子访问能力
- 包含方法：Board、getCell、parseBonusType、isEmpty
- 继承/实现：无
- 引用关系：BonusType 用于识别奖励格类型

| 方法 | 作用 |
| --- | --- |
| `Board` | 构造方法：初始化 Board 所需的基础状态 |
| `getCell` | 获取格子 |
| `parseBonusType` | 把奖励格字符转换为奖励类型 |
| `isEmpty` | 判断是否Empty |

### 文件：`src/main/java/com/kotva/domain/model/Cell.java`

#### 类：`Cell`

- 类型：class
- 类作用：表示棋盘上的单个格子，保存坐标、奖励类型和已放置字牌
- 包含方法：Cell、getPosition、getBonusType、getPlacedTile、isEmpty、setPlacedTile
- 继承/实现：无
- 引用关系：BonusType 用于识别奖励格类型

| 方法 | 作用 |
| --- | --- |
| `Cell` | 构造方法：初始化 Cell 所需的基础状态 |
| `getPosition` | 获取坐标 |
| `getBonusType` | 获取奖励类型 |
| `getPlacedTile` | 获取Placed字牌 |
| `isEmpty` | 判断是否Empty |
| `setPlacedTile` | 设置Placed字牌 |

### 文件：`src/main/java/com/kotva/domain/model/GameState.java`

#### 类：`GameState`

- 类型：class
- 类作用：保存整局游戏的核心领域状态，包括棋盘、牌袋、玩家和当前轮到谁
- 包含方法：GameState、getBoard、getTileBag、getPlayers、getCurrentPlayerIndex、getCurrentPlayer、requireCurrentActivePlayer、advanceToNextActivePlayer、getActivePlayerCount、hasActivePlayers、getPlayerById、nextTurn、markGameOver、isGameOver、getGameEndReason、initialDraw
- 继承/实现：无
- 引用关系：GameEndReason 用于标识对局结束原因

| 方法 | 作用 |
| --- | --- |
| `GameState` | 构造方法：初始化 GameState 所需的基础状态 |
| `getBoard` | 获取棋盘 |
| `getTileBag` | 获取字牌牌袋 |
| `getPlayers` | 获取玩家列表 |
| `getCurrentPlayerIndex` | 获取当前玩家索引 |
| `getCurrentPlayer` | 获取当前玩家 |
| `requireCurrentActivePlayer` | 获取当前应当行动的有效玩家，不存在时抛出异常 |
| `advanceToNextActivePlayer` | 切换到下一位仍然处于有效状态的玩家 |
| `getActivePlayerCount` | 获取有效状态玩家数量 |
| `hasActivePlayers` | 判断是否具有有效状态玩家列表 |
| `getPlayerById` | 获取玩家By标识 |
| `nextTurn` | 切换到下一位有效玩家 |
| `markGameOver` | 将对局标记为结束并记录结束原因 |
| `isGameOver` | 判断是否对局Over |
| `getGameEndReason` | 获取对局结束原因 |
| `initialDraw` | 为所有玩家执行开局摸牌 |

### 文件：`src/main/java/com/kotva/domain/model/Player.java`

#### 类：`Player`

- 类型：class
- 类作用：表示对局中的玩家实体，保存身份、得分、牌架、控制器和时钟
- 包含方法：Player、getActive、setActive、getPlayerId、getPlayerName、getPlayerType、getController、getScore、getRack、addScore、setController、getClock、setClock
- 继承/实现：无
- 引用关系：PlayerController 用于接收并产出玩家动作；PlayerType 用于区分玩家控制器类型

| 方法 | 作用 |
| --- | --- |
| `Player` | 构造方法：初始化 Player 所需的基础状态 |
| `getActive` | 获取有效状态 |
| `setActive` | 设置有效状态 |
| `getPlayerId` | 获取玩家标识 |
| `getPlayerName` | 获取玩家名称 |
| `getPlayerType` | 获取玩家类型 |
| `getController` | 获取控制器 |
| `getScore` | 获取分数 |
| `getRack` | 获取牌架 |
| `addScore` | 处理 addScore 相关逻辑 |
| `setController` | 设置控制器 |
| `getClock` | 获取计时 |
| `setClock` | 设置计时 |

### 文件：`src/main/java/com/kotva/domain/model/PlayerClock.java`

#### 类：`PlayerClock`

- 类型：class
- 类作用：表示单个玩家的计时状态，包括主时间、读秒和阶段
- 包含方法：PlayerClock、disabled、timed、getMainTimeRemainingMillis、setMainTimeRemainingMillis、getByoYomiPerTurnMillis、getByoYomiRemainingMillis、setByoYomiRemainingMillis、getPhase、setPhase、isEnabled、resetByoYomiTurn
- 继承/实现：无
- 引用关系：ClockPhase 用于区分计时阶段

| 方法 | 作用 |
| --- | --- |
| `PlayerClock` | 构造方法：初始化 PlayerClock 所需的基础状态 |
| `disabled` | 处理 disabled 相关逻辑 |
| `timed` | 处理 timed 相关逻辑 |
| `getMainTimeRemainingMillis` | 获取主时间剩余毫秒数 |
| `setMainTimeRemainingMillis` | 设置主时间剩余毫秒数 |
| `getByoYomiPerTurnMillis` | 获取读秒PerTurn毫秒数 |
| `getByoYomiRemainingMillis` | 获取读秒剩余毫秒数 |
| `setByoYomiRemainingMillis` | 设置读秒剩余毫秒数 |
| `getPhase` | 获取阶段 |
| `setPhase` | 设置阶段 |
| `isEnabled` | 判断是否Enabled |
| `resetByoYomiTurn` | 重置当前玩家本回合的读秒时间 |

### 文件：`src/main/java/com/kotva/domain/model/Position.java`

#### 类：`Position`

- 类型：class
- 类作用：表示棋盘中的坐标数据
- 包含方法：Position、getRow、getCol
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `Position` | 构造方法：初始化 Position 所需的基础状态 |
| `getRow` | 获取行 |
| `getCol` | 获取列 |

### 文件：`src/main/java/com/kotva/domain/model/Rack.java`

#### 类：`Rack`

- 类型：class
- 类作用：表示玩家的牌架及其槽位集合
- 包含方法：Rack、setTileAt、getSlots、isEmpty
- 继承/实现：无
- 引用关系：Arrays 用于配合当前类完成与 Arrays 相关的处理

| 方法 | 作用 |
| --- | --- |
| `Rack` | 构造方法：初始化 Rack 所需的基础状态 |
| `setTileAt` | 设置字牌At |
| `getSlots` | 获取槽位列表 |
| `isEmpty` | 判断是否Empty |

### 文件：`src/main/java/com/kotva/domain/model/RackSlot.java`

#### 类：`RackSlot`

- 类型：class
- 类作用：表示牌架中的单个槽位
- 包含方法：RackSlot、getIndex、getTile、setTile、clearSlot、isEmpty
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `RackSlot` | 构造方法：初始化 RackSlot 所需的基础状态 |
| `getIndex` | 获取索引 |
| `getTile` | 获取字牌 |
| `setTile` | 设置字牌 |
| `clearSlot` | 处理 clearSlot 相关逻辑 |
| `isEmpty` | 判断是否Empty |

### 文件：`src/main/java/com/kotva/domain/model/Tile.java`

#### 类：`Tile`

- 类型：class
- 类作用：表示一枚字牌及其字母、分值和空白牌信息
- 包含方法：Tile、getTileID、getLetter、getScore、isBlank、getAssignedLetter、setAssignedLetter
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `Tile` | 构造方法：初始化 Tile 所需的基础状态 |
| `getTileID` | 获取字牌标识 |
| `getLetter` | 获取字母 |
| `getScore` | 获取分数 |
| `isBlank` | 判断是否Blank |
| `getAssignedLetter` | 获取Assigned字母 |
| `setAssignedLetter` | 设置Assigned字母 |

### 文件：`src/main/java/com/kotva/domain/model/TileBag.java`

#### 类：`TileBag`

- 类型：class
- 类作用：管理牌袋中的字牌集合、抽牌和按编号查询逻辑
- 包含方法：TileBag、initialize、getScoreForLetter、drawRandomTile、drawTile、isEmpty、getTileById
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `TileBag` | 构造方法：初始化 TileBag 所需的基础状态 |
| `initialize` | 初始化当前对象内部数据 |
| `getScoreForLetter` | 根据字母返回对应的分值 |
| `drawRandomTile` | 随机抽取一枚字牌 |
| `drawTile` | 抽取一枚字牌 |
| `isEmpty` | 判断是否Empty |
| `getTileById` | 根据字牌标识查找对应字牌 |

### 文件：`src/main/java/com/kotva/domain/model/TilePlacement.java`

#### 类：`TilePlacement`

- 类型：class
- 类作用：封装一次正式落子的字牌与位置信息
- 包含方法：TilePlacement、getTileId、getPosition、getAssignedLetter
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `TilePlacement` | 构造方法：初始化 TilePlacement 所需的基础状态 |
| `getTileId` | 获取字牌标识 |
| `getPosition` | 获取坐标 |
| `getAssignedLetter` | 获取Assigned字母 |

## 包：`com.kotva.domain.utils`

- 包作用：领域工具包，负责执行走子校验、单词提取和分数计算等规则辅助逻辑
- 包含类：CandidateWord、MoveValidator、ScoreCalculator、WordExtractor

### 文件：`src/main/java/com/kotva/domain/utils/CandidateWord.java`

#### 类：`CandidateWord`

- 类型：class
- 类作用：封装从棋盘上提取出的候选单词及其起止坐标
- 包含方法：CandidateWord、getWord、getStartPosition、getEndPosition、equals、hashCode
- 继承/实现：无
- 引用关系：Position 用于表示棋盘坐标

| 方法 | 作用 |
| --- | --- |
| `CandidateWord` | 构造方法：初始化 CandidateWord 所需的基础状态 |
| `getWord` | 获取单词 |
| `getStartPosition` | 获取起始坐标 |
| `getEndPosition` | 获取结束坐标 |
| `equals` | 未提取到方法说明 |
| `hashCode` | 未提取到方法说明 |

### 文件：`src/main/java/com/kotva/domain/utils/MoveValidator.java`

#### 类：`MoveValidator`

- 类型：class
- 类作用：提供走子直线、首手、重叠和连通性校验工具
- 包含方法：MoveValidator、isStraightLine、firstMove、isNotOverlapping、isConnected、checkNeighbor
- 继承/实现：无
- 引用关系：Board 用于访问棋盘结构；Cell 用于访问单个格子的内容和奖励信息；Position 用于表示棋盘坐标

| 方法 | 作用 |
| --- | --- |
| `MoveValidator` | 构造方法：初始化 MoveValidator 所需的基础状态 |
| `isStraightLine` | 判断本次落子是否位于同一直线 |
| `firstMove` | 判断首手落子是否覆盖中心位置 |
| `isNotOverlapping` | 判断本次落子是否与已有字牌重叠 |
| `isConnected` | 判断本次落子是否与棋盘已有字牌连通 |
| `checkNeighbor` | 检查指定相邻坐标上是否存在已有字牌 |

### 文件：`src/main/java/com/kotva/domain/utils/ScoreCalculator.java`

#### 类：`ScoreCalculator`

- 类型：class
- 类作用：根据候选单词、奖励格和新落子计算本次得分
- 包含方法：ScoreCalculator、calculate
- 继承/实现：无
- 引用关系：DraftPlacement 用于描述单个草稿落子位置；TurnDraft 用于保存当前回合草稿数据；Board 用于访问棋盘结构；Cell 用于访问单个格子的内容和奖励信息；GameState 用于访问或更新当前对局状态；Position 用于表示棋盘坐标；Tile 用于访问字牌字母、分值和空白牌状态；TileBag 用于抽牌或按编号查询字牌；BonusType 用于识别奖励格类型

| 方法 | 作用 |
| --- | --- |
| `ScoreCalculator` | 构造方法：初始化 ScoreCalculator 所需的基础状态 |
| `calculate` | 计算本次落子形成的总得分 |

### 文件：`src/main/java/com/kotva/domain/utils/WordExtractor.java`

#### 类：`WordExtractor`

- 类型：class
- 类作用：根据草稿和棋盘提取本次落子形成的主词与交叉词
- 包含方法：WordExtractor、extract、extract、buildIndex、hasTileAt、collectHorizontalWord、collectVerticalWord、getLetterAt、resolveLetter、toPointKey
- 继承/实现：无
- 引用关系：LinkedHashSet 用于配合当前类完成与 LinkedHashSet 相关的处理；DraftPlacement 用于描述单个草稿落子位置；TurnDraft 用于保存当前回合草稿数据；Position 用于表示棋盘坐标；Tile 用于访问字牌字母、分值和空白牌状态；TileBag 用于抽牌或按编号查询字牌；Board 用于访问棋盘结构；Cell 用于访问单个格子的内容和奖励信息

| 方法 | 作用 |
| --- | --- |
| `WordExtractor` | 构造方法：初始化 WordExtractor 所需的基础状态 |
| `extract` | 提取本次草稿形成的候选单词 |
| `extract` | 提取本次草稿形成的候选单词 |
| `buildIndex` | 为草稿落子建立按坐标检索的索引 |
| `hasTileAt` | 判断指定坐标是否存在草稿字牌或棋盘字牌 |
| `collectHorizontalWord` | 收集经过指定坐标的横向单词 |
| `collectVerticalWord` | 收集经过指定坐标的纵向单词 |
| `getLetterAt` | 获取指定坐标上的字母内容 |
| `resolveLetter` | 解析草稿字牌实际代表的字母 |
| `toPointKey` | 把坐标转换为索引键 |

## 包：`com.kotva.infrastructure`

- 包作用：基础设施包，负责提供音频等外部资源相关能力
- 包含类：AudioManager

### 文件：`src/main/java/com/kotva/infrastructure/AudioManager.java`

#### 类：`AudioManager`

- 类型：class
- 类作用：预留的音频管理类
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

## 包：`com.kotva.infrastructure.dictionary`

- 包作用：基础设施词典包，负责加载词典文件并提供单词查询能力
- 包含类：DictionaryLoader、DictionaryRepository

### 文件：`src/main/java/com/kotva/infrastructure/dictionary/DictionaryLoader.java`

#### 类：`DictionaryLoader`

- 类型：class
- 类作用：根据词典类型加载对应词典文件
- 包含方法：DictionaryLoader、load、resolveDictionaryPath
- 继承/实现：无
- 引用关系：DictionaryType 用于区分词典类型；StandardCharsets 用于配合当前类完成与 StandardCharsets 相关的处理

| 方法 | 作用 |
| --- | --- |
| `DictionaryLoader` | 构造方法：初始化 DictionaryLoader 所需的基础状态 |
| `load` | 加载所需的数据或配置 |
| `resolveDictionaryPath` | 根据词典类型解析词典文件路径 |

### 文件：`src/main/java/com/kotva/infrastructure/dictionary/DictionaryRepository.java`

#### 类：`DictionaryRepository`

- 类型：class
- 类作用：管理词典的加载状态和单词查询
- 包含方法：loadDictionary、getDictionary、getLoadedDictionaryType、isAccepted、ensureDictionaryLoaded
- 继承/实现：无
- 引用关系：DictionaryType 用于区分词典类型

| 方法 | 作用 |
| --- | --- |
| `loadDictionary` | 加载词典 |
| `getDictionary` | 获取词典 |
| `getLoadedDictionaryType` | 获取已加载词典类型 |
| `isAccepted` | 判断是否Accepted |
| `ensureDictionaryLoaded` | 确保词典已经被正确加载 |

### 文件：`src/test/java/com/kotva/infrastructure/dictionary/DictionaryRepositorySmokeTest.java`

#### 类：`DictionaryRepositorySmokeTest`

- 类型：class
- 类作用：测试词典仓储的基础加载能力
- 包含方法：americanDictionaryLoadsAndContainsBook、britishDictionaryLoadsAndContainsBook
- 继承/实现：无
- 引用关系：DictionaryType 用于区分词典类型；Test 用于标记测试方法

| 方法 | 作用 |
| --- | --- |
| `americanDictionaryLoadsAndContainsBook` | 未提取到方法说明 |
| `britishDictionaryLoadsAndContainsBook` | 未提取到方法说明 |

## 包：`com.kotva.infrastructure.settings`

- 包作用：基础设施设置包，负责应用设置对象和持久化存储
- 包含类：AppSettings、SettingsRepository

### 文件：`src/main/java/com/kotva/infrastructure/settings/AppSettings.java`

#### 类：`AppSettings`

- 类型：class
- 类作用：封装应用音量设置
- 包含方法：AppSettings、defaults、getMusicVolume、getSfxVolume、validateVolume
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `AppSettings` | 构造方法：初始化 AppSettings 所需的基础状态 |
| `defaults` | 创建默认应用设置 |
| `getMusicVolume` | 获取Music音量 |
| `getSfxVolume` | 获取Sfx音量 |
| `validateVolume` | 校验音量参数是否合法 |

### 文件：`src/main/java/com/kotva/infrastructure/settings/SettingsRepository.java`

#### 类：`SettingsRepository`

- 类型：class
- 类作用：负责应用设置的持久化读写
- 包含方法：SettingsRepository、SettingsRepository、load、save、defaultStoragePath、readVolume
- 继承/实现：无
- 引用关系：StandardCharsets 用于配合当前类完成与 StandardCharsets 相关的处理

| 方法 | 作用 |
| --- | --- |
| `SettingsRepository` | 构造方法：初始化 SettingsRepository 所需的基础状态 |
| `SettingsRepository` | 构造方法：初始化 SettingsRepository 所需的基础状态 |
| `load` | 加载所需的数据或配置 |
| `save` | 保存当前数据或配置 |
| `defaultStoragePath` | 处理 defaultStoragePath 相关逻辑 |
| `readVolume` | 从属性集合中读取音量值 |

## 包：`com.kotva.launcher`

- 包作用：启动包，负责组装应用依赖并启动程序入口
- 包含类：AppContext、AppLauncher、MainApp

### 文件：`src/main/java/com/kotva/launcher/AppContext.java`

#### 类：`AppContext`

- 类型：class
- 类作用：组装应用运行时需要的服务和仓储依赖
- 包含方法：AppContext、AppContext、getClockService、getDictionaryRepository、getGameApplicationService、getGameSetupService、getSettingsRepository、getSettlementService
- 继承/实现：无
- 引用关系：GameApplicationService 用于提供对局应用服务；GameApplicationServiceImpl 用于提供对局应用服务实现；ClockService 用于处理计时逻辑；ClockServiceImpl 用于提供计时服务实现；GameSetupService 用于提供建局服务；GameSetupServiceImpl 用于提供建局服务实现；DictionaryRepository 用于查询词典是否合法

| 方法 | 作用 |
| --- | --- |
| `AppContext` | 构造方法：初始化 AppContext 所需的基础状态 |
| `AppContext` | 构造方法：初始化 AppContext 所需的基础状态 |
| `getClockService` | 获取计时Service |
| `getDictionaryRepository` | 获取词典Repository |
| `getGameApplicationService` | 获取对局ApplicationService |
| `getGameSetupService` | 获取对局SetupService |
| `getSettingsRepository` | 获取SettingsRepository |
| `getSettlementService` | 获取结算Service |

### 文件：`src/main/java/com/kotva/launcher/AppLauncher.java`

#### 类：`AppLauncher`

- 类型：class
- 类作用：负责启动应用
- 包含方法：AppLauncher、launch
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `AppLauncher` | 构造方法：初始化 AppLauncher 所需的基础状态 |
| `launch` | 启动应用流程 |

### 文件：`src/main/java/com/kotva/launcher/MainApp.java`

#### 类：`MainApp`

- 类型：class
- 类作用：程序主入口类
- 包含方法：main
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| `main` | 程序入口方法：启动整个应用 |

## 包：`com.kotva.mode`

- 包作用：模式与控制器包，负责描述游戏模式和玩家控制器类型
- 包含类：GameMode、PlayerController

### 文件：`src/main/java/com/kotva/mode/GameMode.java`

#### 类：`GameMode`

- 类型：enum
- 类作用：枚举游戏模式
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/mode/PlayerController.java`

#### 类：`PlayerController`

- 类型：class
- 类作用：为玩家动作提供统一的入队和获取接口，并按控制器类型区分本地、局域网和 AI
- 包含方法：PlayerController、getPlayerId、getType、onSubmit、onPass、onLose、requestAction
- 继承/实现：无
- 引用关系：PlayerAction 用于配合当前类完成与 PlayerAction 相关的处理；TurnDraft 用于保存当前回合草稿数据；PlayerType 用于区分玩家控制器类型

| 方法 | 作用 |
| --- | --- |
| `PlayerController` | 构造方法：初始化 PlayerController 所需的基础状态 |
| `getPlayerId` | 获取玩家标识 |
| `getType` | 获取类型 |
| `onSubmit` | 提交一个落子动作到控制器队列 |
| `onPass` | 提交一个 pass 动作到控制器队列 |
| `onLose` | 提交一个认输动作到控制器队列 |
| `requestAction` | 等待并返回当前玩家控制器提交的动作 |

#### 类：`LocalPlayerController`

- 类型：class
- 类作用：表示本地玩家控制器实现
- 包含方法：LocalPlayerController
- 继承/实现：继承 PlayerController
- 引用关系：PlayerAction 用于配合当前类完成与 PlayerAction 相关的处理；TurnDraft 用于保存当前回合草稿数据；PlayerType 用于区分玩家控制器类型

| 方法 | 作用 |
| --- | --- |
| `LocalPlayerController` | 构造方法：初始化 LocalPlayerController 所需的基础状态 |

#### 类：`LANPlayerController`

- 类型：class
- 类作用：表示局域网玩家控制器实现，目前保留网络接入扩展点
- 包含方法：LANPlayerController
- 继承/实现：继承 PlayerController
- 引用关系：PlayerAction 用于配合当前类完成与 PlayerAction 相关的处理；TurnDraft 用于保存当前回合草稿数据；PlayerType 用于区分玩家控制器类型

| 方法 | 作用 |
| --- | --- |
| `LANPlayerController` | 构造方法：初始化 LANPlayerController 所需的基础状态 |

#### 类：`AIPlayerController`

- 类型：class
- 类作用：表示 AI 玩家控制器实现，目前保留自动决策扩展点
- 包含方法：AIPlayerController
- 继承/实现：继承 PlayerController
- 引用关系：PlayerAction 用于配合当前类完成与 PlayerAction 相关的处理；TurnDraft 用于保存当前回合草稿数据；PlayerType 用于区分玩家控制器类型

| 方法 | 作用 |
| --- | --- |
| `AIPlayerController` | 构造方法：初始化 AIPlayerController 所需的基础状态 |

## 包：`com.kotva.policy`

- 包作用：策略枚举包，负责集中定义动作、奖励格、时钟和状态等枚举常量
- 包含类：ActionType、BonusType、ClockPhase、DictionaryType、PlayerType、SessionStatus、WordType

### 文件：`src/main/java/com/kotva/policy/ActionType.java`

#### 类：`ActionType`

- 类型：enum
- 类作用：枚举玩家动作类型
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/policy/BonusType.java`

#### 类：`BonusType`

- 类型：enum
- 类作用：枚举棋盘奖励格类型
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/policy/ClockPhase.java`

#### 类：`ClockPhase`

- 类型：enum
- 类作用：枚举玩家计时阶段
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/policy/DictionaryType.java`

#### 类：`DictionaryType`

- 类型：enum
- 类作用：枚举可选词典类型
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/policy/PlayerType.java`

#### 类：`PlayerType`

- 类型：enum
- 类作用：枚举玩家控制器类型
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/policy/SessionStatus.java`

#### 类：`SessionStatus`

- 类型：enum
- 类作用：枚举对局会话状态
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

### 文件：`src/main/java/com/kotva/policy/WordType.java`

#### 类：`WordType`

- 类型：enum
- 类作用：枚举单词类型
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

## 包：`com.kotva.presentation.controller`

- 包作用：表示层控制器包，负责承载界面控制器
- 包含类：MainMenuController

### 文件：`src/main/java/com/kotva/presentation/controller/MainMenuController.java`

#### 类：`MainMenuController`

- 类型：class
- 类作用：预留的主菜单控制器
- 包含方法：当前类未声明方法
- 继承/实现：无
- 引用关系：当前类未直接导入其他自定义类

| 方法 | 作用 |
| --- | --- |
| 无 | 当前类未声明方法或未识别到方法。 |

