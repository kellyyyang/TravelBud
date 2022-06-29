package com.codepath.travelbud;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsernameFeed = itemView.findViewById(R.id.tvUsernameFeed);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            tvLocationFeed = itemView.findViewById(R.id.tvLocationFeed);
            tvDescriptionFeed = itemView.findViewById(R.id.tvDescriptionFeed);
            rbRatingBarFeed = itemView.findViewById(R.id.rbRatingBarFeed);
            ivPhotoFeed = itemView.findViewById(R.id.ivPhotoFeed);
        }

        public void bind(Post post) {
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
    }

}
