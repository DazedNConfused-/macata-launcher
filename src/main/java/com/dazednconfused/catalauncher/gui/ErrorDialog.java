package com.dazednconfused.catalauncher.gui;

import static com.dazednconfused.catalauncher.gui.helper.GuiResource.extractIconFrom;
import static com.dazednconfused.catalauncher.helper.Constants.ICONS_PATH;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import javax.swing.*;

import org.apache.batik.swing.JSVGCanvas;

public class ErrorDialog extends JDialog {

    private static final String ERROR_ICON = extractIconFrom(ICONS_PATH + "/" + "errorDialog.svg");

    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel dialogMessage;
    private JSVGCanvas iconSvg;
    private JTextField errorMessage;

    /**
     * Constructor.
     * */
    public ErrorDialog(String message, Throwable t, Consumer<Boolean> doOnResult) {

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
        errorMessage.setText(t.getLocalizedMessage());

        // add action listeners to buttons ---
        buttonOK.addActionListener(e -> onOK(doOnResult));

        // call onCancel() when cross is clicked ---
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK(doOnResult);
            }
        });

        // call onCancel() on ESCAPE ---
        contentPane.registerKeyboardAction(e -> onOK(doOnResult), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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


}

