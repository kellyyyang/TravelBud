package com.codepath.travelbud.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.travelbud.R;
import com.codepath.travelbud.interfaces.SearchAdapterToFragment;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    public static final String TAG = "UserSearchAdapter";

    private Context context;
    private List<ParseUser> users;
    private String KEY_PROFILEPIC = "profilePic";
    private ArrayList<ParseUser> arrayList; //used for the search bar
    private SearchAdapterToFragment mCommunicator;

    public UserSearchAdapter(Context context, List<ParseUser> users, SearchAdapterToFragment communicator) {
        this.context = context;
        this.users = users;
        this.mCommunicator = communicator;
        arrayList = new ArrayList<>();
        this.arrayList.addAll(users);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view, mCommunicator);
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvUsernameSearch;
        private ImageView ivProfilePicSearch;
        private SearchAdapterToFragment mCommunicator1;

        public ViewHolder(@NonNull View itemView, SearchAdapterToFragment communicator1) {
            super(itemView);

            mCommunicator1 = communicator1;
            tvUsernameSearch = itemView.findViewById(R.id.tvUsernameSearch);
            ivProfilePicSearch = itemView.findViewById(R.id.ivProfilePicSearch);

            itemView.setOnClickListener(this);
        }

        public void bind(ParseUser user) {
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
                ParseUser user = users.get(position);
                mCommunicator1.sendUser(position, user);
            }
        }
    }

    // filter username in Search Bar
    public void filter(String characterText, List<ParseUser> searchUsers) {
        characterText = characterText.toLowerCase();
        users.clear();
        if (characterText.length() == 0) {
            users.addAll(searchUsers);
        } else {
            users.clear();
            for (ParseUser user: searchUsers) {
                if (user.getUsername().toLowerCase(Locale.getDefault()).contains(characterText)) {
                    users.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

}
