package com.kotva.presentation.controller;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import com.kotva.presentation.viewmodel.HelpViewModel;

/**
 * Controls the help screen.
 */
public class HelpController {
    private final SceneNavigator navigator;
    private final HelpViewModel viewModel;

    public HelpController(SceneNavigator navigator) {
        this.navigator = navigator;
        this.viewModel = new HelpViewModel(
            "SCRABBLE",
            """
            Scrabble Basic Rules

            1. Players take turns to place letter tiles on the board.
            2. Every new move must connect to tiles already on the board.
            3. On the first move, the word must pass through the center.
            4. Words are scored by adding tile values and bonus cells.
            5. After placing tiles, the player confirms the move.
            6. If a player cannot or does not want to play, they may pass.
            7. The game ends when the end condition is reached by the current rules.

            Common Notes

            - Try to use high-score letters on bonus cells.
            - Keep a balanced rack when possible.
            - Watch the remaining board space and future moves.
            - In local games, remember to hand over the device safely.
            - In LAN mode, wait for the room connection before starting.

            This panel is scrollable.
            Later you can replace this text with a longer rule document,
            multilingual help content, or dynamic help loaded from files.
            """);
        }

        public HelpViewModel getViewModel() {
            return viewModel;
        }

        public void bindBackAction(CommonButton backButton) {
            backButton.setOnAction(event -> navigator.goBack());
        }
    }
