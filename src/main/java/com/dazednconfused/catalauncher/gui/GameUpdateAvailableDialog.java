package com.dazednconfused.catalauncher.gui;

import static com.dazednconfused.catalauncher.gui.helper.GuiResource.extractIconFrom;
import static com.dazednconfused.catalauncher.helper.Paths.RESOURCE_ICONS_PATH;

import com.dazednconfused.catalauncher.update.GameUpdateManager;
import com.dazednconfused.catalauncher.update.GameVersion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.apache.batik.swing.JSVGCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog shown when a game update is available, offering download or view options.
 */
public class GameUpdateAvailableDialog extends JDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameUpdateAvailableDialog.class);

    private static final String INFO_ICON = extractIconFrom(RESOURCE_ICONS_PATH + "/informationDialog.svg");

    private final Component parent;

    /**
     * Constructor.
     *
     * @param latestVersion the latest available version
     */
    public GameUpdateAvailableDialog(Component parent, GameVersion latestVersion) {
        this.parent = parent;

        setTitle("Game Update Available");
        setModal(true);
        setResizable(false);

        // main panel ---
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // icon panel ---
        JSVGCanvas iconSvg = new JSVGCanvas();
        iconSvg.setBackground(new Color(0, 0, 0, 0));
        iconSvg.setURI(INFO_ICON);
        iconSvg.setPreferredSize(new java.awt.Dimension(48, 48));
        contentPane.add(iconSvg, BorderLayout.WEST);

        // message panel ---
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("A new game version is available!");
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        messagePanel.add(titleLabel);

        messagePanel.add(javax.swing.Box.createVerticalStrut(8));

        JLabel versionLabel = new JLabel("New version: " + latestVersion.getOriginalVersion());
        messagePanel.add(versionLabel);

        messagePanel.add(javax.swing.Box.createVerticalStrut(5));

        JLabel questionLabel = new JLabel("What would you like to do?");
        messagePanel.add(questionLabel);

        contentPane.add(messagePanel, BorderLayout.CENTER);

        // button panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton downloadButton = new JButton("Download & Install");
        downloadButton.addActionListener(e -> onDownloadClicked());
        buttonPanel.add(downloadButton);

        JButton viewButton = new JButton("View Releases Page");
        viewButton.addActionListener(e -> onViewReleasesClicked());
        buttonPanel.add(viewButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);

        // close on window close ---
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // close on ESCAPE ---
        contentPane.registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        getRootPane().setDefaultButton(downloadButton);
    }

    /**
     * Handler for Download & Install button.
     */
    private void onDownloadClicked() {
        LOGGER.trace("Download button clicked");
        dispose();

        // check if CDDA path is configured ---
        if (!GameUpdateManager.isCddaPathConfigured()) {
            new ConfirmDialog(
                "CDDA executable path is not configured. Please set it in the Launcher tab first."
            ).packCenterAndShow(parent);
            return;
        }

        // start download ---
        boolean success = GameUpdateDownloadDialog.showAndDownload(parent);

        if (success) {
            new ConfirmDialog(
                "Game updated successfully! The new version is now installed."
            ).packCenterAndShow(parent);
        } else {
            new ConfirmDialog(
                "Update failed. You can try downloading manually from the releases page.",
                ConfirmDialog.ConfirmDialogType.WARNING,
                confirmed -> {
                    if (confirmed) {
                        GameUpdateManager.openLatestGameReleaseInDefaultBrowser();
                    }
                }
            ).packCenterAndShow(parent);
        }
    }

    /**
     * Handler for View Releases Page button.
     */
    private void onViewReleasesClicked() {
        LOGGER.trace("View releases button clicked");
        dispose();
        GameUpdateManager.openLatestGameReleaseInDefaultBrowser();
    }

    /**
     * Packs, centers, and shows the dialog.
     */
    public void packCenterAndShow(Component parent) {
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }

    /**
     * Shows the dialog if an update is available.
     *
     * @param parent the parent component
     * @param showIfNoUpdate whether to show a dialog if no update is available
     */
    public static void checkAndShow(Component parent, boolean showIfNoUpdate) {
        if (!GameUpdateManager.isConfigured()) {
            new ConfirmDialog(
                "Game update source not configured. Configure it first."
            ).packCenterAndShow(parent);
            return;
        }

        if (!GameUpdateManager.isInstalledVersionConfigured()) {
            // offer to download latest instead of just asking to configure
            new ConfirmDialog(
                "Installed game version not set. Download latest version now?",
                ConfirmDialog.ConfirmDialogType.INFO,
                confirmed -> {
                    if (confirmed) {
                        if (!GameUpdateManager.isCddaPathConfigured()) {
                            new ConfirmDialog(
                                "CDDA executable path is not configured. Please set it in the Launcher tab first."
                            ).packCenterAndShow(parent);
                            return;
                        }

                        Optional<GameVersion> latestVersion = GameUpdateManager.getLatestGameReleaseTag();
                        if (latestVersion.isEmpty()) {
                            new ConfirmDialog(
                                "Could not determine latest version. Check your repository configuration."
                            ).packCenterAndShow(parent);
                            return;
                        }

                        new GameUpdateAvailableDialog(parent, latestVersion.get()).packCenterAndShow(parent);
                    }
                }
            ).packCenterAndShow(parent);
            return;
        }

        LOGGER.info("Checking for game updates...");

        Optional<Boolean> updateAvailable = GameUpdateManager.isGameUpdateAvailable();

        if (updateAvailable.isEmpty()) {
            new ConfirmDialog(
                "Could not check for updates. Verify your configuration."
            ).packCenterAndShow(parent);
            return;
        }

        if (!updateAvailable.get()) {
            LOGGER.debug("Game is up to date");
            if (showIfNoUpdate) {
                new ConfirmDialog("Your game is up to date!").packCenterAndShow(parent);
            }
            return;
        }

        // update is available - show the dialog ---
        Optional<GameVersion> latestVersion = GameUpdateManager.getLatestGameReleaseTag();
        if (latestVersion.isEmpty()) {
            new ConfirmDialog(
                "Update available but could not determine version."
            ).packCenterAndShow(parent);
            return;
        }

        new GameUpdateAvailableDialog(parent, latestVersion.get()).packCenterAndShow(parent);
    }
}
