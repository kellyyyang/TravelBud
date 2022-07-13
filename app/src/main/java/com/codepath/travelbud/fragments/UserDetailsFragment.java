package com.codepath.travelbud.fragments;

import static com.codepath.travelbud.HideSoftKeyboard.hideSoftKeyboard;
import static com.codepath.travelbud.fragments.EditProfileFragment.KEY_IS_PRIVATE;
import static com.codepath.travelbud.fragments.ProfileFragment.KEY_BIO;
import static com.codepath.travelbud.fragments.ProfileFragment.KEY_PROFILE_PIC;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.travelbud.Follow;
import com.codepath.travelbud.Post;
import com.codepath.travelbud.PostsAdapter;
import com.codepath.travelbud.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

// Parse Dependencies
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
// Java Dependencies
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailsFragment extends Fragment {

    public static final String TAG = "UserDetailsFragment";
    public static final String KEY_BLOCKEDUSERS = "blockedUsers";

    ParseUser user;
    private ImageView ivProfilePicSearch;
    private TextView tvUsernameSearch;
    private TextView tvBioSearch;
    private RecyclerView rvPostsSearch;
    private Button btnFollow;
    private Toolbar tbUserDetails;
    private ImageView ivLock;
    private TextView tvPrivateP;
    private View divPrivate;

    private PostsAdapter adapter;
    private List<Post> allPosts;
    private List<ParseUser> requestedFollowers;

    private boolean isFollowing1;
    private List<String> followingUsers;
    ParseUser currentUser = ParseUser.getCurrentUser();

    public UserDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hideSoftKeyboard(requireActivity());

        ivProfilePicSearch = view.findViewById(R.id.ivProfilePicSearch);
        tvUsernameSearch = view.findViewById(R.id.tvUsernameSearch);
        tvBioSearch = view.findViewById(R.id.tvBioSearch);
        rvPostsSearch = view.findViewById(R.id.rvPostsSearch);
        btnFollow = view.findViewById(R.id.btnFollow);

        ivLock = view.findViewById(R.id.ivLock);
        tvPrivateP = view.findViewById(R.id.tvPrivateP);
        divPrivate = view.findViewById(R.id.divPrivate);

        tbUserDetails = view.findViewById(R.id.tbUserDetails);
        tbUserDetails.getMenu().clear();
        tbUserDetails.inflateMenu(R.menu.menu_user_details);

        allPosts = new ArrayList<>();
        requestedFollowers = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts, true);

        followingUsers = new ArrayList<>();
        isFollowing1 = false;

        assert getArguments() != null;
        user = getArguments().getParcelable("USER");

//        user = (ParseUser) Parcels.unwrap(getIntent().getParcelableExtra(ParseUser.class.getSimpleName()));
//        Log.i(TAG, "User: " + user);
//        Log.d(TAG, "Showing details for " + user.getUsername());
        tvUsernameSearch.setText(user.getUsername());

        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                tvBioSearch.setText(user.getString(KEY_BIO));
                ParseFile profileImage = ((ParseFile) user.get(KEY_PROFILE_PIC));
                if (profileImage != null) {
                    Glide.with(UserDetailsFragment.this).load(profileImage.getUrl()).into(ivProfilePicSearch);
                }
            }
        });

        try {
            setButtonAppearanceFix();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            Log.i(TAG, "bool: " + (isFollowing1 || !user.getBoolean(KEY_IS_PRIVATE)));
            Log.i(TAG, "isFollowing: " + isFollowing1);
            Log.i(TAG, "private: " + user.getBoolean(KEY_IS_PRIVATE));
            if (isFollowing1 || !user.getBoolean(KEY_IS_PRIVATE)) {
                Log.i(TAG, "isFollowing: " + isFollowingFunc(currentUser, user) + " " + isFollowing1);
                ivLock.setVisibility(View.GONE);
                tvPrivateP.setVisibility(View.GONE);
                divPrivate.setVisibility(View.GONE);
            } else {
                rvPostsSearch.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tbUserDetails.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_block:
                        Log.i(TAG, "inside block");
                        if (isFollowing1) {
                            unfollowUserBoth(currentUser, user);
                        }
                        try {
                            if (isFollowingFunc(user, currentUser)) {
                                boolean prevIsFollowing = isFollowing1;
                                unfollowUserBoth(user, currentUser);
                                isFollowing1 = prevIsFollowing;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        try {
                            if (!getBlockedUsers(currentUser).contains(user)) {
                                ParseRelation<ParseUser> relation = currentUser.getRelation(KEY_BLOCKEDUSERS);
                                relation.add(user);
                                currentUser.saveInBackground();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                }
                return true;
            }
        });

        // using Follow table
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing1) {
                    unfollowUserBoth(currentUser, user);
                } else {
                    try {
                        Log.i(TAG, "in try");
                        if (user.getBoolean(KEY_IS_PRIVATE) && hasRequested()) {;
                            removeFollowRequest();
                            setBtnFollowColor();
                        } else if (user.getBoolean(KEY_IS_PRIVATE) && !isFollowing1) {
                            sendFollowRequest();
                            setBtnRequestColor();
                        }
                        else {
                            followUserBoth(currentUser, user);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        rvPostsSearch.setAdapter(adapter);
        rvPostsSearch.setLayoutManager(new GridLayoutManager(getContext(), 3));

        queryPosts();
    }

    private boolean hasRequested() throws ParseException {
        getRequestedFollowers();
        for (ParseUser pUser : requestedFollowers) {
            if (pUser.getObjectId().equals(currentUser.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    private void setBtnRequestColor() {
        btnFollow.setBackgroundColor(Color.LTGRAY);
        btnFollow.setTextColor(Color.MAGENTA);
        btnFollow.setText("Requested");
    }

    private void removeFollowRequest() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userA", currentUser.getObjectId());
        parameters.put("userB", user.getObjectId());

        Log.i(TAG, "parameters: " + parameters);

        ParseCloud.callFunctionInBackground("removeFollowRequest", parameters, new FunctionCallback<String>() {
            @Override
            public void done(String object, ParseException e) {
                if (e == null) {
                    // Everything is alright
                    Toast.makeText(getContext(), "Answer = " + object.toString(), Toast.LENGTH_LONG).show();
                }
                else {
                    // Something went wrong
                    Log.i(TAG, "Something went wrong with Parse Cloud code: " + e);
                }
            }
        });
    }

    private void sendFollowRequest() {
        // Use this map to send parameters to your Cloud Code function
        // Just push the parameters you want into it
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userA", currentUser.getObjectId());
        parameters.put("userB", user.getObjectId());

        Log.i(TAG, "parameters: " + parameters);

        ParseCloud.callFunctionInBackground("sendFollowRequest", parameters, new FunctionCallback<String>() {
            @Override
            public void done(String object, ParseException e) {
                if (e == null) {
                    // Everything is alright
                    Toast.makeText(getContext(), "Answer = " + object.toString(), Toast.LENGTH_LONG).show();
                }
                else {
                    // Something went wrong
                    Log.i(TAG, "Something went wrong with Parse Cloud code: " + e);
                }
            }
        });
    }

    private List<ParseUser> getBlockedUsers(ParseUser pUser) throws ParseException {
        ParseRelation<ParseUser> relation = pUser.getRelation(KEY_BLOCKEDUSERS);
        ParseQuery<ParseUser> query = relation.getQuery();
        query.include(KEY_BLOCKEDUSERS);
        return query.find();
    }

    private void followUserBoth(ParseUser userA, ParseUser userB) {
        Log.i(TAG, "in followUserBoth");
        Follow newFollow = new Follow();
        newFollow.setFollower(userA);
        newFollow.setFollowing(userB);
        isFollowing1 = true;
        setBtnUnfollowColor();
        newFollow.saveInBackground();
    }

    private void unfollowUserBoth(ParseUser userA, ParseUser userB) {
        ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
        query.include(Follow.KEY_FOLLOWING);
        query.include(Follow.KEY_FOLLOWING);
        query.whereEqualTo(Follow.KEY_FOLLOWER, userA);
        query.whereEqualTo(Follow.KEY_FOLLOWING, userB);
        isFollowing1 = false;
        setBtnFollowColor();
        query.findInBackground(new FindCallback<Follow>() {
            @Override
            public void done(List<Follow> objects, ParseException e) {
                Log.i(TAG, "hopefully only one: " + objects);
                for (Follow obj : objects) {
                    obj.deleteInBackground();
                }
            }
        });
    }

    private void setButtonAppearanceFix() throws ParseException {
        try {
            setIsFollowing(currentUser, user);
        } catch (ParseException e) {
            Log.e(TAG, "setIsFollowing exception: " + e);
        }
        Log.i(TAG, "setButtonAppearanceFix + isFollowing1: " + isFollowing1);
        getRequestedFollowers();
        if (inRequestedFollowers()) {
            setBtnRequestColor();
        }
        else if (isFollowing1) {
            setBtnUnfollowColor();
        } else {
            setBtnFollowColor();
        }
    }

    private boolean inRequestedFollowers() {
        Log.i(TAG, "requested followers: " + requestedFollowers);
        for (ParseUser pUser : requestedFollowers) {
            if (pUser.getObjectId().equals(currentUser.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    private void getRequestedFollowers() throws ParseException {
        requestedFollowers.clear();
        ParseRelation<ParseUser> relation = user.getRelation("incoming_follow_requests");
        ParseQuery<ParseUser> query = relation.getQuery();
        requestedFollowers.addAll(query.find());
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> objects, ParseException e) {
//                if (e == null) {
//                    Log.e(TAG, "error occurred: " + e);
//                } else {
//                    for (ParseUser user : objects) {
//                        Log.i(TAG, "requested follow: " + user.getUsername());
//                    }
//                }
//                requestedFollowers.addAll(objects);
//            }
//        });
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

    private boolean isFollowingFunc(ParseUser userA, ParseUser userB) throws ParseException {
        Log.i(TAG, "in setIsFollowing");
        ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
        query.include(Follow.KEY_FOLLOWER);
        query.include(Follow.KEY_FOLLOWING);
        query.whereEqualTo(Follow.KEY_FOLLOWER, userA);
        query.whereEqualTo(Follow.KEY_FOLLOWING, userB);
        List<Follow> follows = query.find();
        Log.i(TAG, "following list: " + follows);
        return follows.size() > 0;
    }

    private void setIsFollowing(ParseUser userA, ParseUser userB) throws ParseException {
        Log.i(TAG, "in setIsFollowing");
        ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
        query.include(Follow.KEY_FOLLOWER);
        query.include(Follow.KEY_FOLLOWING);
        query.whereEqualTo(Follow.KEY_FOLLOWER, userA);
        query.whereEqualTo(Follow.KEY_FOLLOWING, userB);
        List<Follow> follows = query.find();
        Log.i(TAG, "following list: " + follows);
        isFollowing1 = follows.size() > 0;
    }

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