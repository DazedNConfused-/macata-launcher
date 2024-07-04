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
     * Constructor.
     * */
    public ConfirmDialog(String message, ConfirmDialogType dialogType, Consumer<Boolean> doOnResult) {

        // initialize dialog ---
        setContentPane(contentPane);
        setModal(true);

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
        buttonOK.setVisible(false);
        buttonCancel.setText("OK");
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

    private void onOK(Consumer<Boolean> doOnResult) {
        doOnResult.accept(true);
        dispose();
    }

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

