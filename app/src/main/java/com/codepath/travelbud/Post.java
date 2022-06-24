package com.codepath.travelbud;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_RATING = "rating";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_IMAGE_URL = "image_url";

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

    public Integer getRating() { return getInt(KEY_RATING); }

    public void setRating(Float rating) { put(KEY_RATING, rating); }

    public ParseGeoPoint getLocation() { return getParseGeoPoint(KEY_LOCATION); }

    public void setLocation(ParseGeoPoint latlong) { put(KEY_LOCATION, latlong); }

    public String getImageUrl() { return getString(KEY_IMAGE_URL); }

    public void setImageUrl(String imageUrl) {put(KEY_IMAGE_URL, imageUrl); }

}
