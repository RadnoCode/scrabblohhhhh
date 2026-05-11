Team Name:
Kotava
Team Members:
Qinjian Luo 2717366 / Zhehan Lin 2717301 / Lirong Zeng 2717396 / Hongyi Guo 2717623
1 Introduction
1.1 Overview of Scrabble
Scrabble is a classic word game in which players form connected words on a board using letter tiles, with each letter carrying a different score value. The game combines vocabulary knowledge, spelling ability, strategic thinking, and score management, making it both educational and entertaining. Because of its balance between language learning and competition, Scrabble has remained popular among students, families, and word-game enthusiasts for many years.
1.2 Purpose of Project and Report
As a traditional table game, however, Scrabble still has some limitations, such as difficulty in checking valid words, a monotonous form of entertainment, and limitations of the venue and setting. Hence, the target of his project is not only to create a digital version of the Scrabble game, but also to improve the overall gameplay experience. By turning the traditional board game into software, the system can provide more convenient interaction and automatically handle rule checking, score calculation, and other management tasks. Moreover, the addition of LAN multiplayer and AI gameplay further enhances the game by making it more flexible, interactive, and suitable for different users and playing situations.
Moreover, from an educational perspective, this project is intended to provide students with an opportunity to apply theoretical knowledge of Java programming language to a practical software development task. Through the design and implementation of this system, team members are able to strengthen their understanding of basic concepts of Java, software architecture, modular design, user interaction, error handling, testing, and teamwork. The project also encourages students to explore unfamiliar technical areas, thereby improving their problem-solving and self-learning abilities.
This report aims to give a detailed and structured presentation of the whole project about the development process and technical design of the Scrabble game system. It provides a structured explanation of the system architecture, implementation details, and key functionalities. It also presents the challenges encountered during development, the solutions adopted, and an objective and comprehensive evaluation, along with identified directions for future work.
1.3 Target Users and Application Scenarios
The target users of this software include casual players who enjoy word games for entertainment, students who want to improve their vocabulary and spelling skills, and groups of players who would like to play together in different modes. The software is applicable in a variety of scenarios, such as personal entertainment, practice of spelling and vocabulary, face-to-face or LAN multiplayer gaming with friends, and small group recreational activities. Therefore, the system is designed to support both recreational and educational use, while also providing flexibility for personalized user needs and playing environments.
1.4 System Overview and Features
The software is a Java-based Scrabble game with a graphical user interface. It allows players to play Scrabble in different modes, including local multiplayer, human-versus-AI, and LAN-based multiplayer. The system is designed to manage the complete game process, from game setup to scoring and final settlement.
Its main functions include creating a game, selecting settings such as player count, dictionary, time control, and game mode, placing and recalling tiles, validating words, calculating scores, switching turns, and ending the game when the required conditions are met. The software also includes an AI module that enables human-versus-AI gameplay, allowing single players to play against a computer-controlled opponent. In LAN mode, the software further supports room discovery, lobby waiting, and synchronized multiplayer gameplay over a local network.
1.5 Organization of the Report
The rest of this report is organized as follows. The next section presents the build and compilation instructions, including the development environment, required dependencies, and the procedures for building and running the system. This is followed by a user guide, which explains the interface layout, game settings, and core gameplay procedures. The report then provides a code overview, covering the repository structure, system architecture, key classes, and the main data flow of the application. A critical evaluation section follows, where the major challenges encountered during development and the corresponding solutions are discussed. The subsequent section reviews the project requirements and their implementation status. Finally, the conclusion summarizes the overall project outcomes, team contributions, and AI declaration.

---
2 Compiling Instruction
2.1 Build Tool Chain
1. JDK: JDK 25.0.2
2. LLVM/Clang: clang++ 21.1.8 
3. mingw-w64: 15.2.0 
4. Maven: 3.9.14 
5. CMake: 3.10+  
6. OS: 
  1. MacOS (Recommended): Can build MacOS and Windows native library.
  2. Windows: Can only build Windows native library.
2.2 Dependencies
1.  JavaFX: 25
Java dependencies will be automatically managed and installed by Maven, so it should not be installed manually.

---
2.3 Build Step
2.3.1 Build Native Library
Build scripts(Bash sctript) are provided in the folder /scrabblohhhhh/scripts/. Run scripts to easily build native libraries for MacOS and Windows, and compiled libraries will be located at /scrabblohhhhh/target/native.
Sample commands are as follows:
# Open the project root folder first
cd scripts
bash build-quackle-ffm-win.sh
bash build-quackle-ffm-mac.sh
2.3.2 Build Java Project
This project uses the Maven build tool to manage building, therefore please make sure Maven is installed in computer. The compile and launch command are as follow:
# Open the project root folder first

# Build command
mvn clean
mvn clean package -DskipTests
# Run Command
mvn javafx:run

---
3.User Guide
3.1 Overview of the System
Scribble is a GUI-based word-building game in which players place letter tiles on the board to form words and complete a match. The main objective is to create valid words based on the current board and the player’s rack, while scoring as many points as possible under the game’s scoring rules.
The system supports three game modes: Local Multiplayer, AI Mode, and LAN Mode.

---
3.2 Main Menu Navigation
When the game starts, the main menu provides four primary options.
Play: Opens the game mode selection page.
Tutorial: Opens the tutorial system, which introduces the basic gameplay step by step.
Settings: Opens the settings page, where the user can adjust system preferences such as audio options.
Help: Opens the help page, which displays the basic game rules and instructions.
This structure allows new users to quickly choose whether they want to start a match, learn the system first, or review the rules before playing.

---
3.3 Game Modes
3.3.1 Local Multiplayer
In Local Multiplayer mode, multiple players take turns playing on the same device.
Before the match starts, the following options can be configured:
Total game time: an integer from 15 to 90 minutes
Step time: an integer from 0 to 180 seconds
Dictionary: North American or British
Number of players: 2, 3, or 4
After configuration, players enter their nicknames before the game begins.
3.3.2 AI Mode
In AI Mode, a human player competes against a computer-controlled opponent.
Before the match starts, the following options can be configured:
Total game time: an integer from 15 to 90 minutes
Step time: an integer from 0 to 180 seconds
Dictionary: North American or British
Difficulty: Easy, Middle, or Hard
After the setup is completed, the game starts immediately with the selected AI difficulty.
3.3.3 LAN Mode
LAN Mode allows players to play over a local network.
From the LAN menu, users can choose between the following options:
Search Room: Search for available LAN rooms, refresh the room list, and join a selected room
Create Room: Create a new LAN room and configure the room name and match settings
When creating a room, the user can set the room name together with game time, step time, dictionary, and number of players. After that, the host and other players can enter their nicknames before the game starts.

---
3.4 Game Setup
Before entering a match, players must complete the required setup for the selected mode. Depending on the game mode, this may include entering player nicknames, selecting the game time, setting the step time, choosing the dictionary, choosing the number of players, or selecting the AI difficulty.
This setup process ensures that the rules and match conditions are defined before gameplay begins.

---
3.5 Gameplay Instructions
3.5.1 Turn Flow
During the game, players take turns in sequence. On each turn, a player places tiles on the board to form a word, then submits the move for validation. If the move is valid, the system calculates the score and passes the turn to the next player.
This turn-based structure applies across the different game modes, although the opponent or control style may differ.
3.5.2 Action Buttons
The in-game interface includes several action buttons that support player interaction:
Submit: Confirms and submits the current move
Skip Turn: Passes the current turn without placing tiles
Rearrange: Reorders the tiles currently shown on the rack
Recall: Removes all tiles placed in the current draft and returns them to the rack
Resign: Concedes the game and leaves the current competitive state
These controls allow players to adjust their move before submission or leave the match when necessary.
3.5.3 Word Placement Rules
The rule system follows a clear logic chain: it first validates the placement structure, then extracts all words formed in the move, performs dictionary validation, and finally calculates the score.
The current implementation applies the following placement rules:
The move must be a valid tile-placement action and cannot be empty.
The first move must cover the center cell.
Newly placed tiles must be in the same row or the same column.
Tiles cannot overlap existing tiles.
The word must be contiguous. Existing tiles may fill gaps, but empty spaces are not allowed.
If it is not the first move, the placement must connect to tiles already on the board.
The move must form at least one new word.
The system checks all words formed in the move, including cross words.
If any word fails dictionary validation, the entire move is rejected.
A blank tile must be assigned a letter before submission.
These rules ensure that every accepted move is structurally valid and dictionary-compliant  .
3.5.4 Scoring Rules
After a move is validated, the system scores all valid words formed in that move and sums them as the score for the turn.
The current scoring rules include the following:
Newly placed tiles may activate board bonus cells
Double Letter: letter score ×2
Triple Letter: letter score ×3
Double Word: word multiplier ×2
Triple Word: word multiplier ×3
Existing tiles already on the board contribute only their base score and do not activate bonus cells again
This scoring system rewards both good word formation and effective use of bonus cells.
3.5.5 Game End Conditions
In the current implementation, the game ends under the following conditions:
Only one active player remains
All active players pass within the same round
The tile bag is empty and a player has used all tiles in the rack
Once one of these conditions is reached, the system enters the settlement phase and displays the final rankings.

---
3.6 In-Game Interface and Controls
The main in-game interface includes the board, the tile rack, the preview panel, player information, timers, and the action buttons.

Players place tiles by dragging and dropping them from the rack onto the board. The preview panel displays the current word and score information for move evaluation before submission. The interface also shows player-related information, score status, and time-related information to help users follow the progress of the match.
Together, these interface elements allow users to understand the current state of the game and interact with it efficiently.

---
3.7 Tutorial and Help System
The tutorial system provides step-by-step guidance for new users. It introduces important concepts such as bonus cells, first-move rules, valid placement, and the main control buttons.
The help page provides the basic game rules and common gameplay notes, allowing users to review the rules during use. The settings page provides customization options that help improve the user experience.
By following the tutorial, reviewing the help page, and using the in-game preview and controls, new players can quickly learn how to start and play Scribble successfully.

---
4.Code Overview
4.1 Repository Structure
4.1.1 Root Folder Structure
scrabblohhhhh/
|-- README.md              # Project overview
|-- pom.xml                # Maven configuration
|-- folder_tree.txt        # Directory tree document
|-- build-exe.bat          # Windows app image build script
|-- build-installer.bat    # Windows installer build script
|-- docs/                  # Project documentation
|-- scripts/               # Build and package scripts
|-- native/                # Native library output
|-- src/                   # Application source and tests
|-- quackle-master/        # Quackle AI source and data
|-- target/                # Build output
`-- .github/               # GitHub configuration
4.1.2 Source Code Structure
src/
|-- main/                    
|   |-- java/com/kotva/       # Java source root
|   |   |-- launcher/         # Application startup
|   |   |-- presentation/     # JavaFX UI layer
|   |   |-- application/      # Use cases and orchestration
|   |   |   |-- draft/        # Turn draft handling
|   |   |   |-- preview/      # Move preview logic
|   |   |   |-- result/       # Result data models
|   |   |   |-- runtime/      # Application runtime support
|   |   |   |-- service/      # Application services
|   |   |   |-- session/      # Game session state
|   |   |   |-- setup/        # Game setup flow
|   |   |   `-- turn/         # Turn coordination
|   |   |-- domain/           # Core game rules and models
|   |   |-- infrastructure/   # Persistence, dictionary, network, settings
|   |   |-- ai/               # AI bridge
|   |   |-- lan/              # LAN multiplayer support
|   |   |-- mode/             # Game mode definitions
|   |   |-- policy/           # Policy and configuration types
|   |   |-- runtime/          # Runtime factory support
|   |   `-- tutorial/         # Tutorial support
|   `-- resources/            # CSS, images, audio, dictionaries
`-- test/                     # Test source root

---
4.2 Application Architecture
4.2.1 Level Graph
  Graph 4.1 shows the overall architecture of the software. The presentation layer handles UI and user input. Runtime serves as a bridge between presentation and application, providing uniform interface to Presentation. The application layer contains core logic flow, interacting with the domain layer, which stores the rules and models of the game. The infrastructure layer provides network service, dictionary service, and game saving service.
暂时无法在飞书文档外展示此内容
4.2.2 Active diagram
1. Overall Diagram
The picture 4.2 shows overall game flow from entering the application to finishing a game.Player will start from a homepage then config upcoming game and finnaly finish the game.
暂时无法在飞书文档外展示此内容
2. Game Loop Diagram
Picture 4.3 shows how the game loop works. When the game starts, players will get a random turn order then the game loop starts. Current player has three option, skip, resign and place tiles. If a palyer skip, this turn will end immediately. If a player resign, current turn eill end as well and this player will be remove from active player list. If a player place tiles on the board, there will be real-time preview showing validity and predictive score. When the draft is submitted, it will be checked whether it is valid. If it is, the game will continue. Otherwise, an error message will be shown.
暂时无法在飞书文档外展示此内容
3. AI Diagram
 The AI diagram consists of two parts: set up and game play. AI will be loaded and initialized when an AI game starts. In an AI game, whenever is comes to AI's turn, AI will get current game board, rack and unseen tiles then output ten options in which the best one will be submitted. If submission is not acceptable, AI will try the next option. Until there is no option left, AI will skip the turn.
暂时无法在飞书文档外展示此内容
4. LAN Diagram
The first diagram mainly shows how a room is created and when the game starts. The host shall create first, then clients can search for the room and join in. When the number of room members reaches the target, the game can be started by host.
暂时无法在飞书文档外展示此内容
In game playing, LAN mode's flow is similar wih Local mode. The main difference is that the host hold the game, and all clients can only send command to host and get snapshot as a copy of the game.Picture shows how this procedure works. 
When the host acts, most things go like how local mode works. After the host's action is applied, the host will send the newest snapshot to clients. When a client acts, the client will send an envelope containing attempted action to host and receive a response depending on validity.
暂时无法在飞书文档外展示此内容

---
4.3 Key Class
The following table lists some key classes, which build the overall structure of the game.
Class Name
Responsibility
Key Data
Interactions
GameRuntime
Provide uniform API used by UI events. Handle player action.
Recieve NewGameRequest
Assemble necessary services 
GameRuntimeFactory (Created)
Presentation layer (Being called)
GameController
Controls the screen. Receives UI input.
UI state 
Work withRuntime, Renderer, and Navigator.
GameApplicationService
Provides the main gameplay logic API
GameSession
Received results
Called by runtime
Call other core helpers
MovePreviewService
Builds a move preview for draft
Game state and PreviewResult
Rely on RuleEngine
TurnCoordinator
Pushes the game turn to the next one.
turn-level state
Works with end checker and settlement services
RuleEngine
Contains the core game rules

GameState, PlayerAction
Dictionary Service
Use to generate Preview
Vlidtae and score final submmition

---
4.4 Key Data & Data Flow 
4.4.1 Key Data
The project has four types of key data. Configuration data is used to start the application and game.
And authoritative game data stores the only truth of the game. Additionally, turn interation data contians draft and player's action. Finally, there is presentation data, which provides a source for presentation to render. There are only some representative fields listed.
1. Configuration Data
// Game configuration from UI. 
public class NewGameRequest{
    private final GameMode gameMode;
    private final int playerCount;
    private final List<String> playerNames;
    private final DictionaryType dictionaryType;
    private final TimeControlConfig timeControlConfig;
    private final AiDifficulty aiDifficulty;
}
// Launch game page
public class GameLaunchContext {
    private final RuntimeLaunchSpec launchSpec;
    private final GameRuntime providedRuntime;
    private final AiDifficulty aiDifficulty;
}
2. Authoritative Game Data
The authoritative game data is the only truth of the game, which can be read to get real game state. There are only some representative fields listed.
// Top container of the game, containing whole 
public class GameSession {
    private final String sessionId; 
    private final GameConfig config; 
    private final GameState gameState; 
    private TurnDraft turnDraft;
    private SessionStatus sessionStatus;
    private final SettlementService settlementService;
    private final TurnCoordinator turnCoordinator;
}
// Current board state and player satus.
public class GameState 