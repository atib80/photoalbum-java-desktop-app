package net.fotoalbum;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

class MainPanel extends JPanel {

    MainPanel() {
        UserData loggedOnUserData = DBManager.getLoggedInUserData();

        JLabel fullNameLabel, fullName;
        JLabel userNameLabel, userName;
        JLabel eMailLabel, eMail;
        JLabel passwordLabel, password;
        JLabel addressLabel, address;
        JLabel ageLabel, age;
        JLabel userLevelLabel, userLevel;

        setLayout(new GridLayout(7, 2, 5, 5));

        fullNameLabel = new JLabel("Full name:");
        Font labelFont = new Font("Arial", Font.BOLD + Font.ITALIC, 16);
        fullNameLabel.setFont(labelFont);
        fullNameLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        fullNameLabel.setBackground(Color.YELLOW);
        fullNameLabel.setOpaque(true);
        add(fullNameLabel);

        fullName = new JLabel(loggedOnUserData.getFullName());
        fullName.setFont(labelFont);
        fullName.setBorder(new BevelBorder(BevelBorder.LOWERED));
        fullName.setBackground(Color.WHITE);
        fullName.setForeground(Color.RED);
        fullName.setOpaque(true);
        add(fullName);

        userNameLabel = new JLabel("Username:");
        userNameLabel.setFont(labelFont);
        userNameLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        userNameLabel.setBackground(Color.YELLOW);
        userNameLabel.setOpaque(true);
        add(userNameLabel);

        userName = new JLabel(loggedOnUserData.getUserName());
        userName.setFont(labelFont);
        userName.setBorder(new BevelBorder(BevelBorder.LOWERED));
        userName.setBackground(Color.WHITE);
        userName.setForeground(Color.RED);
        userName.setOpaque(true);
        add(userName);

        passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        passwordLabel.setBackground(Color.YELLOW);
        passwordLabel.setOpaque(true);
        add(passwordLabel);

        password = new JLabel("hidden");
        password.setFont(labelFont);
        password.setBorder(new BevelBorder(BevelBorder.LOWERED));
        password.setBackground(Color.WHITE);
        password.setForeground(Color.RED);
        password.setOpaque(true);
        add(password);

        eMailLabel = new JLabel("e-mail:");
        eMailLabel.setFont(labelFont);
        eMailLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        eMailLabel.setBackground(Color.YELLOW);
        eMailLabel.setOpaque(true);
        add(eMailLabel);

        eMail = new JLabel(loggedOnUserData.geteMail());
        eMail.setFont(labelFont);
        eMail.setBorder(new BevelBorder(BevelBorder.LOWERED));
        eMail.setBackground(Color.WHITE);
        eMail.setForeground(Color.RED);
        eMail.setOpaque(true);
        add(eMail);

        addressLabel = new JLabel("Address:");
        addressLabel.setFont(labelFont);
        addressLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        addressLabel.setBackground(Color.YELLOW);
        addressLabel.setOpaque(true);
        add(addressLabel);

        address = new JLabel(loggedOnUserData.getAddress());
        address.setFont(labelFont);
        address.setBorder(new BevelBorder(BevelBorder.LOWERED));
        address.setBackground(Color.WHITE);
        address.setForeground(Color.RED);
        address.setOpaque(true);
        add(address);

        ageLabel = new JLabel("Age:");
        ageLabel.setFont(labelFont);
        ageLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        ageLabel.setBackground(Color.YELLOW);
        ageLabel.setOpaque(true);
        add(ageLabel);

        age = new JLabel(String.valueOf(loggedOnUserData.getAge()));
        age.setFont(labelFont);
        age.setBorder(new BevelBorder(BevelBorder.LOWERED));
        age.setBackground(Color.WHITE);
        age.setForeground(Color.RED);
        age.setOpaque(true);
        add(age);

        userLevelLabel = new JLabel("User privileges:");
        userLevelLabel.setFont(labelFont);
        userLevelLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        userLevelLabel.setBackground(Color.YELLOW);
        userLevelLabel.setOpaque(true);
        add(userLevelLabel);

        userLevel = new JLabel(DBManager.getAppropriateUserLevelInformation(loggedOnUserData.getUserLevel()));
        userLevel.setFont(labelFont);
        userLevel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        userLevel.setBackground(Color.WHITE);
        userLevel.setForeground(Color.RED);
        userLevel.setOpaque(true);
        add(userLevel);
    }
}
