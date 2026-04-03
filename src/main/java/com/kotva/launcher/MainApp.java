package com.kotva.launcher;

public class MainApp {
    private AppContext appcontext;
    public static void main(String[] args) {
        MainApp mainApp = new MainApp();
        mainApp.appcontext = new AppContext();
        AppLauncher appLauncher = new AppLauncher(mainApp.appcontext);
        appLauncher.launch();
    }
}


