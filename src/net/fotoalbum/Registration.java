package net.fotoalbum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.InputMismatchException;


class Registration extends JFrame implements ActionListener {

    private final LaunchWindow parentwindow;
    private final JTextField tf1;
    private final JTextField tf2;
    private final JTextField tf5;
    private final JTextField tf6;
    private final JTextField tEmailAddress;
    private final JButton btn1;
    private final JButton btn2;
    private final JPasswordField p1;
    private final JPasswordField p2;

    Registration(String title, LaunchWindow parent) {
        parentwindow = parent;
        setPreferredSize(new Dimension(700, 500));
        setMinimumSize(new Dimension(700, 500));
        setResizable(true);
        setLayout(null);
        setTitle(title);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                parentwindow.setVisible(true);
                parentwindow.requestFocus();
            }
        });

        JLabel l1 = new JLabel("Registration form");
        l1.setForeground(Color.blue);
        l1.setFont(new Font("Serif", Font.BOLD, 20));
        JLabel l2 = new JLabel("Full Name:");
        Font font = new Font("Arial", Font.BOLD, 16);
        l2.setFont(font);
        JLabel l3 = new JLabel("Username:");
        l3.setFont(font);
        JLabel l4 = new JLabel("Password:");
        l4.setFont(font);
        JLabel l5 = new JLabel("Confirm password:");
        l5.setFont(font);
        JLabel lEmailAddress = new JLabel("E-mail address:");
        lEmailAddress.setFont(font);
        JLabel l6 = new JLabel("Address:");
        l6.setFont(font);
        JLabel l7 = new JLabel("Age:");
        l7.setFont(font);
        tf1 = new JTextField();
        tf1.setFont(font);
        KeyEventHandler keyEventHandler = new KeyEventHandler(this);
        tf1.addKeyListener(keyEventHandler);
        tf2 = new JTextField();
        tf2.setFont(font);
        tf2.addKeyListener(keyEventHandler);
        p1 = new JPasswordField();
        p1.addKeyListener(keyEventHandler);
        p1.setFocusable(true);
        p2 = new JPasswordField();
        p2.addKeyListener(keyEventHandler);
        p2.setFocusable(true);
        tEmailAddress = new JTextField();
        tEmailAddress.setFont(font);
        tEmailAddress.addKeyListener(keyEventHandler);
        tf5 = new JTextField();
        tf5.setFont(font);
        tf5.addKeyListener(keyEventHandler);
        tf6 = new JTextField();
        tf6.setFont(font);
        tf6.addKeyListener(keyEventHandler);
        btn1 = new JButton("Submit");
        btn1.setFont(font);
        btn1.addKeyListener(keyEventHandler);
        btn2 = new JButton("Clear");
        btn2.setFont(font);
        btn2.addKeyListener(keyEventHandler);
        btn1.addActionListener(this);
        btn2.addActionListener(this);

        l1.setBounds(100, 30, 400, 30);

        l2.setBounds(80, 70, 200, 30);

        l3.setBounds(80, 110, 200, 30);

        l4.setBounds(80, 150, 200, 30);

        l5.setBounds(80, 190, 200, 30);

        lEmailAddress.setBounds(80, 230, 200, 30);

        l6.setBounds(80, 270, 200, 30);

        l7.setBounds(80, 310, 200, 30);

        tf1.setBounds(300, 70, 200, 30);

        tf2.setBounds(300, 110, 200, 30);

        p1.setBounds(300, 150, 200, 30);

        p2.setBounds(300, 190, 200, 30);

        tEmailAddress.setBounds(300, 230, 200, 30);

        tf5.setBounds(300, 270, 200, 30);

        tf6.setBounds(300, 310, 200, 30);

        btn1.setBounds(50, 350, 100, 30);

        btn2.setBounds(170, 350, 100, 30);

        add(l1);

        add(l2);

        add(tf1);

        add(l3);

        add(tf2);

        add(l4);

        add(p1);

        add(l5);

        add(p2);

        add(lEmailAddress);

        add(tEmailAddress);

        add(l6);

        add(tf5);

        add(l7);

        add(tf6);

        add(btn1);

        add(btn2);

        setLocationRelativeTo(null);
        setVisible(true);

    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == btn1) {

            String fullName = tf1.getText().trim();

            if (fullName.length() < 6) {
                tf1.setSelectionStart(0);
                tf1.setSelectionEnd(fullName.length());
                tf1.setCaretPosition(0);
                JOptionPane.showMessageDialog(this, "Full name must be at least 6 characters long!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String userName = tf2.getText().trim();

            char[] pw1 = p1.getPassword();

            char[] pw2 = p2.getPassword();

            String userPassword = new String(pw1);

            final int userPasswordHashCode = !userPassword.isEmpty() ? userPassword.hashCode() : 0;

            String confirmedUserPassword = new String(pw2);

            final int confirmedUserPasswordHashCode = !confirmedUserPassword.isEmpty() ? confirmedUserPassword.hashCode() : 0;

            String eMailAddress = tEmailAddress.getText();

            String userAddress = tf5.getText().trim();

            if (userAddress.length() < 3) {
                tf5.setSelectionStart(0);
                tf5.setSelectionEnd(fullName.length());
                tf5.setCaretPosition(0);
                JOptionPane.showMessageDialog(this, "Address must be at least 3 characters long!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String age = tf6.getText();

            int ageValue;

            try {

                ageValue = Integer.parseInt(age);

            } catch (InputMismatchException ex) {
                System.out.println(ex.getMessage());
                return;
            }

            if (ageValue < 10 || ageValue > 120) {
                JOptionPane.showMessageDialog(this, String.format("Specified age %s is not a valid number (10 - 120)!", age), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (userName.isEmpty() || DBManager.getRegisteredUsersLookup().containsKey(userName)) {
                tf2.setSelectionStart(0);
                tf2.setSelectionEnd(userName.length());
                tf2.setCaretPosition(0);
                JOptionPane.showMessageDialog(this, "Specified username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            final ArrayList<UserData> userData = DBManager.getRegisteredUserData();

            for (UserData user : userData) {
                if (eMailAddress.equalsIgnoreCase(user.geteMail())) {
                    tEmailAddress.setSelectionStart(0);
                    tEmailAddress.setSelectionEnd(userName.length());
                    tEmailAddress.setCaretPosition(0);
                    JOptionPane.showMessageDialog(this, "Specified e-mail address already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (userPasswordHashCode != 0 && confirmedUserPasswordHashCode != 0 && userPasswordHashCode == confirmedUserPasswordHashCode) {

                final UserData user = new UserData(fullName, userName, eMailAddress, userPasswordHashCode, userAddress, ageValue, Timestamp.valueOf(LocalDateTime.now()),
                        0, true);
                DBManager.getRegisteredUserData().add(user);
                DBManager.getRegisteredUsersLookup().put(userName, user);
                DBManager.insertUserRegistrationData(user);
//                public UserStatisticsData(final String userName, final Timestamp lastLoginTime, final Timestamp lastVoteTime, final int lastVoteScore,
//                final long numberOf1Votes, final long numberOf2Votes, final long numberOf3Votes, final long numberOf4Votes,
//                final long numberOf5Votes, final long numberOfLogins, final long numberOfImageUploads, final long numberOfImageDeletions,
//                final long numberOfUserDeletions, final long numberOfAdminPromotions)
                UserStatisticsData userStatsData = new UserStatisticsData(userName, new Timestamp(0), new Timestamp(0), 0,
                        0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0);
                DBManager.getSavedUserDataStats().add(userStatsData);
                DBManager.getSavedUserDataStatsLookup().put(userName, userStatsData);
                DBManager.insertNewUserStatsDataIntoDatabase(userStatsData);

                JOptionPane.showMessageDialog(this, "Data saved successfully!");
                setVisible(false);
                dispose();
                parentwindow.setVisible(true);
                parentwindow.requestFocus();

            } else {

                JOptionPane.showMessageDialog(this, "Password and confirm password entries do not match!");
                p2.requestFocus();
                p2.grabFocus();
                p2.setSelectionColor(Color.BLUE);
                p2.setSelectedTextColor(Color.RED);
                p2.setSelectionStart(0);
                p2.setSelectionEnd(confirmedUserPassword.length() - 1);
                p2.selectAll();

            }

        } else if (btn2 == e.getSource()) {

            tf1.setText("");

            tf2.setText("");

            p1.setText("");

            p2.setText("");

            tEmailAddress.setText("");

            tf5.setText("");

            tf6.setText("");

        }

    }

    class KeyEventHandler implements KeyListener {

        private final Registration regFrame;

        KeyEventHandler(final Registration regFrame) {
            this.regFrame = regFrame;
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                JOptionPane.showConfirmDialog(keyEvent.getComponent().getParent(), "Do you really want to enter data?", "Confirmation", JOptionPane.YES_NO_OPTION);
                // processUserData();
            else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                this.regFrame.setVisible(false);
                parentwindow.setVisible(true);
                parentwindow.requestFocus();
                this.regFrame.dispose();
                // System.exit(0);
            }

        }

        @Override
        public void keyReleased(KeyEvent arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void keyTyped(KeyEvent arg0) {
            // TODO Auto-generated method stub

        }

    }

}


