package com.kotva.application.service;

import com.kotva.application.result.SettlementResult;
import java.io.Serializable;

public interface SettlementNavigationPort extends Serializable {

    void showSettlement(SettlementResult result);
}
