package com.dazednconfused.catalauncher;

import com.dazednconfused.catalauncher.gui.MainWindow;

public class Application {

    public static void main(final String[] args) {
        MainWindow.main(args);
    }

    /**
     * Returns the folder where the current instance of the {@link Application} is running.
     * */
    public static String getRootFolder() {
        return System.getProperty("user.dir");
    }
}
