package com.kotva.presentation.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.kotva.application.result.BoardSnapshot;
import com.kotva.application.result.PlayerSettlement;
import com.kotva.application.result.SettlementResult;
import com.kotva.domain.endgame.GameEndReason;
import java.util.List;
import org.junit.Test;

public class SettlementViewModelTest {

    @Test
    public void fromResultUsesEndReasonAndSummaryMessages() {
        SettlementResult settlementResult = new SettlementResult(
            GameEndReason.ALL_PLAYERS_PASSED,
            List.of(
                new PlayerSettlement("Alice", 42, 1),
                new PlayerSettlement("Bob", 35, 2)),
            List.of(
                "Game ended because all active players passed in the round.",
                "Winner: Alice"),
            new BoardSnapshot(List.of()));

        SettlementViewModel viewModel = SettlementViewModel.fromResult(settlementResult);

        assertEquals("All Players Passed", viewModel.getReasonText());
        assertEquals(
            "Game ended because all active players passed in the round.",
            viewModel.getReasonDetailText());
        assertEquals("Winner: Alice", viewModel.getSummaryText());
        assertEquals(2, viewModel.getRankings().size());
    }

    @Test
    public void fromResultProvidesFallbackWhenResultIsMissing() {
        SettlementViewModel viewModel = SettlementViewModel.fromResult(null);

        assertEquals("Settlement Unavailable", viewModel.getReasonText());
        assertTrue(viewModel.getRankings().isEmpty());
    }
}
