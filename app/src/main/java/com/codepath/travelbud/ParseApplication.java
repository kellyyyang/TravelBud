package com.codepath.travelbud;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register Parse models
        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("T8tqcdYvdKQYkJmJrA7uixDd3I6UObmnU6xXd3QH")
                .clientKey("g7k6OdLJJOm9LgFvOMuQ3rLbFNSMhyNp0CzoiWsX")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
