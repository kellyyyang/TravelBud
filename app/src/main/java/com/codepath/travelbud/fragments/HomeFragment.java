package com.codepath.travelbud.fragments;

import static com.codepath.travelbud.models.Post.KEY_HASHTAGS;
import static com.codepath.travelbud.activities.SignUpActivity.KEY_INTERESTS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import java.lang.Math;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Parcelable;
import android.preference.PreferenceManager;
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

import com.codepath.travelbud.models.Hashtag;
import com.codepath.travelbud.utils.MapUtil;
import com.codepath.travelbud.models.Post;
import com.codepath.travelbud.adapters.PostsAdapter;
import com.codepath.travelbud.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    public static final String KEY_FOLLOWING = "following";

    private RecyclerView rvHome;
    private PostsAdapter adapter;
    private List<Post> allPosts;
    private List<Post> rankedPosts;
    private List<Post> myPosts;
    private List<String> allHashtagsString;
    private List<Hashtag> allHashtagsObject;
    private List<String> postHashtags;
    private String hashtagSelected;
    private List<ParseUser> followingUsers;
    private AutoCompleteTextView actvHashtagFilter;
    private List<Hashtag> myHashtags;
    private List<String> myHashtagString;
    private Map<String, Integer> hMap;
    private List<String> mInterests;
    private Map<Post, Float> postTreeMap;
    private Map<ParseGeoPoint, Double> myLocations;  // location, rating
    private int SWIPE_REFRESH;
//    PostDatabase db;

    private SwipeRefreshLayout swipeContainer;
    private double RADIUS = 6371;

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

        SWIPE_REFRESH = 0;

        rvHome = view.findViewById(R.id.rvHome);
        actvHashtagFilter = view.findViewById(R.id.actvHashtagFilter);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        myLocations = new HashMap<>();
        postTreeMap = new HashMap<>();
        hMap = new HashMap<String, Integer>();
        myPosts = new ArrayList<>();
        myHashtags = new ArrayList<>();
        allHashtagsString = new ArrayList<>();
        allHashtagsObject = new ArrayList<>();
        followingUsers = new ArrayList<>();
        myHashtagString = new ArrayList<>();
        postHashtags = new ArrayList<>();

        try {
            // get all the users that currentUser is following
            queryGetFollowing();
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
        adapter = new PostsAdapter(getContext(), rankedPosts, false);

        rvHome.setAdapter(adapter);
        rvHome.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SWIPE_REFRESH = 1;  // completely re-query posts
                adapter.clear();
                try {
                    queryPostsHT();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                swipeContainer.setRefreshing(false);
            }
        });

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
            queryPostsHT();
        } catch (ParseException e) {
            Log.e(TAG, "exception with queryPosts: " + e);
        }
    }

    /**
     * Gets posts and their scores from local cache SharedPreferences and loads them.
     */
    public void loadData() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getContext());

        for (Post post : allPosts) {
            float s1 = sh.getFloat(post.getObjectId(), 0);
            postTreeMap.put(post, s1);
        }

        postTreeMap = MapUtil.sortByValue(postTreeMap);
        for (Map.Entry<Post, Float> entry : postTreeMap.entrySet()) {
            rankedPosts.add(entry.getKey());
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Saves the current posts and their scores
     */
    public void saveData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // write all the data entered by the user in SharedPreference and apply
        for (Map.Entry<Post, Float> entry : postTreeMap.entrySet()) {
            myEdit.putFloat(entry.getKey().getObjectId(), entry.getValue());
        }
        myEdit.apply();

    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    /**
     * Finds all hashtags in the database.
     */
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
                    allHashtagsString.add(tag.getHashtag());
                }
                allHashtagsObject.addAll(objects);
            }
        });
    }

    /**
     * Finds the users that the currentUser is following.
     * @throws ParseException
     */
    private void queryGetFollowing() throws ParseException {

        ParseRelation<ParseUser> relation = currentUser.getRelation(KEY_FOLLOWING);
        ParseQuery<ParseUser> followingQuery = relation.getQuery();
        List<ParseUser> followingL = followingQuery.find();
        for (ParseUser pUser : followingL) {
            followingUsers.add(pUser);
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("following_users_bundleKey", (ArrayList<? extends Parcelable>) followingUsers);
        getParentFragmentManager().setFragmentResult("following_users_requestKey", bundle);
    }

    /**
     * Finds all the posts that contain the hashtagSelected (i.e., the hashtag entered
     * into the Filter bar.
     * @throws ParseException
     */
    private void queryPostsHT() throws ParseException {

        allPosts.clear();
        rankedPosts.clear();

        myPosts.clear();
        postHashtags.clear();
        myHashtagString.clear();
        hMap.clear();
        postTreeMap.clear();

        queryMyPosts();
        findMyHashtags();

        ParseQuery<Post> query;
        query = null;

        // if there is no hashtag in the filter bar, query all posts of followed users
        // otherwise, only query posts that are relations of the hashtag selected
        if (hashtagSelected == null) {
            query = ParseQuery.getQuery(Post.class);
            query.include(Post.KEY_USER);
        }
        else {
            ParseQuery<Hashtag> queryHT = ParseQuery.getQuery(Hashtag.class);
            queryHT.include(Hashtag.KEY_HASHTAG);
            List<Hashtag> hashtagsQueryList = queryHT.find();
            for (Hashtag tag : hashtagsQueryList) {
                if (tag.getHashtag().equals(hashtagSelected)) {
                    ParseRelation<Post> hashtagPostRelation = tag.getRelation(Hashtag.KEY_POSTS);  // get all posts with hashtagSelected
                    query = hashtagPostRelation.getQuery();
                    query.include(Post.KEY_USER);
                    break;
                }
            }
        }

        // only look through posts of the users that currentUser is following
        // only look through posts that are labelled as visible to all followers or everyone
        assert query != null;
        query.whereContainedIn("user", followingUsers);
        query.whereContainedIn("visibility", new ArrayList<>(Arrays.asList(null, 0, 1)));
        query.setLimit(20);

        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                allPosts.addAll(posts);

                loadData();

                if (SWIPE_REFRESH == 1) {
                    try {
                        // rank the posts based on the ranking algorithm
                        rankPosts(allPosts);
                        adapter.notifyDataSetChanged();
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("home_post_bundleKey", (ArrayList<? extends Parcelable>) allPosts);
                getParentFragmentManager().setFragmentResult("home_post_requestKey", bundle);
            }
        });
    }

    /**
     * Takes a list of all posts and ranks them
     * @param allPosts The list of all posts
     */
    private void rankPosts(List<Post> allPosts) throws ParseException {
        int MAX_TAGS = findMaxHashtags();
        int MAX_INTS = findMaxInterests();
        for (Post post : allPosts) {
            float score = (float) calculateScore(MAX_TAGS, MAX_INTS, post);
            // higher (10 - score) = the lower the "rank", i.e., if (10 - score) is high,
            // then this post is not very relevant
            postTreeMap.put(post, 10 - score);
        }
        postTreeMap = MapUtil.sortByValue(postTreeMap);
        for (Map.Entry<Post, Float> entry : postTreeMap.entrySet()) {
            rankedPosts.add(entry.getKey());
        }
    }

    /**
     * Takes a list of factors and a post and calculates the post's score.
     * @param max_tags The maximum number of tags across all posts.
     * @param max_ints The maximum number of interests across all users.
     * @param post The post whose score will be calculated.
     * @return The score of the post.
     */
    private double calculateScore(int max_tags, int max_ints, Post post) throws ParseException {
        double totalScore = 0;
        double totalTags = 0;

        // query all hashtags in the post
        ParseRelation<Hashtag> relation = post.getRelation(KEY_HASHTAGS);
        ParseQuery<Hashtag> query = relation.getQuery();
        List<Hashtag> hashtagList = query.find();
        List<String> hashtagListString = new ArrayList<>();
        for (Hashtag tag : hashtagList) {
            hashtagListString.add(tag.getHashtag());
        }

        // count the total number of hashtags in common with one of the currentUser's hashtags
        for (String mTag : hashtagListString) {
            for (String t : myHashtagString) {
                if (t.equals(mTag)) {
                    totalTags += 1;
                }
            }
        }

        // the total score is 10 and hashtags are weighted 0.5
        // find the total weight of the tags in common
        if (max_tags != 0) {
            totalScore += (totalTags / max_tags) * 5;
        }

        // calculate interests score (weighted 0.3)
        if (max_ints != 0) {
            double intScore = (hMap.get(post.getUser().getObjectId()) / max_ints) * 3;
            totalScore += intScore;
        }

        // calculate location score (weighted 0.2)
        // if there is a location that currentUser has visited that is within 20 km of the user,
        // find the closest location and its rating, then calculate the location score
        double latitude = post.getLocation().getLatitude();
        double longitude = post.getLocation().getLongitude();
        setMyLocations();
        double[] distRatingL = locationWithinRadiusRating(latitude, longitude);
        double closestLocRating = distRatingL[1];
        double closestLocDist = distRatingL[0];
        if (distRatingL[0] == Double.POSITIVE_INFINITY) {
            closestLocDist = 20;
        }
        double locationScore = ((20 - closestLocDist) / 20) * 2 * (closestLocRating / 5);
        totalScore += locationScore;

        return totalScore;
    }

    /**
     * Takes in latitude and longitude of the user's post's location and finds the closest location
     * that the currentUser has been to and its rating.
     * @param lat The latitude of the user's post.
     * @param lon The longitude of the user's post.
     * @return An array containing [closest distance, rating of closest distance].
     */
    private double[] locationWithinRadiusRating(double lat, double lon) {
        double closestDist = Double.POSITIVE_INFINITY;
        double finalRating = 0;
        // calculate the Haversine distance from the user's post's location to each of the
        // currentUser's locations
        for (Map.Entry<ParseGeoPoint, Double> entry : myLocations.entrySet()) {
            double haversineDist = haversine(entry.getKey().getLatitude(), lat, entry.getKey().getLongitude(), lon);
            if (haversineDist < closestDist && haversineDist < 20) {
                closestDist = haversineDist;
                finalRating = entry.getValue();
            }
        }
        return new double[]{closestDist, finalRating};
    }

    /**
     * Finds the rating of each of the currentUser's visited locations
     */
    private void setMyLocations() {
        for (Post mPost : myPosts) {
            myLocations.put(mPost.getLocation(), (double) mPost.getRating());
        }
    }

    /**
     * Finds the Haversine distance between two points on the Earth in kilometers.
     * @param a The latitude of the first location.
     * @param b The latitude of the second location.
     * @param x The longitude of the first location.
     * @param y The longitude of the second location.
     * @return The Haversine distance between two points on the Earth in kilometers.
     */
    private double haversine(double a, double b, double x, double y) {
        double arg1 = Math.cos(a) * Math.cos(b) * Math.cos(x - y);
        double arg2 = Math.sin(a) * Math.sin(b);
        return RADIUS * Math.acos(arg1 + arg2);
    }

    /**
     * Finds the maximum number of interests between a single user and the currentUser.
     * @return The maximum number of interests between a single user and the currentUser.
     */
    private int findMaxInterests() {
        int maxInts = 0;
        // iterate through allPosts and find the number of interests the post's user has in
        // common with currentUser
        for (Post mPost : allPosts) {
            int currMax = 0;  // the current, updating maximum number of interests
            ParseUser mUser = mPost.getUser();

            List<String> userInterests = mUser.getList(KEY_INTERESTS);

            assert userInterests != null;
            for (String interest : userInterests) {
                if (mInterests.contains(interest)) {
                    currMax += 1;
                }
            }
            // update user max tags in hMap (hashmap) for later use
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
        return maxInts;
    }

    /**
     * Finds the maximum number of hashtags in common between a post and the currentUser's
     * hashtags.
     * @return The maximum number of hashtags in common a post and the currentUser's
     * hashtags.
     * @throws ParseException
     */
    private int findMaxHashtags() throws ParseException {
        int maxTags = 0;
        // iterate through allPosts and find the number of hashtags each post has in
        // common with currentUser's total hashtags
        for (Post mPost : allPosts) {
            postHashtags.clear();
            getPostHashtags(mPost);
            int currTags = 0;
            for (String tag : postHashtags) {
                if (myHashtagString.contains(tag)) {
                    currTags += 1;
                }
            }
            if (currTags > maxTags) {
                maxTags = currTags;
            }
        }
        return maxTags;
    }

    /**
     * Finds the hashtags in a post.
     * @param thisPost The post that we're getting hashtags from.
     * @throws ParseException
     */
    private void getPostHashtags(Post thisPost) throws ParseException {
        ParseRelation<Hashtag> relation = thisPost.getRelation(KEY_HASHTAGS);
        ParseQuery<Hashtag> query = relation.getQuery();
        List<Hashtag> listHashtags = query.find();
        for (Hashtag tag : listHashtags) {
            postHashtags.add(tag.getHashtag());
        }
    }

    /**
     * Queries the posts that the currentUser created.
     * @throws ParseException
     */
    private void queryMyPosts() throws ParseException {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder("createdAt");
        List<Post> myPostsL = query.find();
        for (Post mPost : myPostsL) {
            myPosts.add(mPost);
        }
    }

    /**
     * Finds all the hashtags that the currentUser has created.
     * @throws ParseException
     */
    private void findMyHashtags() throws ParseException {
        for (Post mPost : myPosts) {
            ParseRelation<Hashtag> relation = mPost.getRelation(KEY_HASHTAGS);
            ParseQuery<Hashtag> query = relation.getQuery();
            List<Hashtag> myHashtagsL = query.find();
            for (Hashtag tag : myHashtagsL) {
                myHashtagString.add(tag.getHashtag());
            }
        }
    }
}