package com.codepath.travelbud;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

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

//    public ArrayList<Post> getFollowing() throws ParseException {
//        ParseRelation<Post> relation = getRelation(KEY_POSTS);
//        ParseQuery<Post> query = relation.getQuery();
//        query.include(KEY_POSTS);
//        List<Post> followingPosts = query.find();
//        return (ArrayList<Post>) followingPosts;
//    }
    public ParseRelation<Post> getFollowing() {
        return getRelation(KEY_POSTS);
    }
}
