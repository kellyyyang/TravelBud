package com.codepath.travelbud.fragments.viewpager_fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.travelbud.ExploreAdapter;
import com.codepath.travelbud.Post;
import com.codepath.travelbud.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    public static final String TAG = "ExploreFragment";

    private RecyclerView rvExplore;
    private ExploreAdapter adapter;
    private List<Post> allPosts;
    private List<ParseUser> users;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvExplore = view.findViewById(R.id.rvExplore);
        allPosts = new ArrayList<>();
        users = new ArrayList<>();
        adapter = new ExploreAdapter(getContext(), users, allPosts);

        rvExplore.setAdapter(adapter);
        rvExplore.setLayoutManager(new GridLayoutManager(getContext(), 3));

        try {
            queryPosts();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            queryUsers();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "allPosts: " + allPosts);
        Log.i(TAG, "allUsers: " + users);
        adapter.notifyDataSetChanged();
    }

    private void queryUsers() throws ParseException {
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.include("username");
        query.addAscendingOrder("username");
        users.addAll(query.find());
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> objects, ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, "Issue with getting users", e);
//                    return;
//                }
//                for (ParseUser mUser : objects) {
//                    Log.i(TAG, "User: " + mUser.getUsername());
//                }
//                users.addAll(objects);
//                Log.i(TAG, "users: " + users);
////                adapter.notifyDataSetChanged();
//            }
//        });
    }

    private void queryPosts() throws ParseException {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addDescendingOrder("createdAt");
        allPosts.addAll(query.find());
//        query.findInBackground(new FindCallback<Post>() {
//            @Override
//            public void done(List<Post> posts, ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, "Issue with getting posts", e);
//                    return;
//                }
//                for (Post post : posts) {
//                    Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
//                }
//                allPosts.addAll(posts);
//                Log.i(TAG, "posts: " + allPosts);
////                adapter.notifyDataSetChanged();
//            }
//        });
    }
}