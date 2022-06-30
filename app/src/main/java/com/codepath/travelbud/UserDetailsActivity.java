package com.codepath.travelbud;

import static com.codepath.travelbud.fragments.ProfileFragment.KEY_BIO;
import static com.codepath.travelbud.fragments.ProfileFragment.KEY_PROFILE_PIC;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsActivity extends AppCompatActivity {

    public static final String TAG = "UserDetailsActivity";

    ParseUser user;
    private ImageView ivProfilePicSearch;
    private TextView tvUsernameSearch;
    private TextView tvBioSearch;
    private RecyclerView rvPostsSearch;

    private PostsAdapter adapter;
    private List<Post> allPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        ivProfilePicSearch = findViewById(R.id.ivProfilePicSearch);
        tvUsernameSearch = findViewById(R.id.tvUsernameSearch);
        tvBioSearch = findViewById(R.id.tvBioSearch);
        rvPostsSearch = findViewById(R.id.rvPostsSearch);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(this, allPosts, true);

        user = (ParseUser) Parcels.unwrap(getIntent().getParcelableExtra(ParseUser.class.getSimpleName()));
        Log.d(TAG, "Showing details for " + user.getUsername());
        tvUsernameSearch.setText(user.getUsername());

        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                tvBioSearch.setText(user.getString(KEY_BIO));
                ParseFile profileImage = ((ParseFile) ParseUser.getCurrentUser().get(KEY_PROFILE_PIC));
                if (profileImage != null) {
                    Glide.with(UserDetailsActivity.this).load(profileImage.getUrl()).into(ivProfilePicSearch);
                }
            }
        });

        rvPostsSearch.setAdapter(adapter);
        rvPostsSearch.setLayoutManager(new GridLayoutManager(this, 3));

        queryPosts();
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, user);
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
            }
        });
    }
}