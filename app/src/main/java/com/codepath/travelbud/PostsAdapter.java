package com.codepath.travelbud;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";

    private Context context;
    private List<Post> posts;
    private boolean isProfile;

    public PostsAdapter(Context context, List<Post> posts, boolean b) {
        this.context = context;
        this.posts = posts;
        this.isProfile = b;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        if (isProfile == true) {
            view = LayoutInflater.from(context).inflate(R.layout.item_post_profile, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        if (isProfile == false) {
            holder.bindHome(post);
        } else {
            holder.bindProfile(post);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsernameFeed;
        private TextView tvTimeAgo;
        private TextView tvLocationFeed;
        private TextView tvDescriptionFeed;
        private RatingBar rbRatingBarFeed;
        private ImageView ivPhotoFeed;

        private ImageView ivPostProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsernameFeed = itemView.findViewById(R.id.tvUsernameFeed);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            tvLocationFeed = itemView.findViewById(R.id.tvLocationFeed);
            tvDescriptionFeed = itemView.findViewById(R.id.tvDescriptionFeed);
            rbRatingBarFeed = itemView.findViewById(R.id.rbRatingBarFeed);
            ivPhotoFeed = itemView.findViewById(R.id.ivPhotoFeed);

            ivPostProfile = itemView.findViewById(R.id.ivPostProfile);
        }

        /**
         * This method converts dp unit to equivalent pixels, depending on device density.
         *
         * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent px equivalent to dp depending on device density
         */
        public float convertDpToPixel(float dp, Context context){
            return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        }

        public void bindHome(Post post) {
            // bind Post data to the view elements
            tvUsernameFeed.setText(post.getUser().getUsername());
            tvDescriptionFeed.setText(post.getDescription());
            tvLocationFeed.setText(post.getLocationString());
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(post.getImage().getUrl()).into(ivPhotoFeed);
            }
            Date createdAt = post.getCreatedAt();
            String timeAgo = Post.calculateTimeAgo(createdAt);
            tvTimeAgo.setText(timeAgo);
            rbRatingBarFeed.setRating(post.getRating());
        }

        public void bindProfile(Post post) {
            // bind Post data to view elements
            ParseFile image = post.getImage();
            if (image != null) {
                Picasso
                        .with(context)
                        .load(image.getUrl())
                        .resize((int) convertDpToPixel(131, itemView.getContext()), (int) convertDpToPixel(131, itemView.getContext()))
                        .onlyScaleDown() // the image will only be resized if it's bigger than 6000x2000 pixels.
                        .centerCrop()
                        .into(ivPostProfile);
//                Glide.with(context).load(post.getImage().getUrl()).into(ivPostProfile);
            }
        }
    }

}
