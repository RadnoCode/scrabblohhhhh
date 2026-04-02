package com.kotva.launcher;

public class AppLauncher {
    AppContext appContext;

    public AppLauncher(AppContext appContext) {
        this.appContext = appContext;
    }
    public void launch(){
        appContext.getSceneNavigator().navigateToMainMenu();
        //TODO: launch other services.
    }

}
