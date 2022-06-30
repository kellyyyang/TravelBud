package com.codepath.travelbud.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.travelbud.LoginActivity;
import com.codepath.travelbud.Post;
import com.codepath.travelbud.PostsAdapter;
import com.codepath.travelbud.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
    private RecyclerView rvPostsFeed;
    private PostsAdapter adapter;
    private List<Post> allPosts;

    public static final String KEY_BIO = "bio";
    public static final String KEY_PROFILE_PIC = "profilePic";

    private ImageView ivProfilePicProfile;
    private TextView tvUsernameProfile;
    private TextView tvBioProfile;

    private Button btnLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogoutButton();
            }
        });

        rvPostsFeed = view.findViewById(R.id.rvPostsFeed);
        ivProfilePicProfile = view.findViewById(R.id.ivProfilePicProfile);
        tvUsernameProfile = view.findViewById(R.id.tvUsernameProfile);
        tvBioProfile = view.findViewById(R.id.tvBioProfile);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts, true);

        tvUsernameProfile.setText(ParseUser.getCurrentUser().getUsername());
        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                tvBioProfile.setText(ParseUser.getCurrentUser().getString(KEY_BIO));
            }
        });
        ParseFile profileImage = ((ParseFile) ParseUser.getCurrentUser().get(KEY_PROFILE_PIC));
        if (profileImage != null) {
            Glide.with(getContext()).load(profileImage.getUrl()).into(ivProfilePicProfile);
        }

        rvPostsFeed.setAdapter(adapter);
        rvPostsFeed.setLayoutManager(new GridLayoutManager(getContext(), 3));

        queryPosts();

    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
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

    private void onLogoutButton() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with logout ", e);
                    Toast.makeText(getContext(), "Issue with logout!", Toast.LENGTH_SHORT);
                    return;
                } else {
                    goLoginActivity();
                    Log.i(TAG, "Going to login activity.");
                    Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void goLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}