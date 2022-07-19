package com.codepath.travelbud.fragments.search_and_explore;

import static com.codepath.travelbud.parse_classes.Post.KEY_HASHTAGS;
import static com.codepath.travelbud.SignUpActivity.KEY_INTERESTS;
import static com.codepath.travelbud.fragments.HomeFragment.KEY_FOLLOWING;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.travelbud.ExploreAdapter;
import com.codepath.travelbud.parse_classes.Hashtag;
import com.codepath.travelbud.helper_classes.MapUtil;
import com.codepath.travelbud.helper_classes.MutableDouble;
import com.codepath.travelbud.parse_classes.Post;
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
    private Map<String, MutableDouble> interestsMap;
    private Map<ParseUser, Double> rankingMap;
    private List<ParseUser> rankedUsers;
    private int totalInterests;

    private List<Post> rankedPosts;
    private Map<Post, Double> postTreeMap;
    private List<String> myHashtagString;
    private Map<String, Integer> hMap;
    private Map<ParseGeoPoint, Double> myLocations;
    private List<String> mInterests;
    private List<Post> myPosts;
    private List<String> postHashtags;

    private ParseGeoPoint mLocation;

    private ParseUser currentUser;

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

        rvExplore = view.findViewById(R.id.rvExplore);
        allPosts = new ArrayList<>();
        users = new ArrayList<>();
        rankedUsers = new ArrayList<>();
        followingUsers = new ArrayList<>();
        interestsMap = new HashMap<>();
        rankingMap = new HashMap<>();
        totalInterests = 0;

        rankedPosts = new ArrayList<>();
        postTreeMap = new HashMap<>();
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
//        getParentFragmentManager().setFragmentResultListener("following_users_requestKey", this, new FragmentResultListener() {
//            @Override
//            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
//                ArrayList<ParseUser> fUsers = result.getParcelableArrayList("following_users_bundleKey");
//                Log.i(TAG, "followingUsers list in bundle: " + fUsers);
//                followingUsers.addAll(fUsers);
//                queryInterests();
//
//            }
//        });
        try {
            queryGetFollowing();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        queryInterests();
//        Log.i(TAG, "allPosts: " + allPosts);
//        Log.i(TAG, "allUsers: " + users);
        adapter.notifyDataSetChanged();
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
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> objects, ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, "error with queryUsers: " + e);
//                    return;
//                }
//                users.addAll(objects);
//            }
//        });
        users.addAll(query.find());
//        Log.i(TAG, "users: " + users);
    }

    private void queryPosts() throws ParseException {
        rankedPosts.clear();

        myPosts.clear();
        postHashtags.clear();
        myHashtagString.clear();
        hMap.clear();
        postTreeMap.clear();

        queryMyPosts();
        findMyHashtags();

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereNotContainedIn("user", followingUsers);
        query.whereContainedIn("visibility", new ArrayList<Integer>(Arrays.asList(null, 0)));
        query.addDescendingOrder("createdAt");
        allPosts.addAll(query.find());

        rankPosts(allPosts);
    }

    // find the users that currentUser is following
    private void queryGetFollowing() throws ParseException {

        ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation(KEY_FOLLOWING);
        ParseQuery<ParseUser> followingQuery = relation.getQuery();
        List<ParseUser> followingL = followingQuery.find();
        for (ParseUser pUser : followingL) {
            followingUsers.add(pUser);
        }
    }

    // get the interests of those currentUser is following
    private void queryInterests() {
        // fill interestsMap with key:value pairs of interest:count
        for (ParseUser user : followingUsers) {
            List<String> uInts = new ArrayList<>(Objects.requireNonNull(user.getList("interests")));
            for (String interest : uInts) {
                MutableDouble count = interestsMap.get(interest);
                if (count == null) {
                    interestsMap.put(interest, new MutableDouble());
                }
                else {
                    count.increment();
                }
                totalInterests += 1;
            }
        }

        // now, weight the counts of each interest
        for (Map.Entry<String, MutableDouble> entry : interestsMap.entrySet()) {
            entry.getValue().divideBy(totalInterests);
        }

        // fill rankingMap with key:value pairs of ParseUser:score, where score is calculated by
        // the weights of the interests the user has in common with the currentUser's following
        for (ParseUser pUser : users) {
            if (!containsName(followingUsers, pUser.getObjectId())) {
                List<String> pInts = new ArrayList<>(Objects.requireNonNull(pUser.getList("interests")));
                rankingMap.put(pUser, 0.0);
                for (Object interest : pInts) {
                    MutableDouble count = interestsMap.get(interest);
                    if (count != null) {
                        rankingMap.put(pUser, rankingMap.get(pUser) + count.get());
                    }
                }
            }
        }

        // now, we add location to each user's score, based on how close their last registered
        // location is to the currentUser's
        for (Map.Entry<ParseUser, Double> entry : rankingMap.entrySet()) {
            ParseUser pUser = entry.getKey();
            ParseGeoPoint pUserLastLoc = pUser.getParseGeoPoint("last_location");
            if (mLocation != null && pUserLastLoc != null) {
                double userLat = pUserLastLoc.getLatitude();
                double userLong = pUserLastLoc.getLongitude();
                double mLat = mLocation.getLatitude();
                double mLong = mLocation.getLongitude();
                double distBetween = haversine(userLat, mLat, userLong, mLong);
                rankingMap.put(entry.getKey(), entry.getValue() + 1/(distBetween + 5)); // scale the second val to be 0.2 when distBetween == 0
            }
        }

        // sort rankingMap by value so that those with the lowest value come first
        rankingMap = MapUtil.sortByValue(rankingMap);
        for (Map.Entry<ParseUser, Double> entry : rankingMap.entrySet()) {
            rankedUsers.add(entry.getKey());
        }
        Collections.reverse(rankedUsers);
    }

    /**
     * Takes a list of all posts and ranks them
     * @param allPosts The list of all posts
     */
    private void rankPosts(List<Post> allPosts) throws ParseException {
        int MAX_TAGS = findMaxHashtags();
        int MAX_INTS = findMaxInterests();
        for (Post post : allPosts) {
            double score = calculateScore(MAX_TAGS, MAX_INTS, post);
            // higher (10 - score) = the lower the "rank", i.e., if (10 - score) is high, then this post is not very relevant
            postTreeMap.put(post, 10 - score);
        }
        postTreeMap = MapUtil.sortByValue(postTreeMap);
        for (Map.Entry<Post, Double> entry : postTreeMap.entrySet()) {
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
            // TODO: maybe someone has two instances of the same location
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
    public static double haversine(double a, double b, double x, double y) {
        double RADIUS = 6371;
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
//        Log.i(TAG, "myHashtagString: " + myHashtagString);
        for (Post mPost : allPosts) {
            postHashtags.clear();
            getPostHashtags(mPost);
//            Log.i(TAG, "postHashtags: " + postHashtags);
            int currTags = 0;
//            Log.i(TAG, "postHashtags: " + postHashtags);
            for (String tag : postHashtags) {
//                Log.i(TAG, "tag_here: " + tag);
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
//        Log.i(TAG, "myPosts: " + myPosts);
        for (Post mPost : myPosts) {
            ParseRelation<Hashtag> relation = mPost.getRelation(KEY_HASHTAGS);
            ParseQuery<Hashtag> query = relation.getQuery();
            List<Hashtag> myHashtagsL = query.find();
//            Log.i(TAG, "inFindMyHTs: " + myHashtagsL);
            for (Hashtag tag : myHashtagsL) {
                myHashtagString.add(tag.getHashtag());
            }
        }
    }
}