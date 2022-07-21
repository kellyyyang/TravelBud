package com.codepath.travelbud.interfaces;

import com.codepath.travelbud.models.Post;
import com.parse.ParseUser;

public interface SearchAdapterToFragment {
    void sendUser(int position, ParseUser user);
    void sendPost(int position, Post post);
}
