package com.codepath.travelbud;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Follow")
public class Follow extends ParseObject {
    public static final String KEY_FOLLOWER = "follower";
    public static final String KEY_FOLLOWING = "following";

    public void setFollower(ParseUser userObjId) { put(KEY_FOLLOWER, userObjId); }

    public ParseUser getFollower() { return getParseUser(KEY_FOLLOWER); }

    public void setFollowing(ParseUser userObjId) { put(KEY_FOLLOWING, userObjId); }

    public ParseUser getFollowing() { return getParseUser(KEY_FOLLOWING); }
}
