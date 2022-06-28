package com.codepath.travelbud.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;

import com.bumptech.glide.request.target.ImageViewTargetFactory;
import com.codepath.travelbud.MainActivity;
import com.codepath.travelbud.R;
import com.codepath.travelbud.UserSearchAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */

public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";
    private RecyclerView rvUsersSearch;
    private UserSearchAdapter adapter;
    private List<ParseUser> allUsers;
    private List<ParseUser> searchUsers;
    private ImageView ivSearchIcon;
    private ImageView ivBackArrow;
    private EditText etSearchUsers;

    private int mAppBarState;
    private static final int STANDARD_APPBAR = 0;
    private static final int SEARCH_APPBAR = 1;
    private AppBarLayout viewUsersBar, searchBar;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        ivSearchIcon = view.findViewById(R.id.ivSearchIcon);
        ivBackArrow = view.findViewById(R.id.ivBackArrow);
        viewUsersBar = view.findViewById(R.id.viewContactsToolbar);
        searchBar = view.findViewById(R.id.searchToolbar);
        etSearchUsers = view.findViewById(R.id.etSearchUsers);

        allUsers = new ArrayList<>();
        searchUsers = new ArrayList<>();
//        adapter = new UserSearchAdapter(getContext(), allUsers);
        adapter = new UserSearchAdapter(getContext(), searchUsers);

        rvUsersSearch.setAdapter(adapter);
        rvUsersSearch.setLayoutManager(new LinearLayoutManager(getContext()));

        ivSearchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked searched icon");
                toggleToolBarState();
            }
        });

        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked back arrow.");
                toggleToolBarState();

            }
        });

        setupUsersList();

        queryUsers();

    }

    private void setupUsersList() {


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

                // TODO: make searchUsers the filtered array

                etSearchUsers.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String text = etSearchUsers.getText().toString().toLowerCase(Locale.getDefault());
                        adapter.filter(text, allUsers);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                adapter.notifyDataSetChanged();
//                rvUsersSearch.setAdapter(adapter);
            }
        });
    }

    // Initiate toggle (it means when you click the search icon it pops up the editText and clicking the back button goes to the search icon again)
    private void toggleToolBarState() {
        Log.d(TAG, "toggleToolBarState: toggling AppBarState.");
        if (mAppBarState == STANDARD_APPBAR) {
            setAppBarState(SEARCH_APPBAR);
        } else {
            setAppBarState(STANDARD_APPBAR);
        }
    }

    // Sets the appbar state for either search mode or standard mode.
    private void setAppBarState(int state) {

        Log.d(TAG, "setAppBaeState: changing app bar state to: " + state);

        mAppBarState = state;
        if (mAppBarState == STANDARD_APPBAR) {
            searchBar.setVisibility(View.GONE);
            viewUsersBar.setVisibility(View.VISIBLE);

            View view = getView();
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                im.hideSoftInputFromWindow(view.getWindowToken(), 0); // make keyboard hide
            } catch (NullPointerException e) {
                Log.d(TAG, "setAppBaeState: NullPointerException: " + e);
            }
        } else if (mAppBarState == SEARCH_APPBAR) {
            viewUsersBar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // make keyboard popup

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setAppBarState(STANDARD_APPBAR);
    }


}