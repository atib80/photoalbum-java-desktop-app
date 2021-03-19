package net.fotoalbum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

enum ImageSortOrder {UPLOADTIME_ASC, UPLOADTIME_DESC, AVGSCORE_ASC, AVGSCORE_DESC, VIEWS_ASC, VIEWS_DESC}

class DataStatisticsPanel extends JPanel implements ItemListener {

    private static final DateTimeFormatter dtFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static JFrame zoomedImageFrame;
    private final JRadioButton[] sortCriteriaRadioButtons = new JRadioButton[6];
    private ImageSortOrder prevSortOrder = ImageSortOrder.UPLOADTIME_ASC;
    private String selectedImageCategory;
    private String selectedImageLocation;
    private JButton sortImagesBySpecifiedFiltersButton;
    private JComboBox<String> comboBoxCategory;
    private JComboBox<String> comboBoxLocation;
    private JPanel imagesPanel;
    private JLabel[] imageLabels;
    private JLabel previousBorderImage;
    private int srcLabelIndex = 0;
    private ImageData currentlySelectedImageData;
    private ImageStatisticsData currentlySelectedImageStatsData;
    private JTextArea imageInformationTextArea;

    DataStatisticsPanel(final Color c, final String imageCategory, final String imageLocation) {
        selectedImageCategory = imageCategory;
        selectedImageLocation = imageLocation;
        previousBorderImage = null;
        constructGUI(c);
        sortImages(prevSortOrder);
        loadSortedImages();
    }

    private static String getImageInformationForSortOrder(final ImageStatisticsData imgStats, final ImageSortOrder sortCrit) {

        switch (sortCrit) {

            case UPLOADTIME_ASC:
            case UPLOADTIME_DESC:
                return String.format("Upload time: %s", imgStats.getUploadTimeStamp().toLocalDateTime().toString());

            case AVGSCORE_ASC:
            case AVGSCORE_DESC:
                return String.format("Average score: %.2f", imgStats.getAvgScore());

            case VIEWS_ASC:
            case VIEWS_DESC:
                return String.format("Views: %d", imgStats.getSeenTimes());
            default:
                return "";
        }
    }

    private void constructGUI(final Color c) {

        setBackground(c);
        setLayout(null);
        final Font font = new Font("Arial", Font.BOLD, 12);

        JLabel lblChooseImageCategory = new JLabel("Choose an image category:");
        lblChooseImageCategory.setFont(font);
        lblChooseImageCategory.setBounds(50, 30, 230, 25);
        add(lblChooseImageCategory);

        String[] imageCategories = new String[DBManager.getAvailableCategories().size()];
        DBManager.getAvailableCategories().toArray(imageCategories);
        comboBoxCategory = new JComboBox<>(imageCategories);
        comboBoxCategory.setSelectedItem(selectedImageCategory);
        // comboBoxCategory.setSelectedIndex(selectedImageCategoryIndex);
        comboBoxCategory.setFont(font);
        comboBoxCategory.setEditable(false);
        comboBoxCategory.addItemListener(this);
        comboBoxCategory.setBounds(300, 30, 300, 30);
        add(comboBoxCategory);

        String[] imageLocations = new String[DBManager.getAvailableLocations().size()];
        DBManager.getAvailableLocations().toArray(imageLocations);
        comboBoxLocation = new JComboBox<>(imageLocations);
        comboBoxLocation.setSelectedItem(selectedImageLocation);
        // comboBoxLocation.setSelectedIndex(selectedImageLocationIndex);
        comboBoxLocation.setFont(font);
        comboBoxLocation.setEditable(false);
        comboBoxLocation.addItemListener(this);
        comboBoxLocation.setBounds(300, 80, 300, 30);
        add(comboBoxLocation);

        sortImagesBySpecifiedFiltersButton = new JButton("Sort");
        sortImagesBySpecifiedFiltersButton.setFont(font);
        sortImagesBySpecifiedFiltersButton.addActionListener(event -> {
            sortImages(prevSortOrder);
            loadSortedImages();
            sortImagesBySpecifiedFiltersButton.setEnabled(false);
        });

        sortImagesBySpecifiedFiltersButton.setBounds(630, 30, 120, 30);
        sortImagesBySpecifiedFiltersButton.setEnabled(false);
        add(sortImagesBySpecifiedFiltersButton);


        JLabel sortInfoLabel = new JLabel("Sort by:");
        sortInfoLabel.setFont(font);
        sortInfoLabel.setBounds(50, 130, 100, 25);
        add(sortInfoLabel);

        ButtonGroup bg = new ButtonGroup();
        ActionListener sortAction = event -> {
            final JRadioButton src = (JRadioButton) event.getSource();
            ImageSortOrder nextSortOrder = ImageSortOrder.UPLOADTIME_ASC;

            if (sortCriteriaRadioButtons[1] == src)
                nextSortOrder = ImageSortOrder.UPLOADTIME_DESC;
            else if (sortCriteriaRadioButtons[2] == src)
                nextSortOrder = ImageSortOrder.AVGSCORE_ASC;
            else if (sortCriteriaRadioButtons[3] == src)
                nextSortOrder = ImageSortOrder.AVGSCORE_DESC;
            else if (sortCriteriaRadioButtons[4] == src)
                nextSortOrder = ImageSortOrder.VIEWS_ASC;
            else if (sortCriteriaRadioButtons[5] == src)
                nextSortOrder = ImageSortOrder.VIEWS_DESC;

            if (!nextSortOrder.equals(prevSortOrder)) {
                sortImagesBySpecifiedFiltersButton.setEnabled(true);
                prevSortOrder = nextSortOrder;
            }

        };

        sortCriteriaRadioButtons[0] = new JRadioButton("by upload time (ASC)", true);
        sortCriteriaRadioButtons[0].addActionListener(sortAction);
        sortCriteriaRadioButtons[0].setBounds(150, 130, 220, 25);
        bg.add(sortCriteriaRadioButtons[0]);
        add(sortCriteriaRadioButtons[0]);

        sortCriteriaRadioButtons[1] = new JRadioButton("by upload time (DESC)", false);
        sortCriteriaRadioButtons[1].addActionListener(sortAction);
        sortCriteriaRadioButtons[1].setBounds(150, 160, 220, 25);
        bg.add(sortCriteriaRadioButtons[1]);
        add(sortCriteriaRadioButtons[1]);

        sortCriteriaRadioButtons[2] = new JRadioButton("by average score (ASC)", false);
        sortCriteriaRadioButtons[2].addActionListener(sortAction);
        sortCriteriaRadioButtons[2].setBounds(150, 190, 220, 25);
        bg.add(sortCriteriaRadioButtons[2]);
        add(sortCriteriaRadioButtons[2]);

        sortCriteriaRadioButtons[3] = new JRadioButton("by average score (DESC)", false);
        sortCriteriaRadioButtons[3].addActionListener(sortAction);
        sortCriteriaRadioButtons[3].setBounds(150, 220, 220, 25);
        bg.add(sortCriteriaRadioButtons[3]);
        add(sortCriteriaRadioButtons[3]);

        sortCriteriaRadioButtons[4] = new JRadioButton("by view count (ASC)", false);
        sortCriteriaRadioButtons[4].addActionListener(sortAction);
        sortCriteriaRadioButtons[4].setBounds(150, 250, 220, 25);
        bg.add(sortCriteriaRadioButtons[4]);
        add(sortCriteriaRadioButtons[4]);

        sortCriteriaRadioButtons[5] = new JRadioButton("by view count (DESC)", false);
        sortCriteriaRadioButtons[5].addActionListener(sortAction);
        sortCriteriaRadioButtons[5].setBounds(150, 280, 220, 25);
        bg.add(sortCriteriaRadioButtons[5]);
        add(sortCriteriaRadioButtons[5]);

        switch (prevSortOrder) {

            case UPLOADTIME_ASC:
                sortCriteriaRadioButtons[0].setSelected(true);
                break;

            case UPLOADTIME_DESC:
                sortCriteriaRadioButtons[1].setSelected(true);
                break;

            case AVGSCORE_ASC:
                sortCriteriaRadioButtons[2].setSelected(true);
                break;

            case AVGSCORE_DESC:
                sortCriteriaRadioButtons[3].setSelected(true);
                break;

            case VIEWS_ASC:
                sortCriteriaRadioButtons[4].setSelected(true);
                break;

            case VIEWS_DESC:
                sortCriteriaRadioButtons[5].setSelected(true);
                break;

        }

        imageInformationTextArea = new JTextArea();
        imageInformationTextArea.setFont(font);
        imageInformationTextArea.setBackground(Color.LIGHT_GRAY);
        imageInformationTextArea.setLineWrap(true);
        imageInformationTextArea.setBounds(430, 120, 500, 200);
        imageInformationTextArea.setText("Information about selected image:\n");
        JScrollPane imageInformationScrollPane = new JScrollPane(imageInformationTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        imageInformationScrollPane.setBounds(430, 120, 500, 200);
        add(imageInformationScrollPane);

        imagesPanel = new JPanel();
        imagesPanel.setSize(1024, 300);
        imagesPanel.setBackground(new Color(94, 168, 200));

        final JScrollPane scrollPane = new JScrollPane(imagesPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 350, 1024, 150);
        add(scrollPane);

    }

    void loadSortedImages() {

        final int numberOfFilteredImages = DBManager.getFilteredImagesStats().size();

        imagesPanel.removeAll();

        if (numberOfFilteredImages == 0) {

            imageLabels = new JLabel[3];
            for (int i = 0; i < 3; i++) {
                imageLabels[i] = new JLabel();
                imageLabels[i].setMinimumSize(new Dimension(150, 150));
                imageLabels[i].setSize(150, 150);
                imagesPanel.add(imageLabels[i]);
            }

        } else {
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

                            for (int j = 0; j < imageLabels.length; j++) {
                                if (src == imageLabels[j]) {
                                    srcLabelIndex = j;
                                    break;
                                }
                            }

                            final Timestamp imgKey = DBManager.getFilteredImagesStats().get(srcLabelIndex).getUploadTimeStamp();
                            currentlySelectedImageStatsData = DBManager.getFilteredImagesStats().get(srcLabelIndex);
                            currentlySelectedImageData = DBManager.getSavedImagesDataLookup().get(imgKey);

                            String userInformation = String.format("Information about selected image:\nFile name: %s\nDimensions: %d x %d pixels\n" +
                                            "Category: %s\nLocation: %s\nImage score: %d\nAverage image score: %.2f from %d vote(s))\nView count: %d\nName of uploader: %s\nDate of upload: %s\n",
                                    currentlySelectedImageData.getImageName(), currentlySelectedImageStatsData.getImageWidth(),
                                    currentlySelectedImageStatsData.getImageHeight(), currentlySelectedImageStatsData.getImageCategory(), currentlySelectedImageStatsData.getImageLocation(),
                                    currentlySelectedImageStatsData.getScore(), currentlySelectedImageStatsData.getAvgScore(), currentlySelectedImageStatsData.getNumberOfAllVotes(),
                                    currentlySelectedImageStatsData.getSeenTimes(), currentlySelectedImageData.getUploaderUserName(), currentlySelectedImageData.getUploadTimeStamp().toLocalDateTime().format(dtFormat));

                            imageInformationTextArea.setText(userInformation);

                            if (mouseEvent.getClickCount() > 1) {

                                if (zoomedImageFrame != null) {
                                    zoomedImageFrame.setVisible(false);
                                    zoomedImageFrame.dispose();
                                    zoomedImageFrame = null;
                                }

                                zoomedImageFrame = new JFrame("Zoomed image view");
                                zoomedImageFrame.setSize(800, 600);
                                Image scaledImage = new ImageIcon(DBManager.getSavedImagesDataLookup().get(imgKey).getImageData()).getImage().getScaledInstance(800, 600, java.awt.Image.SCALE_SMOOTH);
                                JLabel zoomedImage = new JLabel();
                                zoomedImage.setIcon(new ImageIcon(scaledImage));
                                zoomedImageFrame.getContentPane().add(zoomedImage);
                                zoomedImageFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                zoomedImageFrame.setLocationRelativeTo(DataStatisticsPanel.this);
                                zoomedImageFrame.setVisible(true);
                                currentlySelectedImageStatsData.setSeenTimes(currentlySelectedImageStatsData.getSeenTimes() + 1);
                                userInformation = String.format("Information about selected image:\nFile name: %s\nDimensions: %d x %d pixels\n" +
                                                "Category: %s\nLocation: %s\nImage score: %d\nAverage image score: %.2f from %d vote(s))\nView count: %d\nName of uploader: %s\nDate of upload: %s\n",
                                        currentlySelectedImageData.getImageName(), currentlySelectedImageStatsData.getImageWidth(),
                                        currentlySelectedImageStatsData.getImageHeight(), currentlySelectedImageStatsData.getImageCategory(), currentlySelectedImageStatsData.getImageLocation(),
                                        currentlySelectedImageStatsData.getScore(), currentlySelectedImageStatsData.getAvgScore(), currentlySelectedImageStatsData.getNumberOfAllVotes(),
                                        currentlySelectedImageStatsData.getSeenTimes(), currentlySelectedImageData.getUploaderUserName(), currentlySelectedImageData.getUploadTimeStamp().toLocalDateTime().format(dtFormat));
                                imageInformationTextArea.setText(userInformation);
                                DBManager.dbUpdateExistingImageStatsData(currentlySelectedImageStatsData, "seen_times");

                            }
                        }
                    }

                });

                imageLabels[i].setIcon(new ImageIcon(DBManager.getSavedScaledImages().get(DBManager.getFilteredImagesStats().get(i).getUploadTimeStamp()).getScaledMiniImage()));
                imageLabels[i].setToolTipText(getImageInformationForSortOrder(DBManager.getFilteredImagesStats().get(i), prevSortOrder));
                imagesPanel.add(imageLabels[i]);
            }

            imageLabels[0].setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
            previousBorderImage = imageLabels[0];
            final Timestamp imgKey = DBManager.getFilteredImagesStats().get(0).getUploadTimeStamp();

            if (DBManager.getSavedImagesDataLookup().containsKey(imgKey)) {
                currentlySelectedImageStatsData = DBManager.getFilteredImagesStats().get(0);
                currentlySelectedImageData = DBManager.getSavedImagesDataLookup().get(imgKey);

                final String userInformation = String.format("Information about selected image:\nFile name: %s\nDimensions: %d x %d pixels\n" +
                                "Category: %s\nLocation: %s\nImage score: %d\nAverage image score: %.2f from %d vote(s))\nView count: %d\nName of uploader: %s\nDate of upload: %s\n",
                        currentlySelectedImageData.getImageName(), currentlySelectedImageStatsData.getImageWidth(),
                        currentlySelectedImageStatsData.getImageHeight(), currentlySelectedImageStatsData.getImageCategory(), currentlySelectedImageStatsData.getImageLocation(),
                        currentlySelectedImageStatsData.getScore(), currentlySelectedImageStatsData.getAvgScore(), currentlySelectedImageStatsData.getNumberOfAllVotes(),
                        currentlySelectedImageStatsData.getSeenTimes(), currentlySelectedImageData.getUploaderUserName(), currentlySelectedImageData.getUploadTimeStamp().toLocalDateTime().format(dtFormat));

                imageInformationTextArea.setText(userInformation);
            }
        }

        revalidate();
        repaint();
    }

    void sortImages(final ImageSortOrder sortCategory) {

        DBManager.retrieveSavedImageStatsDataForSpecifiedFilters(sortCategory, selectedImageCategory, selectedImageLocation);

    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        if (comboBoxCategory.equals(itemEvent.getSource())) {

            final String currentlySelectedImageCategory = itemEvent.getItem().toString();
            if (!selectedImageCategory.equalsIgnoreCase(currentlySelectedImageCategory)) {
                selectedImageCategory = currentlySelectedImageCategory;
                sortImagesBySpecifiedFiltersButton.setEnabled(true);
            }

        } else if (comboBoxLocation.equals(itemEvent.getSource())) {

            final String currentlySelectedImageLocation = itemEvent.getItem().toString();
            if (!selectedImageLocation.equalsIgnoreCase(currentlySelectedImageLocation)) {
                selectedImageLocation = currentlySelectedImageLocation;
                sortImagesBySpecifiedFiltersButton.setEnabled(true);
            }

        }
    }
}
