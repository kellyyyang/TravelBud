package com.codepath.travelbud.fragments.search;

import static com.codepath.travelbud.models.Post.KEY_HASHTAGS;
import static com.codepath.travelbud.activities.SignUpActivity.KEY_INTERESTS;
import static com.codepath.travelbud.fragments.HomeFragment.KEY_FOLLOWING;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.travelbud.adapters.ExploreAdapter;
import com.codepath.travelbud.models.Hashtag;
import com.codepath.travelbud.utils.MapUtil;
import com.codepath.travelbud.utils.MutableFloat;
import com.codepath.travelbud.models.Post;
import com.codepath.travelbud.R;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    public static final String TAG = "ExploreFragment";

    private RecyclerView rvExplore;
    private ExploreAdapter adapter;
    private List<Post> allPosts;
    private List<ParseUser> users;
    private List<ParseUser> followingUsers;
    private Map<String, MutableFloat> interestsMap;
    private Map<ParseUser, Float> rankingMap; // ranking map for users
    private List<ParseUser> rankedUsers;
    private int totalInterests;

    private List<Post> rankedPosts;
    private Map<Post, Float> rankPostsMap;
    private List<String> myHashtagString;
    private Map<String, Integer> hMap;
    private Map<ParseGeoPoint, Double> myLocations;
    private List<String> mInterests;
    private List<Post> myPosts;
    private List<String> postHashtags;
    private ParseGeoPoint mLocation;

    private ParseUser currentUser;
    private SwipeRefreshLayout swipeContainerExplore;

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

        currentUser = ParseUser.getCurrentUser();
        swipeContainerExplore = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainerExplore);

        rvExplore = view.findViewById(R.id.rvExplore);
        allPosts = new ArrayList<>();
        users = new ArrayList<>();
        rankedUsers = new ArrayList<>();
        followingUsers = new ArrayList<>();
        interestsMap = new HashMap<>();
        rankingMap = new HashMap<>();
        totalInterests = 0;

        rankedPosts = new ArrayList<>();
        rankPostsMap = new HashMap<>();
        myHashtagString = new ArrayList<>();
        hMap = new HashMap<String, Integer>();
        myLocations = new HashMap<>();
        mInterests = currentUser.getList(KEY_INTERESTS);
        myPosts = new ArrayList<>();
        postHashtags = new ArrayList<>();

        mLocation = currentUser.getParseGeoPoint("last_location");

        adapter = new ExploreAdapter(getContext(), rankedUsers, rankedPosts);

        rvExplore.setAdapter(adapter);
        rvExplore.setLayoutManager(new GridLayoutManager(getContext(), 3));

        swipeContainerExplore.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();

                allPosts.clear();
                users.clear();
                rankedUsers.clear();
                followingUsers.clear();
                interestsMap.clear();
                rankingMap.clear();
                rankedPosts.clear();
                rankPostsMap.clear();
                myHashtagString.clear();
                hMap.clear();
                myLocations.clear();
                mInterests.clear();
                myPosts.clear();
                postHashtags.clear();

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
                try {
                    queryGetFollowing();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                queryInterests();
                rankUsers();
                adapter.notifyDataSetChanged();
                swipeContainerExplore.setRefreshing(false);
            }
        });

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
        try {
            queryGetFollowing();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        queryInterests();

        loadUserData();

        if (rankedUsers.size() == 0) {
            rankUsers();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveUserData();
        savePostData();
    }

    public boolean containsName(final List<ParseUser> list, final String objectId){
        return list.stream().anyMatch(o -> objectId.equals(o.getObjectId()));
    }

    private void queryUsers() throws ParseException {
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.include("username");
        ArrayList<String> me = new ArrayList<>();
        me.add(currentUser.getObjectId());
        query.whereNotContainedIn("objectId", me);
        query.addAscendingOrder("username");
        users.addAll(query.find());
    }

    private void queryPosts() throws ParseException {
        queryMyPosts();
        findMyHashtags();

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereNotContainedIn("user", followingUsers);
        query.whereContainedIn("visibility", new ArrayList<Integer>(Arrays.asList(null, 0)));
        query.addDescendingOrder("createdAt");
        allPosts.addAll(query.find());

        loadPostData();
        if (rankedPosts.size() == 0) {
            rankPosts(allPosts);
        }
    }

    // find the users that currentUser is following
    private void queryGetFollowing() throws ParseException {

        ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation(KEY_FOLLOWING);
        ParseQuery<ParseUser> followingQuery = relation.getQuery();
        List<ParseUser> followingL = followingQuery.find();
        followingUsers.addAll(followingL);
    }

    // get the interests of those currentUser is following
    private void queryInterests() {
        // fill interestsMap with key:value pairs of interest:count
        for (ParseUser user : followingUsers) {
            List<String> uInts = new ArrayList<>(Objects.requireNonNull(user.getList("interests")));
            for (String interest : uInts) {
                MutableFloat count = interestsMap.get(interest);
                if (count == null) {
                    interestsMap.put(interest, new MutableFloat());
                }
                else {
                    count.increment();
                }
                totalInterests += 1;
            }
        }

        // now, weight the counts of each interest
        for (Map.Entry<String, MutableFloat> entry : interestsMap.entrySet()) {
            entry.getValue().divideBy(totalInterests);
        }
    }

    public void rankUsers() {
        // fill rankingMap with key:value pairs of ParseUser:score, where score is calculated by
        // the weights of the interests the user has in common with the currentUser's following
        rankedUsers.clear();

        for (ParseUser pUser : users) {
            if (!containsName(followingUsers, pUser.getObjectId())) {
                List<String> pInts = new ArrayList<>(Objects.requireNonNull(pUser.getList("interests")));
                rankingMap.put(pUser, (float) 0);
                for (Object interest : pInts) {
                    MutableFloat count = interestsMap.get(interest);
                    if (count != null) {
                        rankingMap.put(pUser, rankingMap.get(pUser) + count.get());
                    }
                }
            }
        }

        // now, we add location to each user's score, based on how close their last registered
        // location is to the currentUser's
        for (Map.Entry<ParseUser, Float> entry : rankingMap.entrySet()) {
            ParseUser pUser = entry.getKey();
            ParseGeoPoint pUserLastLoc = pUser.getParseGeoPoint("last_location");
            if (mLocation != null && pUserLastLoc != null) {
                double userLat = pUserLastLoc.getLatitude();
                double userLong = pUserLastLoc.getLongitude();
                double mLat = mLocation.getLatitude();
                double mLong = mLocation.getLongitude();
                float distBetween = haversine(userLat, mLat, userLong, mLong);
                // scale the second val to be 0.2 when distBetween == 0
                rankingMap.put(entry.getKey(), entry.getValue() + 1/(distBetween + 5));
            }
        }

        // sort rankingMap by value so that those with the lowest value come first
        rankingMap = MapUtil.sortByValue(rankingMap);
        for (Map.Entry<ParseUser, Float> entry : rankingMap.entrySet()) {
            rankedUsers.add(entry.getKey());
        }
        Collections.reverse(rankedUsers);
    }

    /**
     * Takes a list of all posts and ranks them
     * @param allPosts The list of all posts
     */
    private void rankPosts(List<Post> allPosts) throws ParseException {
        rankedPosts.clear();
        int MAX_TAGS = findMaxHashtags();
        int MAX_INTS = findMaxInterests();
        for (Post post : allPosts) {
            double score = calculateScore(MAX_TAGS, MAX_INTS, post);
            // the higher (10 - score) -> the lower the "rank", i.e., if (10 - score) is high, then this post is not very relevant
            rankPostsMap.put(post, (float) (10 - score));
        }
        rankPostsMap = MapUtil.sortByValue(rankPostsMap);
        for (Map.Entry<Post, Float> entry : rankPostsMap.entrySet()) {
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
    public static float haversine(double a, double b, double x, double y) {
        float RADIUS = 6371;
        double arg1 = Math.cos(a) * Math.cos(b) * Math.cos(x - y);
        double arg2 = Math.sin(a) * Math.sin(b);
        return (float) (RADIUS * Math.acos(arg1 + arg2));
    }

    /**
     * Finds the maximum number of interests between a single user and the currentUser.
     * @return The maximum number of interests between a single user and the currentUser.
     */
    private int findMaxInterests() {
        int maxInts = 0;
        // query all posts to find their creators/users
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
        return maxInts;
    }

    private int findMaxHashtags() throws ParseException {
        int maxTags = 0;
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

    private void getPostHashtags(Post thisPost) throws ParseException {
        ParseRelation<Hashtag> relation = thisPost.getRelation(KEY_HASHTAGS);
        ParseQuery<Hashtag> query = relation.getQuery();
        List<Hashtag> listHashtags = query.find();
        for (Hashtag tag : listHashtags) {
            postHashtags.add(tag.getHashtag());
        }
    }

    private void queryMyPosts() throws ParseException {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder("createdAt");
        List<Post> myPostsL = query.find();
        myPosts.addAll(myPostsL);
    }

    public void loadPostData() {
        SharedPreferences sh = requireContext().getSharedPreferences("MyPostPref", Context.MODE_PRIVATE);

        rankedPosts.clear();
        rankPostsMap.clear();

        for (Post post : allPosts) {
            if (sh.contains(post.getObjectId())) {
                float s1 = sh.getFloat(post.getObjectId(), 0);
                rankPostsMap.put(post, s1);
            }
        }

        rankPostsMap = MapUtil.sortByValue(rankPostsMap);
        for (Map.Entry<Post, Float> entry : rankPostsMap.entrySet()) {
            rankedPosts.add(entry.getKey());
        }
    }

    public void savePostData() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPostPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // write all the data entered by the user in SharedPreference and apply
        for (Map.Entry<Post, Float> entry : rankPostsMap.entrySet()) {
            myEdit.putFloat(entry.getKey().getObjectId(), entry.getValue());
        }
        myEdit.apply();

    }

    public void loadUserData() {
        SharedPreferences sh = requireActivity().getSharedPreferences("MyUserPref", Context.MODE_PRIVATE);

        rankedUsers.clear();
        rankingMap.clear();

        for (ParseUser user : users) {
            float s1 = sh.getFloat(user.getObjectId(), 0);
            rankingMap.put(user, s1);
        }

        rankingMap = MapUtil.sortByValue(rankingMap);
        for (Map.Entry<ParseUser, Float> entry : rankingMap.entrySet()) {
            rankedUsers.add(entry.getKey());
        }
        Collections.reverse(rankedUsers);
    }

    public void saveUserData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyUserPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // write all the data entered by the user in SharedPreference and apply
        for (Map.Entry<ParseUser, Float> entry : rankingMap.entrySet()) {
            myEdit.putFloat(entry.getKey().getObjectId(), entry.getValue());
        }
        myEdit.apply();

    }

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