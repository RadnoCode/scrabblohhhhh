package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;
import java.io.Serializable;

/**
 * Output port used to show the settlement screen.
 */
public interface SettlementNavigationPort extends Serializable {

    /**
     * Shows a settlement result.
     *
     * @param result settlement result to show
     */
    void showSettlement(SettlementResult result);
}
