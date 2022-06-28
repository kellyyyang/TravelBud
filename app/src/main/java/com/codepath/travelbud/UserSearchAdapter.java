package com.codepath.travelbud;

import android.content.Context;
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

import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    private Context context;
    private List<ParseUser> users;
    private String KEY_PROFILEPIC = "profilePic";

    public UserSearchAdapter(Context context, List<ParseUser> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUsernameSearch;
        private ImageView ivProfilePicSearch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsernameSearch = itemView.findViewById(R.id.tvUsernameSearch);
            ivProfilePicSearch = itemView.findViewById(R.id.ivProfilePicSearch);

        }

        public void bind(ParseUser user) {
            // bind the user data to the view elements
            tvUsernameSearch.setText(user.getUsername());
            ParseFile profilePic = user.getParseFile(KEY_PROFILEPIC);
            if (profilePic != null) {
                Glide.with(context).load(profilePic.getUrl()).into(ivProfilePicSearch);
            }
        }
    }
}
