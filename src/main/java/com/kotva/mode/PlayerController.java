package com.kotva.mode;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.kotva.application.PlayerAction;
import com.kotva.application.draft.TurnDraft;
import com.kotva.policy.PlayerType;

//Generate actions from UI, network, or AI offer a queue to TurnCoordinator.
public class PlayerController {
    private final PlayerType type;
    private final String playerId;
    private final BlockingQueue<PlayerAction> actionQueue;

    PlayerController(String playerId, PlayerType type) {
        this.playerId = playerId;
        this.type = type;
        this.actionQueue = new LinkedBlockingQueue<>();
    }

    public String getPlayerId() {
        return playerId;
    }

    public PlayerType getType() {
        return type;
    }

    public void onSubmit(TurnDraft draft) {
        actionQueue.offer(PlayerAction.place(playerId, draft));
    }

    public void onPass() {
        actionQueue.offer(PlayerAction.pass(playerId));
    }

    public void onLose() {
        actionQueue.offer(PlayerAction.lose(playerId));
    }

    public PlayerAction requestAction() {
        try {
            return actionQueue.take();
            // take() 不能在 UI 线程里调用，否则界面会卡死。
            // 通常让 TurnCoordinator 在后台线程跑，UI 只负责 offer(action)。
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

class LocalPlayerController extends PlayerController {
    LocalPlayerController(String playerId) {
        super(playerId, PlayerType.LOCAL);
    }
}

class LANPlayerController extends PlayerController {
    // todo: implement network communication to receive actions from remote player
    // and offer to the queue.
    LANPlayerController(String playerId) {
        super(playerId, PlayerType.LAN);
    }
}

class AIPlayerController extends PlayerController {
    // todo: implement AI logic, maybe with a separate thread to generate actions
    // and offer to the queue.
    AIPlayerController(String playerId) {
        super(playerId, PlayerType.AI);
    }
}