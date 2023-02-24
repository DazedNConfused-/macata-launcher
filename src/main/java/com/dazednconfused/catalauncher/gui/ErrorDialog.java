package com.dazednconfused.catalauncher.gui;

import static com.dazednconfused.catalauncher.gui.helper.GuiResource.extractIconFrom;
import static com.dazednconfused.catalauncher.helper.Constants.ICONS_PATH;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ErrorDialog extends JDialog {

    private static final String ERROR_ICON = extractIconFrom(ICONS_PATH + "/" + "errorDialog.svg");

    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel dialogMessage;
    private JSVGCanvas iconSvg;
    private JTextArea errorMessage;

    /**
     * Constructor.
     * */
    private ErrorDialog(String message, Throwable t, Runnable doOnWindowClose) {

        // initialize dialog ---
        setContentPane(contentPane);
        setModal(true);

        // set default button ---
        getRootPane().setDefaultButton(buttonOK);

        // set icon ---
        iconSvg.setBackground(new Color(0, 0, 0, 0)); // make transparent
        iconSvg.setURI(ERROR_ICON);

        // set dialog message ---
        dialogMessage.setText(message);
        errorMessage.setText(ExceptionUtils.getMessage(t));

        // add action listeners to buttons ---
        buttonOK.addActionListener(e -> onOK(doOnWindowClose));

        // call onCancel() when cross is clicked ---
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK(doOnWindowClose);
            }
        });

        // call onCancel() on ESCAPE ---
        contentPane.registerKeyboardAction(e -> onOK(doOnWindowClose), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * Constructs a new {@link ErrorDialog} instance that does nothing more than showing the error message.
     * */
    public static ErrorDialog showErrorDialog(String message, Throwable t) {
        return new ErrorDialog(message, t, () -> {});
    }

    /**
     * Constructs a new {@link ErrorDialog} instance that shows the error message and additionally performs the {@code doOnWindowClose}
     * {@link Runnable} on the dialog's closing action.
     * */
    public static ErrorDialog showErrorDialog(String message, Throwable t, Runnable doOnWindowClose) {
        return new ErrorDialog(message, t, doOnWindowClose);
    }

    /**
     * Packs ({@link #pack()}), centers ({@link #setLocationRelativeTo(Component)}), sets the current dialog as visible
     * ({@link #setVisible(boolean)}), brings it to the front ({@link #toFront()}) of the given {@code parent}, and finally
     * requests focus ({@link #requestFocus()}).
     */
    public void packCenterAndShow(JPanel parent) {
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);

        this.toFront();
        this.requestFocus();
    }

    private void onOK(Runnable doOnResult) {
        doOnResult.run();
        dispose();
    }

}

