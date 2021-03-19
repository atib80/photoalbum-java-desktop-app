package net.fotoalbum;

import java.sql.Timestamp;

class UserData {

    private final String fullName;
    private final String userName;
    private final String eMail;
    private final int password;
    private final String address;
    private final int age;
    private final Timestamp registrationTimeStamp;
    private int userLevel;
    private boolean isSavedToDB;

    UserData(final String fullName, final String userName, final String eMail, final int password, final String address,
             final int age, final Timestamp registrationTimeStamp, final int userLevel, final boolean isSavedToDB) {

        this.fullName = fullName;
        this.userName = userName;
        this.eMail = eMail;
        this.password = password;
        this.address = address;
        this.age = age;
        this.registrationTimeStamp = registrationTimeStamp;
        this.userLevel = userLevel;
        this.isSavedToDB = isSavedToDB;
    }

    String getFullName() {
        return fullName;
    }

    String getUserName() {
        return userName;
    }

    String geteMail() {
        return eMail;
    }

    int getPassword() {
        return password;
    }

    String getAddress() {
        return address;
    }

    int getAge() {
        return age;
    }

    Timestamp getRegistrationTimeStamp() {
        return registrationTimeStamp;
    }

    int getUserLevel() {
        return this.userLevel;
    }

    void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    boolean isSavedToDB() {
        return isSavedToDB;
    }

    void setSavedToDB(boolean savedToDB) {
        isSavedToDB = savedToDB;
    }
}
