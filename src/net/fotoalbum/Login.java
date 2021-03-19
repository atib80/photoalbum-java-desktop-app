package net.fotoalbum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;


class Login extends JFrame {

    private final Login loginWindow;
    private final JPanel loginPanel;
    private final LaunchWindow parentWindow;
    private JTextField loginUserName;
    private JPasswordField passwordField;

    Login(final String title, final LaunchWindow parent) {
        loginWindow = this;
        parentWindow = parent;
        loginPanel = new JPanel();
        initUI(title);
    }

    private void initUI(String title) {

        setPreferredSize(new Dimension(350, 350));
        setMinimumSize(new Dimension(350, 350));
        loginPanel.setLayout(null);
        loginPanel.setSize(350, 350);
        loginPanel.setBounds(0, 0, 350, 350);
        loginPanel.setBackground(Color.CYAN);
        setTitle(title);
        setLocationRelativeTo(null);
        setResizable(false);

        Font loginFont = new Font("Arial", Font.BOLD, 14);

        JLabel loginLabel = new JLabel("Login:");
        loginLabel.setFont(loginFont);
        loginLabel.setBounds(25, 100, 80, 30);
        loginPanel.add(loginLabel);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(25, 140, 80, 30);
        passwordLabel.setFont(loginFont);
        loginPanel.add(passwordLabel);

        LoginKeyEventHandler loginKeyEventHandler = new LoginKeyEventHandler();

        loginUserName = new JTextField(30);
        loginUserName.setFont(loginFont);
        loginUserName.addKeyListener(loginKeyEventHandler);
        loginUserName.setBounds(120, 100, 200, 30);
        loginPanel.add(loginUserName);

        passwordField = new JPasswordField(30);
        passwordField.setFont(loginFont);
        passwordField.addKeyListener(loginKeyEventHandler);
        passwordField.setBounds(120, 140, 200, 30);
        loginPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(loginFont);
        loginButton.setBackground(Color.GREEN);
        loginButton.addActionListener(ae -> processLoginAction());
        loginButton.setBounds(100, 180, 100, 30);
        loginPanel.add(loginButton);

        loginButton.addKeyListener(loginKeyEventHandler);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(loginFont);
        cancelButton.setBackground(Color.RED);
        cancelButton.addActionListener((ActionEvent ae) -> {
            loginWindow.setVisible(false);
            parentWindow.setVisible(true);
            parentWindow.requestFocus();
            loginWindow.dispose();
        });

        cancelButton.setBounds(220, 180, 100, 30);
        loginPanel.add(cancelButton);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                loginWindow.setVisible(false);
                parentWindow.setVisible(true);
                parentWindow.requestFocus();
                loginWindow.dispose();
            }
        });

        add(loginPanel);
        setVisible(true);
    }

    private void processLoginAction() {

        final String userName = loginUserName.getText();
        final String pw = String.valueOf(passwordField.getPassword());
        final int pwCode = !pw.isEmpty() ? pw.hashCode() : 0;

        if (DBManager.getRegisteredUsersLookup().containsKey(userName)) {
            final int userPassword = DBManager.getRegisteredUsersLookup().get(userName).getPassword();

            if (userPassword != pwCode) {
                JOptionPane.showMessageDialog(this, "You've entered incorrect username/password!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            setVisible(false);
            dispose();

            DBManager.setLoggedInUserData(DBManager.getRegisteredUsersLookup().get(userName));
            final UserStatisticsData userStatsData = DBManager.getSavedUserDataStatsLookup().get(userName);
            userStatsData.setLastLoginTime(Timestamp.valueOf(LocalDateTime.now()));
            userStatsData.setNumberOfLogins(userStatsData.getNumberOfLogins() + 1);
            DBManager.dbUpdateExistingUserStatsData(userStatsData, "lastlogintime", "number_of_logins");
            MainWindow mainWindow = new MainWindow(this);
            setVisible(false);
            mainWindow.setVisible(true);
        } else
            JOptionPane.showMessageDialog(this, "You've entered incorrect username/password!", "Error", JOptionPane.ERROR_MESSAGE);

    }

    private class LoginKeyEventHandler implements KeyListener {

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                processLoginAction();
            else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                loginWindow.setVisible(false);
                parentWindow.setVisible(true);
                parentWindow.requestFocus();
                loginWindow.dispose();
            }

        }

        @Override
        public void keyReleased(KeyEvent arg0) {

        }

        @Override
        public void keyTyped(KeyEvent arg0) {

        }
    }
}


	
	
	

