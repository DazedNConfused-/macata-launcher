package com.dazednconfused.catalauncher.gui;

import static com.dazednconfused.catalauncher.helper.Constants.APP_NAME;

import com.dazednconfused.catalauncher.configuration.ConfigurationManager;
import com.dazednconfused.catalauncher.helper.GitInfoManager;
import com.dazednconfused.catalauncher.update.UpdateManager;

import io.vavr.control.Try;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionManagerWindow extends JDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionManagerWindow.class);

    private static final String LOGO_PATH = "logo/logo.png";

    private JPanel contentPane;
    private JLabel appNameLabel;
    private JLabel buildVersionLabel;
    private JLabel buildCommitLabel;
    private JLabel buildTimeLabel;
    private JLabel logoLabel;
    private JCheckBox autoUpdaterCheckbox;
    private JButton updateNowButton;

    /**
     * Constructor.
     * */
    public VersionManagerWindow() {

        // initialize dialog ---
        setContentPane(contentPane);
        setModal(true);

        // set logo ---
        BufferedImage cddaImage = Try.of(() -> ImageIO.read(getLogoIcon()))
                .onFailure(t -> LOGGER.error("There was an error while loading AboutDialog's logo", t))
                .getOrElseThrow(() -> new RuntimeException("There was an error while loading AboutDialog's logo"));
        logoLabel.setIcon(new ImageIcon(cddaImage));

        // set build info ---
        appNameLabel.setText(APP_NAME);
        buildVersionLabel.setText("Version " + GitInfoManager.getInstance().getBuildVersion());
        buildCommitLabel.setText("Build " + GitInfoManager.getInstance().getCommitIdFull());
        buildTimeLabel.setText(GitInfoManager.getInstance().getBuildTime());

        // configure update now button ---
        updateNowButton.addActionListener(e -> {
            LOGGER.trace("Update now button clicked");
            checkForUpdates(contentPane, true);
        });

        // configure auto-update checkbox ---
        this.autoUpdaterCheckbox.setSelected(ConfigurationManager.getInstance().isShouldLookForUpdates());
        autoUpdaterCheckbox.addActionListener(e -> {
            LOGGER.trace("Auto updater checkbox changed to [{}]", autoUpdaterCheckbox.isSelected());
            ConfigurationManager.getInstance().setShouldLookForUpdates(autoUpdaterCheckbox.isSelected());
        });

        // call onOK() when cross is clicked ---
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onOK() on ESCAPE ---
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
    }

    /**
     * Checks for new binary releases and prompts the user in case an update is available.
     * */
    public static void checkForUpdates(JPanel parent, boolean showDialogIfNoUpdateAvailable) {
        LOGGER.info("Checking for updates...");

        boolean updateAvailable = UpdateManager.isUpdateAvailable().orElse(false);

        if (!updateAvailable) {
            LOGGER.debug("No update is available");

            if (!showDialogIfNoUpdateAvailable) {
                LOGGER.trace("Skipping showing 'no update available' dialog because showDialogIfNoUpdateAvailable is false'");
                return;
            }

            ConfirmDialog confirmDialog = new ConfirmDialog(
                "There were no updates found",
                ConfirmDialog.ConfirmDialogType.NONE,
                ConfirmDialog.DO_NOTHING_ACTION
            );

            confirmDialog.packCenterAndShow(parent);
        } else {
            LOGGER.debug("Update is available");
            ConfirmDialog confirmDialog = new ConfirmDialog(
                "A new version is available! Check the releases page now?",
                ConfirmDialog.ConfirmDialogType.INFO,
                confirmed -> {
                    if (confirmed) {
                        UpdateManager.openLatestReleaseInDefaultBrowser();
                    } else {
                        LOGGER.debug("Aborting update process due to user input");
                    }
                }
            );
            confirmDialog.packCenterAndShow(parent);
        }
    }

    /**
     * Packs ({@link #pack()}), centers ({@link #setLocationRelativeTo(Component)}) and sets the current dialog as visible
     * ({@link #setVisible(boolean)}).
     */
    public void packCenterAndShow(Component parent) {
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }

    /**
     * Retrieves the application's logo as an {@link InputStream} from {@link #LOGO_PATH}.
     * */
    private InputStream getLogoIcon() {
        return this.getClass().getClassLoader().getResourceAsStream(LOGO_PATH);
    }
}
