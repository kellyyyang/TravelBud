package com.codepath.travelbud.fragments;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.codepath.travelbud.Hashtag;
import com.codepath.travelbud.Post;
import com.codepath.travelbud.PostsAdapter;
import com.codepath.travelbud.R;
import com.codepath.travelbud.UserDetailsActivity;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    private RecyclerView rvHome;
    private PostsAdapter adapter;
    private List<Post> allPosts;
    private List<String> allHashtagsString;
    private List<Hashtag> allHashtagsObject;
    private String hashtagSelected;
    private AutoCompleteTextView actvHashtagFilter;
    private String emptyString = "";

    ParseUser currentUser = ParseUser.getCurrentUser();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHome = view.findViewById(R.id.rvHome);
        actvHashtagFilter = view.findViewById(R.id.actvHashtagFilter);

        allHashtagsString = new ArrayList<>();
        allHashtagsObject = new ArrayList<>();
        queryHashtags();
        ArrayAdapter<String> adapterHashtag = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, allHashtagsString);
        actvHashtagFilter.setAdapter(adapterHashtag);

        final TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s == null || s.equals("")) {
                    hashtagSelected = null;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null || s.length() == 0) {
                    hashtagSelected = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.equals("")) {
                    hashtagSelected = null;
                }
            }
        };

        actvHashtagFilter.addTextChangedListener(mTextWatcher);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts, false);

        rvHome.setAdapter(adapter);
        rvHome.setLayoutManager(new LinearLayoutManager(getContext()));

        actvHashtagFilter.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && (hashtagSelected == null)) {
                    // Perform action on key press
                    InputMethodManager mgr = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(actvHashtagFilter.getWindowToken(), 0);
                    try {
                        queryPostsHT();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });

        actvHashtagFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hashtagSelected = (String) parent.getItemAtPosition(position);
                Log.i(TAG, "item selected: " + hashtagSelected);
                adapter.notifyDataSetChanged();
                InputMethodManager mgr = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(actvHashtagFilter.getWindowToken(), 0);
                try {
                    queryPostsHT();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            Log.i(TAG, "attempting to query posts");
            queryPostsHT();
        } catch (ParseException e) {
            Log.e(TAG, "exception with queryPosts: " + e);
        }
    }

    // get all hashtags
    private void queryHashtags() {
        ParseQuery<Hashtag> query = ParseQuery.getQuery(Hashtag.class);
        query.include("hashtag");
        query.findInBackground(new FindCallback<Hashtag>() {
            @Override
            public void done(List<Hashtag> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting hashtags", e);
                    return;
                }
                for (Hashtag tag : objects) {
                    Log.i(TAG, "Tag string: " + tag.getHashtag());
                    allHashtagsString.add(tag.getHashtag());
                }
                allHashtagsObject.addAll(objects);
            }
        });
    }

    private void queryPosts() throws ParseException {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        ParseRelation<ParseUser> relation = currentUser.getRelation("following");

        ParseQuery<ParseUser> followingQuery = relation.getQuery();
        followingQuery.include("following");
        List<ParseUser> users = followingQuery.find();
        Log.i(TAG, "trying to get list of following: " + users);

        query.whereContainedIn("user", users);

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

                Log.i(TAG, "allPosts: " + allPosts);

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("home_post_bundleKey", (ArrayList<? extends Parcelable>) allPosts);
                getParentFragmentManager().setFragmentResult("home_post_requestKey", bundle);
            }
        });
    }

    private void queryPostsHT() throws ParseException {

        allPosts.clear();

        ParseQuery<Post> query;
        query = null;
        Log.i(TAG, "in queryPostsHT");

        if (hashtagSelected == null) {
            Log.i(TAG, "in hashtag is null: " + hashtagSelected);
            query = ParseQuery.getQuery(Post.class);
            query.include(Post.KEY_USER);
        }
        else {
            Log.i(TAG, "in hashtag is not null: " + hashtagSelected);
            ParseQuery<Hashtag> queryHT = ParseQuery.getQuery(Hashtag.class);
            queryHT.include(Hashtag.KEY_HASHTAG);
            List<Hashtag> hashtagsQueryList = queryHT.find();
            for (Hashtag tag : hashtagsQueryList) {
                if (Objects.equals(tag.getHashtag(), hashtagSelected)) {
                    ParseRelation<Post> hashtagPostRelation = tag.getRelation(Hashtag.KEY_POSTS);
                    query = hashtagPostRelation.getQuery();
                    query.include(Post.KEY_USER);
                    break;
                }
            }
        }

        ParseRelation<ParseUser> relation = currentUser.getRelation("following");

        ParseQuery<ParseUser> followingQuery = relation.getQuery();
        followingQuery.include("following");
        List<ParseUser> users = followingQuery.find();
        Log.i(TAG, "trying to get list of following: " + users);

        query.whereContainedIn("user", users);

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

                Log.i(TAG, "allPosts: " + allPosts);

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("home_post_bundleKey", (ArrayList<? extends Parcelable>) allPosts);
                getParentFragmentManager().setFragmentResult("home_post_requestKey", bundle);
            }
        });
    }
}