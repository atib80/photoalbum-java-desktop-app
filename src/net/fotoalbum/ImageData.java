package net.fotoalbum;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;

class ImageData {

    private final Timestamp uploadTimeStamp;
    private final String imageName;
    private final String imagePath;
    private final String uploadedUserName;
    private final BufferedImage imageData;

    ImageData(Timestamp uploadTimeStamp, final String imageName, final String imagePath, final String uploadedUserName,
              final BufferedImage imageData) {
        this.uploadTimeStamp = uploadTimeStamp;
        this.imageName = imageName;
        this.imagePath = imagePath;

        this.uploadedUserName = uploadedUserName;
        this.imageData = imageData;
    }

    Timestamp getUploadTimeStamp() {
        return uploadTimeStamp;
    }

    String getImageName() {
        return imageName;
    }

    String getImagePath() {
        return imagePath;
    }

    String getUploaderUserName() {
        return uploadedUserName;
    }

    BufferedImage getImageData() {
        return imageData;
    }

}

class ScaledImageData {

    private final Image scaledMiniImage;
    private final Image scaledMediumImage;

    ScaledImageData(final Image scaledMiniImage, final Image scaledMediumImage) {

        this.scaledMiniImage = scaledMiniImage;
        this.scaledMediumImage = scaledMediumImage;

    }

    Image getScaledMiniImage() {
        return scaledMiniImage;
    }

    Image getScaledMediumImage() {
        return scaledMediumImage;
    }

}
