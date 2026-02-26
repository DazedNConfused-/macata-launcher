package com.dazednconfused.catalauncher.gui;

import com.dazednconfused.catalauncher.configuration.ConfigurationManager;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

        // load current configuration ---
        loadConfiguration();

        // configure save button ---
        saveButton.addActionListener(e -> {
            LOGGER.trace("Save button clicked");
            saveConfiguration();
            dispose();
        });

        // configure cancel button ---
        cancelButton.addActionListener(e -> {
            LOGGER.trace("Cancel button clicked");
            dispose();
        });

        // configure check now button ---
        checkNowButton.addActionListener(e -> {
            LOGGER.trace("Check now button clicked");
            // save current values first...
            saveConfiguration();
            // then check for updates...
            checkForGameUpdates();
        });

        // call onCancel() when cross is clicked ---
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE ---
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

        repoOwnerTextField.setText(owner != null ? owner : "");
        repoNameTextField.setText(name != null ? name : "");
        installedVersionTextField.setText(version != null ? version : "");

        boolean autoCheck = config.isShouldCheckForGameUpdates();

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
        // use the new dialog that offers download option ---
        GameUpdateAvailableDialog.checkAndShow(contentPane, true);
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
