package com.codepath.travelbud.fragments;

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

import com.codepath.travelbud.R;
import com.codepath.travelbud.UserSearchAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";
    private RecyclerView rvUsersSearch;
    private UserSearchAdapter adapter;
    private List<ParseUser> allUsers;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvUsersSearch = view.findViewById(R.id.rvUsersSearch);

        allUsers = new ArrayList<>();
        adapter = new UserSearchAdapter(getContext(), allUsers);

        rvUsersSearch.setAdapter(adapter);
        rvUsersSearch.setLayoutManager(new LinearLayoutManager(getContext()));

        queryUsers();

    }

    private void queryUsers() {
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting users", e);
                    return;
                }
                for (ParseUser user : users ) {
                    Log.i(TAG, "User: " + user.getUsername());
                }
                allUsers.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });
    }
}