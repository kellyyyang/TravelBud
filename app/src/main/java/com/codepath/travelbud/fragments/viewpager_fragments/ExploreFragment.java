package com.codepath.travelbud.fragments.viewpager_fragments;

import static com.codepath.travelbud.fragments.HomeFragment.KEY_FOLLOWING;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.travelbud.ExploreAdapter;
import com.codepath.travelbud.MapUtil;
import com.codepath.travelbud.MutableDouble;
import com.codepath.travelbud.Post;
import com.codepath.travelbud.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
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

        rvExplore = view.findViewById(R.id.rvExplore);
        allPosts = new ArrayList<>();
        users = new ArrayList<>();
        rankedUsers = new ArrayList<>();
        adapter = new ExploreAdapter(getContext(), rankedUsers, allPosts);
        followingUsers = new ArrayList<>();
        interestsMap = new HashMap<>();
        rankingMap = new HashMap<>();
        totalInterests = 0;

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
        Log.i(TAG, "allPosts: " + allPosts);
        Log.i(TAG, "allUsers: " + users);
        adapter.notifyDataSetChanged();
    }

    public boolean containsName(final List<ParseUser> list, final String objectId){
        return list.stream().anyMatch(o -> objectId.equals(o.getObjectId()));
    }

    private void queryUsers() throws ParseException {
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.include("username");
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
        Log.i(TAG, "users: " + users);
    }

    private void queryPosts() throws ParseException {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addDescendingOrder("createdAt");
        allPosts.addAll(query.find());
    }

    private void queryGetFollowing() throws ParseException {

        ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation(KEY_FOLLOWING);
        ParseQuery<ParseUser> followingQuery = relation.getQuery();
        List<ParseUser> followingL = followingQuery.find();
        for (ParseUser pUser : followingL) {
            followingUsers.add(pUser);
            Log.i(TAG, "followingUsers: " + pUser.getUsername());
        }
    }

    // get the interests of those currentUser is following
    private void queryInterests() {
        Log.i(TAG, "followingUsers list: " + followingUsers);

        // fill interestsMap with key:value pairs of interest:count
        for (ParseUser user : followingUsers) {
            Log.i(TAG, "in list");
            List<String> uInts = new ArrayList<>(Objects.requireNonNull(user.getList("interests")));
            Log.i(TAG, "uInts: " + uInts);
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
        Log.i(TAG, "interestsMap: " + interestsMap);
        Log.i(TAG, "followingUsers, not in bundle: " + followingUsers);

        // now, weight the counts of each interest
        for (Map.Entry<String, MutableDouble> entry : interestsMap.entrySet()) {
            entry.getValue().divideBy(totalInterests);
            Log.i(TAG, "new val: " + entry.getValue().get());
        }

        // testing
        for (ParseUser us : users) {
            Log.i(TAG, "all user: " + us.getObjectId() + ", " + us.getUsername());
        }
        for (ParseUser us : followingUsers) {
            Log.i(TAG, "followed user: " + us.getObjectId() + ", " + us.getUsername());
        }

        for (ParseUser pUser : users) {
            Log.i(TAG, "pUser: " + pUser + ", " + pUser.getUsername() + ", " + pUser.getObjectId());
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

        rankingMap = MapUtil.sortByValue(rankingMap);
        for (Map.Entry<ParseUser, Double> entry : rankingMap.entrySet()) {
            rankedUsers.add(entry.getKey());
            Log.i(TAG, "ranked user: " + entry.getKey() + ", " + entry.getKey().getUsername() + ", " + entry.getValue());
        }
        Collections.reverse(rankedUsers);
    }
}