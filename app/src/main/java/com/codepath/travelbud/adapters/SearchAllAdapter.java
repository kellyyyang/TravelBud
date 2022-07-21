package com.codepath.travelbud.adapters;

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
import com.codepath.travelbud.R;
import com.codepath.travelbud.interfaces.SearchAdapterToFragment;
import com.codepath.travelbud.utils.UserPostArray;
import com.codepath.travelbud.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchAllAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "SearchAllAdapter";

    private Context context;
    private List<UserPostArray> usersPosts;
    private String KEY_PROFILEPIC = "profilePic";
    private ArrayList<UserPostArray> arrayList; //used for the search bar
    private SearchAdapterToFragment mCommunicator;

    public SearchAllAdapter(Context context, List<UserPostArray> usersPosts, SearchAdapterToFragment communicator) {
        this.context = context;
        this.usersPosts = usersPosts;
        this.mCommunicator = communicator;
        arrayList = new ArrayList<>();
        this.arrayList.addAll(usersPosts);
    }

    @Override
    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
        if (usersPosts.get(position).getUser() != null) {
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
                View view0 = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
                return new SearchAllAdapter.ViewHolder0(view0, mCommunicator);
            case 2:
                View view2 = LayoutInflater.from(context).inflate(R.layout.item_post_search, parent, false);
                return new SearchAllAdapter.ViewHolder2(view2, mCommunicator);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                Log.i(TAG, "case user");
                SearchAllAdapter.ViewHolder0 viewHolder0 = (SearchAllAdapter.ViewHolder0) holder;
                ParseUser user = usersPosts.get(position).getUser();
                viewHolder0.bindUser(user);
                break;
            case 2:
                Log.i(TAG, "case post");
                SearchAllAdapter.ViewHolder2 viewHolder2 = (SearchAllAdapter.ViewHolder2) holder;
                Post post = usersPosts.get(position).getPost();
                viewHolder2.bindPost(post);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return usersPosts.size();
    }

    // holds Users
    class ViewHolder0 extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvUsernameSearch;
        private ImageView ivProfilePicSearch;
        private SearchAdapterToFragment mCommunicator1;

        public ViewHolder0(@NonNull View itemView, SearchAdapterToFragment communicator1) {
            super(itemView);

            mCommunicator1 = communicator1;
            tvUsernameSearch = itemView.findViewById(R.id.tvUsernameSearch);
            ivProfilePicSearch = itemView.findViewById(R.id.ivProfilePicSearch);

            itemView.setOnClickListener(this);
        }

        public void bindUser(ParseUser user) {
            // bind the user data to the view elements
            tvUsernameSearch.setText(user.getUsername());
            ParseFile profilePic = user.getParseFile(KEY_PROFILEPIC);
            if (profilePic != null) {
                Glide.with(context).load(profilePic.getUrl()).into(ivProfilePicSearch);
            }
        }

        @Override
        public void onClick(View v) {
            // get position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. it actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                ParseUser user = usersPosts.get(position).getUser();
                mCommunicator1.sendUser(position, user);
            }
        }
    }

    // holds Posts
    class ViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvUsernamePS;
        private TextView tvLocationPS;
        private RatingBar ratingBarPS;
        private TextView tvDescriptionPS;
        private ImageView ivPhotoPS;

        private SearchAdapterToFragment mCommunicator1;

        public ViewHolder2(@NonNull View itemView, SearchAdapterToFragment communicator1) {
            super(itemView);

            mCommunicator1 = communicator1;
            tvUsernamePS = itemView.findViewById(R.id.tvUsernamePS);
            tvLocationPS = itemView.findViewById(R.id.tvLocationPS);
            ratingBarPS = itemView.findViewById(R.id.ratingBarPS);
            tvDescriptionPS = itemView.findViewById(R.id.tvDescriptionPS);
            ivPhotoPS = itemView.findViewById(R.id.ivPhotoPS);

            itemView.setOnClickListener(this);
        }

        public void bindPost(Post post) {
            // bind the user data to the view elements
            tvUsernamePS.setText(post.getUser().getUsername());
            ParseFile image = post.getParseFile("image");
            if (image != null) {
                Picasso
                        .with(context)
                        .load(image.getUrl())
                        .resize((int) convertDpToPixel(120, itemView.getContext()), (int) convertDpToPixel(120, itemView.getContext()))
                        .onlyScaleDown() // the image will only be resized if it's bigger than 6000x2000 pixels.
                        .centerCrop()
                        .into(ivPhotoPS);
            }
            tvLocationPS.setText(post.getLocationString());
            ratingBarPS.setRating(post.getRating());
            tvDescriptionPS.setText(post.getDescription());
        }

        @Override
        public void onClick(View v) {
            // get position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. it actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                Post post = usersPosts.get(position).getPost();
                mCommunicator1.sendPost(position, post);
            }
        }
    }

    public float convertDpToPixel(float dp, Context context){
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    // filter username and hashtags in Search Bar
    public void filter(String characterText, List<UserPostArray> searchUsersPosts) {
        characterText = characterText.toLowerCase();
        usersPosts.clear();
        if (characterText.length() == 0) {
            usersPosts.addAll(searchUsersPosts);
        } else {
            usersPosts.clear();
            for (UserPostArray item : searchUsersPosts) {
                if (item.getUser() != null) {
                    if (item.getUser().getUsername().toLowerCase(Locale.getDefault()).contains(characterText)) {
                        UserPostArray user = new UserPostArray();
                        user.setUser(item.getUser());
                        usersPosts.add(user);
                    }
                } else {
                    if (item.getPost().getDescription().toLowerCase(Locale.getDefault()).contains(characterText) ||
                            item.getPost().getLocationString().toLowerCase(Locale.getDefault()).contains(characterText)) {
                        UserPostArray post = new UserPostArray();
                        post.setPost(item.getPost());
                        usersPosts.add(post);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

}
