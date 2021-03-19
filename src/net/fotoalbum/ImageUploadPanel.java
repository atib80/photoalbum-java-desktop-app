package net.fotoalbum;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

class ImageUploadPanel extends JPanel {

    static JFrame zoomedImageFrame;
    private final JTextField chosenImagePath;
    private final JComboBox<String> comboBoxCategory;
    private final JComboBox<String> comboBoxLocation;
    private JRadioButton[] scoreRadioButtons;
    private JButton submitImageButton;
    private String selectedFileName;
    private String selectedFilePath;
    private String previousImagePath;
    private String selectedCategory = "All";
    private String selectedLocation = "All";
    private JLabel loadedImageLabel;
    private JLabel averageScoreLabel;
    private JLabel imageNameLabel;
    private JLabel imageDimensionsLabel;
    private JLabel imageCategoryNameLabel;
    private JLabel imageLocationNameLabel;
    private JLabel imageScoreLabel;
    private JLabel imageUploaderUserNameLabel;
    private BufferedImage bufferedImage;
    private int selectedScore = 3;
    private float averageScore;
    private boolean isImageAlreadySavedToDB;
    private ImageStatisticsData newImageStatsData;
    private Image scaledMediumImage;

    ImageUploadPanel(final Color c) {
        setBackground(c);
        setMinimumSize(new Dimension(1024, 710));
        setPreferredSize(new Dimension(1024, 710));
        setLayout(null);

        JLabel imageChooserLabel = new JLabel("Choose an image: ");
        imageChooserLabel.setBackground(Color.LIGHT_GRAY);
        imageChooserLabel.setForeground(Color.BLUE);
        imageChooserLabel.setOpaque(true);

        imageChooserLabel.setBounds(30, 100, 150, 30);
        add(imageChooserLabel);

        chosenImagePath = new JTextField("...");
        chosenImagePath.setBackground(Color.LIGHT_GRAY);
        chosenImagePath.setForeground(Color.BLUE);
        chosenImagePath.setOpaque(true);
        chosenImagePath.setEditable(false);
        chosenImagePath.setBounds(200, 100, 300, 30);
        add(chosenImagePath);

        previousImagePath = null;

        JButton browseImageButton = new JButton("Browse");
        browseImageButton.setBackground(Color.LIGHT_GRAY);
        browseImageButton.setForeground(Color.BLUE);
        browseImageButton.setOpaque(true);
        browseImageButton.setBounds(690, 100, 300, 30);
        add(browseImageButton);
        browseImageButton.addActionListener((ActionEvent) -> {
            isImageAlreadySavedToDB = false;
            JFileChooser chooser = new JFileChooser();
            if (previousImagePath != null) chooser.setCurrentDirectory(new File(previousImagePath));
            chooser.setMultiSelectionEnabled(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "GIF, JPG, PNG Images", "gif", "jpg", "png");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                averageScoreLabel.setVisible(false);
                selectedFileName = chooser.getSelectedFile().getName();

                try {
                    selectedFilePath = chooser.getSelectedFile().getCanonicalPath();
                    previousImagePath = chooser.getSelectedFile().getParentFile().getCanonicalPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    bufferedImage = ImageIO.read(new File(selectedFilePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                scaledMediumImage = bufferedImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH);

                loadedImageLabel.setIcon(new ImageIcon(scaledMediumImage));
                chosenImagePath.setText(selectedFilePath);
                imageNameLabel.setText(String.format("File name: %s", selectedFileName));
                imageDimensionsLabel.setText(String.format("Dimensions: %d x %d pixels", bufferedImage.getWidth(), bufferedImage.getHeight()));
                imageCategoryNameLabel.setText(String.format("Image category: %s", selectedCategory));
                imageLocationNameLabel.setText(String.format("Image location: %s", selectedLocation));
                int existingImgIndex = 0;
                for (int i = 0; i < DBManager.getSavedImagesData().size(); i++) {
                    if (bufferedImagesEqual(bufferedImage, DBManager.getSavedImagesData().get(i).getImageData())) {
                        isImageAlreadySavedToDB = true;
                        existingImgIndex = i;
                        break;
                    }
                }
                if (isImageAlreadySavedToDB) {
                    final Timestamp imageKey = DBManager.getSavedImagesData().get(existingImgIndex).getUploadTimeStamp();
                    if (DBManager.getSavedImagesStatsLookup().containsKey(imageKey)) {
                        final ImageStatisticsData imgStatsData = DBManager.getSavedImagesStatsLookup().get(imageKey);
                        final ArrayList<UserVoteAction> userVoteActions = DBManager.getSavedUserVoteActions();
                        final String userName = DBManager.getLoggedInUserData().getUserName();
                        int userVote = 0;
                        for (UserVoteAction userVoteAction : userVoteActions) {
                            if (userVoteAction.getUploadTime().equals(imageKey) && userVoteAction.getUserName().equals(userName)) {
                                userVote = userVoteAction.getVoteScore();
                                break;
                            }
                        }
                        imageScoreLabel.setText(String.format("Image score: %d", imgStatsData.getScore()));
                        imageUploaderUserNameLabel.setText(String.format("Uploader name: %s", DBManager.getSavedImagesDataLookup().get(imgStatsData.getUploadTimeStamp()).getUploaderUserName()));
                        submitImageButton.setEnabled(false);
                        scoreRadioButtons[userVote].setSelected(true);
                        for (int i = 0; i < 6; i++) scoreRadioButtons[i].setEnabled(false);
                        averageScore = imgStatsData.getAvgScore();
                        final long numberOfVotes = imgStatsData.getNumberOfAllVotes();
                        averageScoreLabel.setVisible(true);
                        averageScoreLabel.setText(String.format("Average image score: %.2f from %d vote(s))", averageScore, numberOfVotes));
                        JOptionPane.showMessageDialog(getParent(), String.format("Chosen image file (%s) already exists in the fotoalbum_images table!", selectedFilePath), "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } else {

                    for (int i = 0; i < 6; i++) scoreRadioButtons[i].setEnabled(true);
                    scoreRadioButtons[3].setSelected(true);
                    submitImageButton.setEnabled(true);
                    newImageStatsData = new ImageStatisticsData(new Timestamp(0), selectedCategory, selectedLocation, bufferedImage.getWidth(), bufferedImage.getHeight(), 0, 0,
                            0, 0, 0, 0, 0, 1, 0);
                    imageScoreLabel.setText("Image score: n/a");
                    imageUploaderUserNameLabel.setText(String.format("Uploader name: %s", DBManager.getLoggedInUserData().getUserName()));
                }
            }
        });

        String[] imageCategories = new String[DBManager.getAvailableCategories().size()];
        DBManager.getAvailableCategories().toArray(imageCategories);
        comboBoxCategory = new JComboBox<>(imageCategories);
        comboBoxCategory.setFont(new Font("Arial", Font.BOLD, 16));
        comboBoxCategory.setEditable(true);
        comboBoxCategory.setSelectedIndex(0);
        comboBoxCategory.addItemListener(itemListenerEvent -> {
            if (itemListenerEvent.getStateChange() == ItemEvent.SELECTED) {
                selectedCategory = itemListenerEvent.getItem().toString();
                imageCategoryNameLabel.setText(String.format("Image category: %s", selectedCategory));
            }
        });
        comboBoxCategory.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                super.keyPressed(keyEvent);
                final String newCategoryEntry = comboBoxCategory.getActionCommand();
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER && newCategoryEntry.length() > 2) {
                    final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    DBManager.addNewCategory(now, DBManager.getLoggedInUserData().getUserName(), newCategoryEntry);
                    DBManager.dbInsertNewCategory(new ImageCategory(now, DBManager.getLoggedInUserData().getUserName(), newCategoryEntry));
                    selectedCategory = newCategoryEntry;
                    comboBoxCategory.addItem(newCategoryEntry);
                    comboBoxCategory.setSelectedItem(newCategoryEntry);

                }

            }

        });
        comboBoxCategory.setBounds(100, 230, 300, 30);
        add(comboBoxCategory);

        String[] imageLocations = new String[DBManager.getAvailableLocations().size()];
        DBManager.getAvailableLocations().toArray(imageLocations);
        comboBoxLocation = new JComboBox<>(imageLocations);
        comboBoxLocation.setFont(new Font("Arial", Font.BOLD, 16));
        comboBoxLocation.setEditable(true);
        comboBoxLocation.setSelectedIndex(0);
        comboBoxLocation.addItemListener(itemListenerEvent -> {
            if (itemListenerEvent.getStateChange() == ItemEvent.SELECTED) {
                selectedLocation = itemListenerEvent.getItem().toString();
                imageLocationNameLabel.setText(String.format("Image location: %s", selectedLocation));
            }
        });
        comboBoxLocation.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                super.keyPressed(keyEvent);
                final String newLocationEntry = comboBoxLocation.getActionCommand();
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER && newLocationEntry.length() > 2) {
                    final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    DBManager.addNewLocation(now, DBManager.getLoggedInUserData().getUserName(), newLocationEntry);
                    DBManager.insertNewLocationIntoDB(new ImageLocation(now, DBManager.getLoggedInUserData().getUserName(), newLocationEntry));
                    selectedLocation = newLocationEntry;
                    comboBoxLocation.addItem(newLocationEntry);
                    comboBoxLocation.setSelectedItem(newLocationEntry);

                }


            }

        });
        comboBoxLocation.setBounds(100, 270, 300, 30);
        add(comboBoxLocation);


        submitImageButton = new JButton("Submit");
        submitImageButton.setForeground(Color.BLUE);
        submitImageButton.setOpaque(true);
        submitImageButton.addActionListener(actionEvent -> {
            for (int i = 0; i < DBManager.getSavedImagesData().size(); i++) {
                if (bufferedImagesEqual(bufferedImage, DBManager.getSavedImagesData().get(i).getImageData())) {
                    isImageAlreadySavedToDB = true;
                    break;
                }
            }

            if (!isImageAlreadySavedToDB) {
                final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                ImageData newImage = new ImageData(now, selectedFileName, selectedFilePath, DBManager.getLoggedInUserData().getUserName(), bufferedImage);
                newImageStatsData.setUploadTimeStamp(now);
                newImageStatsData.increaseCorrectGroupOfNumberOfVotes(selectedScore);
                newImageStatsData.setImageCategory(selectedCategory);
                newImageStatsData.setImageLocation(selectedLocation);
                Image scaledMiniImage = bufferedImage.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
                DBManager.getSavedScaledImages().put(now, new ScaledImageData(scaledMiniImage, scaledMediumImage));
                DBManager.dbInsertNewImage(newImage);
                DBManager.dbInsertNewImageStatsData(newImageStatsData);
                DBManager.getSavedImagesData().add(newImage);
                DBManager.getSavedImagesDataLookup().put(now, newImage);
                DBManager.dbInsertNewImageVoteData(DBManager.getLoggedInUserData().getUserName(), now, selectedScore, now);
                DBManager.getSavedImagesStats().add(newImageStatsData);
                DBManager.getSavedImagesStatsLookup().put(now, newImageStatsData);
                submitImageButton.setEnabled(false);
                scoreRadioButtons[selectedScore].setSelected(true);
                for (int i = 0; i < 6; i++) scoreRadioButtons[i].setEnabled(false);
                imageCategoryNameLabel.setText(String.format("Image category: %s", selectedCategory));
                imageLocationNameLabel.setText(String.format("Image location: %s", selectedLocation));
                imageScoreLabel.setText(String.format("Image score: %d", selectedScore));
                MainWindow.displayDataPanel.loadImages("All", "All");
                MainWindow.dataStatisticsPanel.sortImages(ImageSortOrder.UPLOADTIME_ASC);
                MainWindow.dataStatisticsPanel.loadSortedImages();
                MainWindow.adminPanel.reloadExistingImages();
                JOptionPane.showMessageDialog(ImageUploadPanel.this, String.format("You submitted score %d for image (filename: %s, canonical path: '%s')",
                        selectedScore, selectedFileName, selectedFilePath), "Information", JOptionPane.INFORMATION_MESSAGE);
            }

        });
        submitImageButton.setEnabled(false);
        submitImageButton.setBounds(500, 230, 300, 30);
        add(submitImageButton);

        loadedImageLabel = new JLabel();
        loadedImageLabel.setBounds(250, 320, 300, 300);
        loadedImageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        loadedImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                if (mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() > 1) {

                    if (zoomedImageFrame != null) {
                        zoomedImageFrame.setVisible(false);
                        zoomedImageFrame.dispose();
                        zoomedImageFrame = null;
                    }

                    zoomedImageFrame = new JFrame(String.format("Zoomed image view: %s", selectedFilePath));
                    zoomedImageFrame.setSize(800, 600);
                    Image scaledImage = bufferedImage.getScaledInstance(800, 600, java.awt.Image.SCALE_SMOOTH);
                    JLabel zoomedImage = new JLabel();
                    zoomedImage.setIcon(new ImageIcon(scaledImage));
                    zoomedImageFrame.getContentPane().add(zoomedImage);
                    zoomedImageFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    zoomedImageFrame.setLocationRelativeTo(ImageUploadPanel.this);
                    zoomedImageFrame.setVisible(true);
                    newImageStatsData.setSeenTimes(newImageStatsData.getSeenTimes() + 1);
                }
            }

        });

        add(loadedImageLabel);

        imageNameLabel = new JLabel("Image name: ");
        imageNameLabel.setBounds(600, 320, 400, 20);
        imageNameLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        add(imageNameLabel);
        imageDimensionsLabel = new JLabel("Dimensions: ");
        imageDimensionsLabel.setBounds(600, 350, 400, 20);
        imageDimensionsLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        add(imageDimensionsLabel);
        imageCategoryNameLabel = new JLabel("Image category: ");
        imageCategoryNameLabel.setBounds(600, 380, 400, 20);
        imageCategoryNameLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        add(imageCategoryNameLabel);
        imageLocationNameLabel = new JLabel("Image location: ");
        imageLocationNameLabel.setBounds(600, 410, 400, 20);
        imageLocationNameLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        add(imageLocationNameLabel);
        imageScoreLabel = new JLabel("Image score: ");
        imageScoreLabel.setBounds(600, 440, 400, 20);
        imageScoreLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        add(imageScoreLabel);
        imageUploaderUserNameLabel = new JLabel("Uploader name: ");
        imageUploaderUserNameLabel.setBounds(600, 470, 400, 20);
        imageUploaderUserNameLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        add(imageUploaderUserNameLabel);

        int offset = 40;
        scoreRadioButtons = new JRadioButton[6];
        ButtonGroup bg = new ButtonGroup();
        ActionListener listener = event -> {
            if (event.getActionCommand().equals("none")) {
                selectedScore = 0;
                imageScoreLabel.setText(String.format("Image score: n/a"));
                submitImageButton.setEnabled(false);
            } else {
                selectedScore = Integer.parseInt(event.getActionCommand());
                imageScoreLabel.setText(String.format("Image score: %d", selectedScore));
                submitImageButton.setEnabled(true);
            }
        };

        scoreRadioButtons[0] = new JRadioButton("none", true);
        scoreRadioButtons[0].setEnabled(false);
        bg.add(scoreRadioButtons[0]);
        scoreRadioButtons[0].setBounds(350, 620, 50, 20);
        add(scoreRadioButtons[0]);
        scoreRadioButtons[0].addActionListener(listener);

        for (int i = 1; i <= 5; i++) {
            scoreRadioButtons[i] = new JRadioButton(String.valueOf(i), false);
            scoreRadioButtons[i].setEnabled(false);
            bg.add(scoreRadioButtons[i]);
            scoreRadioButtons[i].setBounds(360 + i * offset, 620, 40, 20);
            add(scoreRadioButtons[i]);
            scoreRadioButtons[i].addActionListener(listener);
        }

        averageScoreLabel = new JLabel();
        averageScoreLabel.setBounds(630, 620, 260, 20);
        averageScoreLabel.setVisible(false);
        add(averageScoreLabel);


    }

    static boolean bufferedImagesEqual(final BufferedImage img1, final BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            for (int x = 0; x < img1.getWidth(); x++) {
                for (int y = 0; y < img1.getHeight(); y++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y))
                        return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

//     private static String incrementCorrectVoteScoreGroup(final ImageStatisticsData imgStatsData, final int scoreValue) {
//
//        if (imgStatsData == null) return "score1_votes";
//
//        switch (scoreValue) {
//            case 1:
//                imgStatsData.increaseCorrectGroupOfNumberOfVotes(1);
//                return "score1_votes";
//
//            case 2:
//                imgStatsData.increaseCorrectGroupOfNumberOfVotes(2);
//                return "score2_votes";
//
//            case 3:
//                imgStatsData.increaseCorrectGroupOfNumberOfVotes(3);
//                return "score3_votes";
//
//            case 4:
//                imgStatsData.increaseCorrectGroupOfNumberOfVotes(4);
//                return "score4_votes";
//
//            case 5:
//                imgStatsData.increaseCorrectGroupOfNumberOfVotes(5);
//                return "score5_votes";
//
//            default:
//                return "score1_votes";
//
//        }
//
//    }

//    static long getCorrectNumberOfVotesForScore(final ImageStatisticsData imgStatsData, final int scoreValue) {
//
//        if (imgStatsData == null) return 0;
//
//        switch (scoreValue) {
//            case 1:
//                return imgStatsData.getNumberOf1Votes();
//
//            case 2:
//                return imgStatsData.getNumberOf2Votes();
//
//            case 3:
//                return imgStatsData.getNumberOf3Votes();
//
//            case 4:
//                return imgStatsData.getNumberOf4Votes();
//
//            case 5:
//                return imgStatsData.getNumberOf5Votes();
//
//            default:
//                return 0;
//
//        }
//
//    }


//    public Dimension getPreferredDimension() {
//        return new Dimension(1024, 700);
//
//    }
//
//    public Dimension getMinimumDimension(){
//
//        return new Dimension(1024, 700);
//    }

}




