package net.fotoalbum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class MainWindow extends JFrame {

    static DisplayDataPanel displayDataPanel;
    static DataStatisticsPanel dataStatisticsPanel;
    static AdminPanel adminPanel;
    private final Login loginWindow;

    MainWindow(Login login) {

        this("Fotoalbum", login);

    }

    private MainWindow(final String title, Login login) {

        this(title, 1024, 768, login);

    }

    private MainWindow(final String title, final int width, final int height, Login login) {
        setMinimumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setTitle(title);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        loginWindow = login;
        MainPanel mainPanel = new MainPanel();
        mainPanel.setBackground(new java.awt.Color(153, 255, 204));
        ImageUploadPanel imageUploadPanel = new ImageUploadPanel(new Color(153, 255, 204));
        imageUploadPanel.setBackground(new java.awt.Color(153, 255, 204));
        displayDataPanel = new DisplayDataPanel(new Color(153, 255, 204), "All", "All");
        dataStatisticsPanel = new DataStatisticsPanel(new java.awt.Color(153, 255, 204), "All", "All");
        adminPanel = new AdminPanel(new Color(153, 255, 204));
        adminPanel.setBackground(new java.awt.Color(153, 255, 204));
        JButton quitButton = new JButton("Logout");
        quitButton.addActionListener(ActionEvent -> {

            if (DisplayDataPanel.zoomedImageFrame != null) {
                DisplayDataPanel.zoomedImageFrame.setVisible(false);
                DisplayDataPanel.zoomedImageFrame.dispose();
                DisplayDataPanel.zoomedImageFrame = null;
            }

            setVisible(false);
            loginWindow.setVisible(true);
            dispose();
        });
        quitButton.setBounds(width - 100, height - 80, 80, 25);
        JTabbedPane mainPane = new JTabbedPane();
        mainPane.setBounds(0, 0, width, height - 90);
        mainPane.setBackground(new java.awt.Color(153, 255, 204));
        mainPane.setForeground(new java.awt.Color(51, 102, 255));
        mainPane.setFont(new java.awt.Font("Arial", Font.BOLD, 14)); // NOI18N
        mainPane.setRequestFocusEnabled(false);

        mainPane.addTab("Main", mainPanel);
        mainPane.addTab("Data entry", imageUploadPanel);
        mainPane.addTab("Data view", displayDataPanel);
        mainPane.addTab("Data statistics", dataStatisticsPanel);
        mainPane.addTab("For admins only", adminPanel);
        if (DBManager.getLoggedInUserData().getUserLevel() == 0) mainPane.getTabComponentAt(4).setEnabled(false);
        add(mainPane);
        add(quitButton);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (ImageUploadPanel.zoomedImageFrame != null) {
                    ImageUploadPanel.zoomedImageFrame.setVisible(false);
                    ImageUploadPanel.zoomedImageFrame.dispose();
                    ImageUploadPanel.zoomedImageFrame = null;
                }

                if (DisplayDataPanel.zoomedImageFrame != null) {
                    DisplayDataPanel.zoomedImageFrame.setVisible(false);
                    DisplayDataPanel.zoomedImageFrame.dispose();
                    DisplayDataPanel.zoomedImageFrame = null;
                }


                loginWindow.setVisible(true);
            }
        });

    }

}
