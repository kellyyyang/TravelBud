package com.codepath.travelbud.fragments;

import static com.codepath.travelbud.HideSoftKeyboard.hideSoftKeyboard;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class UserDetailsFragment extends Fragment {

    public static final String TAG = "UserDetailsFragment";

    ParseUser user;
    private ImageView ivProfilePicSearch;
    private TextView tvUsernameSearch;
    private TextView tvBioSearch;
    private RecyclerView rvPostsSearch;
    private Button btnFollow;
    private Toolbar tbUserDetails;

    private PostsAdapter adapter;
    private List<Post> allPosts;

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

        tbUserDetails = view.findViewById(R.id.tbUserDetails);
        tbUserDetails.getMenu().clear();
        tbUserDetails.inflateMenu(R.menu.menu_user_details);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts, true);

        followingUsers = new ArrayList<>();
        isFollowing1 = false;

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

        rvPostsSearch.setAdapter(adapter);
        rvPostsSearch.setLayoutManager(new GridLayoutManager(getContext(), 3));

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