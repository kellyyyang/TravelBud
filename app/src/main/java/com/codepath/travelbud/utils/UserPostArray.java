package com.codepath.travelbud.utils;

import com.codepath.travelbud.models.Post;
import com.parse.ParseUser;

public class UserPostArray {
    ParseUser user;
    Post post;

    public UserPostArray() {
    }

    public ParseUser getUser() {
        return user;
    }

    public void setUser(ParseUser pUser) {
        this.user = pUser;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
