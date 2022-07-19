package com.codepath.travelbud.parse_classes;

import android.app.Application;

import com.codepath.travelbud.parse_classes.Follow;
import com.codepath.travelbud.parse_classes.Hashtag;
import com.codepath.travelbud.parse_classes.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register Parse models
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Hashtag.class);
        ParseObject.registerSubclass(Follow.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("T8tqcdYvdKQYkJmJrA7uixDd3I6UObmnU6xXd3QH")
                .clientKey("g7k6OdLJJOm9LgFvOMuQ3rLbFNSMhyNp0CzoiWsX")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
