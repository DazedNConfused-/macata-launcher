package com.dazednconfused.catalauncher.gui;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class StringInputDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel dialogMessage;
    private JTextField inputTextField;

    /**
     * Constructor.
     * */
    public StringInputDialog(String message, Consumer<Optional<String>> doOnResult) {

        // initialize dialog ---
        setContentPane(contentPane);
        setModal(true);

        // set default button ---
        getRootPane().setDefaultButton(buttonOK);

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
     * Action to do on {@link #buttonOK} click.
     * */
    private void onOK(Consumer<Optional<String>> doOnResult) {
        doOnResult.accept(Optional.of(this.inputTextField.getText()).flatMap(s -> {
            if (s.isBlank()) {
                return Optional.empty();
            } else {
                return Optional.of(s);
            }
        }));
        dispose();
    }

    /**
     * Action to do on {@link #buttonCancel} click.
     * */
    private void onCancel(Consumer<Optional<String>> doOnResult) {
        doOnResult.accept(Optional.empty());
        dispose();
    }
}

