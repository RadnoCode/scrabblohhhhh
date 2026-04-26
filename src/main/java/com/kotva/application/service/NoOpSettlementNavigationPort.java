package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;

/**
 * Settlement navigation port that intentionally does nothing.
 */
public class NoOpSettlementNavigationPort implements SettlementNavigationPort {

    /**
     * Ignores the settlement result.
     *
     * @param result settlement result
     */
    @Override
    public void showSettlement(SettlementResult result) {
    }
}
