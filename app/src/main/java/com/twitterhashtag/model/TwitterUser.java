package com.twitterhashtag.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Hardik9850 on 21-Nov-15.
 */
public class TwitterUser {
    @SerializedName("id")
    private long id;

    @SerializedName("screen_name")
    private String screenName;

    @SerializedName("name")
    private String name;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("followers_count")
    private int follower_count;

    @SerializedName("friends_count")
    private int friends_count;

    @SerializedName("statuses_count")
    private int status_count;

    @SerializedName("profile_banner_url")
    private String profileBannerUrl;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription() {
        this.description = description;
    }

    public int getFollower_count() {
        return follower_count;
    }

    public void setFollower_count() {
        this.follower_count = follower_count;
    }

    public int getFriends_count() {
        return friends_count;
    }

    public void setFriends_count() {
        this.friends_count = friends_count;
    }

    public int getStatus_count() {
        return status_count;
    }

    public void setStatus_count() {
        this.status_count = status_count;
    }

    public String getProfileBannerUrl() {
        return profileBannerUrl;
    }

    public void setProfileBannerUrl() {
        this.profileBannerUrl = profileBannerUrl;
    }

}
