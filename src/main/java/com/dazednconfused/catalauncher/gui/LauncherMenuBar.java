package com.dazednconfused.catalauncher.gui;

import com.dazednconfused.catalauncher.configuration.ConfigurationManager;
import com.dazednconfused.catalauncher.helper.LogLevelManager;
import com.dazednconfused.catalauncher.update.GameUpdateManager;
import com.dazednconfused.catalauncher.update.GameVersion;

import java.util.Optional;

import io.vavr.control.Try;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import lombok.Getter;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class comprises all actions related to the Launcher's Menu Bar and its associated GUI elements.
 * */
public class LauncherMenuBar {

    private static final Logger LOGGER = LoggerFactory.getLogger(LauncherMenuBar.class);

    @Getter
    private final JMenuBar menuBar;

    private final JMenu helpMenu;
    private final JMenu developerToolsMenu;
    private final JMenu gameMenu;

    private final JMenuItem showConsoleLogMenuItem;
    private final JCheckBoxMenuItem debugModeCheckBoxMenuItem;
    private final JMenuItem aboutMenuItem;
    private final JMenuItem checkForGameUpdatesMenuItem;
    private final JMenuItem configureGameUpdateSourceMenuItem;

    /**
     * Public constructor.
     * */
    public LauncherMenuBar(Component parent) {

        LOGGER.trace("Building menu bar...");

        // main menu bar ---
        this.menuBar = new JMenuBar();

        // help menu ---
        this.helpMenu = new JMenu("Help");
        this.helpMenu.setMnemonic(KeyEvent.VK_H);
        this.menuBar.add(helpMenu);

        // developer tools submenu --
        this.developerToolsMenu = new JMenu("Developer Tools");
        this.helpMenu.add(developerToolsMenu);

        // show console log button -
        this.showConsoleLogMenuItem = new JMenuItem("Show console log");
        this.showConsoleLogMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
        this.showConsoleLogMenuItem.addActionListener(LauncherMenuBar.onShowConsoleButtonClicked(parent));
        this.developerToolsMenu.add(this.showConsoleLogMenuItem);

        // debug mode checkbox -
        this.debugModeCheckBoxMenuItem = new JCheckBoxMenuItem("Debug mode");
        this.debugModeCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK));
        this.debugModeCheckBoxMenuItem.setState(ConfigurationManager.getInstance().isDebug());
        this.debugModeCheckBoxMenuItem.addActionListener(LauncherMenuBar.onDebugModeButtonClicked(this.debugModeCheckBoxMenuItem));
        this.developerToolsMenu.add(this.debugModeCheckBoxMenuItem);

        // separator --
        helpMenu.addSeparator();

        // about button --
        this.aboutMenuItem = new JMenuItem("About");
        this.aboutMenuItem.setMnemonic(KeyEvent.VK_T);
        this.aboutMenuItem.addActionListener(LauncherMenuBar.onAboutButtonClicked(parent));
        this.helpMenu.add(this.aboutMenuItem);

        // game menu ---
        this.gameMenu = new JMenu("Game");
        this.gameMenu.setMnemonic(KeyEvent.VK_G);
        this.menuBar.add(gameMenu);

        // check for game updates button --
        this.checkForGameUpdatesMenuItem = new JMenuItem("Check for Game Updates");
        this.checkForGameUpdatesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_DOWN_MASK));
        this.checkForGameUpdatesMenuItem.addActionListener(LauncherMenuBar.onCheckForGameUpdatesClicked(parent));
        this.gameMenu.add(this.checkForGameUpdatesMenuItem);

        // configure game update source button --
        this.configureGameUpdateSourceMenuItem = new JMenuItem("Configure Update Source...");
        this.configureGameUpdateSourceMenuItem.addActionListener(LauncherMenuBar.onConfigureGameUpdateSourceClicked(parent));
        this.gameMenu.add(this.configureGameUpdateSourceMenuItem);
    }

    /**
     * The action to be performed on {@link #showConsoleLogMenuItem}'s click.
     * */
    private static ActionListener onShowConsoleButtonClicked(Component parent) {
        return e -> {
            LOGGER.trace("Show console button clicked");
            Try.of(ConsoleLogReader::new)
                .andThen(consoleLogReader -> consoleLogReader.packCenterAndShow(parent))
                .onFailure(throwable -> LOGGER.error("There was an error while ConsoleLogReader window: [{}]", throwable.getMessage()));
        };
    }

    /**
     * The action to be performed on {@link #debugModeCheckBoxMenuItem}'s click.
     * */
    private static ActionListener onDebugModeButtonClicked(JCheckBoxMenuItem debugMode) {
        return e -> {
            LOGGER.trace("Debug mode checkbox clicked. Enabled: [{}]", debugMode.getState());
            ConfigurationManager.getInstance().setDebug(debugMode.getState());
            LogLevelManager.changeGlobalLogLevelTo(debugMode.getState() ? Level.TRACE : Level.INFO);
        };
    }

    /**
     * The action to be performed on {@link #aboutMenuItem}'s click.
     * */
    private static ActionListener onAboutButtonClicked(Component parent) {
        return e -> {
            LOGGER.trace("About button clicked");

            VersionManagerWindow versionManagerWindow = new VersionManagerWindow();
            versionManagerWindow.packCenterAndShow(parent);
        };
    }

    /**
     * The action to be performed on {@link #checkForGameUpdatesMenuItem}'s click.
     * */
    private static ActionListener onCheckForGameUpdatesClicked(Component parent) {
        return e -> {
            LOGGER.trace("Check for game updates button clicked");

            if (!GameUpdateManager.isConfigured()) {
                new ConfirmDialog(
                    "Game update source not configured. Configure now?",
                    ConfirmDialog.ConfirmDialogType.INFO,
                    confirmed -> {
                        if (confirmed) {
                            GameUpdateSettingsDialog dialog = new GameUpdateSettingsDialog();
                            dialog.packCenterAndShow(parent);
                        }
                    }
                ).packCenterAndShow(parent);
                return;
            }

            if (!GameUpdateManager.isInstalledVersionConfigured()) {
                new ConfirmDialog(
                    "Installed game version not set. Download latest version now?",
                    ConfirmDialog.ConfirmDialogType.INFO,
                    confirmed -> {
                        if (confirmed) {
                            // offer to download latest based on current configuration
                            offerLatestDownload(parent);
                        }
                    }
                ).packCenterAndShow(parent);
                return;
            }

            // use the new dialog that offers download option ---
            GameUpdateAvailableDialog.checkAndShow(parent, true);
        };
    }

    /**
     * The action to be performed on {@link #configureGameUpdateSourceMenuItem}'s click.
     * */
    private static ActionListener onConfigureGameUpdateSourceClicked(Component parent) {
        return e -> {
            LOGGER.trace("Configure game update source button clicked");
            GameUpdateSettingsDialog dialog = new GameUpdateSettingsDialog();
            dialog.packCenterAndShow(parent);
        };
    }

    /**
     * Offers to download the latest version when installed version is not configured.
     */
    private static void offerLatestDownload(Component parent) {
        LOGGER.trace("Offering latest download (no installed version set)");

        // check CDDA path first
        if (!GameUpdateManager.isCddaPathConfigured()) {
            new ConfirmDialog(
                "CDDA executable path is not configured. Please set it in the Launcher tab first."
            ).packCenterAndShow(parent);
            return;
        }

        // get the latest version
        Optional<GameVersion> latestVersion = GameUpdateManager.getLatestGameReleaseTag();
        if (latestVersion.isEmpty()) {
            new ConfirmDialog(
                "Could not determine latest version. Check your repository configuration."
            ).packCenterAndShow(parent);
            return;
        }

        // show the download dialog
        new GameUpdateAvailableDialog(parent, latestVersion.get()).packCenterAndShow(parent);
    }
}
