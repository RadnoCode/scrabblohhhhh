package com.kotva.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.kotva.application.service.ClockServiceImpl;
import com.kotva.application.service.GameApplicationServiceImpl;
import com.kotva.application.service.lan.LanHostService;
import com.kotva.application.session.BoardCellRenderSnapshot;
import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.PlayerConfig;
import com.kotva.application.session.RackTileSnapshot;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Position;
import com.kotva.domain.model.Tile;
import com.kotva.lan.GameSessionBroker;
import com.kotva.mode.GameMode;
import com.kotva.mode.PlayerController;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.List;
import org.junit.Test;

public class LobbyHostGameRuntimeTest {

    @Test
    public void assignBlankTileLetterUpdatesLobbyHostSnapshot() {
        GameApplicationServiceImpl gameApplicationService =
                new GameApplicationServiceImpl(new ClockServiceImpl());
        GameSession session = createLanSession();
        Player hostPlayer = session.getGameState().requireCurrentActivePlayer();
        Tile blankTile = session.getGameState().getTileBag().takeBlankTile();
        hostPlayer.getRack().setTileAt(0, blankTile);

        LobbyHostGameRuntime runtime =
                new LobbyHostGameRuntime(
                        gameApplicationService,
                        session,
                        new LanHostService(session, gameApplicationService),
                        new GameSessionBroker(0));

        runtime.placeDraftTile(blankTile.getTileID(), new Position(7, 7));
        runtime.assignBlankTileLetter(blankTile.getTileID(), 'q');

        assertEquals(Character.valueOf('Q'), blankTile.getAssignedLetter());
        assertEquals(
                Character.valueOf('Q'),
                session.getTurnDraft().getAssignedLettersByTileId().get(blankTile.getTileID()));
        assertEquals(
                Character.valueOf('Q'),
                session.getTurnDraft().getPlacements().get(0).getAssignedLetter());

        GameSessionSnapshot snapshot = runtime.getSessionSnapshot();
        RackTileSnapshot rackTile = snapshot.getCurrentRackTiles().stream()
                .filter(candidate -> blankTile.getTileID().equals(candidate.getTileId()))
                .findFirst()
                .orElseThrow();
        assertEquals(Character.valueOf('Q'), rackTile.getAssignedLetter());
        assertEquals(Character.valueOf('Q'), rackTile.getDisplayLetter());

        BoardCellRenderSnapshot boardCell = snapshot.getBoardCells().stream()
                .filter(candidate -> candidate.getRow() == 7 && candidate.getCol() == 7)
                .findFirst()
                .orElseThrow();
        assertTrue(boardCell.isDraft());
        assertEquals(Character.valueOf('Q'), boardCell.getDisplayLetter());
    }

    private GameSession createLanSession() {
        Player host = new Player("player-1", "Host", PlayerType.LOCAL);
        host.setController(PlayerController.create(host.getPlayerId(), host.getPlayerType()));
        Player guest = new Player("player-2", "Guest", PlayerType.LAN);
        guest.setController(PlayerController.create(guest.getPlayerId(), guest.getPlayerType()));

        GameSession session = new GameSession(
                "session-lobby-host-blank",
                new GameConfig(
                        GameMode.LAN_MULTIPLAYER,
                        List.of(
                                new PlayerConfig("Host", PlayerType.LOCAL),
                                new PlayerConfig("Guest", PlayerType.LAN)),
                        DictionaryType.AM,
                        null),
                new GameState(List.of(host, guest)));
        session.setSessionStatus(SessionStatus.IN_PROGRESS);
        return session;
    }
}
