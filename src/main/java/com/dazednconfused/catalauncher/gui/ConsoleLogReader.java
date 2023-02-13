package com.dazednconfused.catalauncher.gui;

import static com.dazednconfused.catalauncher.helper.Constants.LOG_FILE_PATH;

import io.vavr.control.Try;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

public class ConsoleLogReader {

    private Timer timer;
    private JTextArea textArea;
    private JFrame frame;

    /**
     * Constructor.
     * */
    public ConsoleLogReader() throws IOException {
        this.frame = new JFrame("Log");

        this.textArea = new JTextArea(25, 60);
        this.textArea.setEditable(false);

        // populate current console with existing logs ---
        File file = new File(LOG_FILE_PATH);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            this.textArea.append(line + "\n");
        }

        // setup reader timer, so it keeps reading logs as they come ---
        this.timer = new Timer(100, e ->
                Try.of(reader::readLine).andThen(s -> {
                    if (s != null) {
                        textArea.append(s + "\n");
                    }
                })
        );
        this.timer.start();

        // finish setting up log window ---
        this.frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    /**
     * Packs ({@link JFrame#pack()}), centers ({@link JFrame#setLocationRelativeTo(Component)}) and sets the current dialog as visible
     * ({@link JFrame#setVisible(boolean)}).
     * */
    public void packCenterAndShow(Component parent) {
        this.frame.pack();
        this.frame.setLocationRelativeTo(parent);
        this.frame.setVisible(true);
    }
}