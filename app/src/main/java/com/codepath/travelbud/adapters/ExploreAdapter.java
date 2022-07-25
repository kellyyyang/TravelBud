package com.codepath.travelbud.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.travelbud.R;
import com.codepath.travelbud.fragments.search.ExploreFragment;
import com.codepath.travelbud.models.Post;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class ExploreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "ExploreAdapter";

    private Context context;
    private List<ParseUser> users;
    private List<Post> allPosts;
    private final String KEY_PROFILE_PIC = "profilePic";

    public ExploreAdapter(Context context, List<ParseUser> users, List<Post> allPosts) {
        this.context = context;
        this.users = users;
        this.allPosts = allPosts;
    }

    public float convertDpToPixel(float dp, Context context){
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public int getItemViewType(int position) {
        int posMod5 = position % 5;
        if (posMod5 == 0) {
            return 0;
        } else {
            return 2;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View view0 = LayoutInflater.from(context).inflate(R.layout.item_user_explore, parent, false);
                return new ViewHolder0(view0);
            case 2:
                View view2 = LayoutInflater.from(context).inflate(R.layout.item_post_explore, parent, false);
                return new ViewHolder2(view2);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            // case 0: user
            // case 2: post
            case 0:
                ViewHolder0 viewHolder0 = (ViewHolder0) holder;
                int iterationUser = position / 5;
                ParseUser user = users.get(iterationUser);
                viewHolder0.bindUser(user);
                break;
            case 2:
                ViewHolder2 viewHolder2 = (ViewHolder2) holder;
                int iterationPost = position / 5;
                Post post = allPosts.get(position - iterationPost - 1); // these numbers aren't working out well, so it's grabbing the same post sometimes
                // as i use breakpoints to see which post is in the variable above, I see Lake Tahoe came out twice
                // therefore, it displays twice on the emulator screen
                // can you backtrack iterationPost to figure out what the numbers are, and whether the math is correct as you intended?
                // see if you can add more breakpoints and closely track the math, and I'll keep your name on the support queue so someone can check in with you again
                viewHolder2.bindPost(post);
                break;

        }
    }

    @Override
    public int getItemCount() {
        if (users.size()*5 < allPosts.size()) {
            return users.size() + (allPosts.size() / 5) * 4;
        }
        else {
            return allPosts.size() + (allPosts.size() / 5) + 1;
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        users.clear();
        allPosts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAllPosts(List<Post> list) {
        allPosts.addAll(list);
        notifyDataSetChanged();
    }

    public void addAllUsers(List<ParseUser> userL) {
        users.addAll(userL);
        notifyDataSetChanged();
    }

    // user viewholder
    class ViewHolder0 extends RecyclerView.ViewHolder {

        private TextView tvUsernameExplore;
        private ImageView ivUserExplore;
        private TextView tvDistanceAway;

        public ViewHolder0(@NonNull View itemView) {
            super(itemView);

            tvUsernameExplore = itemView.findViewById(R.id.tvUsernameExplore);
            ivUserExplore = itemView.findViewById(R.id.ivUserExplore);
            tvDistanceAway = itemView.findViewById(R.id.tvDistanceAway);
        }

        public void bindUser(ParseUser user) {
            tvUsernameExplore.setText(String.format("@%s", user.getUsername()));
            if (user.getParseGeoPoint("last_location") != null && ParseUser.getCurrentUser().getParseGeoPoint("last_location") != null) {
                double distAway = distanceAway(ParseUser.getCurrentUser(), user);
                if (distAway > 50) {
                    tvDistanceAway.setText(">50 km away");
                } else {
                    tvDistanceAway.setText(String.format(Locale.US, "%.1f km away", distAway));
                }
            }
            ParseFile image = user.getParseFile("profilePic");
            if (image != null) {
                Picasso
                        .with(context)
                        .load(image.getUrl())
                        .resize((int) convertDpToPixel(130, itemView.getContext()), (int) convertDpToPixel(130, itemView.getContext()))
                        .onlyScaleDown() // the image will only be resized if it's bigger than 6000x2000 pixels.
                        .centerCrop()
                        .into(ivUserExplore);
            }
        }

        private double distanceAway(ParseUser currentUser, ParseUser user) {
            ParseGeoPoint pUserLastLoc = user.getParseGeoPoint("last_location");
            double userLat = pUserLastLoc.getLatitude();
            double userLong = pUserLastLoc.getLongitude();
            ParseGeoPoint mLocation = currentUser.getParseGeoPoint("last_location");
            double mLat = mLocation.getLatitude();
            double mLong = mLocation.getLongitude();
            return ExploreFragment.haversine(userLat, mLat, userLong, mLong);
        }
    }

    // post viewholder
    class ViewHolder2 extends RecyclerView.ViewHolder {

        private TextView tvLocationExplore;
        private ImageView ivPostExplore;

        public ViewHolder2(@NonNull View itemView) {
            super(itemView);

            tvLocationExplore = itemView.findViewById(R.id.tvLocationExplore);
            ivPostExplore = itemView.findViewById(R.id.ivPostExplore);
        }

        public void bindPost(Post post) {
            tvLocationExplore.setText(post.getLocationString());
            ParseFile image = post.getImage();
            if (image != null) {
                Picasso
                        .with(context)
                        .load(image.getUrl())
                        .resize((int) convertDpToPixel(130, itemView.getContext()), (int) convertDpToPixel(130, itemView.getContext()))
                        .onlyScaleDown() // the image will only be resized if it's bigger than 6000x2000 pixels.
                        .centerCrop()
                        .into(ivPostExplore);
            }
        }
    }
}
