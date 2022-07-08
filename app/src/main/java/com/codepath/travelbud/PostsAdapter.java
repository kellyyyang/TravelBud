package com.codepath.travelbud;

import static android.view.View.GONE;
import static com.codepath.travelbud.Post.KEY_HASHTAGS;
import static com.codepath.travelbud.fragments.ProfileFragment.KEY_PROFILE_PIC;

import android.content.Context;
import android.media.Image;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";

    private Context context;
    private List<Post> posts;
    private List<Hashtag> hashtags;
    private List<Post> postsFull;
    private List<Hashtag> hashtagListFilter;
    private List<Hashtag> allHashtags;
    private ParseRelation<Post> allHashtagPosts;
    private boolean isProfile;

    public PostsAdapter(Context context, List<Post> posts, boolean isProfile) {
        this.context = context;
        this.posts = posts;
        this.isProfile = isProfile;
        postsFull = new ArrayList<>(posts);
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

    // filter hashtags
    // allHashtagFilterList is a list of all hashtags in the database
    public void hashtagFilter(String characterText, List<Hashtag> allHashtagFilterList) {
        characterText = characterText.toLowerCase();
        hashtags.clear();
        if (characterText.length() > 0) {
            hashtags.clear();
            for (Hashtag tag : allHashtagFilterList) {
                if (tag.getHashtag().toLowerCase().contains(characterText)) {
                    hashtags.add(tag);
                }
            }
        }
    }

//    @Override
//    public Filter getFilter() {
//        return postsFilter;
//    }
//
//    private Filter postsFilter = new Filter() {
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//            List<Post> filteredList = new ArrayList<>();
//            if (constraint == null || constraint.length() == 0) {
//                filteredList.addAll(postsFull);
//            } else {
//                String filterPattern = constraint.toString().toLowerCase().trim();
//
//                ParseQuery<Hashtag> hashtagParseQuery = ParseQuery.getQuery(Hashtag.class);
//                hashtagParseQuery.include("hashtag");
//                try {
//                    allHashtags = hashtagParseQuery.find();
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//                for (Hashtag tag : allHashtags) {
//                    if (tag.getHashtag().toLowerCase().contains(filterPattern)) {
//                        hashtagListFilter.add(tag);
//                    }
//                }
//
//            }
//
//            FilterResults results = new FilterResults();
//
//
//        }
//
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//
//        }
//    };

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsernameFeed;
        private TextView tvTimeAgo;
        private TextView tvLocationFeed;
        private TextView tvDescriptionFeed;
        private RatingBar rbRatingBarFeed;
        private ImageView ivPhotoFeed;
        private ImageView ivProfilePicFeed;
        private TextView tvHashtagsOnPost;

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

//            setIsRecyclable(false);
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

//             set hashtags
            ParseRelation<Hashtag> hashtagParseRelation = post.getRelation(KEY_HASHTAGS);
            ParseQuery<Hashtag> hashtagParseQuery = hashtagParseRelation.getQuery();
            hashtagParseQuery.include("hashtags");
            List<Hashtag> hashtags = hashtagParseQuery.find();
            Log.i(TAG, "hashtags: " + hashtags);
            String hashtagStr = "";
            if (hashtags.size() == 0) {
                tvHashtagsOnPost.setVisibility(GONE);
            }
            else {
                for (Hashtag tag : hashtags) {
                    Log.i(TAG, "adding hashtag: " + tag.getHashtag() + " " + post.getLocationString());
                    hashtagStr = hashtagStr.concat("#" + tag.getHashtag() + " ");
                }
                Log.i(TAG, "hashtagStr: " + hashtagStr);
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
