package com.dazednconfused.catalauncher.gui;

import com.dazednconfused.catalauncher.update.GameUpdateManager;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog that shows download progress when updating the game.
 */
public class GameUpdateDownloadDialog extends JDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameUpdateDownloadDialog.class);

    private final JPanel contentPane;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    private SwingWorker<Boolean, Void> downloadWorker;
    private boolean completed = false;
    private boolean success = false;

    /**
     * Constructor.
     */
    public GameUpdateDownloadDialog() {
        setTitle("Downloading Game Update");
        setModal(true);
        setResizable(false);

        // build UI manually (simple dialog) ---
        contentPane = new JPanel();
        contentPane.setLayout(new java.awt.BorderLayout(10, 10));
        contentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        statusLabel = new JLabel("Initializing...");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        contentPane.add(statusLabel, java.awt.BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new java.awt.Dimension(400, 25));
        contentPane.add(progressBar, java.awt.BorderLayout.CENTER);

        setContentPane(contentPane);

        // prevent closing during download ---
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (completed) {
                    dispose();
                }
            }
        });
    }

    /**
     * Starts the download process.
     */
    public void startDownload() {
        downloadWorker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return GameUpdateManager.downloadAndInstallUpdate(
                    progress -> SwingUtilities.invokeLater(() -> progressBar.setValue(progress)),
                    status -> SwingUtilities.invokeLater(() -> statusLabel.setText(status))
                );
            }

            @Override
            protected void done() {
                completed = true;
                try {
                    success = get();
                } catch (Exception e) {
                    LOGGER.error("Download worker error", e);
                    success = false;
                }
                dispose();
            }
        };

        downloadWorker.execute();
    }

    /**
     * Returns whether the download was successful.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Shows the dialog, starts the download, and returns success status.
     *
     * @param parent the parent component
     * @return true if download and installation succeeded
     */
    public static boolean showAndDownload(Component parent) {
        GameUpdateDownloadDialog dialog = new GameUpdateDownloadDialog();
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        // Start download after dialog is visible
        SwingUtilities.invokeLater(dialog::startDownload);

        dialog.setVisible(true); // blocks until dialog is closed

        return dialog.isSuccess();
    }
}
