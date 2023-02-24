package com.dazednconfused.catalauncher.gui;

import static com.dazednconfused.catalauncher.helper.Constants.APP_NAME;
import static com.dazednconfused.catalauncher.helper.Constants.CUSTOM_SAVE_PATH;
import static com.dazednconfused.catalauncher.helper.Constants.CUSTOM_TRASHED_SAVE_PATH;
import static com.dazednconfused.catalauncher.helper.Constants.CUSTOM_USER_DIR;

import com.dazednconfused.catalauncher.backup.SaveManager;
import com.dazednconfused.catalauncher.configuration.ConfigurationManager;
import com.dazednconfused.catalauncher.helper.FileExplorerManager;
import com.dazednconfused.catalauncher.helper.GitInfoManager;
import com.dazednconfused.catalauncher.helper.LogLevelManager;
import com.dazednconfused.catalauncher.helper.sysinfo.SystemInfoManager;
import com.dazednconfused.catalauncher.launcher.CDDALauncherManager;
import com.dazednconfused.catalauncher.soundpack.SoundpackManager;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import io.vavr.control.Try;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindow {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);

    private static final String[] CUSTOM_SAVE_DIR_ARGS = { "--savedir", CUSTOM_SAVE_PATH };
    private static final String[] CUSTOM_USER_DIR_ARGS = { "--userdir", CUSTOM_USER_DIR };

    /**
     * The array of all {@link Runnable}s to be executed on invocation of {@link #refreshGuiElements()}.
     * */
    private final Runnable[] guiRefreshingRunnables;

    private JPanel mainPanel;
    private JProgressBar globalProgressBar; // global between all tabs
    private JTabbedPane tabbedPane;

    // LAUNCHER TAB ---
    private JFormattedTextField cddaExecutableFTextField;
    private JButton openFinderButton;
    private JButton runButton;
    private JButton runLatestWorldButton;

    // SAVE BACKUPS TAB ---
    private JTable saveBackupsTable;
    private JButton backupNowButton;
    private JButton backupDeleteButton;
    private JButton backupRestoreButton;
    private JCheckBox backupOnExitCheckBox;

    // SOUNDPACKS TAB ---
    private JTable soundpacksTable;
    private JButton installSoundpackButton;
    private JButton uninstallSoundpackButton;

    /**
     * {@link MainWindow}'s main entrypoint.
     * */
    public static void main(String[] args) {
        LogLevelManager.changeGlobalLogLevelTo(ConfigurationManager.getInstance().isDebug() ? Level.TRACE : Level.INFO);

        SystemInfoManager.logSystemInformation(Level.DEBUG);

        LOGGER.info(
                "{} - Version {} - Build {} {}",
                APP_NAME,
                GitInfoManager.getInstance().getBuildVersion(),
                GitInfoManager.getInstance().getCommitIdFull(),
                GitInfoManager.getInstance().getBuildTime()
        );

        LOGGER.debug("Initializing main window [{}]...", APP_NAME);

        initializeLookAndFeel();

        JFrame frame = new JFrame(APP_NAME);

        frame.setJMenuBar(buildMenuBarFor(frame));

        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.setLocationRelativeTo(null); // center window
    }

    /**
     * Constructor.
     * */
    public MainWindow() {

        // INITIALIZE ALL GUI ELEMENTS ---
        this.guiRefreshingRunnables = new Runnable[] {
                this.setupTabbedPane(),
                this.setupMainExecutableGui(),
                this.setupSaveBackupsGui(),
                this.setupSoundpacksGui()
        };

        this.refreshGuiElements();
    }

    /**
     * Setups all GUI elements related to the tabbed pane management.
     * */
    private Runnable setupTabbedPane() {

        // TABBED PANE LISTENER ---
        this.tabbedPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                LOGGER.trace("Tabbed pane - key pressed [{}]", e.getKeyCode());
                super.keyTyped(e);

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    LOGGER.trace("Down arrow pressed. Shifting focus to underlying panel, if possible...");

                    saveBackupsTable.requestFocusInWindow();
                    soundpacksTable.requestFocusInWindow();
                }
            }
        });

        return () -> { }; // no GUI-refreshing action necessary for tabbed pane
    }

    /**
     * Setups all GUI elements related to the main executable management.
     *
     * @return The {@link Runnable} in charge or refreshing all GUI elements related to this setup on-demand.
     * */
    private Runnable setupMainExecutableGui() {

        // GLOBAL PROGRESS BAR LISTENER ---
        this.globalProgressBar.addChangeListener(e -> {
            if (this.globalProgressBar.getValue() == 100) {
                // refresh gui
                this.refreshGuiElements();

                // reset backup progressbar
                this.globalProgressBar.setValue(0);

                // disable until next backup
                this.globalProgressBar.setEnabled(false);
            }
        });

        // RUN BUTTON LISTENER ---
        this.runButton.addActionListener(e -> {
            LOGGER.trace("Run button clicked");

            String[] launcherArgs = ArrayUtils.addAll(CUSTOM_SAVE_DIR_ARGS, CUSTOM_USER_DIR_ARGS);

            CDDALauncherManager.executeCddaApplication(
                    ConfigurationManager.getInstance().getCddaPath(), launcherArgs
            );

            this.refreshGuiElements();
        });

        // RUN LATEST WORLD BUTTON LISTENER ---
        runLatestWorldButton.addActionListener(e -> {
            LOGGER.trace("Run Latest World clicked");

            String[] lastWorldArgs = SaveManager.getLatestSave().map(latestSave -> new String[]{ "--world", latestSave.getName() }).orElse(new String[]{});
            String[] launcherArgs = ArrayUtils.addAll(
                    ArrayUtils.addAll(CUSTOM_SAVE_DIR_ARGS, CUSTOM_USER_DIR_ARGS), lastWorldArgs
            );

            CDDALauncherManager.executeCddaApplication(
                    ConfigurationManager.getInstance().getCddaPath(), launcherArgs
            );

            this.refreshGuiElements();
        });

        // OPEN FINDER BUTTON LISTENER ---
        this.openFinderButton.addActionListener(e -> {
            LOGGER.trace("Explore button clicked");

            this.openFinder(this.mainPanel);

            this.refreshGuiElements();
        });

        return () -> {
            LOGGER.trace("Refreshing executable-management GUI elements...");

            String cddaPath = ConfigurationManager.getInstance().getCddaPath();

            boolean saveFilesExist = SaveManager.saveFilesExist();
            boolean pathPointsToValidGameExecutable = cddaPath != null && !cddaPath.isBlank();

            // SET EXECUTABLE TEXT FIELD WITH CDDA PATH FROM CONFIG ---
            // DETERMINE IF RUN BUTTON SHOULD BE ENABLED ---
            if (pathPointsToValidGameExecutable) {
                this.cddaExecutableFTextField.setText(cddaPath);
                this.runButton.setEnabled(true);
            } else {
                this.cddaExecutableFTextField.setText(null);
                this.runButton.setEnabled(false);
            }

            // DETERMINE IF RUN LATEST WORLD BUTTON SHOULD BE ENABLED ---
            if (pathPointsToValidGameExecutable && saveFilesExist) {
                this.runLatestWorldButton.setEnabled(true);
            } else {
                this.runLatestWorldButton.setEnabled(false);
            }
        };
    }

    /**
     * Setups all GUI elements related to save backup management.
     *
     * @return The {@link Runnable} in charge or refreshing all GUI elements related to this setup on-demand.
     * */
    private Runnable setupSaveBackupsGui() {

        // BACKUP NOW BUTTON LISTENER ---
        this.backupNowButton.addActionListener(e -> {
            LOGGER.trace("Save backup button clicked");

            // enable backup progressbar
            this.globalProgressBar.setEnabled(true);

            // disable backup buttons (don't want to do multiple operations simultaneously)
            this.disableSaveBackupButtons();

            SaveManager.backupCurrentSaves(percentageComplete -> this.globalProgressBar.setValue(percentageComplete)).ifPresent(Thread::start);
        });

        // BACKUP RESTORE BUTTON LISTENER ---
        this.backupRestoreButton.setMnemonic(KeyEvent.VK_R);
        this.backupRestoreButton.addActionListener(e -> {
            LOGGER.trace("Save backup restore button clicked");

            File selectedBackup = (File) this.saveBackupsTable.getValueAt(this.saveBackupsTable.getSelectedRow(), 1);
            LOGGER.trace("Save backup currently on selection: [{}]", selectedBackup);

            ConfirmDialog confirmDialog = new ConfirmDialog(
                String.format("Are you sure you want to restore the backup [%s]? Current save will be moved to trash folder [%s]", selectedBackup.getName(), CUSTOM_TRASHED_SAVE_PATH),
                ConfirmDialog.ConfirmDialogType.INFO,
                confirmed -> {
                    LOGGER.trace("Confirmation dialog result: [{}]", confirmed);

                    if (confirmed) {
                        // enable backup progressbar
                        this.globalProgressBar.setEnabled(true);

                        // disable backup buttons (don't want to do multiple operations simultaneously)
                        this.disableSaveBackupButtons();

                        SaveManager.restoreBackup(
                            selectedBackup,
                            percentageComplete -> this.globalProgressBar.setValue(percentageComplete)
                        ).ifPresent(Thread::start);
                    }

                    this.refreshGuiElements();
                }
            );

            confirmDialog.packCenterAndShow(this.mainPanel);
        });

        // BACKUP DELETE BUTTON LISTENER ---
        this.backupDeleteButton.setMnemonic(KeyEvent.VK_D);
        this.backupDeleteButton.addActionListener(e -> {
            LOGGER.trace("Delete backup button clicked");

            File selectedBackup = (File) this.saveBackupsTable.getValueAt(this.saveBackupsTable.getSelectedRow(), 1);
            LOGGER.trace("Save backup currently on selection: [{}]", selectedBackup);

            ConfirmDialog confirmDialog = new ConfirmDialog(
                String.format("Are you sure you want to delete the backup [%s]? This action is irreversible!", selectedBackup.getName()),
                ConfirmDialog.ConfirmDialogType.WARNING,
                confirmed -> {
                    LOGGER.trace("Confirmation dialog result: [{}]", confirmed);

                    if (confirmed) {
                        SaveManager.deleteBackup(selectedBackup);
                    }

                    this.refreshGuiElements();
                }
            );

            confirmDialog.packCenterAndShow(this.mainPanel);
        });

        // BACKUP TABLE LISTENER(S) ---
        this.saveBackupsTable.getSelectionModel().addListSelectionListener(event -> {
            LOGGER.trace("Save backups table row selected");

            if (saveBackupsTable.getSelectedRow() > -1) {
                this.backupDeleteButton.setEnabled(true);
                this.backupRestoreButton.setEnabled(true);
            }
        });

        BiConsumer<MouseEvent, JTable> onSaveBackupsTableRightClickEvent = (e, table) -> {
            LOGGER.trace("Save backups table clicked");

            int r = table.rowAtPoint(e.getPoint());
            if (r >= 0 && r < table.getRowCount()) {
                table.setRowSelectionInterval(r, r);
            } else {
                table.clearSelection();
            }

            int rowindex = table.getSelectedRow();
            if (rowindex < 0) {
                return;
            }

            if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                LOGGER.trace("Opening right-click popup for [{}]", table.getName());
                File targetFile = ((File) table.getValueAt(table.getSelectedRow(), 1));
                LOGGER.trace("File under selection: [{}]", targetFile);

                JPopupMenu popup = new JPopupMenu();

                JMenuItem openInFinder = new JMenuItem("Open in file explorer");
                openInFinder.addActionListener(e1 -> FileExplorerManager.openFileInFileExplorer(targetFile, true));
                popup.add(openInFinder);

                JMenuItem renameTo = new JMenuItem("Rename to...");
                renameTo.addActionListener(e1 -> {
                    LOGGER.trace("Rename backup menu clicked");

                    StringInputDialog confirmDialog = new StringInputDialog(
                        String.format("Rename backup [%s] to...", targetFile.getName()),
                        newNameOptional -> {
                            LOGGER.trace("User input dialog result: [{}]", newNameOptional);

                            newNameOptional.ifPresent(newName ->
                                SaveManager.renameBackup(targetFile, newName).peekLeft(throwable ->
                                    ErrorDialog.showErrorDialog("Could not rename save backup!", throwable).packCenterAndShow(this.mainPanel)
                                )
                            );

                            this.refreshGuiElements();
                        }
                    );

                    confirmDialog.packCenterAndShow(this.mainPanel);
                });
                popup.add(renameTo);

                JMenuItem deleteBackup = new JMenuItem("Delete...");
                deleteBackup.addActionListener(e1 -> backupDeleteButton.doClick());
                popup.add(deleteBackup);

                JMenuItem restoreBackup = new JMenuItem("Restore...");
                restoreBackup.addActionListener(e1 -> backupRestoreButton.doClick());
                popup.add(restoreBackup);

                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        };

        this.saveBackupsTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { // mousedPressed event needed for macOS - https://stackoverflow.com/a/3558324
                onSaveBackupsTableRightClickEvent.accept(e, (JTable) e.getComponent());
            }

            public void mouseReleased(MouseEvent e) { // mouseReleased event needed for other OSes
                onSaveBackupsTableRightClickEvent.accept(e, (JTable) e.getComponent());
            }
        });

        return () -> {
            LOGGER.trace("Refreshing save-backup-management GUI elements...");

            boolean saveFilesExist = SaveManager.saveFilesExist();

            // DETERMINE IF BACKUP NOW BUTTON SHOULD BE ENABLED ---
            if (saveFilesExist) {
                this.backupNowButton.setEnabled(true);
            } else {
                this.backupNowButton.setEnabled(false);
            }

            // SET SAVE BACKUPS TABLE ---
            this.refreshSaveBackupsTable();

            // DETERMINE IF BACKUP RESTORE BUTTON SHOULD BE DISABLED  ---
            // DETERMINE IF BACKUP DELETE BUTTON SHOULD BE DISABLED ---
            // (ie: if last backup was just deleted)
            if (SaveManager.listAllBackups().size() == 0 || this.saveBackupsTable.getSelectedRow() == -1) {
                this.backupDeleteButton.setEnabled(false);
                this.backupRestoreButton.setEnabled(false);
            }
        };
    }

    /**
     * Setups all GUI elements related to soundpack management.
     *
     * @return The {@link Runnable} in charge or refreshing all GUI elements related to this setup on-demand.
     * */
    private Runnable setupSoundpacksGui() {

        // SOUNDPACK INSTALL BUTTON LISTENER ---
        this.installSoundpackButton.addActionListener(e -> {
            LOGGER.trace("Install soundpack button clicked");

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select soundpack folder to install");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showSaveDialog(mainPanel);

            if (result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
                // setup dummy timer to give user visual feedback that his operation is in progress...
                Timer dummyTimer = new Timer(10, e1 -> {
                    if (this.globalProgressBar.getValue() <= 100) {
                        this.globalProgressBar.setValue(this.globalProgressBar.getValue() + 1);
                    }
                });

                // start timer before triggering installation
                dummyTimer.start();

                // start installation and give it a callback to stop the dummy timer
                SoundpackManager.installSoundpack(fileChooser.getSelectedFile(), p -> dummyTimer.stop());
            } else {
                LOGGER.trace("Exiting soundpack finder dialog with no selection...");
            }

            this.refreshGuiElements();
        });

        // SOUNDPACK DELETE BUTTON LISTENER ---
        this.uninstallSoundpackButton.addActionListener(e -> {
            LOGGER.trace("Uninstall soundpack button clicked");

            File selectedSoundpack = (File) this.soundpacksTable.getValueAt(this.soundpacksTable.getSelectedRow(), 1);
            LOGGER.trace("Soundpack currently on selection: [{}]", selectedSoundpack);

            ConfirmDialog confirmDialog = new ConfirmDialog(
                String.format("Are you sure you want to delete the soundpack [%s]? This action is irreversible!", selectedSoundpack.getName()),
                ConfirmDialog.ConfirmDialogType.WARNING,
                confirmed -> {
                    LOGGER.trace("Confirmation dialog result: [{}]", confirmed);

                    if (confirmed) {
                        SoundpackManager.deleteSoundpack(selectedSoundpack);
                    }

                    this.refreshGuiElements();
                }
            );

            confirmDialog.packCenterAndShow(this.mainPanel);
        });

        // SOUNDPACKS TABLE LISTENER(S) ---
        this.soundpacksTable.getSelectionModel().addListSelectionListener(event -> {
            LOGGER.trace("Soundpacks table row selected");

            if (soundpacksTable.getSelectedRow() > -1) {
                this.uninstallSoundpackButton.setEnabled(true);
            }
        });

        BiConsumer<MouseEvent, JTable> onSoundpacksTableRightClickEvent = (e, table) -> {
            LOGGER.trace("Soundpacks table clicked");

            int r = table.rowAtPoint(e.getPoint());
            if (r >= 0 && r < table.getRowCount()) {
                table.setRowSelectionInterval(r, r);
            } else {
                table.clearSelection();
            }

            int rowindex = table.getSelectedRow();
            if (rowindex < 0) {
                return;
            }

            if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                LOGGER.trace("Opening right-click popup for [{}]", table.getName());
                File targetFile = ((File) table.getValueAt(table.getSelectedRow(), 1));

                JPopupMenu popup = new JPopupMenu();

                JMenuItem openInFinder = new JMenuItem("Open folder in file explorer");
                openInFinder.addActionListener(e1 -> {
                    FileExplorerManager.openFileInFileExplorer(targetFile, false);
                });
                popup.add(openInFinder);

                JMenuItem uninstall = new JMenuItem("Uninstall...");
                uninstall.addActionListener(e1 -> uninstallSoundpackButton.doClick());
                popup.add(uninstall);

                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        };

        this.soundpacksTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { // mousedPressed event needed for macOS - https://stackoverflow.com/a/3558324
                onSoundpacksTableRightClickEvent.accept(e, (JTable) e.getComponent());
            }

            public void mouseReleased(MouseEvent e) { // mouseReleased event needed for other OSes
                onSoundpacksTableRightClickEvent.accept(e, (JTable) e.getComponent());
            }
        });

        return () -> {
            LOGGER.trace("Refreshing soundpack-management GUI elements...");

            // SET SOUNDPACKS TABLE ---
            this.refreshSoundpacksTable();

            // DETERMINE IF SOUNDPACK DELETE BUTTON SHOULD BE DISABLED ---
            // (ie: if last backup was just deleted)
            if (SoundpackManager.listAllSoundpacks().size() == 0 || this.soundpacksTable.getSelectedRow() == -1) {
                this.uninstallSoundpackButton.setEnabled(false);
            }
        };
    }

    /**
     * Disables all backup-section buttons.
     */
    private void disableSaveBackupButtons() {
        LOGGER.trace("Disabling save backup buttons...");

        this.backupNowButton.setEnabled(false);
        this.backupDeleteButton.setEnabled(false);
        this.backupRestoreButton.setEnabled(false);
    }

    /**
     * Initializes {@link FlatLaf}'s Look & Feel (and other MacOS-specific goodies).
     */
    private static void initializeLookAndFeel() {
        LOGGER.trace("Initializing Look & Feel...");

        System.setProperty("apple.awt.application.name", APP_NAME);
        System.setProperty("apple.awt.application.appearance", "system");

        FlatDarkLaf.setup();
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarculaLaf"); // cascade look & feel to all children widgets from now on
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds the main {@link Component}'s {@link JMenuBar}.
     */
    private static JMenuBar buildMenuBarFor(Component parent) {
        LOGGER.trace("Building menu bar...");

        // main menu bar ---
        JMenuBar menuBar = new JMenuBar();

        // help menu ---
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        // developer tools submenu --
        JMenu developerTools = new JMenu("Developer Tools");
        helpMenu.add(developerTools);

        // show console log button -
        JMenuItem showConsoleLog = new JMenuItem("Show console log");
        showConsoleLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
        showConsoleLog.addActionListener(e -> {
            LOGGER.trace("Show console button clicked");
            Try.of(ConsoleLogReader::new)
                    .andThen(consoleLogReader -> consoleLogReader.packCenterAndShow(parent))
                    .onFailure(throwable -> LOGGER.error("There was an error while ConsoleLogReader window: [{}]", throwable.getMessage()));
        });
        developerTools.add(showConsoleLog);

        // debug mode checkbox -
        JCheckBoxMenuItem debugMode = new JCheckBoxMenuItem("Debug mode");
        debugMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK));
        debugMode.setState(ConfigurationManager.getInstance().isDebug());
        debugMode.addActionListener(e -> {
            LOGGER.trace("Debug mode checkbox clicked. Enabled: [{}]", debugMode.getState());
            ConfigurationManager.getInstance().setDebug(debugMode.getState());
            LogLevelManager.changeGlobalLogLevelTo(debugMode.getState() ? Level.TRACE : Level.INFO);
        });
        developerTools.add(debugMode);

        // separator --
        helpMenu.addSeparator();

        // about button --
        JMenuItem about = new JMenuItem("About");
        about.setMnemonic(KeyEvent.VK_T);
        about.addActionListener(e -> {
            LOGGER.trace("About button clicked");

            AboutDialog aboutDialog = new AboutDialog();
            aboutDialog.packCenterAndShow(parent);
        });
        helpMenu.add(about);

        return menuBar;
    }

    /**
     * Refreshes all GUI elements according to diverse app statuses.
     */
    private void refreshGuiElements() {
        for (Runnable guiRefreshRunnable : this.guiRefreshingRunnables) {
            guiRefreshRunnable.run();
        }
    }

    /**
     * Opens a finder dialog and populates the executable field with the selected file/folder.
     */
    private void openFinder(Component parent) {
        this.cddaExecutableFTextField.setText(""); // clear your JTextArea.

        JFileChooser cddaAppChooser = new JFileChooser();
        cddaAppChooser.setDialogTitle("Select your CDDA Executable");
        cddaAppChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        cddaAppChooser.setFileFilter(new FileNameExtensionFilter("CDDA .app file", ".app"));
        int result = cddaAppChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION && cddaAppChooser.getSelectedFile() != null) {
            String fileName = cddaAppChooser.getSelectedFile().getPath();
            ConfigurationManager.getInstance().setCddaPath(fileName);
            this.cddaExecutableFTextField.setText(fileName);
        } else {
            LOGGER.trace("Exiting CDDA .app finder with no selection...");
        }
    }

    /**
     * Refreshes current {@link #saveBackupsTable} with latest info coming from {@link SaveManager}.
     */
    private void refreshSaveBackupsTable() {
        LOGGER.trace("Refreshing save backups table...");

        String[] columns = new String[]{"Name", "Path", "Size", "Date"};

        List<Object[]> values = new ArrayList<>();
        SaveManager.listAllBackups().stream().sorted(Comparator.comparing(File::lastModified).reversed()).forEach(backup ->
            values.add(new Object[]{
                backup.getName(),
                backup,
                backup.length() / (1024 * 1024) + " MB",
                new Date(backup.lastModified())
            })
        );

        TableModel tableModel = new DefaultTableModel(values.toArray(new Object[][]{}), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.saveBackupsTable.setModel(tableModel);
    }

    /**
     * Refreshes current {@link #soundpacksTable} with latest info coming from {@link SoundpackManager}.
     */
    private void refreshSoundpacksTable() {
        LOGGER.trace("Refreshing soundpacks table...");

        String[] columns = new String[]{"Name", "Path", "Size", "Date"};

        List<Object[]> values = new ArrayList<>();
        SoundpackManager.listAllSoundpacks().stream().sorted(Comparator.comparing(File::lastModified).reversed()).forEach(soundpack ->
                values.add(new Object[]{
                    soundpack.getName(),
                    soundpack,
                    FileUtils.sizeOfDirectory(soundpack) / (1024 * 1024) + " MB",
                    new Date(soundpack.lastModified())
                })
        );

        TableModel tableModel = new DefaultTableModel(values.toArray(new Object[][]{}), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.soundpacksTable.setModel(tableModel);
    }
}
