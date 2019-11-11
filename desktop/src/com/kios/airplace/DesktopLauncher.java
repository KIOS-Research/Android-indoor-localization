package com.kios.airplace;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class DesktopLauncher {
    private static JFrame jFrame;
    private JPanel jPanel;
    private JButton logFilesFolderButton;
    private JButton buildButton;
    private JButton radioMapsFolderButton;
    private JButton logFileButton;
    private JButton outputDirectoryButton;
    private JButton runButton;

    private String logsFilesFolderPath;
    private boolean[] clicked = {false, false, false};

    private DesktopLauncher() {
        logFilesFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    boolean checkFolderName = jFileChooser.getSelectedFile().getName().equals("logs");
                    boolean checkRss = new File(jFileChooser.getSelectedFile() + "\\rss").exists();
                    boolean checkMagnetic = new File(jFileChooser.getSelectedFile() + "\\magnetic").exists();

                    if (checkFolderName && checkRss && checkMagnetic) {
                        buildButton.setEnabled(true);
                        logsFilesFolderPath = String.valueOf(jFileChooser.getSelectedFile().getParent());
                    }
                }
            }
        });
        buildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CreateRadioMaps(logsFilesFolderPath);
            }
        });
        radioMapsFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    clicked[0] = true;

                    if (clicked[1] && clicked[2])
                        runButton.setEnabled(true);

                    String radioMapsDirectory = jFileChooser.getSelectedFile().getAbsolutePath();

                    Globals.RSS_NAV_FILE_PATH = radioMapsDirectory + "\\rss\\rssRadioMap-mean.txt";
                    Globals.MAGNETIC_NAV_FILE_PATH = radioMapsDirectory + "\\magnetic\\magneticRadioMap-mean.txt";
                }
            }
        });
        logFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jFileChooser.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));

                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    clicked[1] = true;

                    if (clicked[0] && clicked[2])
                        runButton.setEnabled(true);

                    String logsFileDirectory = jFileChooser.getCurrentDirectory().getParent();
                    String logFilesDate = jFileChooser.getSelectedFile().getName().split("-")[1];

                    Globals.RSS_LOG_FILE_PATH = logsFileDirectory + "\\rss\\rssFile-" + logFilesDate;
                    Globals.MAGNETIC_LOG_FILE_PATH = logsFileDirectory + "\\magnetic\\magneticFile-" + logFilesDate;
                }
            }
        });
        outputDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    clicked[2] = true;

                    if (clicked[0] && clicked[1])
                        runButton.setEnabled(true);

                    Globals.WRITER_PATH = jFileChooser.getSelectedFile().getAbsolutePath();
                }
            }
        });
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.width = 1920;
                config.height = 1080;
                config.title = "AirPlace Simulator";
                jFrame.dispose();
                WiFiMagnetic wiFiMagnetic = new WiFiMagnetic();
                new LwjglApplication(new AirPlace(wiFiMagnetic), config);
            }
        });
    }

    public static void main(String[] args) {
        jFrame = new JFrame("DesktopLauncher");

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setContentPane(new DesktopLauncher().jPanel);
        jFrame.setSize(280, 180);

        jFrame.setVisible(true);
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        jPanel = new JPanel();
        jPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 2, new Insets(5, 5, 5, 5), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Radio Map Builder");
        jPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logFilesFolderButton = new JButton();
        logFilesFolderButton.setText("LogFiles Folder");
        jPanel.add(logFilesFolderButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buildButton = new JButton();
        buildButton.setEnabled(false);
        buildButton.setText("Build");
        jPanel.add(buildButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Airplace Simulator");
        jPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioMapsFolderButton = new JButton();
        radioMapsFolderButton.setText("RadioMaps Folder");
        jPanel.add(radioMapsFolderButton, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logFileButton = new JButton();
        logFileButton.setText("Log File");
        jPanel.add(logFileButton, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        outputDirectoryButton = new JButton();
        outputDirectoryButton.setText("Output Directory");
        jPanel.add(outputDirectoryButton, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        runButton = new JButton();
        runButton.setEnabled(false);
        runButton.setText("Run");
        jPanel.add(runButton, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jPanel;
    }
}