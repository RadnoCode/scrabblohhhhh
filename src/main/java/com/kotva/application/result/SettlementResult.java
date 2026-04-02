package com.kotva.application.result;

import java.util.List;

public class SettlementResult {
    private GameEndReason endReason;
    private List<String> summaryMessages;

    public GameEndReason getEndReason() {
        return endReason;
    }

    public List<String> getSummaryMessages() {
        return summaryMessages;
    }
}
