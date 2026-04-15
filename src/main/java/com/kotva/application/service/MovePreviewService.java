package com.kotva.application.service;

import com.kotva.application.preview.PreviewResult;
import com.kotva.application.session.GameSession;

public interface MovePreviewService {
    PreviewResult preview(GameSession session);
}
