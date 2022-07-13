package com.codepath.travelbud.fragments;

import static com.codepath.travelbud.Post.KEY_USER;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.travelbud.FollowRequestsAdapter;
import com.codepath.travelbud.R;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FollowRequestsFragment extends Fragment {

    public static final String TAG = "FollowRequestsFragment";

    protected FollowRequestsAdapter adapter;
    protected List<ParseUser> users;
    private RecyclerView rvFollowRequests;
    private ParseUser currentUser;

    public FollowRequestsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_follow_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFollowRequests = view.findViewById(R.id.rvFollowRequests);
        currentUser = ParseUser.getCurrentUser();

        // initialize the array that will hold posts and create a PostsAdapter
        users = new ArrayList<>();
        adapter = new FollowRequestsAdapter(getContext(), users);

        // set the adapter on the recycler view
        rvFollowRequests.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvFollowRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        // query follow requests
        queryUsers();
    }

    private void queryUsers() {
        // look in incoming follow requests fo the current user
        ParseRelation<ParseUser> relation = currentUser.getRelation("incoming_follow_requests");
        ParseQuery<ParseUser> query = relation.getQuery();
        // start an asynchronous call for users
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (ParseUser user : objects) {
                    Log.i(TAG, "User: " + user.getUsername());
                }
                users.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });
    }
}