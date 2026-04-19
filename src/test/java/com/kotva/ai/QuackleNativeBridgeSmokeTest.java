package com.kotva.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assume;
import org.junit.Test;

public class QuackleNativeBridgeSmokeTest {
    private static final String STANDARD_TILE_POOL =
    "AAAAAAAAABBCCDDDDEEEEEEEEEEEEFFGGGHHIIIIIIIIIJKLLLLMMNNNNNNOOOOOOOOOPPQRRRRRRSSSSTTTTTTUUUUVVWWXYYZ??";

        @Test
    public void createChooseDestroyRoundTrip() {
        QuackleNativeBridge bridge = new QuackleNativeBridge();
        Assume.assumeTrue(Files.isRegularFile(bridge.getLibraryPath()));
        Assume.assumeTrue(Files.isDirectory(bridge.getDataDirectory()));

        String rack = "AEIRST?";
        AiPositionSnapshot snapshot = new AiPositionSnapshot(
            emptyBoard(),
            rack,
            buildUnseenTiles(rack),
            0,
            0);

        try (QuackleNativeBridge.Engine engine =
            bridge.createEngine(DictionaryType.AM, AiDifficulty.EASY)) {
            AiMoveOptionSet moveOptions = engine.chooseMoveOptions(snapshot);
            assertNotNull(moveOptions);
            assertTrue(!moveOptions.isEmpty());
            assertTrue(moveOptions.size() > 1);
            assertTrue(moveOptions.size() <= 10);

            AiMove move = moveOptions.moves().get(0);
            assertEquals(move, engine.chooseMove(snapshot));
            assertNotNull(move.action());
            if (move.action() == AiMove.Action.PLACE) {
                assertTrue(!move.placements().isEmpty());
            }
        }
    }

        @Test
    public void exchangeCandidatesDoNotDisplacePlayableMoves() {
        QuackleNativeBridge bridge = new QuackleNativeBridge();
        Assume.assumeTrue(Files.isRegularFile(bridge.getLibraryPath()));
        Assume.assumeTrue(Files.isDirectory(bridge.getDataDirectory()));

        String rack = "OBEEEOE";
        AiPositionSnapshot snapshot = new AiPositionSnapshot(
            emptyBoard(),
            rack,
            buildUnseenTiles(rack),
            0,
            0);

        try (QuackleNativeBridge.Engine engine =
            bridge.createEngine(DictionaryType.AM, AiDifficulty.EASY)) {
            AiMoveOptionSet moveOptions = engine.chooseMoveOptions(snapshot);
            assertNotNull(moveOptions);
            assertFalse(moveOptions.isEmpty());
            assertEquals(AiMove.Action.PLACE, moveOptions.moves().get(0).action());
            assertTrue(
                moveOptions.moves().stream().anyMatch(move -> move.action() == AiMove.Action.PLACE));
        }
    }

    private static List<AiPositionSnapshot.BoardCell> emptyBoard() {
        List<AiPositionSnapshot.BoardCell> board = new ArrayList<>(AiPositionSnapshot.BOARD_CELL_COUNT);
        for (int index = 0; index < AiPositionSnapshot.BOARD_CELL_COUNT; index++) {
            board.add(new AiPositionSnapshot.BoardCell(false, '\0', false, null));
        }
        return board;
    }

    private static String buildUnseenTiles(String rack) {
        StringBuilder unseen = new StringBuilder(STANDARD_TILE_POOL);
        for (int index = 0; index < rack.length(); index++) {
            char tile = Character.toUpperCase(rack.charAt(index));
            int tileIndex = unseen.indexOf(String.valueOf(tile));
            if (tileIndex >= 0) {
                unseen.deleteCharAt(tileIndex);
            }
        }
        return unseen.toString();
    }
}
