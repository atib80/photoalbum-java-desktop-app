package net.fotoalbum;

import java.sql.Timestamp;

class UserVoteAction {

    private final String userName;
    private final Timestamp uploadTime;
    private final int voteScore;
    private final Timestamp votetime;

    UserVoteAction(String userName, Timestamp uploadTime, int voteScore, Timestamp votetime) {
        this.userName = userName;
        this.uploadTime = uploadTime;
        this.voteScore = voteScore;
        this.votetime = votetime;
    }

    String getUserName() {
        return userName;
    }

    Timestamp getUploadTime() {
        return uploadTime;
    }

    int getVoteScore() {
        return voteScore;
    }

}
