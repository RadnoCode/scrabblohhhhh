package com.kotva;

import com.kotva.launcher.AppLauncher;

/**
 * Compatibility launcher kept for existing scripts that still target the old
 * front-end entry class. The real startup flow now lives in AppLauncher.
 */
public final class FrontEndLauncher {
    private FrontEndLauncher() {}

    public static void main(String[] args) {
        AppLauncher.main(args);
    }
}
