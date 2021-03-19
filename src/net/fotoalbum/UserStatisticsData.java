package net.fotoalbum;

import java.sql.Timestamp;

class UserStatisticsData {

    private final String userName;
    private Timestamp lastLoginTime;
    private Timestamp lastVoteTime;
    private int lastVoteScore;
    private final Long[] numberOfVotes;
    private long numberOfLogins;
    private long numberOfImageUploads;
    private long numberOfImageDeletions;
    private final long numberOfUserDeletions;
    private final long numberOfAdminPromotions;
    private final long numberOfAdminDemotions;

    UserStatisticsData(final String userName, final Timestamp lastLoginTime, final Timestamp lastVoteTime, final int lastVoteScore,
                       final long numberOf1Votes, final long numberOf2Votes, final long numberOf3Votes, final long numberOf4Votes,
                       final long numberOf5Votes, final long numberOfLogins, final long numberOfImageUploads, final long numberOfImageDeletions,
                       final long numberOfUserDeletions, final long numberOfAdminPromotions, final long numberOfAdminDemotions) {
        this.userName = userName;
        this.lastLoginTime = lastLoginTime;
        this.lastVoteTime = lastVoteTime;
        this.lastVoteScore = lastVoteScore;
        this.numberOfVotes = new Long[]{numberOf1Votes, numberOf2Votes, numberOf3Votes, numberOf4Votes, numberOf5Votes};
        this.numberOfLogins = numberOfLogins;
        this.numberOfImageUploads = numberOfImageUploads;
        this.numberOfImageDeletions = numberOfImageDeletions;
        this.numberOfUserDeletions = numberOfUserDeletions;
        this.numberOfAdminPromotions = numberOfAdminPromotions;
        this.numberOfAdminDemotions = numberOfAdminDemotions;
    }

    String getUserName() {
        return userName;
    }

    Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    Timestamp getLastVoteTime() {
        return lastVoteTime;
    }

    void setLastVoteTime(Timestamp lastVoteTime) {
        this.lastVoteTime = lastVoteTime;
    }

    int getLastVoteScore() {
        return lastVoteScore;
    }

    void setLastVoteScore(int lastVoteScore) {
        this.lastVoteScore = lastVoteScore;
    }

    long getNumberOfVotes(final int voteScore) {

        switch (voteScore) {
            case 1:
                return this.numberOfVotes[0];
            case 2:
                return this.numberOfVotes[1];
            case 3:
                return this.numberOfVotes[2];
            case 4:
                return this.numberOfVotes[3];
            case 5:
                return this.numberOfVotes[4];
            default:
                return 0;
        }
    }

    long getNumberOfAllVotes() {
        return this.numberOfVotes[0] + this.numberOfVotes[1] + this.numberOfVotes[2] + this.numberOfVotes[3] + this.numberOfVotes[4];
    }

    void increaseCorrectGroupOfNumberOfVotes(final int voteScore) {

        switch (voteScore) {

            case 1:
                this.numberOfVotes[0]++;
                break;
            case 2:
                this.numberOfVotes[1]++;
                break;
            case 3:
                this.numberOfVotes[2]++;
                break;
            case 4:
                this.numberOfVotes[3]++;
                break;
            case 5:
                this.numberOfVotes[4]++;
                break;
            default:
                break;
        }
    }

    void decreaseCorrectGroupOfNumberOfVotes(final int voteScore) {

        switch (voteScore) {

            case 1:
                if (this.numberOfVotes[0] > 0) this.numberOfVotes[0]--;
                break;
            case 2:
                if (this.numberOfVotes[1] > 0) this.numberOfVotes[1]--;
                break;
            case 3:
                if (this.numberOfVotes[2] > 0) this.numberOfVotes[2]--;
                break;
            case 4:
                if (this.numberOfVotes[3] > 0) this.numberOfVotes[3]--;
                break;
            case 5:
                if (this.numberOfVotes[4] > 0) this.numberOfVotes[4]--;
                break;
            default:
                break;
        }

    }

    long getNumberOfLogins() {
        return numberOfLogins;
    }

    void setNumberOfLogins(long numberOfLogins) {
        this.numberOfLogins = numberOfLogins;
    }

    long getNumberOfImageUploads() {
        return numberOfImageUploads;
    }

    void setNumberOfImageUploads(long numberOfImageUploads) {
        this.numberOfImageUploads = numberOfImageUploads;
    }

    long getNumberOfImageDeletions() {
        return numberOfImageDeletions;
    }

    void setNumberOfImageDeletions(long numberOfImageDeletions) {
        this.numberOfImageDeletions = numberOfImageDeletions;
    }

    long getNumberOfUserDeletions() {
        return numberOfUserDeletions;
    }

    long getNumberOfAdminPromotions() {
        return numberOfAdminPromotions;
    }

    long getNumberOfAdminDemotions() {
        return numberOfAdminDemotions;
    }

}
