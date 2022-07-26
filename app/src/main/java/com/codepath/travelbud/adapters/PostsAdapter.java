package com.codepath.travelbud.adapters;

import static android.view.View.GONE;
import static com.codepath.travelbud.models.Post.KEY_HASHTAGS;
import static com.codepath.travelbud.fragments.profile.ProfileFragment.KEY_PROFILE_PIC;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.travelbud.R;
import com.codepath.travelbud.models.Hashtag;
import com.codepath.travelbud.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";

    private Context context;
    private List<Post> posts;
    private boolean isProfile;

    public PostsAdapter(Context context, List<Post> posts, boolean isProfile) {
        this.context = context;
        this.posts = posts;
        this.isProfile = isProfile;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        if (isProfile) {
            view = LayoutInflater.from(context).inflate(R.layout.item_post_profile, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        if (!isProfile) {
            try {
                holder.bindHome(post);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            holder.bindProfile(post);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        this.posts.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsernameFeed;
        private TextView tvTimeAgo;
        private TextView tvLocationFeed;
        private TextView tvDescriptionFeed;
        private RatingBar rbRatingBarFeed;
        private ImageView ivPhotoFeed;
        private ImageView ivProfilePicFeed;
        private TextView tvHashtagsOnPost;
        private ImageButton btnUpvote;
        private ImageButton btnDownvote;
        private TextView tvNumUps;
        private TextView tvNumDowns;

        private ImageView ivPostProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsernameFeed = itemView.findViewById(R.id.tvUsernameFeed);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            tvLocationFeed = itemView.findViewById(R.id.tvLocationFeed);
            tvDescriptionFeed = itemView.findViewById(R.id.tvDescriptionFeed);
            rbRatingBarFeed = itemView.findViewById(R.id.rbRatingBarFeed);
            ivPhotoFeed = itemView.findViewById(R.id.ivPhotoFeed);
            ivProfilePicFeed = itemView.findViewById(R.id.ivProfilePicFeed);
            tvHashtagsOnPost = itemView.findViewById(R.id.tvHashtagsOnPost);

            ivPostProfile = itemView.findViewById(R.id.ivPostProfile);

            btnUpvote = itemView.findViewById(R.id.btnUpvote);
            btnDownvote = itemView.findViewById(R.id.btnDownvote);
            tvNumUps = itemView.findViewById(R.id.tvNumUps);
            tvNumDowns = itemView.findViewById(R.id.tvNumDowns);
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

        public void bindHome(Post post) throws ParseException {
            // bind Post data to the view elements
            tvUsernameFeed.setText(post.getUser().getUsername());
            tvDescriptionFeed.setText(post.getDescription());
            tvLocationFeed.setText(post.getLocationString());
            tvNumUps.setText(String.valueOf(post.getNumUpvotes()));
            tvNumDowns.setText(String.valueOf(post.getNumDownvotes()));

            // check if the currentUser has already upvoted/liked this post
            boolean hasLiked = alreadyLiked(ParseUser.getCurrentUser(), post);
            boolean hasDisliked = alreadyDisliked(ParseUser.getCurrentUser(), post);
            if (hasLiked) {
                btnUpvote.setImageResource(R.drawable.filled_thumb_up);
            } else if (hasDisliked) {
                btnDownvote.setImageResource(R.drawable.filled_thumb_down);
            }

            btnUpvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean hasLiked = false;
                    try {
                        hasLiked = alreadyLiked(ParseUser.getCurrentUser(), post);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    ParseRelation<ParseUser> relation = post.getRelation("upvoters");
                    int previousUpvotes = post.getNumUpvotes();
                    if (!hasLiked) {
                        btnUpvote.setImageResource(R.drawable.filled_thumb_up);
                        post.setNumUpvotes(previousUpvotes + 1);
                        relation.add(ParseUser.getCurrentUser());
                        ParseUser.getCurrentUser().saveInBackground();
                        post.saveInBackground();
                        Log.i(TAG, "upvoted!");
                    } else {
                        btnUpvote.setImageResource(R.drawable.ic_outline_thumb_up_alt_24);
                        post.setNumUpvotes(previousUpvotes - 1);
                        relation.remove(ParseUser.getCurrentUser());
                        ParseUser.getCurrentUser().saveInBackground();
                        post.saveInBackground();
                    }
                    tvNumUps.setText(String.valueOf(post.getNumUpvotes()));
                }
            });

            btnDownvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean hasDisliked = alreadyDisliked(ParseUser.getCurrentUser(), post);
                    ParseRelation<ParseUser> relation = post.getRelation("downvoters");
                    int previousDownvotes = post.getNumDownvotes();
                    if (!hasDisliked) {
                        btnUpvote.setImageResource(R.drawable.filled_thumb_down);
                        post.setNumUpvotes(previousDownvotes + 1);
                        relation.add(ParseUser.getCurrentUser());
                        ParseUser.getCurrentUser().saveInBackground();
                        post.saveInBackground();
                    } else {
                        btnUpvote.setImageResource(R.drawable.ic_outline_thumb_down_alt_24);
                        post.setNumUpvotes(previousDownvotes - 1);
                        relation.remove(ParseUser.getCurrentUser());
                        ParseUser.getCurrentUser().saveInBackground();
                        post.saveInBackground();
                    }
                    tvNumDowns.setText(String.valueOf(post.getNumDownvotes()));
                }
            });

            // set hashtags
            ParseRelation<Hashtag> hashtagParseRelation = post.getRelation(KEY_HASHTAGS);
            ParseQuery<Hashtag> hashtagParseQuery = hashtagParseRelation.getQuery();
            hashtagParseQuery.include("hashtags");
            List<Hashtag> hashtags = hashtagParseQuery.find();
            String hashtagStr = "";
            if (hashtags.size() == 0) {
                tvHashtagsOnPost.setVisibility(GONE);
            }
            else {
                for (Hashtag tag : hashtags) {
                    hashtagStr = hashtagStr.concat("#" + tag.getHashtag() + " ");
                }
                tvHashtagsOnPost.setText(hashtagStr);
            }

            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivPhotoFeed);
            }
            else {
                ivPhotoFeed.setVisibility(GONE);
            }
            ParseFile profilePicFeed = post.getUser().getParseFile(KEY_PROFILE_PIC);
            if (profilePicFeed != null) {
                Glide.with(context).load(profilePicFeed.getUrl()).into(ivProfilePicFeed);
            }
            Date createdAt = post.getCreatedAt();
            String timeAgo = Post.calculateTimeAgo(createdAt);
            tvTimeAgo.setText(timeAgo);
            rbRatingBarFeed.setRating(post.getRating());
        }

        public boolean alreadyLiked(ParseUser aUser, Post aPost) throws ParseException {
            ParseRelation<ParseUser> likes = aPost.getRelation("upvoters");
            ParseQuery<ParseUser> userLikes = likes.getQuery();
            for (ParseUser pUser : userLikes.find()) {
                if (pUser.getObjectId().equals(aUser.getObjectId())) {
                    return true;
                }
            }
            return false;
        }

        public boolean alreadyDisliked(ParseUser aUser, Post aPost) {
            ParseRelation<ParseUser> downs = aPost.getRelation("downvoters");
            ParseQuery<ParseUser> userDowns = downs.getQuery();
            final boolean[] hasLiked = {false};
            userDowns.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    for (ParseUser pUser : objects) {
                        if (pUser.getObjectId().equals(aUser.getObjectId())) {
                            hasLiked[0] = true;
                        }
                    }
                }
            });
            return hasLiked[0];
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
            }
        }
    }

}
