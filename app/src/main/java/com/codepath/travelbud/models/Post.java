package com.codepath.travelbud.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_RATING = "rating";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_IMAGE_URL = "image_url";
    public static final String KEY_LOCATION_STRING = "location_string";
    public static final String KEY_HASHTAGS = "hashtags";
    public static final String KEY_VISIBILITY = "visibility";

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public float getRating() { return (float) getDouble(KEY_RATING); }

    public void setRating(Float rating) { put(KEY_RATING, rating); }

    public ParseGeoPoint getLocation() { return getParseGeoPoint(KEY_LOCATION); }

    public void setLocation(ParseGeoPoint latlong) { put(KEY_LOCATION, latlong); }

    public String getLocationString() { return getString(KEY_LOCATION_STRING); }

    public void setLocationString(String location) { put(KEY_LOCATION_STRING, location); }

    public int getVisibility() { return getInt(KEY_VISIBILITY); }

    public void setVisibility(int visibility) { put(KEY_VISIBILITY, visibility); }

    public ParseRelation getHashtags() { return getRelation(KEY_HASHTAGS); }

    public void setHashtag(Hashtag hashtag) {
        ParseRelation<Hashtag> relation = this.getRelation(KEY_HASHTAGS);
        relation.add(hashtag);
    }

    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

}
