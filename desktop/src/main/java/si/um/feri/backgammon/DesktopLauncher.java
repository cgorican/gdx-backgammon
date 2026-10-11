package si.um.feri.backgammon;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** Desktop entry point. On macOS the JVM must be started with -XstartOnFirstThread (the Gradle run task does it). */
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Backgammon");
        // Window and taskbar icon (ignored on macOS, where the app bundle icon is used).
        config.setWindowIcon("icons/icon-128.png", "icons/icon-64.png", "icons/icon-32.png", "icons/icon-16.png");
        config.setWindowedMode(1600, 900);
        config.setWindowSizeLimits(960, 540, -1, -1);
        config.setResizable(true);
        config.useVsync(true);
        // Cap at the monitor refresh rate (plus one) in case vsync is ignored by the driver.
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        new Lwjgl3Application(new BackgammonGame(), config);
    }
}
