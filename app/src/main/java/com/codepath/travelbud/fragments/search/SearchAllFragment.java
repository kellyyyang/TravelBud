package com.codepath.travelbud.fragments.search;

import static com.codepath.travelbud.fragments.search.UserDetailsFragment.KEY_BLOCKEDUSERS;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.codepath.travelbud.R;
import com.codepath.travelbud.interfaces.SearchAdapterToFragment;
import com.codepath.travelbud.adapters.SearchAllAdapter;
import com.codepath.travelbud.utils.UserPostArray;
import com.codepath.travelbud.models.Post;
import com.google.android.material.appbar.AppBarLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */

public class SearchAllFragment extends Fragment {

    public static final String TAG = "SearchAllFragment";
    private String KEY_USERNAME = "username";

    private RecyclerView rvUsersSearch;
    private SearchAllAdapter adapter;
    private List<UserPostArray> allUsersPosts;
    private List<UserPostArray> searchUsersPosts;

    private ImageView ivSearchIcon;
    private ImageView ivBackArrow;
    private EditText etSearchUsers;
    private ParseUser currentUser;
    private ImageView ivExploreIcon;

    private int mAppBarState;
    private static final int STANDARD_APPBAR = 0;
    private static final int SEARCH_APPBAR = 1;
    private AppBarLayout viewUsersBar, searchBar;

    public SearchAllFragment() {
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
        return inflater.inflate(R.layout.fragment_search_all, container, false);
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
        ivExploreIcon = view.findViewById(R.id.ivExploreIcon);

        currentUser = ParseUser.getCurrentUser();

        allUsersPosts = new ArrayList<>();
        searchUsersPosts = new ArrayList<>();
        adapter = new SearchAllAdapter(getContext(), searchUsersPosts, communication);

        rvUsersSearch.setAdapter(adapter);
        rvUsersSearch.setLayoutManager(new LinearLayoutManager(getContext()));

        ivSearchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleToolBarState();
            }
        });

        ivExploreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new ExploreFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContainer, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleToolBarState();

            }
        });

        queryUsers();

    }

    // sends the user or post selected to the adapter
    SearchAdapterToFragment communication = new SearchAdapterToFragment() {
        @Override
        public void sendUser(int position, ParseUser user) {
            UserDetailsFragment userDetailsFragment = new UserDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("USER", user);
            userDetailsFragment.setArguments(bundle);
            FragmentManager manager=getFragmentManager();
            FragmentTransaction transaction=manager.beginTransaction();
            transaction.replace(R.id.flContainer, userDetailsFragment).commit();
        }

        @Override
        public void sendPost(int position, Post post) {
            PostDetailsFragment postDetailsFragment = new PostDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("POST", post);
            postDetailsFragment.setArguments(bundle);
            FragmentManager manager=getFragmentManager();
            FragmentTransaction transaction=manager.beginTransaction();
            transaction.replace(R.id.flContainer, postDetailsFragment).commit();
        }
    };

    /**
     * Retrieves the users that a user has blocked.
     * @param pUser The user whose blockedUsers column we want to check.
     * @return A list of ParseUsers that pUser has blocked.
     * @throws ParseException
     */
    private List<ParseUser> getBlockedUsers(ParseUser pUser) throws ParseException {
        ParseRelation<ParseUser> relation = pUser.getRelation(KEY_BLOCKEDUSERS);
        ParseQuery<ParseUser> query = relation.getQuery();
        query.include(KEY_BLOCKEDUSERS);
        return query.find();
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                for (Post post : posts ) {
                    boolean currUserIsBlocked = false;
                    try {
                        List<ParseUser> blockedUsers = getBlockedUsers(post.getUser());
                        for (ParseUser bUser : blockedUsers) {
                            if (bUser.getObjectId().equals(currentUser.getObjectId())) {
                                currUserIsBlocked = true;
                                // the currentUser is blocked, so we shouldn't query this user's posts
                                break;
                            }
                        }
                        if (!currUserIsBlocked && (post.getVisibility() != 2 || !post.getUser().getBoolean("isPrivate"))) {
                                UserPostArray mPost = new UserPostArray();
                                mPost.setPost(post);
                                allUsersPosts.add(mPost);
                        }
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }

                etSearchUsers.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String text = etSearchUsers.getText().toString().toLowerCase(Locale.getDefault());
                        adapter.filter(text, allUsersPosts);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

            }
        });
    }

    private void queryUsers() {
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.addAscendingOrder(KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                for (ParseUser user : users ) {
                    boolean currUserIsBlocked = false;

                    try {
                        List<ParseUser> blockedUsers = getBlockedUsers(user);
                        for (ParseUser bUser : blockedUsers) {
                            if (bUser.getObjectId().equals(currentUser.getObjectId())) {
                                currUserIsBlocked = true;
                                // this user is blocked, so they shouldn't show up in the search bar
                                break;
                            }
                        }
                        if (!currUserIsBlocked) {
                            UserPostArray mUser = new UserPostArray();
                            mUser.setUser(user);
                            allUsersPosts.add(mUser);
                        }
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }

                queryPosts();
            }
        });
    }

    /**
     * Initiate toggle: when you click the search icon it pops up the editText and
     * clicking the back button goes to the search icon again
     */
    private void toggleToolBarState() {
        if (mAppBarState == STANDARD_APPBAR) {
            setAppBarState(SEARCH_APPBAR);
        } else {
            setAppBarState(STANDARD_APPBAR);
        }
    }

    /**
     * Sets the appbar state for either search mode or standard mode.
     * @param state An integer that describes the state (search or standard) of the app bar.
     */
    private void setAppBarState(int state) {

        mAppBarState = state;
        if (mAppBarState == STANDARD_APPBAR) {
            searchBar.setVisibility(View.GONE);
            viewUsersBar.setVisibility(View.VISIBLE);

            View view = getView();
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                im.hideSoftInputFromWindow(view.getWindowToken(), 0); // make keyboard hide
            } catch (NullPointerException e) {
                e.printStackTrace();
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