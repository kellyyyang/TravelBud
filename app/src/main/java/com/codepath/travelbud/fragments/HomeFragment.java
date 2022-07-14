package com.codepath.travelbud.fragments;

import static com.codepath.travelbud.Post.KEY_HASHTAGS;
import static com.codepath.travelbud.SignUpActivity.KEY_INTERESTS;

import android.content.Context;
import android.os.Bundle;
import java.lang.Math;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.codepath.travelbud.Follow;
import com.codepath.travelbud.Hashtag;
import com.codepath.travelbud.MapUtil;
import com.codepath.travelbud.Post;
import com.codepath.travelbud.PostsAdapter;
import com.codepath.travelbud.R;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    private RecyclerView rvHome;
    private PostsAdapter adapter;
    private List<Post> allPosts;
    private List<Post> rankedPosts;
    private List<Post> myPosts;
    private List<String> allHashtagsString;
    private List<Hashtag> allHashtagsObject;
    private String hashtagSelected;
    private List<ParseUser> followingUsers;
    private AutoCompleteTextView actvHashtagFilter;
    private List<Hashtag> myHashtags;
    private String emptyString = "";
    private Map<String, Integer> hMap;
    private List<String> mInterests;
    private Map<Post, Double> postTreeMap;
    private Map<ParseGeoPoint, Integer> myLocations;

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

        postTreeMap = new HashMap<>();
        hMap = new HashMap<String, Integer>();
        myPosts = new ArrayList<>();
        myHashtags = new ArrayList<>();
        allHashtagsString = new ArrayList<>();
        allHashtagsObject = new ArrayList<>();
        followingUsers = new ArrayList<>();
        try {
            queryGetFollowing();
            Log.i(TAG, "List of following main: " + followingUsers);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        queryHashtags();
        ArrayAdapter<String> adapterHashtag = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, allHashtagsString);
        actvHashtagFilter.setAdapter(adapterHashtag);

        mInterests = currentUser.getList(KEY_INTERESTS);

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

        rankedPosts = new ArrayList<>();
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), rankedPosts, false); // TODO: check!

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

    private void queryGetFollowing() throws ParseException {
        ParseQuery<Follow> query = ParseQuery.getQuery(Follow.class);
        query.include(Follow.KEY_FOLLOWER);
        query.whereEqualTo(Follow.KEY_FOLLOWER, currentUser);
//        query.findInBackground(new FindCallback<Follow>() {
//            @Override
//            public void done(List<Follow> followRelations, ParseException e) {
//                for (Follow follow : followRelations) {
//                    followingUsers.add(follow.getFollowing());
//                }
//            }
//        });
        List<Follow> followRelations = query.find();
        for (Follow follow : followRelations) {
            followingUsers.add(follow.getFollowing());
        }
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

        assert query != null;
        query.whereContainedIn("user", followingUsers);
        query.setLimit(20);

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

                try {
                    rankPosts(allPosts);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                adapter.notifyDataSetChanged();

                Log.i(TAG, "allPosts: " + allPosts);

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("home_post_bundleKey", (ArrayList<? extends Parcelable>) allPosts);
                getParentFragmentManager().setFragmentResult("home_post_requestKey", bundle);
            }
        });
    }

    private void rankPosts(List<Post> allPosts) throws ParseException {
        queryMyPosts();
        findMyHashtags();
        int MAX_TAGS = findMaxHashtags();
        int MAX_INTS = findMaxInterests();
        for (Post post : allPosts) {
            double score = calculateScore(MAX_TAGS, MAX_INTS, post);
            postTreeMap.put(post, score);
        }
        Log.i(TAG, "treE: " + postTreeMap);
        postTreeMap = MapUtil.sortByValue(postTreeMap);
        for (Map.Entry<Post, Double> entry : postTreeMap.entrySet()) {
            rankedPosts.add(entry.getKey());
        }
    }

    private double calculateScore(int max_tags, int max_ints, Post post) throws ParseException {
        double totalScore = 0;
        double totalTags = 0;
        ParseRelation<Hashtag> relation = post.getRelation(KEY_HASHTAGS);
        // calculate hashtag score
        for (Hashtag mTag : relation.getQuery().find()) {
            if (myHashtags.contains(mTag)) {
                totalTags += 1;
            }
        }
        if (max_tags != 0) {
            totalScore += (totalTags / max_tags) * 3;
        }
        // calculate interests score
        if (max_ints != 0) {
            double intScore = (hMap.get(post.getUser().getObjectId()) / max_ints) * 5;
            totalScore += intScore;
        }
        Log.i(TAG, "score: total " + totalScore);
        // calculate location score
        // TODO
        Log.i(TAG, "score: " + post.getUser().getUsername() + ", " + post.getUser().getObjectId() + ", " + hMap.get(post.getUser().getObjectId()) + ", " + post.getLocationString() + totalScore);
        return totalScore;
    }

    // r = radius of sphere
    // a, b = latitudes
    // x, y = longitudes
    private double haversine(double r, double a, double b, double x, double y) {
        double arg1 = Math.cos(a) * Math.cos(b) * Math.cos(x - y);
        double arg2 = Math.sin(a) * Math.sin(b);
        return r * Math.acos(arg1 + arg2);
    }

    private int findMaxInterests() {
        int maxInts = 0;
        for (Post mPost : allPosts) {
            int currMax = 0;
            ParseUser mUser = mPost.getUser();

            List<String> userInterests = currentUser.getList(KEY_INTERESTS);

            assert userInterests != null;
            for (String interest : userInterests) {
                if (mInterests.contains(interest)) {
                    currMax += 1;
                }
            }
            // update user max tags in hash map
            if (hMap.containsKey(mUser.getObjectId())) {
                if (currMax > hMap.get(mUser.getObjectId())) {
                    hMap.put(mUser.getObjectId(), currMax);
                }
            } else {
                hMap.put(mUser.getObjectId(), currMax);
            }

            if (currMax > maxInts) {
                maxInts = currMax;
            }
        }
        Log.i(TAG, "findMaxInterests: " + hMap);
        return maxInts;
    }

    private int findMaxHashtags() throws ParseException {
        int maxTags = 0;
        for (Post mPost : allPosts) {
            List<Hashtag> postHashtags = getPostHashtags(mPost);
            int currTags = 0;
            for (Hashtag tag : postHashtags) {
                if (myHashtags.contains(tag)) {
                    currTags += 1;
                }
            }
            if (currTags > maxTags) {
                maxTags = currTags;
            }
        }
        return maxTags;
    }

    private List<Hashtag> getPostHashtags(Post thisPost) throws ParseException {
        ParseRelation<Hashtag> relation = thisPost.getRelation(KEY_HASHTAGS);
        ParseQuery<Hashtag> query = relation.getQuery();
        return query.find();
    }

    private void queryMyPosts() {
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
                myPosts.addAll(posts);

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("my_posts_bundleKey", (ArrayList<? extends Parcelable>) allPosts);
                getParentFragmentManager().setFragmentResult("my_posts_requestKey", bundle);
            }
        });
    }

    private void findMyHashtags() {
        for (Post mPost : myPosts) {
            ParseRelation<Hashtag> relation = mPost.getRelation(KEY_HASHTAGS);
            ParseQuery<Hashtag> query = relation.getQuery();
            query.findInBackground(new FindCallback<Hashtag>() {
                @Override
                public void done(List<Hashtag> objects, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "error with findMyHashtags");
                    }
                    myHashtags.addAll(objects);
                }
            });
        }
    }
}