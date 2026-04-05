package com.kotva.application.result;

public enum GameEndReason {
    ALL_PLAYERS_PASSED,
    ONLY_ONE_PLAYER_REMAINING,
    TILE_BAG_EMPTY_AND_PLAYER_FINISHED,
    BOARD_FULL,
    NO_LEGAL_PLACEMENT_AVAILABLE,
    NORMAL_FINISH
}
