package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;

public class NoOpSettlementNavigationPort implements SettlementNavigationPort {
    @Override
    public void showSettlement(SettlementResult result) {
        // Intentionally empty until scene navigation is implemented.
    }
}
