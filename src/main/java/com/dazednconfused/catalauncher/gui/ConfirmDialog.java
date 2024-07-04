package com.dazednconfused.catalauncher.gui;

import static com.dazednconfused.catalauncher.gui.helper.GuiResource.extractIconFrom;
import static com.dazednconfused.catalauncher.helper.Constants.ICONS_PATH;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.apache.batik.swing.JSVGCanvas;

public class ConfirmDialog extends JDialog {

    public static final Consumer<Boolean> DO_NOTHING_ACTION = bool -> { }; // does nothing - represents an empty action

    private static final String ERROR_ICON = extractIconFrom(ICONS_PATH + "/" + "errorDialog.svg");
    private static final String INFO_ICON = extractIconFrom(ICONS_PATH + "/" + "informationDialog.svg");
    private static final String WARN_ICON = extractIconFrom(ICONS_PATH + "/" + "warningDialog.svg");

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel dialogMessage;
    private JSVGCanvas iconSvg;

    /**
     * Shows a confirmation dialog, which is basically a {@link JDialog} composed of a {@link String} {@code message}, a {@link ConfirmDialogType}
     * which gives visual feedback of the severity of the message by means of different icons, and a {@link Consumer} action
     * to be triggered upon confirmation or cancellation of the dialog.
     *
     * @param message The message to show in the dialog.
     * @param dialogType The type of dialog, representing its severity. Applicable types are {@link ConfirmDialogType#INFO},
     *                   {@link ConfirmDialogType#WARNING} and {@link ConfirmDialogType#ERROR}. {@link ConfirmDialogType#NONE}
     *                   is equivalent to using {@link ConfirmDialogType#INFO}.
     * @param doOnResult The action to be executed upon <i>confirmation</i> (user clicks the {@code OK} button) or <i>cancellation</i>
     *                   (user clicks the {@code Cancel} button and/or exits the {@link JDialog} by simply closing it).
     *                   <ul>
     *                      <li>A <i>confirmation</i> will pass a {@code true} to the provided consumer.</li>
     *                      <li>A <i>cancellation</i> will pass a {@code false} to the provided consumer.</li>
     *                   </ul>
     *                   If <i>confirmation</i> or <i>cancellation</i> is irrelevant for the desired {@link ConfirmDialog}
     *                   (ie: we just want to show a non-actionable message to the user), then a {@link #DO_NOTHING_ACTION}
     *                   may be passed to this parameter. This will make the resulting {@link JDialog} to only have an {@code OK}
     *                   button that will just close the dialog and nothing else.
     * */
    public ConfirmDialog(String message, ConfirmDialogType dialogType, Consumer<Boolean> doOnResult) {

        // initialize dialog ---
        setContentPane(contentPane);
        setModalityType(ModalityType.APPLICATION_MODAL);

        // set default button ---
        getRootPane().setDefaultButton(buttonCancel);

        // set icon ---
        iconSvg.setBackground(new Color(0, 0, 0, 0)); // make transparent
        switch (dialogType) {
            case WARNING:
                iconSvg.setURI(WARN_ICON);
                break;
            case ERROR:
                iconSvg.setURI(ERROR_ICON);
                break;
            case NONE:
            case INFO:
            default:
                iconSvg.setURI(INFO_ICON);
                break;
        }

        // set dialog message ---
        dialogMessage.setText(message);

        // add action listeners to buttons ---
        buttonOK.addActionListener(e -> onOK(doOnResult));
        buttonCancel.addActionListener(e -> onCancel(doOnResult));

        // call onCancel() when cross is clicked ---
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel(doOnResult);
            }
        });

        // call onCancel() on ESCAPE ---
        contentPane.registerKeyboardAction(e -> onCancel(doOnResult), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // if defined action is DO_NOTHING_ACTION, then the OK/Cancel buttons do virtually the same thing: nothing
        // hide the OK button from view, there is no need for redundancy
        // also, make the Cancel feel like an 'OK' button from a user's perspective
        if (doOnResult == DO_NOTHING_ACTION) {
            buttonOK.setVisible(false);
            buttonCancel.setText("OK");
        }

        this.spawnOnTopOfParentComponent(); // without this, dialog gets created _behind_ parent and doesn't come on top until Swing redraws, for some reason
    }

    /**
     * Packs ({@link #pack()}), centers ({@link #setLocationRelativeTo(Component)}) and sets the current dialog as visible
     * ({@link #setVisible(boolean)}).
     */
    public void packCenterAndShow(JPanel parent) {
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }

    /**
     * Forces this {@link JDialog} to always spawn on top of its parent.
     * */
    private void spawnOnTopOfParentComponent() {
        final JDialog thisDialog = this;
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent evt) {
                thisDialog.toFront();
                thisDialog.requestFocus();
            }
        });
    }

    /**
     * Represents the action to be taken when the {@code OK} button is clicked.
     * */
    private void onOK(Consumer<Boolean> doOnResult) {
        doOnResult.accept(true);
        dispose();
    }

    /**
     * Represents the action to be taken when the {@code Cancel} button is clicked.
     * */
    private void onCancel(Consumer<Boolean> doOnResult) {
        doOnResult.accept(false);
        dispose();
    }

    /**
     * The {@link ConfirmDialog}'s type. Affects things like its custom icon.
     */
    public enum ConfirmDialogType {
        NONE,
        WARNING,
        ERROR,
        INFO
    }
}

