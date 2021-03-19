package net.fotoalbum;

import java.sql.Timestamp;

class ImageStatisticsData {

    private final int imageWidth;
    private final int imageHeight;
    private final Long[] numberOfVotes;
    private Timestamp uploadTime;
    private String imageCategory;
    private String imageLocation;
    private long score;
    private float avgScore;
    private long seenTimes;
    private int lastVoteScore;

    ImageStatisticsData(final Timestamp uploadTime, final String imageCategory, final String imageLocation, final int imageWidth, final int imageHeight, final long score, final float avgScore, final long numberOf1Votes,
                        final long numberOf2Votes, final long numberOf3Votes, final long numberOf4Votes, final long numberOf5Votes, final long seenTimes, final int lastVoteScore) {
        this.uploadTime = uploadTime;
        this.imageCategory = imageCategory;
        this.imageLocation = imageLocation;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.score = score;
        this.avgScore = avgScore;
        this.seenTimes = seenTimes;
        this.numberOfVotes = new Long[]{numberOf1Votes, numberOf2Votes, numberOf3Votes, numberOf4Votes, numberOf5Votes};
        this.lastVoteScore = lastVoteScore;

    }

    Timestamp getUploadTimeStamp() {
        return uploadTime;
    }

    void setUploadTimeStamp(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
    }

    String getImageCategory() {
        return imageCategory;
    }

    public void setImageCategory(String imageCategory) {
        this.imageCategory = imageCategory;
    }

    String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    int getImageWidth() {
        return imageWidth;
    }

    int getImageHeight() {
        return imageHeight;
    }

    long getScore() {
        return score;
    }

    private void setScore(final long score) {
        this.score = score;
    }

    float getAvgScore() {
        return avgScore;
    }

    private void updateCurrentAverageScore() {

        if (0 == this.getNumberOfAllVotes()) this.avgScore = 0.f;
        else this.avgScore = (float) this.score / this.getNumberOfAllVotes();
    }

    int getLastVoteScore() {
        return lastVoteScore;
    }

    void setLastVoteScore(int lastVoteScore) {
        this.lastVoteScore = lastVoteScore;
    }

    long getNumberOfVotes(final int voteScore) {

        if (voteScore < 1 || voteScore > 5) return 0;
        return this.numberOfVotes[voteScore - 1];
    }

    long getNumberOfAllVotes() {
        return this.numberOfVotes[0] + this.numberOfVotes[1] + this.numberOfVotes[2] + this.numberOfVotes[3] + this.numberOfVotes[4];
    }

    void increaseCorrectGroupOfNumberOfVotes(final int voteScore) {

        if (voteScore < 1 || voteScore > 5) return;
        this.numberOfVotes[voteScore - 1]++;
        setScore(getScore() + voteScore);
        updateCurrentAverageScore();
    }

    void decreaseCorrectGroupOfNumberOfVotes(final int voteScore) {

        if (voteScore < 1 || voteScore > 5) return;
        if (this.numberOfVotes[voteScore - 1] > 0) this.numberOfVotes[voteScore - 1]--;
        setScore(getScore() - voteScore);
        updateCurrentAverageScore();

    }

    long getSeenTimes() {
        return seenTimes;
    }

    void setSeenTimes(long seenTimes) {
        this.seenTimes = seenTimes;
    }

}
