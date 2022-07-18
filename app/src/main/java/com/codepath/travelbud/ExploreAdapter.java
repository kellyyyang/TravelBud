package com.codepath.travelbud;

import android.content.Context;
import android.media.Image;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

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
//        return super.getItemViewType(position);
        Log.i(TAG, "position: " + position);
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
        Log.i(TAG, "users: " + users);
        Log.i(TAG, "posts: " + allPosts);
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
            case 0:
                Log.i(TAG, "case user");
                ViewHolder0 viewHolder0 = (ViewHolder0) holder;
                int iterationUser = position / 5;
                ParseUser user = users.get(iterationUser);
                viewHolder0.bindUser(user);
                break;
            case 2:
                Log.i(TAG, "case post");
                ViewHolder2 viewHolder2 = (ViewHolder2) holder;
                int iterationPost = position / 5;

                Post post = allPosts.get(position - iterationPost - 1);
                viewHolder2.bindPost(post);
                break;

        }
    }

    @Override
    public int getItemCount() {
//        return 11;
        if (users.size()*5 < allPosts.size()) {
            return users.size() + (allPosts.size() / 4) * 4;
        }
        else {
            return allPosts.size() + (allPosts.size() / 4) + 1;
        }
    }

    // user viewholder
    class ViewHolder0 extends RecyclerView.ViewHolder {

        private TextView tvUsernameExplore;
        private ImageView ivUserExplore;

        public ViewHolder0(@NonNull View itemView) {
            super(itemView);

            tvUsernameExplore = itemView.findViewById(R.id.tvUsernameExplore);
            ivUserExplore = itemView.findViewById(R.id.ivUserExplore);
        }

        public void bindUser(ParseUser user) {
            tvUsernameExplore.setText(String.format("@%s", user.getUsername()));
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
