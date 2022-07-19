package com.codepath.travelbud;

import com.codepath.travelbud.parse_classes.Post;
import com.parse.ParseUser;

public interface SearchAdapterToFragment {
    void sendUser(int position, ParseUser user);
    void sendPost(int position, Post post);
}
