package com.kotva.mode;

import com.kotva.policy.PlayerType;

final class LocalPlayerController extends PlayerController {

    LocalPlayerController(String playerId) {
        super(playerId, PlayerType.LOCAL);
    }
}