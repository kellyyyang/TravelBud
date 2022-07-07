package com.codepath.travelbud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class LocationPostsActivity extends AppCompatActivity {

    public static final String TAG = "LocationPostsActivity";
    private List<Post> allPostsList;
    private List<Post> allLocationPosts;
    private ParseGeoPoint currentLocation;
    private PostsAdapter adapter;

    private TextView tvLocationMarker;
    private RatingBar rbLocationAvg;
    private TextView tvRatingAvg;
    private RecyclerView rvLocationPosts;

    private ParseUser currentUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_posts);

        tvLocationMarker = findViewById(R.id.tvLocationMarker);
        rbLocationAvg = findViewById(R.id.rbLocationAvg);
        tvRatingAvg = findViewById(R.id.tvRatingAvg);
        rvLocationPosts = findViewById(R.id.rvLocationPosts);

        currentLocation = Parcels.unwrap(getIntent().getParcelableExtra("geoPoint"));
        allPostsList = Parcels.unwrap(getIntent().getParcelableExtra("allPostsList"));

        allLocationPosts = new ArrayList<>();
        adapter = new PostsAdapter(this, allLocationPosts, false);

        rvLocationPosts.setAdapter(adapter);
        rvLocationPosts.setLayoutManager(new LinearLayoutManager(this));

        queryLocationPosts();
    }

    private void queryLocationPosts() {
        for (Post post : allPostsList) {
            // might use these later to implement a radius / within 5 miles of LOCATION feature later
            double latitude = post.getLocation().getLatitude();
            double longitude = post.getLocation().getLongitude();
            if (post.getLocation().equals(currentLocation)) {
                allLocationPosts.add(post);
            }
            adapter.notifyDataSetChanged();
        }
    }
}