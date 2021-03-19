package net.fotoalbum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class LaunchWindow extends JFrame implements KeyListener {

    private final LaunchWindow parent;

    LaunchWindow() {

        parent = this;

        if (!initDBConnection()) {
            JOptionPane.showMessageDialog(this, "Could not connect to specified Oracle database!", "Connection error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        setSize(new Dimension(200, 200));
        setMinimumSize(new Dimension(200, 200));
        setResizable(false);
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 10, 10));
        final JButton loginButton = new JButton("Login");
        loginButton.setBackground(Color.CYAN);
        loginButton.setOpaque(true);
        panel.add(loginButton);
        loginButton.addActionListener(event -> loginActionPerformed());
        loginButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                    loginActionPerformed();
                else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    DBManager.close();
                    System.exit(0);
                }

            }

            @Override
            public void keyReleased(KeyEvent arg0) {

            }

            @Override
            public void keyTyped(KeyEvent arg0) {

            }
        });

        addKeyListener(this);
        JButton registerButton = new JButton("Register");
        registerButton.setBackground(Color.GREEN);
        registerButton.setOpaque(true);
        panel.add(registerButton);
        registerButton.addActionListener((ActionEvent e) -> {
            Registration registerWindow = new Registration("Registration", parent);
            registerWindow.setVisible(true);
            registerWindow.setSize(200, 200);
            setVisible(false);
        });

        JButton quitButton = new JButton("Quit");
        quitButton.setBackground(Color.RED);
        quitButton.setOpaque(true);
        panel.add(quitButton);
        quitButton.addActionListener((ActionEvent e) -> {
            DBManager.close();
            System.exit(0);
        });

        add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setAutoRequestFocus(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                DBManager.close();
            }
        });

        setVisible(true);
    }

    private boolean initDBConnection() {
        return DBManager.connectToOracleDB();
    }

    private void loginActionPerformed() {
        final Login loginWindow = new Login("Login", this);
        loginWindow.setVisible(true);
        setVisible(false);
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

        if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
            loginActionPerformed();
        else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
            DBManager.close();
            System.exit(0);
        }

    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}

