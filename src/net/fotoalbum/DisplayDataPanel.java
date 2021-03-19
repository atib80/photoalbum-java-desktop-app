package net.fotoalbum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class DisplayDataPanel extends JPanel implements ItemListener {

    private static final DateTimeFormatter dtFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Font biggerFont = new Font("Arial", Font.BOLD, 14);
    static JFrame zoomedImageFrame = null;
    private final JComboBox<String> comboBoxImageCategory;
    private final JComboBox<String> comboBoxImageLocation;
    private String selectedImageCategory;
    private String selectedImageLocation;
    private ImageData currentlySelectedImageData;
    private ImageStatisticsData currentlySelectedImageStatsData;
    private final JButton submitImageCategoryAndLocationButton;
    private final JTextArea imageInformationTextArea;
    private final JPanel imagesPanel;
    private JLabel[] imageLabels;
    private final JRadioButton[] scoreRadioButtons;
    private final JButton resetMyVoteButton;
    private JButton submitSelectedScoreButton;
    private int numberOfFilteredImages;
    private JLabel previousBorderImage;
    private final JLabel averageScoreLabel;
    private int srcLabelIndex = 0;
    private int selectedImageNewScore = 3;
    private long selectedImageVotes;
    private Timestamp selectedImageTimestamp;
    private int selectedImageUserScore;
    private String infoText;
    private final String userName;

    DisplayDataPanel(final Color c, final String filterImageCategory, final String filterImageLocation) {
        setBackground(c);
        setLayout(null);
        selectedImageCategory = filterImageCategory;
        selectedImageLocation = filterImageLocation;
        userName = DBManager.getLoggedInUserData().getUserName();

        JLabel lblChooseImageCategory = new JLabel("Choose image category:");
        lblChooseImageCategory.setFont(biggerFont);
        lblChooseImageCategory.setBounds(100, 50, 200, 30);
        add(lblChooseImageCategory);
        String[] imageCategories = new String[DBManager.getAvailableCategories().size()];
        DBManager.getAvailableCategories().toArray(imageCategories);
        comboBoxImageCategory = new JComboBox<>(imageCategories);
        comboBoxImageCategory.setFont(biggerFont);
        comboBoxImageCategory.setEditable(false);
        comboBoxImageCategory.addItemListener(this);
        comboBoxImageCategory.setSelectedItem(selectedImageCategory);
        comboBoxImageCategory.setBounds(330, 50, 300, 30);
        add(comboBoxImageCategory);

        JLabel lblChooseImageLocation = new JLabel("Choose image location:");
        lblChooseImageLocation.setFont(biggerFont);
        lblChooseImageLocation.setBounds(100, 100, 200, 30);
        add(lblChooseImageLocation);
        String[] imageLocations = new String[DBManager.getAvailableLocations().size()];
        DBManager.getAvailableLocations().toArray(imageLocations);
        comboBoxImageLocation = new JComboBox<>(imageLocations);
        comboBoxImageLocation.setFont(biggerFont);
        comboBoxImageLocation.setEditable(false);
        comboBoxImageLocation.addItemListener(this);
        comboBoxImageLocation.setSelectedItem(selectedImageLocation);
        comboBoxImageLocation.setBounds(330, 100, 300, 30);
        add(comboBoxImageLocation);

        submitImageCategoryAndLocationButton = new JButton("Submit");
        submitImageCategoryAndLocationButton.setFont(biggerFont);
        submitImageCategoryAndLocationButton.addActionListener(event -> {
            loadImages(selectedImageCategory, selectedImageLocation);
            submitImageCategoryAndLocationButton.setEnabled(false);
        });

        submitImageCategoryAndLocationButton.setBounds(650, 100, 120, 30);
        add(submitImageCategoryAndLocationButton);
        submitImageCategoryAndLocationButton.setEnabled(false);

        imageInformationTextArea = new JTextArea();
        imageInformationTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        imageInformationTextArea.setBackground(Color.CYAN);
        imageInformationTextArea.setLineWrap(true);
        JScrollPane imageInformationScrollPane = new JScrollPane(imageInformationTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        imageInformationScrollPane.setBounds(270, 440, 500, 250);
        add(imageInformationScrollPane);

        imagesPanel = new JPanel();
        imagesPanel.setSize(1024, 300);
        imagesPanel.setBackground(new Color(94, 168, 200));

        final JScrollPane scrollPane = new JScrollPane(imagesPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 200, 1024, 150);
        add(scrollPane);

        ButtonGroup bg = new ButtonGroup();

        int offset = 40;
        scoreRadioButtons = new JRadioButton[6];
        scoreRadioButtons[0] = new JRadioButton("none", true);
        bg.add(scoreRadioButtons[0]);
        scoreRadioButtons[0].setBounds(360, 370, 50, 25);
        add(scoreRadioButtons[0]);
        ActionListener listener = event -> {
            if (event.getActionCommand().equals("none")) {
                selectedImageNewScore = 0;
                submitSelectedScoreButton.setEnabled(false);
            } else {
                selectedImageNewScore = Integer.parseInt(event.getActionCommand());
                submitSelectedScoreButton.setEnabled(true);
            }
        };

        scoreRadioButtons[0].addActionListener(listener);

        for (int i = 1; i <= 5; i++) {
            scoreRadioButtons[i] = new JRadioButton(String.valueOf(i), false);
            bg.add(scoreRadioButtons[i]);
            scoreRadioButtons[i].setBounds(370 + i * offset, 370, 40, 25);
            add(scoreRadioButtons[i]);
            scoreRadioButtons[i].addActionListener(listener);
        }

        resetMyVoteButton = new JButton("Vote again?");
        resetMyVoteButton.setFont(biggerFont);
        resetMyVoteButton.setBounds(200, 370, 140, 30);
        resetMyVoteButton.addActionListener(actionEvent -> {
            currentlySelectedImageStatsData.decreaseCorrectGroupOfNumberOfVotes(selectedImageUserScore);
            DBManager.getSavedUserDataStatsLookup().get(userName).decreaseCorrectGroupOfNumberOfVotes(selectedImageUserScore);
            DBManager.dbUpdateExistingImageStatsData(currentlySelectedImageStatsData, "score", String.format("score%d_votes", selectedImageUserScore), "avgscore");
            if (-1 != DBManager.getVoteScoreForSpecifiedUserNameAndImageTimeStamp(userName, currentlySelectedImageStatsData.getUploadTimeStamp())) {
                DBManager.deleteExistingUserVoteForImage(userName, selectedImageTimestamp);
                DBManager.removeExistingUserVoteForImage(userName, selectedImageTimestamp);
            }
            selectedImageNewScore = 0;
            scoreRadioButtons[0].setSelected(true);
            for (int j = 0; j < 6; j++) scoreRadioButtons[j].setEnabled(true);
            submitSelectedScoreButton.setEnabled(false);
            resetMyVoteButton.setEnabled(false);
        });

        add(resetMyVoteButton);

        submitSelectedScoreButton = new JButton("Submit score");
        submitSelectedScoreButton.setFont(biggerFont);
        submitSelectedScoreButton.setBounds(630, 370, 140, 30);
        submitSelectedScoreButton.addActionListener(actionEvent -> {
            final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            currentlySelectedImageStatsData.increaseCorrectGroupOfNumberOfVotes(selectedImageNewScore);
            DBManager.getSavedUserDataStatsLookup().get(userName).increaseCorrectGroupOfNumberOfVotes(selectedImageNewScore);
            DBManager.dbUpdateExistingUserStatsData(DBManager.getSavedUserDataStatsLookup().get(userName), "lastvotetime",
                    "lastvotescore", String.format("score%d_votes", selectedImageNewScore));
            DBManager.dbUpdateExistingImageStatsData(currentlySelectedImageStatsData, "score", String.format("score%d_votes", selectedImageNewScore), "avgscore");
            if (-1 != DBManager.getVoteScoreForSpecifiedUserNameAndImageTimeStamp(userName, currentlySelectedImageStatsData.getUploadTimeStamp())) {
                DBManager.deleteExistingUserVoteForImage(userName, currentlySelectedImageStatsData.getUploadTimeStamp());
                DBManager.removeExistingUserVoteForImage(userName, currentlySelectedImageStatsData.getUploadTimeStamp());
            }
            DBManager.dbInsertNewImageVoteData(userName, currentlySelectedImageStatsData.getUploadTimeStamp(), selectedImageNewScore, now);
            scoreRadioButtons[selectedImageNewScore].setSelected(true);
            for (int j = 0; j < 6; j++) scoreRadioButtons[j].setEnabled(false);
            resetMyVoteButton.setEnabled(true);
            submitSelectedScoreButton.setEnabled(false);
            JOptionPane.showMessageDialog(DisplayDataPanel.this, String.format("You submitted score %d for image no. %d", selectedImageNewScore, srcLabelIndex + 1),
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            loadImages(selectedImageCategory, selectedImageLocation);
            MainWindow.dataStatisticsPanel.loadSortedImages();

        });

        add(submitSelectedScoreButton);

        averageScoreLabel = new JLabel();
        averageScoreLabel.setBounds(400, 400, 200, 25);
        add(averageScoreLabel);

        loadImages(selectedImageCategory, selectedImageLocation);
    }

    void loadImages(final String imageCategory, final String imageLocation) {

        previousBorderImage = null;
        imagesPanel.removeAll();
        DBManager.retrieveSavedImageStatsDataForSpecifiedFilters(ImageSortOrder.UPLOADTIME_ASC, imageCategory, imageLocation);
        numberOfFilteredImages = DBManager.getFilteredImagesStats().size();

        if (numberOfFilteredImages == 0) {

            imageLabels = new JLabel[3];
            for (int i = 0; i < 3; i++) {
                imageLabels[i] = new JLabel();
                imageLabels[i].setMinimumSize(new Dimension(150, 150));
                imageLabels[i].setSize(150, 150);
                imagesPanel.add(imageLabels[i]);
            }

            imageLabels[0].setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        } else {

            currentlySelectedImageData = DBManager.getSavedImagesDataLookup().get(DBManager.getFilteredImagesStats().get(0).getUploadTimeStamp());
            currentlySelectedImageStatsData = DBManager.getFilteredImagesStats().get(0);

            infoText = String.format("Information about selected image:\nFile name: %s\nDimensions: %d x %d pixels\n" +
                            "Category: %s\nLocation: %s\nImage score: %d\nAverage image score: %.2f from %d vote(s))\nName of uploader: %s\nDate of upload: %s\n",
                    currentlySelectedImageData.getImageName(), currentlySelectedImageStatsData.getImageWidth(),
                    currentlySelectedImageStatsData.getImageHeight(), currentlySelectedImageStatsData.getImageCategory(), currentlySelectedImageStatsData.getImageLocation(),
                    currentlySelectedImageStatsData.getScore(), currentlySelectedImageStatsData.getAvgScore(), currentlySelectedImageStatsData.getNumberOfAllVotes(),
                    currentlySelectedImageData.getUploaderUserName(), currentlySelectedImageData.getUploadTimeStamp().toLocalDateTime().format(dtFormat));

            imageInformationTextArea.setText(infoText);

            imageLabels = new JLabel[numberOfFilteredImages];
            for (int i = 0; i < numberOfFilteredImages; i++) {
                imageLabels[i] = new JLabel();
                imageLabels[i].setMinimumSize(new Dimension(150, 150));
                imageLabels[i].setSize(150, 150);
                imageLabels[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                            super.mouseClicked(mouseEvent);
                            JLabel src = (JLabel) mouseEvent.getSource();
                            if (previousBorderImage != null && previousBorderImage != src)
                                previousBorderImage.setBorder(null);
                            src.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
                            previousBorderImage = src;

                            for (int i = 0; i < numberOfFilteredImages; i++) {
                                if (src == imageLabels[i]) {
                                    srcLabelIndex = i;
                                    break;
                                }
                            }

                            currentlySelectedImageStatsData = DBManager.getFilteredImagesStats().get(srcLabelIndex);
                            selectedImageTimestamp = currentlySelectedImageStatsData.getUploadTimeStamp();
                            selectedImageVotes = currentlySelectedImageStatsData.getNumberOfAllVotes();
                            final float averageScore = currentlySelectedImageStatsData.getAvgScore();

                            selectedImageUserScore = DBManager.getVoteScoreForSpecifiedUserNameAndImageTimeStamp(userName, selectedImageTimestamp);
                            if (selectedImageUserScore != -1) {
                                scoreRadioButtons[selectedImageUserScore].setSelected(true);
                                for (int j = 0; j < 6; j++) scoreRadioButtons[j].setEnabled(false);
                                resetMyVoteButton.setEnabled(true);
                                submitSelectedScoreButton.setEnabled(false);
                            } else {
                                for (int j = 0; j < 6; j++) scoreRadioButtons[j].setEnabled(true);
                                scoreRadioButtons[0].setSelected(true);
                                resetMyVoteButton.setEnabled(false);
                                submitSelectedScoreButton.setEnabled(true);
                            }

                            averageScoreLabel.setText(String.format("Average score: %.2f", averageScore));

                            currentlySelectedImageData = DBManager.getSavedImagesDataLookup().get(selectedImageTimestamp);

                            infoText = String.format("Information about selected image:\nFile name: %s\nDimensions: %d x %d pixels\n" +
                                            "Category: %s\nLocation: %s\nImage score: %d\nAverage image score: %.2f from %d vote(s))\nName of uploader: %s\nDate of upload: %s\n",
                                    currentlySelectedImageData.getImageName(), currentlySelectedImageStatsData.getImageWidth(),
                                    currentlySelectedImageStatsData.getImageHeight(), currentlySelectedImageStatsData.getImageCategory(), currentlySelectedImageStatsData.getImageLocation(),
                                    currentlySelectedImageStatsData.getScore(), currentlySelectedImageStatsData.getAvgScore(), currentlySelectedImageStatsData.getNumberOfAllVotes(),
                                    currentlySelectedImageData.getUploaderUserName(), currentlySelectedImageData.getUploadTimeStamp().toLocalDateTime().format(dtFormat));

                            imageInformationTextArea.setText(infoText);

                            if (mouseEvent.getClickCount() > 1) {

                                if (zoomedImageFrame != null) {
                                    zoomedImageFrame.setVisible(false);
                                    zoomedImageFrame.dispose();
                                    zoomedImageFrame = null;
                                }

                                zoomedImageFrame = new JFrame("Zoomed image view");
                                zoomedImageFrame.setSize(800, 600);
                                Image scaledImage = new ImageIcon(DBManager.getSavedImagesDataLookup().get(selectedImageTimestamp).getImageData()).getImage().getScaledInstance(800, 600, java.awt.Image.SCALE_SMOOTH);
                                JLabel zoomedImage = new JLabel();
                                zoomedImage.setIcon(new ImageIcon(scaledImage));
                                zoomedImageFrame.getContentPane().add(zoomedImage);
                                zoomedImageFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                zoomedImageFrame.setLocationRelativeTo(DisplayDataPanel.this);
                                zoomedImageFrame.setVisible(true);
                                currentlySelectedImageStatsData.setSeenTimes(currentlySelectedImageStatsData.getSeenTimes() + 1);
                                currentlySelectedImageStatsData.setSeenTimes(currentlySelectedImageStatsData.getSeenTimes() + 1);
                                infoText = String.format("Information about selected image:\nFile name: %s\nDimensions: %d x %d pixels\n" +
                                                "Category: %s\nLocation: %s\nImage score: %d\nAverage image score: %.2f from %d vote(s))\nView count: %d\nName of uploader: %s\nDate of upload: %s\n",
                                        currentlySelectedImageData.getImageName(), currentlySelectedImageStatsData.getImageWidth(),
                                        currentlySelectedImageStatsData.getImageHeight(), currentlySelectedImageStatsData.getImageCategory(), currentlySelectedImageStatsData.getImageLocation(),
                                        currentlySelectedImageStatsData.getScore(), currentlySelectedImageStatsData.getAvgScore(), currentlySelectedImageStatsData.getNumberOfAllVotes(),
                                        currentlySelectedImageStatsData.getSeenTimes(), currentlySelectedImageData.getUploaderUserName(), currentlySelectedImageData.getUploadTimeStamp().toLocalDateTime().format(dtFormat));

                                imageInformationTextArea.setText(infoText);
                                DBManager.dbUpdateExistingImageStatsData(currentlySelectedImageStatsData, "seen_times");

                            }

                        }
                    }

                });

                ImageIcon imageIcon = new ImageIcon(DBManager.getSavedScaledImages().get(DBManager.getFilteredImagesStats().get(i).getUploadTimeStamp()).getScaledMiniImage());
                imageLabels[i].setIcon(imageIcon);
                imagesPanel.add(imageLabels[i]);
            }
        }

        float averageScore = 0.f;

        if (numberOfFilteredImages > 0) {

            currentlySelectedImageData = DBManager.getSavedImagesDataLookup().get(DBManager.getFilteredImagesStats().get(0).getUploadTimeStamp());
            currentlySelectedImageStatsData = DBManager.getFilteredImagesStats().get(0);

            selectedImageVotes = currentlySelectedImageStatsData.getNumberOfAllVotes();
            averageScore = currentlySelectedImageStatsData.getAvgScore();
            int userScore = DBManager.getVoteScoreForSpecifiedUserNameAndImageTimeStamp(userName, currentlySelectedImageStatsData.getUploadTimeStamp());
            if (userScore != -1) {
                scoreRadioButtons[userScore].setSelected(true);
                for (int j = 0; j < 6; j++) scoreRadioButtons[j].setEnabled(false);
                submitSelectedScoreButton.setEnabled(false);
                resetMyVoteButton.setEnabled(true);
            } else {
                scoreRadioButtons[0].setSelected(true);
                resetMyVoteButton.setEnabled(false);
            }

            imageLabels[0].setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
            previousBorderImage = imageLabels[0];

        }


        averageScoreLabel.setText(String.format("Average score: %.2f from %d vote(s)", averageScore, selectedImageVotes));

        revalidate();
        repaint();
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        if (comboBoxImageCategory.equals(itemEvent.getSource())) {

            final String currentlySelectedImageCategory = itemEvent.getItem().toString();
            if (!selectedImageCategory.equalsIgnoreCase(currentlySelectedImageCategory)) {
                selectedImageCategory = currentlySelectedImageCategory;
                submitImageCategoryAndLocationButton.setEnabled(true);
            }

        } else if (comboBoxImageLocation.equals(itemEvent.getSource())) {

            final String currentlySelectedImageLocation = itemEvent.getItem().toString();
            if (!selectedImageLocation.equalsIgnoreCase(currentlySelectedImageLocation)) {
                selectedImageLocation = currentlySelectedImageLocation;
                submitImageCategoryAndLocationButton.setEnabled(true);
            }

        }
    }


}
