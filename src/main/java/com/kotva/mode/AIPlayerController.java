package com.kotva.mode;

import com.kotva.policy.PlayerType;

final class AIPlayerController extends PlayerController {
    AIPlayerController(String playerId) {
        super(playerId, PlayerType.AI);
    }
}
