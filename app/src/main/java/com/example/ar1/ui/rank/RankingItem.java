package com.example.ar1.ui.rank;

import android.graphics.Bitmap;

public class RankingItem {
    private String userImage;
    private String userName;
    private int missionCount;

    public RankingItem(String userImage, String userName, int missionCount) {
        this.userImage = userImage;
        this.userName = userName;
        this.missionCount = missionCount;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getMissionCount() {
        return missionCount;
    }

    public void setMissionCount(int missionCount) {
        this.missionCount = missionCount;
    }
}