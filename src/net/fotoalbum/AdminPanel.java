package net.fotoalbum;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

class AdminPanel extends JPanel {

    private static final DateTimeFormatter dtFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final JList<String> userList;
    private final DefaultListModel<String> userListModel;
    private final int adminLevel;
    private final String adminUserName;
    private final UserStatisticsData adminStatsData;
    private JList<String> pictureList;
    private JScrollPane pictureScrollPane;
    private DefaultListModel<String> pictureListModel;
    private ArrayList<Timestamp> savedImagesTimeStamps;
    private ArrayList<String> savedImageNames;
    private final ArrayList<String> registeredUserNames;
    private final JButton promoteAdminButton;
    private JButton demoteAdminButton;
    private JButton deleteUserButton;
    private final JButton deletePictureButton;
    private JTextArea itemInformationTextArea;
    private JLabel pictureLabel;
    private int selectedUserIndex = 0;
    private String selectedUserName;
    private int selectedImageIndex = 0;
    private String selectedImageName;

    AdminPanel(final Color c) {
        setBackground(c);
        setSize(1024, 740);
        setLayout(null);
        adminUserName = DBManager.getLoggedInUserData().getUserName();
        adminLevel = DBManager.getLoggedInUserData().getUserLevel();
        adminStatsData = DBManager.getSavedUserDataStatsLookup().get(adminUserName);
        registeredUserNames = new ArrayList<>();

        for (final UserData u : DBManager.getRegisteredUserData()) {
            registeredUserNames.add(u.getUserName());
        }

        Collections.sort(registeredUserNames);
        String[] registeredUserNamesForJList = new String[registeredUserNames.size()];
        registeredUserNames.toArray(registeredUserNamesForJList);


        promoteAdminButton = new JButton("Promote user");
        if (adminLevel != 2) promoteAdminButton.setEnabled(false);
        promoteAdminButton.addActionListener(actionEvent -> {
            if (adminLevel != 2) {
                JOptionPane.showMessageDialog(AdminPanel.this, String.format("You must have master admin privileges in order to be able to change the user level" +
                                " of an existing user account!\nYour currently registered admin account (%s) has regular admin privileges (USER_LEVEL_1) only!", DBManager.getLoggedInUserData().getUserName()),
                        "Information", JOptionPane.WARNING_MESSAGE);
            } else if (DBManager.getRegisteredUsersLookup().get(selectedUserName).getUserLevel() != 0) {
                JOptionPane.showMessageDialog(AdminPanel.this, String.format("Selected user (%s) is already assigned regular admin privileges (USER_LEVEL_1)!", selectedUserName), "Information", JOptionPane.WARNING_MESSAGE);
            } else {
                final UserData userData = DBManager.getRegisteredUsersLookup().get(selectedUserName);
                final UserStatisticsData userStatsData = DBManager.getSavedUserDataStatsLookup().get(selectedUserName);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                userData.setUserLevel(1);
                DBManager.updateUserRegistrationData(userData);
                final String actionMessage = String.format("Master admin %s promoted user %s (USER_LEVEL_1) at %s",
                        adminUserName, selectedUserName, now.toLocalDateTime().format(dtFormat));
                final UserActivity userActivity = new UserActivity(DBManager.getSavedUserDataStatsLookup().get(adminUserName).getLastLoginTime(),
                        adminUserName, actionMessage, now);
                DBManager.dbInsertNewUserActivityData(userActivity);
                promoteAdminButton.setEnabled(false);
                demoteAdminButton.setEnabled(true);
                final String userInformation = String.format("Information about selected user:\nFull name: %s\ne-mail: %s\nAddress: %s\nAge: %d\nRegistration date: %s\n" +
                                "User level: %s\nLast login: %s\nNumber of logins: %d\nLast vote time: %s\nLast vote score: %d\nNumber of 1 votes: %d\nNumber of 2 votes: %d\nNumber of 3 votes: %d\n" +
                                "Number of 4 votes: %d\nNumber of 5 votes: %d\nNumber of uploaded images: %d\nNumber of deleted images: %d\nNumber of deleted users: %d\nNumber of promoted admins: %d\n" +
                                "Number of demoted admins: %d", userData.getFullName(), userData.geteMail(), userData.getAddress(), userData.getAge(), userData.getRegistrationTimeStamp().toLocalDateTime().format(dtFormat),
                        DBManager.getAppropriateUserLevelInformation(userData.getUserLevel()), userStatsData.getLastLoginTime().toLocalDateTime().format(dtFormat), userStatsData.getNumberOfLogins(),
                        userStatsData.getLastVoteTime().toLocalDateTime().format(dtFormat), userStatsData.getLastVoteScore(), userStatsData.getNumberOfVotes(1),
                        userStatsData.getNumberOfVotes(2), userStatsData.getNumberOfVotes(3), userStatsData.getNumberOfVotes(4), userStatsData.getNumberOfVotes(5),
                        userStatsData.getNumberOfImageUploads(), userStatsData.getNumberOfImageDeletions(), userStatsData.getNumberOfUserDeletions(),
                        userStatsData.getNumberOfAdminPromotions(), userStatsData.getNumberOfAdminDemotions());
                itemInformationTextArea.setText(userInformation);
                JOptionPane.showMessageDialog(this, String.format("Fotoalbum user %s has been assigned regular admin privileges by %s %s", selectedUserName, DBManager.getAppropriateUserLevelInformation(adminLevel), adminUserName),
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        promoteAdminButton.setBounds(350, 140, 120, 30);

        demoteAdminButton = new JButton("Demote admin");
        if (adminLevel != 2) demoteAdminButton.setEnabled(false);
        demoteAdminButton.addActionListener(actionEvent -> {
            if (adminLevel != 2) {
                JOptionPane.showMessageDialog(this, String.format("You must have master admin privileges in order to be able to change user level" +
                                " of an existing admin account!\nYour currently registered admin account (%s) has regular admin privileges (USER_LEVEL_1) only!", DBManager.getLoggedInUserData().getUserName()),
                        "Information", JOptionPane.WARNING_MESSAGE);
            } else if (DBManager.getRegisteredUsersLookup().get(selectedUserName).getUserLevel() == 0) {
                JOptionPane.showMessageDialog(this, String.format("Selected user name (%s) is assigned a regular user account (USER_LEVEL_0)!\nYou cannot make further changes to it.", selectedUserName),
                        "Warning", JOptionPane.WARNING_MESSAGE);
            } else if (DBManager.getRegisteredUsersLookup().get(selectedUserName).getUserLevel() == 2) {
                JOptionPane.showMessageDialog(this, String.format("You are not allowed to change user level of an existing master admin account!\n" +
                        "Registered user account (%s) has master admin privileges assigned to it (USER_LEVEL_2)!", selectedUserName), "Information", JOptionPane.WARNING_MESSAGE);
            } else {
                final UserData userData = DBManager.getRegisteredUsersLookup().get(selectedUserName);
                final UserStatisticsData userStatsData = DBManager.getSavedUserDataStatsLookup().get(selectedUserName);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                userData.setUserLevel(0);
                DBManager.updateUserRegistrationData(userData);
                final String actionMessage = String.format("Master admin %s demoted existing admin account %s (USER_LEVEL_1) at %s",
                        adminUserName, selectedUserName, now.toLocalDateTime().format(dtFormat));
                final UserActivity userActivity = new UserActivity(adminStatsData.getLastLoginTime(), adminUserName, actionMessage, now);
                DBManager.dbInsertNewUserActivityData(userActivity);
                promoteAdminButton.setEnabled(true);
                demoteAdminButton.setEnabled(false);
                final String userInformation = String.format("Information about selected user:\nFull name: %s\ne-mail: %s\nAddress: %s\nAge: %d\nRegistration date: %s\n" +
                                "User level: %s\nLast login: %s\nNumber of logins: %d\nLast vote time: %s\nLast vote score: %d\nNumber of 1 votes: %d\nNumber of 2 votes: %d\nNumber of 3 votes: %d\n" +
                                "Number of 4 votes: %d\nNumber of 5 votes: %d\nNumber of uploaded images: %d\nNumber of deleted images: %d\nNumber of deleted users: %d\nNumber of promoted admins: %d\n" +
                                "Number of demoted admins: %d", userData.getFullName(), userData.geteMail(), userData.getAddress(), userData.getAge(), userData.getRegistrationTimeStamp().toLocalDateTime().format(dtFormat),
                        DBManager.getAppropriateUserLevelInformation(userData.getUserLevel()), userStatsData.getLastLoginTime().toLocalDateTime().format(dtFormat), userStatsData.getNumberOfLogins(),
                        userStatsData.getLastVoteTime().toLocalDateTime().format(dtFormat), userStatsData.getLastVoteScore(), userStatsData.getNumberOfVotes(1),
                        userStatsData.getNumberOfVotes(2), userStatsData.getNumberOfVotes(3), userStatsData.getNumberOfVotes(4), userStatsData.getNumberOfVotes(5),
                        userStatsData.getNumberOfImageUploads(), userStatsData.getNumberOfImageDeletions(), userStatsData.getNumberOfUserDeletions(),
                        userStatsData.getNumberOfAdminPromotions(), userStatsData.getNumberOfAdminDemotions());
                itemInformationTextArea.setText(userInformation);
                JOptionPane.showMessageDialog(this, String.format("%s's regular admin privileges have been removed by master admin %s", selectedUserName, adminUserName),
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        demoteAdminButton.setBounds(350, 180, 120, 30);

        userList = new JList<>(registeredUserNamesForJList);
        userListModel = new DefaultListModel<>();
        for (final String userName : registeredUserNames) userListModel.addElement(userName);
        userList.setModel(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(listSelectionEvent -> {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                selectedUserIndex = userList.getSelectedIndex();
                if (-1 != selectedUserIndex) {
                    selectedUserName = userList.getSelectedValue();
                    final UserData userData = DBManager.getRegisteredUsersLookup().get(selectedUserName);
                    final UserStatisticsData userStatsData = DBManager.getSavedUserDataStatsLookup().get(selectedUserName);
                    promoteAdminButton.setEnabled(userData.getUserLevel() == 0);
                    demoteAdminButton.setEnabled(userData.getUserLevel() == 1);
                    if (userData.getUserLevel() != 2) deleteUserButton.setEnabled(true);
                    final String userInformation = String.format("Information about selected user:\nFull name: %s\ne-mail: %s\nAddress: %s\nAge: %d\nRegistration date: %s\n" +
                                    "User level: %s\nLast login: %s\nNumber of logins: %d\nLast vote time: %s\nLast vote score: %d\nNumber of 1 votes: %d\nNumber of 2 votes: %d\nNumber of 3 votes: %d\n" +
                                    "Number of 4 votes: %d\nNumber of 5 votes: %d\nNumber of uploaded images: %d\nNumber of deleted images: %d\nNumber of deleted users: %d\nNumber of promoted admins: %d\n" +
                                    "Number of demoted admins: %d", userData.getFullName(), userData.geteMail(), userData.getAddress(), userData.getAge(), userData.getRegistrationTimeStamp().toLocalDateTime().format(dtFormat),
                            DBManager.getAppropriateUserLevelInformation(userData.getUserLevel()), userStatsData.getLastLoginTime().toLocalDateTime().format(dtFormat), userStatsData.getNumberOfLogins(),
                            userStatsData.getLastVoteTime().toLocalDateTime().format(dtFormat), userStatsData.getLastVoteScore(), userStatsData.getNumberOfVotes(1),
                            userStatsData.getNumberOfVotes(2), userStatsData.getNumberOfVotes(3), userStatsData.getNumberOfVotes(4), userStatsData.getNumberOfVotes(5),
                            userStatsData.getNumberOfImageUploads(), userStatsData.getNumberOfImageDeletions(), userStatsData.getNumberOfUserDeletions(),
                            userStatsData.getNumberOfAdminPromotions(), userStatsData.getNumberOfAdminDemotions());
                    itemInformationTextArea.setText(userInformation);
                }
            }

        });

        JScrollPane userScrollPane = new JScrollPane(userList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        userScrollPane.setBounds(30, 30, 300, 300);

        deleteUserButton = new JButton("Delete user");
        if (adminLevel != 2) deleteUserButton.setEnabled(false);
        deleteUserButton.addActionListener(actionEvent -> {
            if (DBManager.getRegisteredUsersLookup().get(selectedUserName).getUserLevel() == 2) {
                JOptionPane.showMessageDialog(this, String.format("Master admin account (%s) cannot be deleted!", selectedUserName), "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                UserData userData = DBManager.getRegisteredUsersLookup().get(selectedUserName);
                UserStatisticsData userStatsData = DBManager.getSavedUserDataStatsLookup().get(selectedUserName);
                final String actionMessage = String.format("Master admin %s deleted user account %s (USER_LEVEL_%d) at %s", adminUserName, selectedUserName,
                        userData.getUserLevel(), now.toLocalDateTime().format(dtFormat));
                final UserActivity userActivity = new UserActivity(adminStatsData.getLastLoginTime(), adminUserName, actionMessage, now);
                DBManager.dbInsertNewUserActivityData(userActivity);
                DBManager.dbDeleteUserData(userData);
                DBManager.dbDeleteUserStatisticsData(userStatsData);
                DBManager.removeRegisteredUserData(userData);
                DBManager.removeRegisteredUserStatsData(userStatsData);
                final String deletedUserInformationMessage = String.format("Fotoalbum user account belonging to user %s has been deleted master admin %s", selectedUserName, adminUserName);
                registeredUserNames.remove(selectedUserIndex);
                userListModel.removeElementAt(selectedUserIndex);
                if (!registeredUserNames.isEmpty()) {
                    userList.setSelectedIndex(0);
                    selectedUserName = registeredUserNames.get(0);
                } else {
                    userListModel.addElement("There are no registered users available at the moment!");
                    deleteUserButton.setEnabled(false);
                }

                userData = DBManager.getRegisteredUsersLookup().get(selectedUserName);
                userStatsData = DBManager.getSavedUserDataStatsLookup().get(selectedUserName);
                final String userInformation = String.format("Information about selected user:\nFull name: %s\ne-mail: %s\nAddress: %s\nAge: %d\nRegistration date: %s\n" +
                                "User level: %s\nLast login: %s\nNumber of logins: %d\nLast vote time: %s\nLast vote score: %d\nNumber of 1 votes: %d\nNumber of 2 votes: %d\nNumber of 3 votes: %d\n" +
                                "Number of 4 votes: %d\nNumber of 5 votes: %d\nNumber of uploaded images: %d\nNumber of deleted images: %d\nNumber of deleted users: %d\nNumber of promoted admins: %d\n" +
                                "Number of demoted admins: %d", userData.getFullName(), userData.geteMail(), userData.getAddress(), userData.getAge(), userData.getRegistrationTimeStamp().toLocalDateTime().format(dtFormat),
                        DBManager.getAppropriateUserLevelInformation(userData.getUserLevel()), userStatsData.getLastLoginTime().toLocalDateTime().format(dtFormat), userStatsData.getNumberOfLogins(),
                        userStatsData.getLastVoteTime().toLocalDateTime().format(dtFormat), userStatsData.getLastVoteScore(), userStatsData.getNumberOfVotes(1),
                        userStatsData.getNumberOfVotes(2), userStatsData.getNumberOfVotes(3), userStatsData.getNumberOfVotes(4), userStatsData.getNumberOfVotes(5),
                        userStatsData.getNumberOfImageUploads(), userStatsData.getNumberOfImageDeletions(), userStatsData.getNumberOfUserDeletions(),
                        userStatsData.getNumberOfAdminPromotions(), userStatsData.getNumberOfAdminDemotions());
                itemInformationTextArea.setText(userInformation);
                JOptionPane.showMessageDialog(this, deletedUserInformationMessage, "Information", JOptionPane.INFORMATION_MESSAGE);

            }

        });
        deleteUserButton.setBounds(350, 220, 120, 30);
        itemInformationTextArea = new JTextArea();
        itemInformationTextArea.setFont(new Font("Courier", Font.PLAIN, 12));
        itemInformationTextArea.setLineWrap(true);
        itemInformationTextArea.setBounds(500, 50, 500, 300);
        JScrollPane itemInformationScrollPane = new JScrollPane(itemInformationTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        itemInformationScrollPane.setBounds(500, 50, 500, 300);

        deletePictureButton = new JButton("Delete image");
        deletePictureButton.addActionListener(actionEvent -> {
            final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            final String actionMessage = String.format("%s %s deleted image %s (uploaded at %s) at %s", DBManager.getAppropriateUserLevelInformation(adminLevel),
                    adminUserName, selectedImageName, savedImagesTimeStamps.get(selectedImageIndex).toLocalDateTime().format(dtFormat), now.toLocalDateTime().format(dtFormat));
            final UserActivity userActivity = new UserActivity(adminStatsData.getLastLoginTime(),
                    adminUserName, actionMessage, now);
            adminStatsData.setNumberOfImageDeletions(adminStatsData.getNumberOfImageDeletions() + 1);
            DBManager.dbUpdateExistingUserStatsData(adminStatsData, "imgdelnumber");
            DBManager.dbDeleteExistingImageData(DBManager.getSavedImagesDataLookup().get(savedImagesTimeStamps.get(selectedImageIndex)));
            DBManager.dbDeleteExistingImageStatisticsData(DBManager.getSavedImagesStatsLookup().get(savedImagesTimeStamps.get(selectedImageIndex)));
            DBManager.dbRemoveExistingImageData(DBManager.getSavedImagesDataLookup().get(savedImagesTimeStamps.get(selectedImageIndex)));
            DBManager.dbRemoveExistingImageStatsData(DBManager.getSavedImagesStatsLookup().get(savedImagesTimeStamps.get(selectedImageIndex)));
            DBManager.dbInsertNewUserActivityData(userActivity);
            JOptionPane.showMessageDialog(this, actionMessage, "Information", JOptionPane.INFORMATION_MESSAGE);
            savedImagesTimeStamps.remove(selectedImageIndex);
            savedImageNames.remove(selectedImageIndex);
            pictureListModel.removeElementAt(selectedImageIndex);
            if (!savedImageNames.isEmpty()) pictureList.setSelectedIndex(0);
            else {
                ((DefaultListModel<String>) pictureList.getModel()).addElement("There are no uploaded images available at the moment!");
                deletePictureButton.setEnabled(false);
            }
            MainWindow.displayDataPanel.loadImages("All", "All");
            MainWindow.dataStatisticsPanel.sortImages(ImageSortOrder.UPLOADTIME_ASC);
            MainWindow.dataStatisticsPanel.loadSortedImages();

        });
        deletePictureButton.setBounds(350, 500, 150, 30);

        if (!registeredUserNames.isEmpty()) userList.setSelectedIndex(0);
        else {
            userListModel.addElement("There are no registered users available at the moment!");
            deleteUserButton.setEnabled(false);
        }
        selectedUserName = registeredUserNames.get(0);
        final UserData userData = DBManager.getRegisteredUsersLookup().get(selectedUserName);
        final UserStatisticsData userStatsData = DBManager.getSavedUserDataStatsLookup().get(selectedUserName);
        promoteAdminButton.setEnabled(userData.getUserLevel() == 0);
        demoteAdminButton.setEnabled(userData.getUserLevel() == 1);
        deleteUserButton.setEnabled(adminLevel == 2 && userData.getUserLevel() != 2);
        final String userInformation = String.format("Information about selected user:\nFull name: %s\ne-mail: %s\nAddress: %s\nAge: %d\nRegistration date: %s\n" +
                        "User level: %s\nLast login: %s\nNumber of logins: %d\nLast vote time: %s\nLast vote score: %d\nNumber of 1 votes: %d\nNumber of 2 votes: %d\nNumber of 3 votes: %d\n" +
                        "Number of 4 votes: %d\nNumber of 5 votes: %d\nNumber of uploaded images: %d\nNumber of deleted images: %d\nNumber of deleted users: %d\nNumber of promoted admins: %d\n" +
                        "Number of demoted admins: %d", userData.getFullName(), userData.geteMail(), userData.getAddress(), userData.getAge(), userData.getRegistrationTimeStamp().toLocalDateTime().format(dtFormat),
                DBManager.getAppropriateUserLevelInformation(userData.getUserLevel()), userStatsData.getLastLoginTime().toLocalDateTime().format(dtFormat), userStatsData.getNumberOfLogins(),
                userStatsData.getLastVoteTime().toLocalDateTime().format(dtFormat), userStatsData.getLastVoteScore(), userStatsData.getNumberOfVotes(1),
                userStatsData.getNumberOfVotes(2), userStatsData.getNumberOfVotes(3), userStatsData.getNumberOfVotes(4), userStatsData.getNumberOfVotes(5),
                userStatsData.getNumberOfImageUploads(), userStatsData.getNumberOfImageDeletions(), userStatsData.getNumberOfUserDeletions(),
                userStatsData.getNumberOfAdminPromotions(), userStatsData.getNumberOfAdminDemotions());
        itemInformationTextArea.setText(userInformation);

        reloadExistingImages();

        if (!savedImageNames.isEmpty()) pictureList.setSelectedIndex(0);
        else {
            pictureListModel.addElement("There are no uploaded images available at the moment!");
            deletePictureButton.setEnabled(false);
        }

        add(userScrollPane);
        add(itemInformationScrollPane);
        add(promoteAdminButton);
        add(demoteAdminButton);
        add(deleteUserButton);
        add(deletePictureButton);

    }

    void reloadExistingImages() {
        if (pictureScrollPane != null) remove(pictureScrollPane);
        if (pictureList != null) remove(pictureList);
        if (pictureLabel != null) remove(pictureLabel);
        savedImagesTimeStamps = new ArrayList<>();
        for (final ImageData imageData : DBManager.getSavedImagesData()) {
            savedImagesTimeStamps.add(imageData.getUploadTimeStamp());
        }

        Collections.sort(savedImagesTimeStamps);

        savedImageNames = new ArrayList<>();

        for (final Timestamp savedImagesTimeStamp : savedImagesTimeStamps)
            savedImageNames.add(DBManager.getSavedImagesDataLookup().get(savedImagesTimeStamp).getImageName());

        String[] savedImageNamesForJList = new String[savedImageNames.size()];
        savedImageNames.toArray(savedImageNamesForJList);
        pictureList = new JList<>(savedImageNamesForJList);
        pictureListModel = new DefaultListModel<>();
        for (final String imageName : savedImageNames) pictureListModel.addElement(imageName);
        pictureList.setModel(pictureListModel);
        pictureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pictureList.addListSelectionListener(listSelectionEvent -> {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                selectedImageIndex = pictureList.getSelectedIndex();
                if (-1 != selectedImageIndex) {
                    selectedImageName = pictureList.getSelectedValue();
                    final Image scaledMediumImage = DBManager.getSavedScaledImages().get(savedImagesTimeStamps.get(selectedImageIndex)).getScaledMediumImage();
                    pictureLabel.setIcon(new ImageIcon(scaledMediumImage));

                    final Timestamp imgKey = savedImagesTimeStamps.get(selectedImageIndex);

                    if (DBManager.getSavedImagesStatsLookup().containsKey(imgKey)) {
                        final ImageStatisticsData currentlySelectedImageStatsData = DBManager.getSavedImagesStatsLookup().get(imgKey);
                        final ImageData currentlySelectedImageData = DBManager.getSavedImagesDataLookup().get(imgKey);

                        final String itemInfo = String.format("Information about selected image:\nFile name: %s\nDimensions: %d x %d pixels\n" +
                                        "Category: %s\nLocation: %s\nImage score: %d\nAverage image score: %.2f from %d vote(s))\nView count: %d\nName of uploader: %s\nDate of upload: %s\n",
                                currentlySelectedImageData.getImageName(), currentlySelectedImageStatsData.getImageWidth(),
                                currentlySelectedImageStatsData.getImageHeight(), currentlySelectedImageStatsData.getImageCategory(), currentlySelectedImageStatsData.getImageLocation(),
                                currentlySelectedImageStatsData.getScore(), currentlySelectedImageStatsData.getAvgScore(), currentlySelectedImageStatsData.getNumberOfAllVotes(),
                                currentlySelectedImageStatsData.getSeenTimes(), currentlySelectedImageData.getUploaderUserName(), currentlySelectedImageData.getUploadTimeStamp().toLocalDateTime().format(dtFormat));

                        itemInformationTextArea.setText(itemInfo);
                    }
                }
            }

        });
        pictureScrollPane = new JScrollPane(pictureList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pictureScrollPane.setBounds(30, 340, 300, 300);
        add(pictureScrollPane);

        pictureLabel = new JLabel();
        pictureLabel.setBounds(580, 360, 300, 300);
        final Image scaledMediumImage = DBManager.getSavedScaledImages().get(savedImagesTimeStamps.get(0)).getScaledMediumImage();
        pictureLabel.setIcon(new ImageIcon(scaledMediumImage));
        pictureLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(pictureLabel);

        revalidate();
        repaint();
    }

}
