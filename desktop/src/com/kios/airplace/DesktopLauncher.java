package com.kios.airplace;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class DesktopLauncher {
    private static JFrame jFrame;
    private JPanel jPanel;
    private JButton logFilesFolderButton;
    private JButton buildButton;
    private JButton radioMapsFolderButton;
    private JButton routeFileButton;
    private JButton outputDirectoryButton;
    private JButton runButton;

    private String previousPath;
    private String logsFilesFolderPath;
    private boolean[] clicked = {false, false, false};

    private DesktopLauncher() {
        logFilesFolderButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser(previousPath);

            jFileChooser.setDialogTitle("LogFiles Folder");
            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                boolean checkFolderName = jFileChooser.getSelectedFile().getName().equals("logs");
                boolean checkRss = new File(jFileChooser.getSelectedFile() + "\\rss").exists();
                boolean checkMagnetic = new File(jFileChooser.getSelectedFile() + "\\magnetic").exists();

                if (checkFolderName && checkRss && checkMagnetic) {
                    logsFilesFolderPath = String.valueOf(jFileChooser.getSelectedFile().getParent());
                    previousPath = logsFilesFolderPath;
                    buildButton.setEnabled(true);
                }
            }
        });
        buildButton.addActionListener(e -> new CreateRadioMaps(logsFilesFolderPath));

        radioMapsFolderButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser(previousPath);

            jFileChooser.setDialogTitle("RadioMaps Folder");
            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                clicked[0] = true;
                routeFileButton.setEnabled(true);
                outputDirectoryButton.setEnabled(true);

                String radioMapsDirectory = jFileChooser.getSelectedFile().getAbsolutePath();

                previousPath = jFileChooser.getSelectedFile().getParent();
                Globals.RSS_NAV_FILE_PATH = radioMapsDirectory + "\\rss\\rssRadioMap-mean.txt";
                Globals.MAGNETIC_NAV_FILE_PATH = radioMapsDirectory + "\\magnetic\\magneticRadioMap-mean.txt";
            }
        });
        routeFileButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser(previousPath);

            jFileChooser.setDialogTitle("Route File");
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooser.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));

            if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                clicked[1] = true;

                if (clicked[0] && clicked[2])
                    runButton.setEnabled(true);

                String logsFileDirectory = jFileChooser.getCurrentDirectory().getAbsolutePath();
                String logFilesDate = jFileChooser.getSelectedFile().getName().split("-")[1];

                Globals.RSS_LOG_FILE_PATH = logsFileDirectory + "\\rssFile-" + logFilesDate;
                Globals.MAGNETIC_LOG_FILE_PATH = logsFileDirectory + "\\magneticFile-" + logFilesDate;
            }
        });
        outputDirectoryButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser(previousPath);

            jFileChooser.setDialogTitle("Output Directory");
            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                clicked[2] = true;

                if (clicked[0] && clicked[1])
                    runButton.setEnabled(true);

                Globals.WRITER_PATH = jFileChooser.getSelectedFile().getAbsolutePath();
            }
        });
        runButton.addActionListener(e -> {
            LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
            config.width = 1920;
            config.height = 1080;
            config.forceExit = false;
            config.title = "AirPlace Simulator";

            jFrame.dispose();
            WiFiMagnetic wiFiMagnetic = new WiFiMagnetic();
            new LwjglApplication(new AirPlace(wiFiMagnetic), config);
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
        routeFileButton = new JButton();
        routeFileButton.setEnabled(false);
        routeFileButton.setText("Route File");
        jPanel.add(routeFileButton, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        outputDirectoryButton = new JButton();
        outputDirectoryButton.setEnabled(false);
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