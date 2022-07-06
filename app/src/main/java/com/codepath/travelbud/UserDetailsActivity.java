package com.codepath.travelbud;

import static com.codepath.travelbud.fragments.ProfileFragment.KEY_BIO;
import static com.codepath.travelbud.fragments.ProfileFragment.KEY_PROFILE_PIC;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.travelbud.fragments.ComposeFragment;
import com.codepath.travelbud.fragments.HomeFragment;
import com.codepath.travelbud.fragments.MapsFragment;
import com.codepath.travelbud.fragments.ProfileFragment;
import com.codepath.travelbud.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsActivity extends AppCompatActivity {

    public static final String TAG = "UserDetailsActivity";

    ParseUser user;
    private ImageView ivProfilePicSearch;
    private TextView tvUsernameSearch;
    private TextView tvBioSearch;
    private RecyclerView rvPostsSearch;
    private Button btnFollow;

    private BottomNavigationView bottomNavigation;

    private PostsAdapter adapter;
    private List<Post> allPosts;

    private List<String> followingUsers;
    private boolean isFollowing1;

    ParseUser currentUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        ivProfilePicSearch = findViewById(R.id.ivProfilePicSearch);
        tvUsernameSearch = findViewById(R.id.tvUsernameSearch);
        tvBioSearch = findViewById(R.id.tvBioSearch);
        rvPostsSearch = findViewById(R.id.rvPostsSearch);
        btnFollow = findViewById(R.id.btnFollow);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(this, allPosts, true);

        followingUsers = new ArrayList<>();
        isFollowing1 = false;

        user = (ParseUser) Parcels.unwrap(getIntent().getParcelableExtra(ParseUser.class.getSimpleName()));
        Log.i(TAG, "User: " + user);
        Log.d(TAG, "Showing details for " + user.getUsername());
        tvUsernameSearch.setText(user.getUsername());

        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                tvBioSearch.setText(user.getString(KEY_BIO));
                ParseFile profileImage = ((ParseFile) user.get(KEY_PROFILE_PIC));
                if (profileImage != null) {
                    Glide.with(UserDetailsActivity.this).load(profileImage.getUrl()).into(ivProfilePicSearch);
                }
            }
        });

        setButtonAppearanceFix();

        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "ONCLICK BABY");
                if (isFollowing1) {
                    Log.i(TAG, "isFollowing1 true!");
                    unfollowUserFix(user);
                } else {
                    Log.i(TAG, "isFollowing1 false!");
                    followUserFix(user);
                }
            }
        });

//        btnFollow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "ONCLICK BABY");
//                if (isFollowing(user)) {
//                    unfollowUser(user);
//                } else {
//                    followUser(user);
//                }
//            }
//        });

        rvPostsSearch.setAdapter(adapter);
        rvPostsSearch.setLayoutManager(new GridLayoutManager(this, 3));

        queryPosts();
    }

    private void setButtonAppearanceFix() {
        try {
            setIsFollowing(user);
        } catch (ParseException e) {
            Log.e(TAG, "setIsFollowing exception: " + e);
        }
        Log.i(TAG, "setButtonAppearanceFix + isFollowing1: " + isFollowing1);
        if (isFollowing1) {
            setBtnUnfollowColor();
        } else {
            setBtnFollowColor();
        }
    }

    private void setBtnFollowColor() {
        btnFollow.setBackgroundColor(Color.MAGENTA);
        btnFollow.setTextColor(Color.WHITE);
        btnFollow.setText("Follow");
    }

    private void setBtnUnfollowColor() {
        btnFollow.setBackgroundColor(Color.WHITE);
        btnFollow.setTextColor(Color.MAGENTA);
        btnFollow.setText("Following");
    }

    private void followUserFix(ParseUser user) {
        ParseRelation<ParseUser> relation = currentUser.getRelation("following");
        relation.add(user);
        setBtnUnfollowColor();
        isFollowing1 = true;
        currentUser.saveInBackground();
    }

    private void unfollowUserFix(ParseUser user) {
        ParseRelation<ParseUser> relation = currentUser.getRelation("following");
        relation.remove(user);
        setBtnFollowColor();
        isFollowing1 = false;
        currentUser.saveInBackground();
    }

    private void setIsFollowing(ParseUser searchUser) throws ParseException {
        ParseRelation<ParseUser> relation = currentUser.getRelation("following");
        ParseQuery<ParseUser> query = relation.getQuery();
        query.include("following");
        List<ParseUser> users = query.find();
        Log.i(TAG, "list of following: " + users);
        for (ParseUser obj : users) {
            if (obj.getObjectId().equals(searchUser.getObjectId())) {
                isFollowing1 = true;
                Log.i(TAG, "isFollowing1: " + isFollowing1);
                return;
            }
        }
    }

//    private void setIsFollowing1(ParseUser searchUser) {
//        ParseRelation<ParseUser> relation = currentUser.getRelation("following");
//        ParseQuery<ParseUser> query = relation.getQuery();
//        query.include("following");
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> objects, ParseException e) {
//
//            }
//        });
//    }


//    private void isFollowing2(ParseUser searchUser) {
//        Log.i(TAG, "isFollowing2");
//        ParseRelation<ParseUser> relation = currentUser.getRelation("following");
//        ParseQuery<ParseUser> query = relation.getQuery();
//        query.include("following");
////        boolean[] returnVal = {false};
//
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> users, ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, "Issue with getting following", e);
//                    return;
//                }
//                for (ParseUser user : users) {
//                    Log.i(TAG, "User: " + user.getUsername());
//                    String objectID = searchUser.getObjectId();
//                    Log.i(TAG, "for loop: " + objectID);
//                    if (objectID.equals(user.getObjectId())) {
////                        returnVal[0] = true;
//                        isFollowing1 = true;
//                        Log.i(TAG, "TRUE");
//                        Log.i(TAG, "isFollowing is (inside done): " + isFollowing1);
//                        return;
//                    }
//                }
//            }
//        });
//        Log.i(TAG, "isFollowing is ... " + isFollowing1);
//    }


//    private void followingList() {
//        ParseRelation<ParseUser> relation = currentUser.getRelation("following");
////        Log.i(TAG, "followingList: " + relation);
//        ParseQuery<ParseUser> query = relation.getQuery();
//        query.include("following");
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> users, ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, "Issue with getting following", e);
//                    return;
//                }
//                for (ParseUser user : users) {
//                    Log.i(TAG, "User: " + user.getUsername());
//                    if (!followingUsers.contains(user.getObjectId())) {
//                        followingUsers.add(user.getObjectId());
//                    }
//                }
////                followingUsers.addAll(users);
//                Log.i(TAG, "followingUsers: " + followingUsers);
////                setButtonAppearance();
//            }
//        });
//    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, user);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }
}