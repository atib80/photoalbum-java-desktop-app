package net.fotoalbum;

import java.sql.Timestamp;

class ImageCategory {

    private final Timestamp uploadedTime;
    private final String uploadedByUserName;
    private final String categoryName;

    ImageCategory(Timestamp uploadedTime, String uploadedByUserName, String categoryName) {
        this.uploadedTime = uploadedTime;
        this.uploadedByUserName = uploadedByUserName;
        this.categoryName = categoryName;
    }

    Timestamp getUploadedTime() {
        return uploadedTime;
    }

    String getUploadedByUserName() {
        return uploadedByUserName;
    }

    String getCategoryName() {
        return categoryName;
    }

}
