package net.fotoalbum;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static net.fotoalbum.ImageSortOrder.UPLOADTIME_ASC;

class DBManager {

    private static final ArrayList<UserData> registeredUserData = new ArrayList<>();
    private static final HashMap<String, UserData> registeredUsersLookup = new HashMap<>();
    private static final ArrayList<UserStatisticsData> savedUserDataStats = new ArrayList<>();
    private static final HashMap<String, UserStatisticsData> savedUserDataStatsLookup = new HashMap<>();
    private static final ArrayList<ImageData> savedImagesData = new ArrayList<>();
    private static final HashMap<Timestamp, ImageData> savedImagesDataLookup = new HashMap<>();
    private static final ArrayList<ImageStatisticsData> filteredImagesStats = new ArrayList<>();
    private static final ArrayList<ImageStatisticsData> savedImagesStats = new ArrayList<>();
    private static final HashMap<Timestamp, ImageStatisticsData> savedImagesStatsLookup = new HashMap<>();
    private static final HashMap<Timestamp, ScaledImageData> savedScaledImages = new HashMap<>();
    private static final ArrayList<UserVoteAction> savedUserVoteActions = new ArrayList<>();
    private static final HashMap<String, HashMap<Timestamp, UserVoteAction>> savedUserVoteActionsLookup = new HashMap<>();
    private static final ArrayList<UserActivity> savedUserActivities = new ArrayList<>();
    private static final HashMap<Timestamp, UserActivity> savedUserActivitiesLookup = new HashMap<>();
    private static final ArrayList<String> availableCategories = new ArrayList<>();
    private static final ArrayList<ImageCategory> availableCategoriesData = new ArrayList<>();
    private static final HashMap<String, ImageCategory> availableCategoriesLookUp = new HashMap<>();
    private static final ArrayList<String> availableLocations = new ArrayList<>();
    private static final ArrayList<ImageLocation> availableLocationsData = new ArrayList<>();
    private static final HashMap<String, ImageLocation> availableLocationsLookUp = new HashMap<>();
    private static final ArrayList<String> adminUserNames = new ArrayList<>();
    private static final HashSet<Timestamp> generatedRandomTimestamps = new HashSet<>();
    private static final SecureRandom randomEngine = new SecureRandom();
    private static Connection dbConnection;
    private static UserData loggedInUserData;
    private static ImageSortOrder prevFilterSortOrder = UPLOADTIME_ASC;
    private static String prevFilterImageCategory = "All";
    private static String prevFilterImageLocation = "All";

    static UserData getLoggedInUserData() {
        return loggedInUserData;
    }

    static void setLoggedInUserData(final UserData userData) {
        loggedInUserData = userData;
    }

    private static HashMap<String, String> parseOracleDatabaseConnectionRelatedSettings(final String filePath) {

        HashMap<String, String> dbConnectionSettings = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(filePath).toAbsolutePath().toFile()))) {

            String line;

            while ((line = br.readLine()) != null) {

                final String trimmedLine = line.trim();

                String[] parts = trimmedLine.split("=", 2);
                parts[0] = parts[0].trim().toLowerCase();
                parts[1] = parts[1].trim();
                if ('"' == parts[1].charAt(0)) {
                    parts[1] = parts[1].substring(1, parts[1].lastIndexOf('"'));
                }

                dbConnectionSettings.put(parts[0], parts[1]);

            }

        } catch (IOException ioe) {
            System.err.printf("IOException occurred!\nException message: %s\n", ioe.getMessage());
        }

        return dbConnectionSettings;
    }

    static boolean connectToOracleDB() {

        final HashMap<String, String> dbConnectionSettings = parseOracleDatabaseConnectionRelatedSettings("sql/dbconfig.ini");

        if (!dbConnectionSettings.containsKey("connection_address")) {
            System.err.println("Error: 'connection_address' key - value pair data does not exist in dbConnectionSettings HashMap<String, String>!\n");
            return false;
        }

        if (!dbConnectionSettings.containsKey("username")) {
            System.err.println("Error: 'username' key - value pair data does not exist in dbConnectionSettings HashMap<String, String>!\n");
            return false;
        }

        if (!dbConnectionSettings.containsKey("password")) {
            System.err.println("Error: 'password' key - value pair data does not exist in dbConnectionSettings HashMap<String, String>!\n");
            return false;
        }

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            dbConnection = DriverManager.getConnection(dbConnectionSettings.get("connection_address"), dbConnectionSettings.get("username"), dbConnectionSettings.get("password"));

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return false;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Database connection error", JOptionPane.ERROR_MESSAGE);
        }

        System.out.println("Dropping existing fotoalbum database related tables...");
        dbDropExistingFotoAlbumTables();
        System.out.println("Creating the necessary tables for the fotoalbum database...");
        dbCreateFotoAlbumTables();
//        System.out.println("Retrieving registered user accounts related data from the fotoalbum_users table...");
//        retrieveAllRegisteredUsersData();
//        System.out.println("Retrieving registered user accounts related statistics data from the fotoalbum_userstats table...");
//        retrieveAllRegisteredUserStatsData();
//        System.out.println("Retrieving saved images related from the fotoalbum_images table...");
//        dbRetrieveAllSavedImagesData();
//        System.out.println("Retrieving saved images related statistics data in sorted order from the fotoalbum_imgstats table...");
//        dbRetrieveAllSavedImageStatsData(ImageSortOrder.UPLOADTIME_ASC);
        System.out.println("Loading sample user accounts related data...");
        loadSampleUserAccountsDataFromSpecifiedFile("sql/users_data.csv");
        for (final UserData u : registeredUserData) if (u.getUserLevel() != 0) adminUserNames.add(u.getUserName());
        System.out.println("Loading default image categories...");
        loadDefaultCategoriesDataFromSpecifiedFile("sql/categories.lst");
        System.out.println("Loading default image locations...");
        loadDefaultLocationsDataFromSpecifiedFile("sql/locations.lst");
        System.out.println("Loading sample images from user-defined folder 'images'...");
        loadSampleImagesFromSpecifiedFile("images");
        System.out.println("Generating various random data for the sample user accounts and images...");
        generateRandomDefaultUserAndImageRelatedData();
        System.out.println("Saving the sample user accounts into the fotoalbum_users table...");
        dbSaveUserAccountsData();
        System.out.println("Saving the predefined default image categories into the fotoalbum_categories table...");
        dbSaveDefaultCategoriesData();
        System.out.println("Saving the predefined default image locations into the fotoalbum_locations table...");
        dbSaveDefaultLocationsData();
        System.out.println("Saving the sample images related data into the fotoalbum_images table...");
        saveSampleImagesToDB();
        System.out.println("Saving registered user actions into the fotoalbum_useractions table...");
        dbSaveAllUserActions();
        System.out.println("Retrieving registered user accounts related data from the fotoalbum_users table...");
        retrieveAllRegisteredUsersData();
        System.out.println("Retrieving registered user accounts related statistics data from the fotoalbum_userstats table...");
        retrieveAllRegisteredUserStatsData();
        System.out.println("Retrieving saved images related from the fotoalbum_images table...");
        dbRetrieveAllSavedImagesData();
        System.out.println("Retrieving saved images related statistics data in sorted order from the fotoalbum_imgstats table...");
        dbRetrieveAllSavedImageStatsData();
        System.out.println("Retrieving previously saved user vote actions related data from the fotoalbum_uservotes table...");
        dbRetrieveAllUserVoteActions();
        System.out.println("Retrieving previously saved user actions related data from the fotoalbum_useractions table...");
        dbRetrieveAllUserActivityData();

        return true;
    }

    private static void dbCreateFotoAlbumTables() {

        try (PreparedStatement stm = dbConnection.prepareStatement("CREATE TABLE fotoalbum_users (fullname NVARCHAR2(50) NOT NULL, username NVARCHAR2(50) NOT NULL, " +
                "email NVARCHAR2(50) NOT NULL, userpass NUMBER NOT NULL, address NVARCHAR2(100) NOT NULL, age NUMBER NOT NULL, " +
                "registration_time TIMESTAMP DEFAULT current_timestamp NOT NULL, user_level NUMBER(1) NOT NULL, " +
                "CONSTRAINT fupk PRIMARY KEY (username))")) {
            stm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("CREATE TABLE fotoalbum_images (uploadtime TIMESTAMP NOT NULL, filename NVARCHAR2(1024) NOT NULL, " +
                "uploader NVARCHAR2(50) NOT NULL, imagedata blob, CONSTRAINT fipk PRIMARY KEY (uploadtime))")) {
            stm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("CREATE TABLE fotoalbum_uservotes (username NVARCHAR2(50) NOT NULL, uploadtime TIMESTAMP NOT NULL, " +
                "score NUMBER(1) NOT NULL, votetime TIMESTAMP DEFAULT current_timestamp NOT NULL, CONSTRAINT fauv PRIMARY KEY (username, uploadtime))")) {
            stm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("CREATE TABLE fotoalbum_imgstats (uploadtime TIMESTAMP NOT NULL, category NVARCHAR2(100) NOT NULL, location NVARCHAR2(1024) NOT NULL," +
                "image_width NUMBER NOT NULL, image_height NUMBER NOT NULL, score NUMBER NOT NULL, avgscore FLOAT NOT NULL, score1_votes NUMBER NOT NULL, score2_votes NUMBER NOT NULL, score3_votes NUMBER NOT NULL, " +
                "score4_votes NUMBER NOT NULL, score5_votes NUMBER NOT NULL, seen_times NUMBER NOT NULL, lastvotescore NUMBER NOT NULL, CONSTRAINT fisk PRIMARY KEY (uploadtime))")) {
            stm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("CREATE TABLE fotoalbum_userstats (username NVARCHAR2(50) NOT NULL, lastlogintime TIMESTAMP NOT NULL, " +
                "lastvotetime TIMESTAMP NOT NULL, lastvotescore NUMBER NOT NULL, score1_votes NUMBER NOT NULL, score2_votes NUMBER NOT NULL, " +
                "score3_votes NUMBER NOT NULL, score4_votes NUMBER NOT NULL, score5_votes NUMBER NOT NULL, number_of_logins NUMBER NOT NULL, " +
                "imguploadnumber NUMBER NOT NULL, imgdelnumber NUMBER NOT NULL, userdelnumber NUMBER NOT NULL, adminpromonumber NUMBER NOT NULL, " +
                "admindemotenumber NUMBER NOT NULL, CONSTRAINT fustk PRIMARY KEY (username))")) {
            stm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("CREATE TABLE fotoalbum_useractions (username NVARCHAR2(50) NOT NULL, logintime TIMESTAMP NOT NULL, " +
                "action NVARCHAR2(200) NOT NULL, actiontime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, CONSTRAINT fuapk PRIMARY KEY (actiontime))")) {
            stm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("CREATE TABLE fotoalbum_categories (creationtime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                "username NVARCHAR2(50) NOT NULL, category NVARCHAR2(100) NOT NULL," +
                "CONSTRAINT fc_pk PRIMARY KEY (category))")) {
            stm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("CREATE TABLE fotoalbum_locations (creationtime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                "username NVARCHAR2(50) NOT NULL, location NVARCHAR2(1024) NOT NULL, " +
                "CONSTRAINT fl_pk PRIMARY KEY (location))")) {
            stm.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void dbDropExistingFotoAlbumTables() {

        try (PreparedStatement stm = dbConnection.prepareStatement("DROP TABLE fotoalbum_categories")) {
            stm.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("DROP TABLE fotoalbum_locations")) {
            stm.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("DROP TABLE fotoalbum_uservotes")) {
            stm.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("DROP TABLE fotoalbum_useractions")) {
            stm.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("DROP TABLE fotoalbum_imgstats")) {
            stm.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("DROP TABLE fotoalbum_userstats")) {
            stm.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("DROP TABLE fotoalbum_images")) {
            stm.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stm = dbConnection.prepareStatement("DROP TABLE fotoalbum_users")) {
            stm.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

//    private static boolean dbCheckIfFotoAlbumDatabaseTablesHaveAlreadyBeenCreated() {
//
//        boolean result = false;
//
//        try (Statement stmt = dbConnection.createStatement()) {
//
//            ResultSet rs = stmt.executeQuery("select count(*) from ALL_OBJECTS where OBJECT_TYPE in ('TABLE','VIEW') and OBJECT_NAME = 'fotoalbum_users'");
//             if (rs.next()) {
//                 result = rs.getInt("count(*)") != 0;
//             }
//
//            rs.close();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//
//        }
//
//        try (Statement stmt = dbConnection.createStatement()) {
//
//            ResultSet rs = stmt.executeQuery("select count(*) from ALL_OBJECTS where OBJECT_TYPE in ('TABLE','VIEW') and OBJECT_NAME = 'fotoalbum_images'");
//            if (rs.next()) {
//                result =  rs.getInt("count(*)") != 0;
//            }
//            rs.close();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//
//        }
//
//        try (Statement stmt = dbConnection.createStatement()) {
//
//            ResultSet rs = stmt.executeQuery("select count(*) from ALL_OBJECTS where OBJECT_TYPE in ('TABLE','VIEW') and OBJECT_NAME = 'fotoalbum_uservotes'");
//            if (rs.next()) {
//                result =  rs.getInt("count(*)") != 0;
//            }
//            rs.close();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//
//        }
//
//        return result;
//
//    }

    private static void loadDefaultCategoriesDataFromSpecifiedFile(final String filePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(filePath).toAbsolutePath().toFile()))) {

            String line;

            final Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            final long currentTime = now.getTime();

            final int adminUserNamesSize = adminUserNames.size();

            while ((line = br.readLine()) != null) {

                do {

                    now.setTime(currentTime + randomEngine.nextInt(Integer.MAX_VALUE));

                } while (generatedRandomTimestamps.contains(now));

                generatedRandomTimestamps.add(now);

                final String randomlySelectedUserName = adminUserNames.get(randomEngine.nextInt(adminUserNamesSize));

                addNewCategory(now, randomlySelectedUserName, line);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dbSaveDefaultCategoriesData() {

        for (final ImageCategory imgCategory : availableCategoriesData) {
            dbInsertNewCategory(imgCategory);
        }
    }

    private static void parsingAndProcessingDefaultLocationsData(final String srcFilePath, final String dstFilePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(srcFilePath).toAbsolutePath().toFile()))) {

            BufferedWriter brOutput = new BufferedWriter(new FileWriter(Paths.get(dstFilePath).toAbsolutePath().toFile()));
            String line;

            while ((line = br.readLine()) != null) {

                final String location = line.trim();
                if (!location.contains(",")) {
                    brOutput.write(location + '\n');
                    // brOutput.write('\n');
                } else {
                    String[] parts = location.split(",");
                    if (parts.length >= 3) {
                        if (parts[0].length() > 2)
                            parts[0] = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
                        if (parts[1].length() > 2)
                            parts[1] = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
                        if (parts[2].length() > 2)
                            parts[2] = parts[2].substring(0, 1).toUpperCase() + parts[2].substring(1);
                        final String newLine = String.format("%s, %s, %s\n", parts[0], parts[1], parts[2]);
                        brOutput.write(newLine);
                    }
                }

            }

            brOutput.flush();
            brOutput.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void loadDefaultLocationsDataFromSpecifiedFile(final String filePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(filePath).toAbsolutePath().toFile()))) {

            String line;

            final Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            final long currentTime = now.getTime();

            HashSet<Timestamp> generatedRandomTimestamps = new HashSet<>();

            final int adminUserNamesSize = adminUserNames.size();

            while ((line = br.readLine()) != null) {

                final String location = line.trim();

                do {

                    now.setTime(currentTime + randomEngine.nextInt(Integer.MAX_VALUE));

                } while (generatedRandomTimestamps.contains(now));

                generatedRandomTimestamps.add(now);

                final String randomlySelectedUserName = adminUserNames.get(randomEngine.nextInt(adminUserNamesSize));

                addNewLocation(now, randomlySelectedUserName, location);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void dbSaveDefaultLocationsData() {

        for (final ImageLocation imgLocation : availableLocationsData) {
            insertNewLocationIntoDB(imgLocation);
        }
    }

    private static void loadSampleUserAccountsDataFromSpecifiedFile(final String csvFilePath) {

        CSVReader csvReader = new CSVReader(csvFilePath);
        ArrayList<String[]> userData = csvReader.getCSVData();


        for (String[] anUserData : userData) {

            final String userName = anUserData[1];

            if (!registeredUsersLookup.containsKey(userName)) {
                UserData user = new UserData(anUserData[0], anUserData[1], anUserData[2], anUserData[3].hashCode(), anUserData[4],
                        Integer.parseInt(anUserData[5]), generateRandomTimestamp(), Integer.parseInt(anUserData[7]), false);
                registeredUserData.add(user);
                registeredUsersLookup.put(userName, user);
            }

            if (!savedUserDataStatsLookup.containsKey(userName)) {
                UserStatisticsData userStatsData = new UserStatisticsData(anUserData[1], generateRandomTimestamp(), generateRandomTimestamp(), 0,
                        0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0);
                savedUserDataStats.add(userStatsData);
                savedUserDataStatsLookup.put(anUserData[1], userStatsData);
            }
        }

    }

    private static void dbSaveUserAccountsData() {

        for (final UserData userData : registeredUserData) {
            if (!userData.isSavedToDB()) {
                insertUserRegistrationData(userData);
                userData.setSavedToDB(true);
                insertNewUserStatsDataIntoDatabase(savedUserDataStatsLookup.get(userData.getUserName()));
            }
        }

    }

    private static void dbSaveAllUserActions() {

        for (final UserActivity userActivity : savedUserActivities)
            dbInsertNewUserActivityData(userActivity);

    }

    private static void loadSampleImagesFromSpecifiedFile(final String folderPath) {

        final File folderFile = new File(Paths.get(folderPath).toAbsolutePath().toUri());
        if (!folderFile.isDirectory()) {
            System.out.println("folderPath is not a valid directory!\n");
            return;
        }

        savedImagesData.clear();
        savedImagesDataLookup.clear();
        savedImagesStats.clear();
        savedImagesStatsLookup.clear();
        savedScaledImages.clear();

        final File[] files = folderFile.listFiles();

        final String[] acceptedImageExtensions = {".png", ".gif", ".jpeg", ".jpg"};

        final int numberOfUsers = registeredUserData.size();

        final int availableCategoriesSize = availableCategories.size();
        final int availableLocationsSize = availableLocations.size();

        long hourOffset = 1;

        if (files != null) {

            for (final File file : files) {

                for (final String ext : acceptedImageExtensions) {

                    if (file.getName().toLowerCase().endsWith(ext)) {

                        final String imageName = file.getName();
                        String imagePath;
                        BufferedImage bufferedImage;
                        try {
                            imagePath = file.getCanonicalPath();
                            bufferedImage = ImageIO.read(new File(imagePath));
                        } catch (IOException e) {
                            break;
                        }
                        boolean imagesEqual = false;
                        for (final ImageData imgData : savedImagesData) {
                            if (ImageUploadPanel.bufferedImagesEqual(bufferedImage, imgData.getImageData())) {
                                imagesEqual = true;
                                break;
                            }
                        }

                        if (imagesEqual) continue;

                        Timestamp now = Timestamp.valueOf(LocalDateTime.now().minusHours(hourOffset));
                        hourOffset++;

                        final ImageData imageData = new ImageData(now, imageName, imagePath, registeredUserData.get(randomEngine.nextInt(numberOfUsers)).getUserName(), bufferedImage);
                        savedImagesData.add(imageData);
                        savedImagesDataLookup.put(now, imageData);

                        final ImageStatisticsData imageStatsData = new ImageStatisticsData(now, availableCategories.get(randomEngine.nextInt(availableCategoriesSize)),
                                availableLocations.get(randomEngine.nextInt(availableLocationsSize)), bufferedImage.getWidth(), bufferedImage.getHeight(), 0, 0.f,
                                0, 0, 0, 0, 0, 0, 0);
                        savedImagesStats.add(imageStatsData);
                        savedImagesStatsLookup.put(now, imageStatsData);

                        Image scaledMiniImage = bufferedImage.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
                        Image scaledMediumImage = bufferedImage.getScaledInstance(300, 300, java.awt.Image.SCALE_SMOOTH);
                        savedScaledImages.put(now, new ScaledImageData(scaledMiniImage, scaledMediumImage));
                        break;
                    }
                }

            }

        }


    }

    private static void saveSampleImagesToDB() {

        for (ImageData img : savedImagesData) {
            dbInsertNewImage(img);
            dbInsertNewImageStatsData(savedImagesStatsLookup.get(img.getUploadTimeStamp()));
        }
    }

    private static void generateRandomDefaultUserAndImageRelatedData() {

        final DateTimeFormatter dtFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (ImageData aSavedImagesData : savedImagesData) {
            final UserData userData = registeredUserData.get(randomEngine.nextInt(registeredUserData.size()));
            final String userName = userData.getUserName();
            final UserStatisticsData userStatsData = savedUserDataStatsLookup.get(userName);
            final long imageUploads = userStatsData.getNumberOfImageUploads() + 1;
            userStatsData.setNumberOfImageUploads(imageUploads);
            Timestamp loginTime = generateRandomTimestamp();
            final Timestamp uploadTime = aSavedImagesData.getUploadTimeStamp();
            final String uploadImageActionMessage = String.format("%s %s uploaded image '%s' at %s",
                    getAppropriateUserLevelInformation(userData.getUserLevel()), userName, aSavedImagesData.getImageName(),
                    uploadTime.toLocalDateTime().format(dtFormat));
            UserActivity userActivity = new UserActivity(loginTime, userName, uploadImageActionMessage, uploadTime);
            if (!savedUserActivitiesLookup.containsKey(uploadTime)) {
                savedUserActivitiesLookup.put(uploadTime, userActivity);
                savedUserActivities.add(userActivity);
            }

            final int randomNumberOfVoters = generateRandomNumberInSpecifiedIntervalInclusive(1, registeredUserData.size() / 4);

            final long randomNumberOfTimesSeen = generateRandomNumberInSpecifiedIntervalInclusive(randomNumberOfVoters, registeredUserData.size());

            final ImageStatisticsData imgStatsData = savedImagesStatsLookup.get(uploadTime);
            imgStatsData.setSeenTimes(randomNumberOfTimesSeen);

            Collections.shuffle(registeredUserData);

            for (int j = 0; j < randomNumberOfVoters; j++) {
                loginTime = generateRandomTimestamp();
                Timestamp actionTime = new Timestamp(loginTime.getTime());
                actionTime.setTime(actionTime.getTime() + randomEngine.nextInt(1_000_000));
                final int randomVote = 1 + randomEngine.nextInt(5);
                final Timestamp voteTime = generateRandomTimestamp();
                final String name = registeredUserData.get(j).getUserName();
                final UserStatisticsData user = savedUserDataStatsLookup.get(name);
                user.setLastVoteTime(voteTime);
                user.setLastVoteScore(randomVote);
                user.increaseCorrectGroupOfNumberOfVotes(randomVote);
                imgStatsData.increaseCorrectGroupOfNumberOfVotes(randomVote);
                imgStatsData.setLastVoteScore(randomVote);
                dbInsertNewImageVoteData(name, uploadTime, randomVote, actionTime);
                final String voteImageActionMessage = String.format("%s %s gave score '%d' for image '%s' at %s",
                        getAppropriateUserLevelInformation(registeredUserData.get(j).getUserLevel()), name, randomVote, aSavedImagesData.getImageName(),
                        voteTime.toLocalDateTime().format(dtFormat));
                userActivity = new UserActivity(loginTime, name, voteImageActionMessage, actionTime);
                if (!savedUserActivitiesLookup.containsKey(actionTime)) {
                    savedUserActivitiesLookup.put(actionTime, userActivity);
                    savedUserActivities.add(userActivity);
                }

            }
        }
    }

    private static void retrieveAllRegisteredUsersData() {

        try (Statement stmt = dbConnection.createStatement()) {

            ResultSet rs = stmt.executeQuery("select * from fotoalbum_users");

            while (rs.next()) {
                String fullName = rs.getString("fullname");
                String userName = rs.getString("username");
                String emailAddress = rs.getString("email");
                int userPassword = rs.getInt("userpass");
                String userAddress = rs.getString("address");
                int userAge = rs.getInt("age");
                Timestamp userRegistrationTime = rs.getTimestamp("registration_time");
                int userLevel = rs.getInt("user_level");
                if (!registeredUsersLookup.containsKey(userName)) {
                    final UserData userData = new UserData(fullName, userName, emailAddress, userPassword, userAddress, userAge, userRegistrationTime, userLevel, true);
                    registeredUserData.add(userData);
                    registeredUsersLookup.put(userName, userData);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    private static void retrieveAllRegisteredUserStatsData() {

        try (Statement stmt = dbConnection.createStatement()) {

            ResultSet rs = stmt.executeQuery("select * from fotoalbum_userstats");

            while (rs.next()) {
                String userName = rs.getString("username");
                Timestamp lastLoginTime = rs.getTimestamp("lastlogintime");
                Timestamp lastVoteTime = rs.getTimestamp("lastvotetime");
                int lastVoteScore = rs.getInt("lastvotescore");
                long score1Votes = rs.getLong("score1_votes");
                long score2Votes = rs.getLong("score2_votes");
                long score3Votes = rs.getLong("score3_votes");
                long score4Votes = rs.getLong("score4_votes");
                long score5Votes = rs.getLong("score5_votes");
                long numberOfLogins = rs.getLong("number_of_logins");
                long imageUploadNumber = rs.getLong("imguploadnumber");
                long imageDeleteNumber = rs.getLong("imgdelnumber");
                long userdelnumber = rs.getLong("userdelnumber");
                long adminPromoNumber = rs.getLong("adminpromonumber");
                long adminDemoteNumber = rs.getLong("admindemotenumber");

                if (!savedUserDataStatsLookup.containsKey(userName)) {
                    final UserStatisticsData userStats = new UserStatisticsData(userName, lastLoginTime, lastVoteTime, lastVoteScore, score1Votes, score2Votes, score3Votes, score4Votes,
                            score5Votes, numberOfLogins, imageUploadNumber, imageDeleteNumber, userdelnumber, adminPromoNumber, adminDemoteNumber);
                    savedUserDataStats.add(userStats);
                    savedUserDataStatsLookup.put(userName, userStats);
                }
            }

            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    private static Timestamp generateRandomTimestamp() {

        final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        final long currentTime = now.getTime();

        do {

            now.setTime(currentTime + randomEngine.nextInt(Integer.MAX_VALUE));

        } while (generatedRandomTimestamps.contains(now));

        generatedRandomTimestamps.add(now);

        return now;
    }

    private static int generateRandomNumberInSpecifiedIntervalInclusive(final int min, final int max) {
        return min + randomEngine.nextInt(max - min + 1); // [1, 100]
    }

    static void retrieveAllSavedCategoriesData() {

        try (Statement stmt = dbConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery("select * from fotoalbum_categories");
            while (rs.next()) {
                final Timestamp creationTime = rs.getTimestamp("creationtime");
                final String userName = rs.getString("username");
                String imageCategory = rs.getString("category");
                addNewCategory(creationTime, userName, imageCategory);
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }
    }

    static void retrieveAllSavedLocationsData() {

        try (Statement stmt = dbConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery("select * from fotoalbum_locations");
            while (rs.next()) {
                final Timestamp creationTime = rs.getTimestamp("creationtime");
                final String userName = rs.getString("username");
                String imageLocation = rs.getString("location");
                addNewLocation(creationTime, userName, imageLocation);

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }
    }

    private static void dbRetrieveAllSavedImagesData() {

        savedImagesData.clear();
        savedImagesDataLookup.clear();

        try (Statement stmt = dbConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery("select * from fotoalbum_images");
            while (rs.next()) {
                final Timestamp uploadedTime = rs.getTimestamp("uploadtime");
                final String imageName = rs.getString("filename");
                final String uploadedUserName = rs.getString("uploader");
                final Blob imageBlob = rs.getBlob("imagedata");

                final InputStream is = imageBlob.getBinaryStream();
                BufferedImage bufferedImage = null;
                try {
                    bufferedImage = ImageIO.read(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final ImageData imageData = new ImageData(uploadedTime, imageName, "", uploadedUserName, bufferedImage);

                savedImagesData.add(imageData);
                savedImagesDataLookup.put(uploadedTime, imageData);

                if (!savedScaledImages.containsKey(uploadedTime)) {
                    Image scaledMiniImage = bufferedImage.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
                    Image scaledMediumImage = bufferedImage.getScaledInstance(300, 300, java.awt.Image.SCALE_SMOOTH);
                    savedScaledImages.put(uploadedTime, new ScaledImageData(scaledMiniImage, scaledMediumImage));
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }
    }

    private static void dbRetrieveAllSavedImageStatsData() {

        savedImagesStats.clear();
        savedImagesStatsLookup.clear();

        try (Statement stmt = dbConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM fotoalbum_imgstats"));
            while (rs.next()) {
                Timestamp uploadTime = rs.getTimestamp("uploadtime");
                String imageCategory = rs.getString("category");
                String imageLocation = rs.getString("location");
                int imageWidth = rs.getInt("image_width");
                int imageHeight = rs.getInt("image_height");
                int imageScore = rs.getInt("score");
                float avgScore = rs.getFloat("avgscore");
                long score1Votes = rs.getLong("score1_votes");
                long score2Votes = rs.getLong("score2_votes");
                long score3Votes = rs.getLong("score3_votes");
                long score4Votes = rs.getLong("score4_votes");
                long score5Votes = rs.getLong("score5_votes");
                long seenTimes = rs.getLong("seen_times");
                int lastVoteScore = rs.getInt("lastvotescore");
                ImageStatisticsData imgStatsData = new ImageStatisticsData(uploadTime, imageCategory, imageLocation, imageWidth, imageHeight, imageScore, avgScore,
                        score1Votes, score2Votes, score3Votes, score4Votes, score5Votes, seenTimes, lastVoteScore);
                savedImagesStats.add(imgStatsData);
                savedImagesStatsLookup.put(uploadTime, imgStatsData);
            }

            rs.close();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    static void retrieveSavedImageStatsDataForSpecifiedFilters(final ImageSortOrder imageSortOrder, final String filterImageCategory, final String filterImageLocation) {

//        if (filteredImagesStats.size() == savedImagesStats.size()
//                && imageSortOrder.equals(prevFilterSortOrder)
//                && filterImageCategory.equalsIgnoreCase(prevFilterImageCategory)
//                && filterImageLocation.equalsIgnoreCase(prevFilterImageLocation)) return;

        System.out.printf("Filtering ImageStatisticsData objects for specified input filters: imageSortOrder=%s, filterImageCategory=%s, filterImageLocation=%s\n",
                displayCorrectSortOrderInformation(imageSortOrder), filterImageCategory, filterImageLocation);

        filteredImagesStats.clear();

        if (filterImageCategory.equals("All") && filterImageLocation.equals("All")) {

            filteredImagesStats.addAll(savedImagesStats);

        } else if (filterImageCategory.equals("All")) {

            for (final ImageStatisticsData imgStatsData : savedImagesStats) {
                if (imgStatsData.getImageLocation().equalsIgnoreCase(filterImageLocation))
                    filteredImagesStats.add(imgStatsData);
            }

        } else if (filterImageLocation.equals("All")) {
            for (final ImageStatisticsData imgStatsData : savedImagesStats) {
                if (imgStatsData.getImageCategory().equalsIgnoreCase(filterImageCategory))
                    filteredImagesStats.add(imgStatsData);
            }

        } else {
            for (final ImageStatisticsData imgStatsData : savedImagesStats) {
                if (imgStatsData.getImageCategory().equalsIgnoreCase(filterImageCategory) && imgStatsData.getImageLocation().equalsIgnoreCase(filterImageLocation))
                    filteredImagesStats.add(imgStatsData);
            }

        }

        switch (imageSortOrder) {

            case UPLOADTIME_ASC:
                Collections.sort(filteredImagesStats, Comparator.comparing(ImageStatisticsData::getUploadTimeStamp));
                break;

            case UPLOADTIME_DESC:
                Collections.sort(filteredImagesStats, Comparator.comparing(ImageStatisticsData::getUploadTimeStamp).reversed());
                break;

            case AVGSCORE_ASC:
                Collections.sort(filteredImagesStats, Comparator.comparing(ImageStatisticsData::getAvgScore));
                break;

            case AVGSCORE_DESC:
                Collections.sort(filteredImagesStats, Comparator.comparing(ImageStatisticsData::getAvgScore).reversed());
                break;

            case VIEWS_ASC:
                Collections.sort(filteredImagesStats, Comparator.comparing(ImageStatisticsData::getSeenTimes));
                break;

            case VIEWS_DESC:
                Collections.sort(filteredImagesStats, Comparator.comparing(ImageStatisticsData::getSeenTimes).reversed());
                break;

            default:
                break;

        }

//        StringBuffer sortCrit = new StringBuffer();
//
//        if (!filterImageCategory.equalsIgnoreCase("All")) {
//            sortCrit.append("WHERE category='").append(filterImageCategory).append('\'');
//            if (!filterImageLocation.equalsIgnoreCase("All"))
//                sortCrit.append(" AND location='").append(filterImageLocation).append('\'');
//
//        } else if (!filterImageLocation.equalsIgnoreCase("All"))
//            sortCrit.append("WHERE location='").append(filterImageLocation).append('\'');
//
//        switch (imageSortOrder) {
//
//            case UPLOADTIME_ASC:
//                sortCrit.append(" ORDER BY uploadtime ASC");
//                break;
//
//            case UPLOADTIME_DESC:
//                sortCrit.append(" ORDER BY uploadtime DESC");
//                break;
//
//            case AVGSCORE_ASC:
//                sortCrit.append(" ORDER BY avgscore ASC");
//                break;
//
//            case AVGSCORE_DESC:
//                sortCrit.append(" ORDER BY avgscore DESC");
//                break;
//
//            case VIEWS_ASC:
//                sortCrit.append(" ORDER BY seen_times ASC");
//                break;
//
//            case VIEWS_DESC:
//                sortCrit.append(" ORDER BY seen_times DESC");
//                break;
//
//            default:
//                sortCrit.append(" ORDER BY uploadtime ASC");
//                break;
//
//        }
//
//        try (Statement stmt = dbConnection.createStatement()) {
//            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM fotoalbum_imgstats %s", sortCrit.toString()));
//            while (rs.next()) {
//                Timestamp uploadTime = rs.getTimestamp("uploadtime");
//                String imageCategory = rs.getString("category");
//                String imageLocation = rs.getString("location");
//                int imageWidth = rs.getInt("image_width");
//                int imageHeight = rs.getInt("image_height");
//                int imageScore = rs.getInt("score");
//                float avgScore = rs.getFloat("avgscore");
//                long score1Votes = rs.getLong("score1_votes");
//                long score2Votes = rs.getLong("score2_votes");
//                long score3Votes = rs.getLong("score3_votes");
//                long score4Votes = rs.getLong("score4_votes");
//                long score5Votes = rs.getLong("score5_votes");
//                long seenTimes = rs.getLong("seen_times");
//                int lastVoteScore = rs.getInt("lastvotescore");
//                ImageStatisticsData imgStatsData = new ImageStatisticsData(uploadTime, imageCategory, imageLocation, imageWidth, imageHeight, imageScore, avgScore,
//                        score1Votes, score2Votes, score3Votes, score4Votes, score5Votes, seenTimes, lastVoteScore);
//                filteredImagesStats.add(imgStatsData);
//            }
//
//            rs.close();
//
//        } catch (SQLException e) {
//
//            e.printStackTrace();
//
//        }

    }

    private static void dbRetrieveAllUserVoteActions() {

        // every user vote is identified by 2 primary keys:
        // username (references a unique UserData object) and uploadtime (references a unique ImageData object)

        try (Statement stmt = dbConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery("select * from fotoalbum_uservotes");
            while (rs.next()) {
                final String userName = rs.getString("username");
                final Timestamp uploadTime = rs.getTimestamp("uploadtime");
                final int score = rs.getInt("score");
                final Timestamp voteTime = rs.getTimestamp("votetime");

                if (!savedUserVoteActionsLookup.containsKey(userName)) {
                    savedUserVoteActionsLookup.put(userName, new HashMap<>());
                }

                if (!savedUserVoteActionsLookup.get(userName).containsKey(uploadTime)) {
                    final UserVoteAction userVoteAction = new UserVoteAction(userName, uploadTime, score, voteTime);
                    savedUserVoteActions.add(userVoteAction);
                    savedUserVoteActionsLookup.get(userName).put(uploadTime, userVoteAction);
                }
            }

            rs.close();

        } catch (SQLException e) {

            e.printStackTrace();

        }
    }

    static void dbInsertNewUserActivityData(final UserActivity userActivity) {

        try (PreparedStatement ps = dbConnection.prepareStatement("insert into fotoalbum_useractions values (?,?,?,?)")) {

            ps.setString(1, userActivity.getUserName());
            ps.setTimestamp(2, userActivity.getLoginTime());
            ps.setString(3, userActivity.getActionMessage());
            ps.setTimestamp(4, userActivity.getActionTime());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void dbRetrieveAllUserActivityData() {

        try (Statement stmt = dbConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery("select * from fotoalbum_useractions");
            while (rs.next()) {
                final String userName = rs.getString("username");
                final Timestamp loginTime = rs.getTimestamp("logintime");
                final String actionMessage = rs.getString("action");
                final Timestamp actionTime = rs.getTimestamp("actiontime");
                if (!savedUserActivitiesLookup.containsKey(actionTime)) {
                    final UserActivity userActivity = new UserActivity(loginTime, userName, actionMessage, actionTime);
                    savedUserActivities.add(userActivity);
                    savedUserActivitiesLookup.put(actionTime, userActivity);
                }
            }

            rs.close();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    static void dbInsertNewImage(final ImageData imageData) {

        final Timestamp key = imageData.getUploadTimeStamp();

        try (PreparedStatement ps = dbConnection.prepareStatement("insert into fotoalbum_images values (?,?,?,?)")) {

            ps.setTimestamp(1, imageData.getUploadTimeStamp());
            ps.setString(2, imageData.getImageName());
            ps.setString(3, imageData.getUploaderUserName());
            FileInputStream fin = new FileInputStream(imageData.getImagePath());
            ps.setBlob(4, fin, fin.available());
            ps.executeUpdate();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

    }

    static void dbInsertNewImageStatsData(ImageStatisticsData imageStatsData) {

        try (PreparedStatement ps = dbConnection.prepareStatement("insert into fotoalbum_imgstats values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {

            ps.setTimestamp(1, imageStatsData.getUploadTimeStamp());
            ps.setString(2, imageStatsData.getImageCategory());
            ps.setString(3, imageStatsData.getImageLocation());
            ps.setInt(4, imageStatsData.getImageWidth());
            ps.setInt(5, imageStatsData.getImageHeight());
            ps.setLong(6, imageStatsData.getScore());
            ps.setFloat(7, imageStatsData.getAvgScore());
            ps.setLong(8, imageStatsData.getNumberOfVotes(1));
            ps.setLong(9, imageStatsData.getNumberOfVotes(2));
            ps.setLong(10, imageStatsData.getNumberOfVotes(3));
            ps.setLong(11, imageStatsData.getNumberOfVotes(4));
            ps.setLong(12, imageStatsData.getNumberOfVotes(5));
            ps.setLong(13, imageStatsData.getSeenTimes());
            ps.setInt(14, imageStatsData.getLastVoteScore());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static int getVoteScoreForSpecifiedUserNameAndImageTimeStamp(final String userName, final Timestamp uploadTime) {

        if (!savedUserVoteActionsLookup.containsKey(userName)) return -1;
        if (!savedUserVoteActionsLookup.get(userName).containsKey(uploadTime)) return -1;
        return savedUserVoteActionsLookup.get(userName).get(uploadTime).getVoteScore();

    }

    static void dbInsertNewImageVoteData(final String userName, final Timestamp uploadTime, final int score, final Timestamp voteTime) {

        try (PreparedStatement ps = dbConnection.prepareStatement("insert into fotoalbum_uservotes values (?,?,?,?)")) {
            ps.setString(1, userName);
            ps.setTimestamp(2, uploadTime);
            ps.setInt(3, score);
            ps.setTimestamp(4, voteTime);
            ps.executeUpdate();

            if (!savedUserVoteActionsLookup.containsKey(userName)) {
                savedUserVoteActionsLookup.put(userName, new HashMap<>());
            }

            if (!savedUserVoteActionsLookup.get(userName).containsKey(uploadTime)) {
                UserVoteAction userVoteAction = new UserVoteAction(userName, uploadTime, score, voteTime);
                savedUserVoteActions.add(userVoteAction);
                savedUserVoteActionsLookup.get(userName).put(uploadTime, userVoteAction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void dbUpdateExistingImageStatsData(final ImageStatisticsData imageStatsData, String... fieldNames) {

        for (final String fieldName : fieldNames) {

            try (PreparedStatement ps = dbConnection.prepareStatement(String.format("update fotoalbum_imgstats set %s=? where uploadtime=?", fieldName))) {

                switch (fieldName) {

                    case "image_width":
                        ps.setInt(1, imageStatsData.getImageWidth());
                        break;

                    case "image_height":
                        ps.setInt(1, imageStatsData.getImageHeight());
                        break;

                    case "score":
                        ps.setLong(1, imageStatsData.getScore());
                        break;

                    case "avgscore":
                        ps.setFloat(1, imageStatsData.getAvgScore());
                        break;

                    case "score1_votes":
                        ps.setLong(1, imageStatsData.getNumberOfVotes(1));
                        break;

                    case "score2_votes":
                        ps.setLong(1, imageStatsData.getNumberOfVotes(2));
                        break;

                    case "score3_votes":
                        ps.setLong(1, imageStatsData.getNumberOfVotes(3));
                        break;

                    case "score4_votes":
                        ps.setLong(1, imageStatsData.getNumberOfVotes(4));
                        break;

                    case "score5_votes":
                        ps.setLong(1, imageStatsData.getNumberOfVotes(5));
                        break;

                    case "seen_times":
                        ps.setLong(1, imageStatsData.getSeenTimes());
                        break;

                    default:
                        return;
                }


                ps.setTimestamp(2, imageStatsData.getUploadTimeStamp());
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }

    }

    static int dbRetrieveUserVoteForImage(final String userName, final Timestamp imageUploadTime) {

        try (PreparedStatement ps = dbConnection.prepareStatement("SELECT score FROM fotoalbum_uservotes WHERE username=? AND uploadtime=?")) {
            ps.setString(1, userName);
            ps.setTimestamp(2, imageUploadTime);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("score");

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return -1;
    }

    static void removeExistingUserVoteForImage(final String userName, final Timestamp uploadTime) {

        if (savedUserVoteActionsLookup.containsKey(userName)) {

            if (savedUserVoteActionsLookup.get(userName).containsKey(uploadTime)) {

                savedUserVoteActionsLookup.get(userName).remove(uploadTime);

                for (int i = 0; i < savedUserVoteActions.size(); i++) {
                    if (userName.equals(savedUserVoteActions.get(i).getUserName()) && uploadTime.equals(savedUserVoteActions.get(i).getUploadTime())) {
                        savedUserVoteActions.remove(i);
                        break;
                    }
                }
            }
        }
    }

    static void deleteExistingUserVoteForImage(final String userName, final Timestamp selectedImageTimestamp) {

        try (PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM fotoalbum_uservotes WHERE username=? AND uploadtime=?")) {

            ps.setString(1, userName);
            ps.setTimestamp(2, selectedImageTimestamp);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static void dbDeleteExistingImageData(final ImageData imageData) {

        try (PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM fotoalbum_images WHERE uploadtime=?")) {

            ps.setTimestamp(1, imageData.getUploadTimeStamp());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static void dbDeleteExistingImageStatisticsData(final ImageStatisticsData imageStats) {

        try (PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM fotoalbum_imgstats WHERE uploadtime=?")) {

            ps.setTimestamp(1, imageStats.getUploadTimeStamp());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static void dbRemoveExistingImageData(final ImageData imageData) {

        final Timestamp key = imageData.getUploadTimeStamp();

        if (savedImagesDataLookup.containsKey(key)) {
            savedImagesDataLookup.remove(key);

            for (int i = 0; i < savedImagesData.size(); i++) {
                if (key.equals(savedImagesData.get(i).getUploadTimeStamp())) {
                    savedImagesData.remove(i);
                    break;
                }
            }

        }
    }

    static void dbRemoveExistingImageStatsData(final ImageStatisticsData imageStatsData) {

        final Timestamp key = imageStatsData.getUploadTimeStamp();

        if (savedImagesStatsLookup.containsKey(key)) {
            savedImagesStatsLookup.remove(key);

            for (int i = 0; i < savedImagesStats.size(); i++) {
                if (key.equals(savedImagesStats.get(i).getUploadTimeStamp())) {
                    savedImagesStats.remove(i);
                    break;
                }
            }

        }
    }

    static void dbDeleteUserData(final UserData userData) {

        try (PreparedStatement ps = dbConnection.prepareStatement("delete from fotoalbum_users where username=?")) {
            ps.setString(1, userData.getUserName());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static void removeRegisteredUserData(final UserData userData) {

        final String key = userData.getUserName();

        if (registeredUsersLookup.containsKey(key)) {
            registeredUsersLookup.remove(key);

            for (int i = 0; i < registeredUserData.size(); i++) {
                if (key.equals(registeredUserData.get(i).getUserName())) {
                    registeredUserData.remove(i);
                    break;
                }
            }

        }
    }

    static void dbDeleteUserStatisticsData(final UserStatisticsData userStatsData) {

        try (PreparedStatement ps = dbConnection.prepareStatement("delete from fotoalbum_userstats where username=?")) {
            ps.setString(1, userStatsData.getUserName());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static void removeRegisteredUserStatsData(final UserStatisticsData userData) {

        final String key = userData.getUserName();

        if (savedUserDataStatsLookup.containsKey(key)) {
            savedUserDataStatsLookup.remove(key);

            for (int i = 0; i < savedUserDataStats.size(); i++) {
                if (key.equals(savedUserDataStats.get(i).getUserName())) {
                    savedUserDataStats.remove(i);
                    break;
                }
            }

        }
    }

    static void insertUserRegistrationData(final UserData userData) {

        try (PreparedStatement ps = dbConnection.prepareStatement("insert into fotoalbum_users values(?,?,?,?,?,?,?,?)")) {

            ps.setString(1, userData.getFullName());
            ps.setString(2, userData.getUserName());
            ps.setString(3, userData.geteMail());
            ps.setInt(4, userData.getPassword());
            ps.setString(5, userData.getAddress());
            ps.setInt(6, userData.getAge());
            ps.setTimestamp(7, userData.getRegistrationTimeStamp());
            ps.setInt(8, userData.getUserLevel());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void updateUserRegistrationData(final UserData userData) {

        try (PreparedStatement ps = dbConnection.prepareStatement("update fotoalbum_users set user_level=? where username=?")) {
            ps.setInt(1, userData.getUserLevel());
            ps.setString(2, userData.getUserName());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void insertNewUserStatsDataIntoDatabase(final UserStatisticsData newUserStatsData) {

        try (PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO fotoalbum_userstats VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {

            ps.setString(1, newUserStatsData.getUserName());
            ps.setTimestamp(2, newUserStatsData.getLastLoginTime());
            ps.setTimestamp(3, newUserStatsData.getLastVoteTime());
            ps.setInt(4, newUserStatsData.getLastVoteScore());
            ps.setLong(5, newUserStatsData.getNumberOfVotes(1));
            ps.setLong(6, newUserStatsData.getNumberOfVotes(2));
            ps.setLong(7, newUserStatsData.getNumberOfVotes(3));
            ps.setLong(8, newUserStatsData.getNumberOfVotes(4));
            ps.setLong(9, newUserStatsData.getNumberOfVotes(5));
            ps.setLong(10, newUserStatsData.getNumberOfLogins());
            ps.setLong(11, newUserStatsData.getNumberOfImageUploads());
            ps.setLong(12, newUserStatsData.getNumberOfImageDeletions());
            ps.setLong(13, newUserStatsData.getNumberOfUserDeletions());
            ps.setLong(14, newUserStatsData.getNumberOfAdminPromotions());
            ps.setLong(15, newUserStatsData.getNumberOfAdminDemotions());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static void dbUpdateExistingUserStatsData(final UserStatisticsData userStatsData, String... fieldNames) {

        for (final String fieldName : fieldNames) {

            try (PreparedStatement ps = dbConnection.prepareStatement(String.format("update fotoalbum_userstats set %s=? where username=?", fieldName))) {

                switch (fieldName) {

                    case "lastlogintime":
                        ps.setTimestamp(1, userStatsData.getLastLoginTime());
                        break;

                    case "lastvotetime":
                        ps.setTimestamp(1, userStatsData.getLastVoteTime());
                        break;

                    case "lastvotescore":
                        ps.setInt(1, userStatsData.getLastVoteScore());
                        break;

                    case "score1_votes":
                        ps.setLong(1, userStatsData.getNumberOfVotes(1));
                        break;

                    case "score2_votes":
                        ps.setLong(1, userStatsData.getNumberOfVotes(2));
                        break;

                    case "score3_votes":
                        ps.setLong(1, userStatsData.getNumberOfVotes(3));
                        break;

                    case "score4_votes":
                        ps.setLong(1, userStatsData.getNumberOfVotes(4));
                        break;

                    case "score5_votes":
                        ps.setLong(1, userStatsData.getNumberOfVotes(5));
                        break;

                    case "number_of_logins":
                        ps.setLong(1, userStatsData.getNumberOfLogins());
                        break;

                    case "imguploadnumber":
                        ps.setLong(1, userStatsData.getNumberOfImageUploads());
                        break;

                    case "imgdelnumber":
                        ps.setLong(1, userStatsData.getNumberOfImageDeletions());
                        break;

                    case "userdelnumber":
                        ps.setLong(1, userStatsData.getNumberOfUserDeletions());
                        break;

                    case "adminpromonumber":
                        ps.setLong(1, userStatsData.getNumberOfAdminPromotions());
                        break;

                    default:
                        break;
                }


                ps.setString(2, userStatsData.getUserName());
                ps.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    static void close() {
        try {
            if (dbConnection == null) return;
            if (!dbConnection.isClosed()) dbConnection.close();
            System.out.println("Closing database connection...");
            System.exit(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static ArrayList<UserData> getRegisteredUserData() {
        return registeredUserData;
    }

    static HashMap<String, UserData> getRegisteredUsersLookup() {
        return registeredUsersLookup;
    }

    static ArrayList<ImageData> getSavedImagesData() {
        return savedImagesData;
    }

    static HashMap<Timestamp, ImageData> getSavedImagesDataLookup() {
        return savedImagesDataLookup;
    }

    static ArrayList<ImageStatisticsData> getSavedImagesStats() {
        return savedImagesStats;
    }

    static ArrayList<UserStatisticsData> getSavedUserDataStats() {
        return savedUserDataStats;
    }

    static HashMap<Timestamp, ImageStatisticsData> getSavedImagesStatsLookup() {
        return savedImagesStatsLookup;
    }

    static HashMap<Timestamp, ScaledImageData> getSavedScaledImages() {
        return savedScaledImages;
    }

    static HashMap<String, UserStatisticsData> getSavedUserDataStatsLookup() {
        return savedUserDataStatsLookup;
    }

    static ArrayList<UserVoteAction> getSavedUserVoteActions() {
        return savedUserVoteActions;
    }

    static void addNewCategory(final Timestamp uploadedTime, final String userName, String categoryName) {

        if (categoryName.length() < 3) return;

        categoryName = categoryName.trim().toLowerCase();

        if (availableCategoriesLookUp.containsKey(categoryName)) return;

        final String titleCasedCategoryName = categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1);

        final ImageCategory category = new ImageCategory(uploadedTime, userName, titleCasedCategoryName);

        availableCategoriesData.add(category);

        availableCategoriesLookUp.put(categoryName, category);

        availableCategories.add(titleCasedCategoryName);

    }

    static void dbInsertNewCategory(final ImageCategory imageCategory) {

        try (PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO fotoalbum_categories VALUES(?,?,?)")) {
            ps.setTimestamp(1, imageCategory.getUploadedTime());
            ps.setString(2, imageCategory.getUploadedByUserName());
            ps.setString(3, imageCategory.getCategoryName());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static ArrayList<String> getAvailableCategories() {

        return availableCategories;
    }

    static HashMap<String, ImageCategory> getAvailableCategoriesLookUp() {
        return availableCategoriesLookUp;
    }

    static void addNewLocation(final Timestamp uploadedTime, final String userName, String locationName) {

        if (locationName.length() < 3) return;

        final String displayLocation = String.format("%s", locationName);

        locationName = locationName.trim().toLowerCase();

        if (availableLocationsLookUp.containsKey(locationName)) return;

        final String titleCasedLocationName = locationName.substring(0, 1).toUpperCase() + locationName.substring(1);

        final ImageLocation imageLocation = new ImageLocation(uploadedTime, userName, displayLocation);

        availableLocationsData.add(imageLocation);

        availableLocationsLookUp.put(locationName, imageLocation);

        availableLocations.add(displayLocation);

    }

    static void insertNewLocationIntoDB(final ImageLocation imageLocation) {

        try (PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO fotoalbum_locations VALUES(?,?,?)")) {
            ps.setTimestamp(1, imageLocation.getUploadedTime());
            ps.setString(2, imageLocation.getUploadedByUserName());
            ps.setString(3, imageLocation.getLocationName());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static ArrayList<String> getAvailableLocations() {

        return availableLocations;
    }

    public static HashMap<String, ImageLocation> getAvailableLocationsLookUp() {
        return availableLocationsLookUp;
    }

    public static ArrayList<ImageStatisticsData> getFilteredImagesStats() {
        return filteredImagesStats;
    }

    static String getAppropriateUserLevelInformation(final int userLevel) {
        switch (userLevel) {
            case 0:
                return "User";
            case 1:
                return "Admin";
            case 2:
                return "Master admin";
            default:
                return "User";
        }
    }

    static String displayCorrectSortOrderInformation(final ImageSortOrder imageSortOrder) {

        switch (imageSortOrder) {

            case UPLOADTIME_ASC:
                return "by upload time in ascending order";

            case UPLOADTIME_DESC:
                return "by upload time in descending order";

            case AVGSCORE_ASC:
                return "by average score in ascending order";

            case AVGSCORE_DESC:
                return "by average score in descending order";

            case VIEWS_ASC:
                return "by view count in ascending order";

            case VIEWS_DESC:
                return "by view count in descending order";

            default:
                return "unknown sort order";
        }
    }

    public boolean executeExternalSQLScript(String aSQLScriptFilePath, Statement stmt) {
        boolean isScriptExecuted = false;
        try {
            BufferedReader in = new BufferedReader(new FileReader(aSQLScriptFilePath));
            String str;
            StringBuffer sb = new StringBuffer();
            while ((str = in.readLine()) != null) {
                sb.append(str + "\n ");
            }
            in.close();
            stmt.executeUpdate(sb.toString());
            isScriptExecuted = true;
        } catch (SQLException | IOException e) {
            System.err.printf("Failed to execute SQL script: %s [Received error message: %s]", aSQLScriptFilePath, e.getMessage());
        }
        return isScriptExecuted;
    }
}
