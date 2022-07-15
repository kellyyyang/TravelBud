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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<String> postHashtags;
    private String hashtagSelected;
    private List<ParseUser> followingUsers;
    private AutoCompleteTextView actvHashtagFilter;
    private List<Hashtag> myHashtags;
    private List<String> myHashtagString;
    private Map<String, Integer> hMap;
    private List<String> mInterests;
    private Map<Post, Double> postTreeMap;
    private Map<ParseGeoPoint, Double> myLocations;  // location, rating
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

        rvHome = view.findViewById(R.id.rvHome);
        actvHashtagFilter = view.findViewById(R.id.actvHashtagFilter);

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
        adapter = new PostsAdapter(getContext(), rankedPosts, false);

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
        rankedPosts.clear();

        myPosts.clear();
        postHashtags.clear();
        myHashtagString.clear();
        hMap.clear();
        postTreeMap.clear();


        Log.i(TAG, "querying postsHT: " + allPosts + ", " + rankedPosts);
        Log.i(TAG, "hashtagSelected: " + hashtagSelected);

        queryMyPosts();
        findMyHashtags();

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
                if (tag.getHashtag().equals(hashtagSelected)) {
                    ParseRelation<Post> hashtagPostRelation = tag.getRelation(Hashtag.KEY_POSTS);  // get all posts with hashtagSelected
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
                    adapter.notifyDataSetChanged();
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
        int MAX_TAGS = findMaxHashtags();
        int MAX_INTS = findMaxInterests();
        Log.i(TAG, "MAX_TAGS: " + MAX_TAGS);
        Log.i(TAG, "MAX_INTS: " + MAX_INTS);
        for (Post post : allPosts) {
            double score = calculateScore(MAX_TAGS, MAX_INTS, post);
            postTreeMap.put(post, 10 - score); // higher (10 - score) = the lower the "rank", i.e., if (10 - score) is high, then this post is not very relevant
        }
        Log.i(TAG, "treE: " + postTreeMap);
        postTreeMap = MapUtil.sortByValue(postTreeMap);
        for (Map.Entry<Post, Double> entry : postTreeMap.entrySet()) {
            rankedPosts.add(entry.getKey());
            Log.i(TAG, "ranked post: " + entry.getKey() + ", " + entry.getKey().getUser().getUsername() + ", " + entry.getValue());
        }
    }

    private double calculateScore(int max_tags, int max_ints, Post post) throws ParseException {
        double totalScore = 0;
        double totalTags = 0;

        ParseRelation<Hashtag> relation = post.getRelation(KEY_HASHTAGS);
        ParseQuery<Hashtag> query = relation.getQuery();
        List<Hashtag> hashtagList = query.find();
        List<String> hashtagListString = new ArrayList<>();
        for (Hashtag tag : hashtagList) {
            hashtagListString.add(tag.getHashtag());
        }

        for (String mTag : hashtagListString) {
            Log.i(TAG, "checking: " + myHashtagString.contains(mTag));
            for (String t : myHashtagString) {
                Log.i(TAG, "LOOK: " + t + " " + mTag);
                if (t.equals(mTag)) {
                    totalTags += 1;
                }
            }
        }

        // calculate hashtag score
        for (Hashtag mTag : relation.getQuery().find()) {
            Log.i(TAG, "mTag.getHashtag: " + mTag.getHashtag());

        }
        if (max_tags != 0) {
            totalScore += (totalTags / max_tags) * 5;
            Log.i(TAG, "tag score: " + totalScore + ", " + post.getUser().getUsername() + ", " + post.getLocationString());
        }
        // calculate interests score
        if (max_ints != 0) {
            double intScore = (hMap.get(post.getUser().getObjectId()) / max_ints) * 3;
            Log.i(TAG, "int score: " + intScore + ", " + post.getUser().getUsername() + ", " + post.getLocationString());
            totalScore += intScore;
        }
        // calculate location score
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
        Log.i(TAG, "location score: " + locationScore + ", " + post.getUser().getUsername() + ", " + post.getLocationString());

        totalScore += locationScore;

        Log.i(TAG, "score: " + post.getUser().getUsername() + ", " + post.getUser().getObjectId() + ", " + hMap.get(post.getUser().getObjectId()) + ", " + post.getLocationString() + totalScore);
        return totalScore;
    }

    // finds the closest location to given lat and long
    private double[] locationWithinRadiusRating(double lat, double lon) {
        double closestDist = Double.POSITIVE_INFINITY;
        double finalRating = 0;
        for (Map.Entry<ParseGeoPoint, Double> entry : myLocations.entrySet()) {
//            rankedPosts.add(entry.getKey());
            double haversineDist = haversine(entry.getKey().getLatitude(), lat, entry.getKey().getLongitude(), lon);
            if (haversineDist < closestDist && haversineDist < 20) {
                closestDist = haversineDist;
                finalRating = entry.getValue();
            }
        }
        return new double[]{closestDist, finalRating};
    }


    private void setMyLocations() {
        for (Post mPost : myPosts) {
            // TODO: maybe someone has two instances of the same location
            Log.i(TAG, "setMyLocations: " + mPost.getLocation() + ", rating: " + mPost.getRating());
            myLocations.put(mPost.getLocation(), (double) mPost.getRating());
        }
    }

    // r = radius of sphere
    // a, b = latitudes
    // x, y = longitudes
    private double haversine(double a, double b, double x, double y) {
        double arg1 = Math.cos(a) * Math.cos(b) * Math.cos(x - y);
        double arg2 = Math.sin(a) * Math.sin(b);
        return RADIUS * Math.acos(arg1 + arg2);
    }

    private int findMaxInterests() {
        int maxInts = 0;
        for (Post mPost : allPosts) {
            int currMax = 0;
            ParseUser mUser = mPost.getUser();

            List<String> userInterests = mUser.getList(KEY_INTERESTS);

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
        Log.i(TAG, "myHashtagString: " + myHashtagString);
        for (Post mPost : allPosts) {
            postHashtags.clear();
            getPostHashtags(mPost);
            Log.i(TAG, "postHashtags: " + postHashtags);
            int currTags = 0;
            Log.i(TAG, "postHashtags: " + postHashtags);
            for (String tag : postHashtags) {
                Log.i(TAG, "tag_here: " + tag);
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

    private void getPostHashtags(Post thisPost) throws ParseException {
        ParseRelation<Hashtag> relation = thisPost.getRelation(KEY_HASHTAGS);
        ParseQuery<Hashtag> query = relation.getQuery();
        List<Hashtag> listHashtags = query.find();
        for (Hashtag tag : listHashtags) {
            postHashtags.add(tag.getHashtag());
        }
//        return query.find();
    }

    private void queryMyPosts() throws ParseException {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder("createdAt");
        List<Post> myPostsL = query.find();
        for (Post mPost : myPostsL) {
            myPosts.add(mPost);
        }
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
//                myPosts.addAll(posts);
//
//                Bundle bundle = new Bundle();
//                bundle.putParcelableArrayList("my_posts_bundleKey", (ArrayList<? extends Parcelable>) allPosts);
//                getParentFragmentManager().setFragmentResult("my_posts_requestKey", bundle);
//            }
//        });
    }

    private void findMyHashtags() throws ParseException {
        Log.i(TAG, "myPosts: " + myPosts);
        for (Post mPost : myPosts) {
            ParseRelation<Hashtag> relation = mPost.getRelation(KEY_HASHTAGS);
            ParseQuery<Hashtag> query = relation.getQuery();
            List<Hashtag> myHashtagsL = query.find();
            Log.i(TAG, "inFindMyHTs: " + myHashtagsL);
            for (Hashtag tag : myHashtagsL) {
                myHashtagString.add(tag.getHashtag());
            }
        }
    }
}