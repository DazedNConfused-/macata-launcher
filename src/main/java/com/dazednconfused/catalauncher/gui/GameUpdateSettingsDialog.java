package com.dazednconfused.catalauncher.gui;

import com.dazednconfused.catalauncher.configuration.ConfigurationManager;
import com.dazednconfused.catalauncher.update.GameUpdateManager;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog for configuring game update source settings.
 */
public class GameUpdateSettingsDialog extends JDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameUpdateSettingsDialog.class);

    private JPanel contentPane;
    private JLabel titleLabel;
    private JTextField repoOwnerTextField;
    private JTextField repoNameTextField;
    private JTextField installedVersionTextField;
    private JCheckBox autoCheckCheckbox;
    private JButton checkNowButton;
    private JButton saveButton;
    private JButton cancelButton;

    /**
     * Constructor.
     */
    public GameUpdateSettingsDialog() {
        setContentPane(contentPane);
        setModal(true);
        setTitle("Game Update Settings");

        // Load current configuration
        loadConfiguration();

        // Configure save button
        saveButton.addActionListener(e -> {
            LOGGER.trace("Save button clicked");
            saveConfiguration();
            dispose();
        });

        // Configure cancel button
        cancelButton.addActionListener(e -> {
            LOGGER.trace("Cancel button clicked");
            dispose();
        });

        // Configure check now button
        checkNowButton.addActionListener(e -> {
            LOGGER.trace("Check now button clicked");
            // Save current values first
            saveConfiguration();
            // Then check for updates
            checkForGameUpdates();
        });

        // Close on window close
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // Close on ESCAPE
        contentPane.registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * Loads current configuration values into the form fields.
     */
    private void loadConfiguration() {
        ConfigurationManager config = ConfigurationManager.getInstance();

        String owner = config.getGameGithubRepoOwner();
        String name = config.getGameGithubRepoName();
        String version = config.getInstalledGameVersion();
        boolean autoCheck = config.isShouldCheckForGameUpdates();

        repoOwnerTextField.setText(owner != null ? owner : "");
        repoNameTextField.setText(name != null ? name : "");
        installedVersionTextField.setText(version != null ? version : "");
        autoCheckCheckbox.setSelected(autoCheck);
    }

    /**
     * Saves form field values to configuration.
     */
    private void saveConfiguration() {
        ConfigurationManager config = ConfigurationManager.getInstance();

        String owner = repoOwnerTextField.getText().trim();
        String name = repoNameTextField.getText().trim();
        String version = installedVersionTextField.getText().trim();
        boolean autoCheck = autoCheckCheckbox.isSelected();

        config.setGameGithubRepoOwner(owner.isEmpty() ? null : owner);
        config.setGameGithubRepoName(name.isEmpty() ? null : name);
        config.setInstalledGameVersion(version.isEmpty() ? null : version);
        config.setShouldCheckForGameUpdates(autoCheck);

        LOGGER.info("Game update settings saved: owner=[{}], repo=[{}], version=[{}], autoCheck=[{}]",
            owner, name, version, autoCheck);
    }

    /**
     * Performs an update check and shows appropriate dialog.
     */
    private void checkForGameUpdates() {
        if (!GameUpdateManager.isConfigured()) {
            new ConfirmDialog(
                "Please configure the GitHub repository owner and name first."
            ).packCenterAndShow(contentPane);
            return;
        }

        if (!GameUpdateManager.isInstalledVersionConfigured()) {
            new ConfirmDialog(
                "Please enter your currently installed game version."
            ).packCenterAndShow(contentPane);
            return;
        }

        LOGGER.info("Checking for game updates...");

        Optional<Boolean> updateAvailable = GameUpdateManager.isGameUpdateAvailable();

        if (updateAvailable.isEmpty()) {
            new ConfirmDialog(
                "Could not determine if an update is available. Check the console log for details."
            ).packCenterAndShow(contentPane);
            return;
        }

        if (!updateAvailable.get()) {
            new ConfirmDialog(
                "Your game is up to date!"
            ).packCenterAndShow(contentPane);
        } else {
            new ConfirmDialog(
                "A new game version is available! Open the releases page?",
                ConfirmDialog.ConfirmDialogType.INFO,
                confirmed -> {
                    if (confirmed) {
                        GameUpdateManager.openLatestGameReleaseInDefaultBrowser();
                    }
                }
            ).packCenterAndShow(contentPane);
        }
    }

    /**
     * Packs, centers, and shows the dialog.
     *
     * @param parent the parent component for centering
     */
    public void packCenterAndShow(Component parent) {
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }
}
