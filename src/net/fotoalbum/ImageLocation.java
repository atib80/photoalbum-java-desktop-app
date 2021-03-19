package net.fotoalbum;

import java.sql.Timestamp;

class ImageLocation {

    private final Timestamp uploadedTime;
    private final String uploadedByUserName;
    private final String locationName;

    ImageLocation(Timestamp uploadedTime, String uploadedByUserName, String locationName) {
        this.uploadedTime = uploadedTime;
        this.uploadedByUserName = uploadedByUserName;
        this.locationName = locationName;
    }

    Timestamp getUploadedTime() {
        return uploadedTime;
    }

    String getUploadedByUserName() {
        return uploadedByUserName;
    }

    String getLocationName() {
        return locationName;
    }

}

