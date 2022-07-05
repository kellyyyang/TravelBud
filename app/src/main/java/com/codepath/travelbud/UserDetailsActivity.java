package com.codepath.travelbud;

import static com.codepath.travelbud.fragments.ProfileFragment.KEY_BIO;
import static com.codepath.travelbud.fragments.ProfileFragment.KEY_PROFILE_PIC;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

//        followingList();
//        isFollowing2(user);
//        Log.i(TAG, "setButtonAppearance1");
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
//            e.printStackTrace();
        }
        Log.i(TAG, "setButtonAppearanceFix + isFollowing1: " + isFollowing1);
        if (isFollowing1) {
            btnFollow.setBackgroundColor(Color.WHITE);
            btnFollow.setTextColor(Color.MAGENTA);
            btnFollow.setText("Following");
//            unfollowUserFix(user);
        } else {
            btnFollow.setBackgroundColor(Color.MAGENTA);
            btnFollow.setTextColor(Color.WHITE);
            btnFollow.setText("Follow");
//            followUserFix(user);
        }
    }

    private void followUserFix(ParseUser user) {
        ParseRelation<ParseUser> relation = currentUser.getRelation("following");
        relation.add(user);
        btnFollow.setBackgroundColor(Color.WHITE);
        btnFollow.setTextColor(Color.MAGENTA);
        btnFollow.setText("Following");
        currentUser.saveInBackground();
    }

    private void unfollowUserFix(ParseUser user) {
        ParseRelation<ParseUser> relation = currentUser.getRelation("following");
        relation.remove(user);
        btnFollow.setBackgroundColor(Color.MAGENTA);
        btnFollow.setTextColor(Color.WHITE);
        btnFollow.setText("Follow");
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
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> objects, ParseException e) {
//                Log.i(TAG, "inside done tag");
//                if (e != null) {
//                    Log.e(TAG, "parse exception thrown: " + e);
//                }
//                for (ParseUser obj : objects) {
//                    if (obj.getObjectId().equals(searchUser.getObjectId())) {
//                        isFollowing1 = true;
//                        Log.i(TAG, "isFollowing1: " + isFollowing1);
//                        return;
//                    }
//                }
//                setButtonAppearanceFix();
//            }
//        });
        }
//        isFollowing1 = false;
    }

//    private void unfollowUser(ParseUser user) {
//        Log.i(TAG, "following, removing user");
//        ParseRelation relation = currentUser.getRelation("following");
//        relation.remove(user);
//        followingUsers.remove(user.getObjectId());
//        followingList();
//        currentUser.saveInBackground();
//        setButtonAppearance();
//    }
//
//    private void followUser(ParseUser user) {
//        Log.i(TAG, "not following, adding user");
//        ParseRelation relation = currentUser.getRelation("following");
//        relation.add(user);
//        followingUsers.remove(user.getObjectId());
//        followingList();
//        currentUser.saveInBackground();
//        setButtonAppearance();
//    }

//    private void setButtonAppearance() {
//        Log.i(TAG, "button appearance " + isFollowing(user));
//        if (isFollowing(user)) {
//            btnFollow.setBackgroundColor(Color.WHITE);
//            btnFollow.setText("Following");
//            btnFollow.setTextColor(Color.MAGENTA);
//        } else {
//            btnFollow.setBackgroundColor(Color.MAGENTA);
//            btnFollow.setText("Follow");
//            btnFollow.setTextColor(Color.WHITE);
//        }
//        return;
//    }

//    private boolean isFollowing(ParseUser searchUser) {
//        Log.i(TAG, "search for: " + searchUser.getObjectId());
//        followingList();
//        if (followingUsers.contains(searchUser.getObjectId())) {
//            return true;
//        }
//        return false;
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

//    private boolean isFollowing1(ParseUser searchUser) {
//        ParseRelation<ParseUser> relation = currentUser.getRelation("following");
//        ParseQuery<ParseUser> query = relation.getQuery();
//        query.include("following");
//        boolean returnVal = false;
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> users, ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, "Issue with getting following", e);
//                    return;
//                }
//                for (ParseUser user : users) {
//                    Log.i(TAG, "User: " + user.getUsername());
//                    String objectID = user.getObjectId();
//                    if (objectID.equals(user.getObjectId())) {
//                        returnVal = true;
//                    }
//            }
//        });
//    }
//        return returnVal;
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

//    private void followingList() {
//        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
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
//                }
//                followingUsers.addAll(users);
//            }
//        });
//
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