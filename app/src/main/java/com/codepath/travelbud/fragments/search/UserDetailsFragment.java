package com.codepath.travelbud.fragments.search;

import static com.codepath.travelbud.utils.HideSoftKeyboard.hideSoftKeyboard;
import static com.codepath.travelbud.fragments.profile.EditProfileFragment.KEY_IS_PRIVATE;
import static com.codepath.travelbud.fragments.HomeFragment.KEY_FOLLOWING;
import static com.codepath.travelbud.fragments.profile.ProfileFragment.KEY_BIO;
import static com.codepath.travelbud.fragments.profile.ProfileFragment.KEY_PROFILE_PIC;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.travelbud.models.Post;
import com.codepath.travelbud.adapters.PostsAdapter;
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
import java.util.Arrays;
import java.util.List;

// Parse Dependencies
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.SaveCallback;
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
    private MenuItem blockBtn;
    private TextView tvNumFollowersSearch;
    private TextView tvNumFollowingSearch;

    private PostsAdapter adapter;
    private List<Post> allPosts;
    private List<ParseUser> requestedFollowers;

    private boolean isFollowing;
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
        tvNumFollowersSearch = view.findViewById(R.id.tvNumFollowersSearch);
        tvNumFollowingSearch = view.findViewById(R.id.tvNumFollowingSearch);

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
        isFollowing = false;

        assert getArguments() != null;
        user = getArguments().getParcelable("USER");

        // set number of following and followers
        tvNumFollowingSearch.setText(String.valueOf(user.getInt("numFollowing")));
        tvNumFollowersSearch.setText(String.valueOf(user.getInt("numFollowers")));

        // set title of block menu item button
        blockBtn = tbUserDetails.getMenu().findItem(R.id.action_block);

        try {
            if (checkBlockedUsersContains(user)) {
                blockBtn.setTitle("Unblock");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

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
            setButtonAppearance();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (isFollowing || !user.getBoolean(KEY_IS_PRIVATE)) {
            ivLock.setVisibility(View.GONE);
            tvPrivateP.setVisibility(View.GONE);
            divPrivate.setVisibility(View.GONE);
        } else {
            rvPostsSearch.setVisibility(View.GONE);
        }

        // onClick of  the menu item in the top right corner
        tbUserDetails.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_block) {
                    // boolean showing whether currentUser has blocked the searched-up user
                    boolean userIsBlocked = false;
                    try {
                        userIsBlocked = checkBlockedUsersContains(user);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // if the user is blocked, then clicking the button unblocks them
                    // otherwise, they are blocked
                    if (userIsBlocked) {
                        unBlockUser(user);
                        blockBtn.setTitle("Block");
                    } else { // else block: currentUser is blocking the searched-up user
                        if (isFollowing) {
                            unfollowUserBoth(currentUser, user);
                        }
                        try {
                            if (isFollowingFunc(user, currentUser)) {
                                forceUnfollow(user, currentUser); // TODO: subtract one following + follower
                                int prevNumFollowers = currentUser.getInt("numFollowers");
                                currentUser.put("numFollowers", prevNumFollowers - 1);
                                currentUser.saveInBackground();

                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        try {
                            if (!checkBlockedUsersContains(user)) {
                                ParseRelation<ParseUser> relation = currentUser.getRelation(KEY_BLOCKEDUSERS);
                                relation.add(user);
                                currentUser.saveInBackground();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        blockBtn.setTitle("Unblock");
                    }
                }
                return true;
            }
        });

        // set the appearance of the follow button and implement un/follow functionality
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if currentUser isFollowing the searched-up user, onClick -> unfollow
                // else: check if the searched-up user is private and adjust accordingly
                if (isFollowing) {
                    unfollowUserBoth(currentUser, user);
                } else {
                    try {
                        if (user.getBoolean(KEY_IS_PRIVATE) && hasRequested()) {
                            removeFollowRequest();
                            setBtnFollowColor();
                        } else if (user.getBoolean(KEY_IS_PRIVATE) && !isFollowing) {
                            sendFollowRequest();
                            setBtnRequestColor();
                        }
                        else {
                            // follow the user
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

    /**
     * Unblocks the searched-up user from currentUser.
     * @param user The user to be unblocked.
     */
    private void unBlockUser(ParseUser user) {
        ParseRelation<ParseUser> relation = currentUser.getRelation(KEY_BLOCKEDUSERS);
        relation.remove(user);
        currentUser.saveInBackground();
    }

    /**
     * Checks if a user is in currentUser's blocked list.
     * @param mUser The user to be checked.
     * @return True, if mUser is in the currentUser's blocked list,
     *         False, otherwise.
     * @throws ParseException
     */
    private boolean checkBlockedUsersContains(ParseUser mUser) throws ParseException {
        for (ParseUser pUser : getBlockedUsers(currentUser)) {
            if (pUser.getObjectId().equals(mUser.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if currentUser has already requested to follow the searched-up user.
     * @return True, if the currentUser has requested
     *         False, otherwise.
     * @throws ParseException
     */
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

    /**
     * Removes a follow request that currentUser sent to the searched-up user.
     */
    private void removeFollowRequest() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userA", currentUser.getObjectId());
        parameters.put("userB", user.getObjectId());

        ParseCloud.callFunctionInBackground("removeFollowRequest", parameters, new FunctionCallback<String>() {
            @Override
            public void done(String object, ParseException e) {
                if (e == null) {
                    // Everything is alright
                    Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Adds or removes currentUser as a follower of the searched-up user via Parse Cloud Code.
     * @param isAdd A boolean that tells us whether or not we're adding currentUser (userA) as a follower.
     * @param userA The user being added/removed as a follower.
     * @param userB The user doing the adding/removing
     */
    protected void addOrRemoveFollower(boolean isAdd, ParseUser userA, ParseUser userB) {
        // Use this map to send parameters to your Cloud Code function
        // Just push the parameters you want into it
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userA", userA.getObjectId());
        parameters.put("userB", userB.getObjectId());

        String funcName = "addFollower";

        if (!isAdd) {
            funcName = "removeFollower";
        }
        ParseCloud.callFunctionInBackground(funcName, parameters, new FunctionCallback<String>() {
            @Override
            public void done(String object, ParseException e) {
                if (e == null) {
                    // Everything is alright
                    Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Adds or subtracts one from the column "numFollowers" of the user that the currentUser just unfollowed.
     */
    private void addOrSubOneFollower(boolean isAdd, ParseUser userB) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userB", user.getObjectId());
        String funcName = "subOneFollower";

        if (isAdd) {
            funcName = "addOneFollower";
        }

        ParseCloud.callFunctionInBackground(funcName, parameters, new FunctionCallback<String>() {
            @Override
            public void done(String object, ParseException e) {
                if (e == null) {
                    // Everything is alright
                    tvNumFollowersSearch.setText(String.valueOf(user.getInt("numFollowers")));
                    Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Sends a follow request from the currentUser to another user via Parse Cloud Code.
     */
    private void sendFollowRequest() {
        // Use this map to send parameters to your Cloud Code function
        // Just push the parameters you want into it
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userA", currentUser.getObjectId());
        parameters.put("userB", user.getObjectId());

        ParseCloud.callFunctionInBackground("sendFollowRequest", parameters, new FunctionCallback<String>() {
            @Override
            public void done(String object, ParseException e) {
                if (e == null) {
                    // Everything is alright
                    Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Retrieves the users that a user has blocked.
     * @param pUser The user whose blocked array we're checking.
     * @return A List of ParseUsers that pUser has blocked.
     * @throws ParseException
     */
    private List<ParseUser> getBlockedUsers(ParseUser pUser) throws ParseException {
        ParseRelation<ParseUser> relation = pUser.getRelation(KEY_BLOCKEDUSERS);
        ParseQuery<ParseUser> query = relation.getQuery();
        query.include(KEY_BLOCKEDUSERS);
        return query.find();
    }

    /**
     * Follows a user.
     * @param userA The user doing the following.
     * @param userB The user being followed.
     */
    private void followUserBoth(ParseUser userA, ParseUser userB) {
        ParseRelation<ParseUser> relationA = userA.getRelation(KEY_FOLLOWING);
        relationA.add(userB);
        int numFollowing = (int) userA.getInt("numFollowing");
        userA.put("numFollowing", numFollowing + 1);

        userA.saveInBackground();
        setBtnUnfollowColor();
        isFollowing = true;

        addOrRemoveFollower(true, userA, userB);
        addOrSubOneFollower(true, userB);
    }

    /**
     * Unfollows a user.
     * @param userA The user doing the unfollowing.
     * @param userB The user being unfollowed.
     */
    private void unfollowUserBoth(ParseUser userA, ParseUser userB) {
        ParseRelation<ParseUser> relationA = userA.getRelation(KEY_FOLLOWING);
        relationA.remove(userB);
        userA.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                int numFollowing = userA.getInt("numFollowing");
                userA.put("numFollowing", numFollowing - 1);
            }
        });

        setBtnFollowColor();
        isFollowing = false;

        addOrRemoveFollower(false, userA, userB);
        addOrSubOneFollower(false, userB);
    }

    /**
     * Forces userA to unfollow userB (i.e., we're logged in as userB).
     * @param userA The user doing the unfollowing.
     * @param userB The user being unfollowed. userA = user, userB = currentUser
     */
    private void forceUnfollow(ParseUser userA, ParseUser userB) {
        // Use this map to send parameters to your Cloud Code function
        // Just push the parameters you want into it
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userA", userA.getObjectId());
        parameters.put("userB", userB.getObjectId());

        String funcName = "removeFollowing";

        ParseCloud.callFunctionInBackground(funcName, parameters, new FunctionCallback<String>() {
            @Override
            public void done(String object, ParseException e) {
                if (e == null) {
                    // Everything is alright
                    addOrRemoveFollower(false, userA, userB);
                    subOneFollowing(userA);
                    Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Subtracts one from "numFollowing" column of userA
     * @param userA The user who unfollowed another user.
     */
    private void subOneFollowing(ParseUser userA) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("userB", userA.getObjectId());

        ParseCloud.callFunctionInBackground("subOneFollowing", parameters, new FunctionCallback<String>() {
            @Override
            public void done(String object, ParseException e) {
                if (e == null) {
                    // Everything is alright
                    Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Sets the appearance of the un/follow button
     * @throws ParseException
     */
    private void setButtonAppearance() throws ParseException {
        try {
            setIsFollowing(currentUser, user);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        getRequestedFollowers();
        if (inRequestedFollowers()) {
            setBtnRequestColor();
        }
        else if (isFollowing) {
            setBtnUnfollowColor();
        } else {
            setBtnFollowColor();
        }
    }

    /**
     * Checks if the currentUser has already requested to follow the searched-up user.
     * @return True, if the currentUser has already requested to follow,
     *         False, otherwise.
     */
    private boolean inRequestedFollowers() {
        for (ParseUser pUser : requestedFollowers) {
            if (pUser.getObjectId().equals(currentUser.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the list of users that want to follow the searched-up user.
     * @throws ParseException
     */
    private void getRequestedFollowers() throws ParseException {
        requestedFollowers.clear();
        ParseRelation<ParseUser> relation = user.getRelation("incoming_follow_requests");
        ParseQuery<ParseUser> query = relation.getQuery();
        requestedFollowers.addAll(query.find());
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

    /**
     * Checks if userA is following userB.
     * @param userA The user that could be following another user.
     * @param userB The user that could be followed by another user.
     * @return True, if userA is following userB.
     *         False, otherwise.
     * @throws ParseException
     */
    private boolean isFollowingFunc(ParseUser userA, ParseUser userB) throws ParseException {
        ParseRelation<ParseUser> relation = userA.getRelation(KEY_FOLLOWING);
        ParseQuery<ParseUser> query = relation.getQuery();
        for (ParseUser pUser : query.find()) {
            if (pUser.getObjectId().equals(userB.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets an instance variable that checks if userA is following userB.
     * @param userA The user that could be following another user.
     * @param userB The user that could be followed by another user.
     * @throws ParseException
     */
    private void setIsFollowing(ParseUser userA, ParseUser userB) throws ParseException {
        ParseRelation<ParseUser> relation = userA.getRelation(KEY_FOLLOWING);
        ParseQuery<ParseUser> query = relation.getQuery();
        for (ParseUser pUser : query.find()) {
            if (pUser.getObjectId().equals(userB.getObjectId())) {
                isFollowing = true;
                return;
            }
        }
        isFollowing = false;
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        if (isFollowing) {
            query.whereContainedIn("visibility", new ArrayList<Integer>(Arrays.asList(null, 0, 1)));
        } else {
            query.whereContainedIn("visibility", new ArrayList<Integer>(Arrays.asList(null, 0)));
        }
        query.whereEqualTo(Post.KEY_USER, user);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                for (Post post : posts) {
                }
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }
}