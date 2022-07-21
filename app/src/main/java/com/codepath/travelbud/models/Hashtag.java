package com.codepath.travelbud.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

@ParseClassName("Hashtag")
public class Hashtag extends ParseObject {
    public static final String KEY_HASHTAG = "hashtag";
    public static final String KEY_POSTS = "connected_posts";

    public void setHashtag(String hashtag) {
        put(KEY_HASHTAG, hashtag);
    }

    public String getHashtag() { return getString(KEY_HASHTAG); }

    public void setFollowing(Post post) {
        ParseRelation<Post> relation = this.getRelation(KEY_POSTS);
        relation.add(post);
    }

    public ParseRelation<Post> getFollowing() {
        return getRelation(KEY_POSTS);
    }
}
