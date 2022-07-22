package com.codepath.travelbud.adapters;

import static com.codepath.travelbud.fragments.profile.ProfileFragment.KEY_PROFILE_PIC;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.travelbud.R;
import com.codepath.travelbud.fragments.search.UserDetailsFragment;
import com.codepath.travelbud.models.Follow;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowRequestsAdapter extends RecyclerView.Adapter<FollowRequestsAdapter.ViewHolder> {

    public static final String TAG = "FollowRequestsAdapter";
    public static final String KEY_FULL_NAME = "vis_name";
    ParseUser currentUser = ParseUser.getCurrentUser();

    private Context context;
    private List<ParseUser> users;

    public FollowRequestsAdapter(Context context, List<ParseUser> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_follow_request, parent, false);
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvUsernameFR;
        private TextView tvFullNameFR;
        private ImageView ivProPicFR;
        private Button btnAccept;
        private Button btnDeny;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsernameFR = itemView.findViewById(R.id.tvUsernameFR);
            tvFullNameFR = itemView.findViewById(R.id.tvFullNameFR);
            ivProPicFR = itemView.findViewById(R.id.ivProPicFR);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDeny = itemView.findViewById(R.id.btnDeny);

            btnAccept.setOnClickListener(this);
            btnDeny.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // get position
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                ParseUser user = users.get(position);
                if (v.getId() == btnAccept.getId()) {
                    btnDeny.setVisibility(View.GONE);
                    acceptedBtnAppearance();
                    acceptFollowRequest(user);
                    removeFollowRequest(user);
                }
                else if (v.getId() == btnDeny.getId()) {
                    btnAccept.setVisibility(View.GONE);
                    deniedBtnAppearance();
                    removeFollowRequest(user);
                }
            }
        }

        private void deniedBtnAppearance() {
            btnDeny.setText("Denied");
            btnDeny.setBackgroundColor(Color.RED);
        }

        private void acceptedBtnAppearance() {
            btnAccept.setText("Done!");
            btnAccept.setBackgroundColor(Color.GREEN);
        }

        private void acceptFollowRequest(ParseUser user) {
            ParseRelation<ParseUser> followersRelation = currentUser.getRelation("followers");
            followersRelation.add(user);
            addFollowing(currentUser, user);
            int prevFollowers = currentUser.getInt("numFollowers");
            addOneFollowing(user);
            currentUser.put("numFollowers", prevFollowers + 1);
            currentUser.saveInBackground();
        }

        private void removeFollowRequest(ParseUser user) {
            ParseRelation<ParseUser> relation = currentUser.getRelation("incoming_follow_requests");
            relation.remove(user);
            currentUser.saveInBackground();
        }

        public void bind(ParseUser user) {
            // Bind the post data to the view elements
            tvUsernameFR.setText(user.getUsername());
            tvFullNameFR.setText(user.getString(KEY_FULL_NAME));
            ParseFile image = user.getParseFile(KEY_PROFILE_PIC);
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivProPicFR);
            }
        }

        /**
         * Adds userA as someone userB is following.
         * @param userA A ParseUser that is to be followed.
         * @param userB A ParseUser that is to do the following.
         */
        protected void addFollowing(ParseUser userA, ParseUser userB) {
            // Use this map to send parameters to your Cloud Code function
            // Just push the parameters you want into it
            Map<String, String> parameters = new HashMap<>();
            parameters.put("userA", userA.getObjectId());
            parameters.put("userB", userB.getObjectId());

            ParseCloud.callFunctionInBackground("addFollowing", parameters, new FunctionCallback<String>() {
                @Override
                public void done(String object, ParseException e) {
                    if (e == null) {
                        // Everything is alright
                        Toast.makeText(btnAccept.getContext(), "Answer = " + object.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        private void addOneFollowing(ParseUser user) {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("userB", user.getObjectId());

            ParseCloud.callFunctionInBackground("addOneFollowing", parameters, new FunctionCallback<String>() {
                @Override
                public void done(String object, ParseException e) {
                    if (e == null) {
                        // Everything is alright
                        Toast.makeText(btnAccept.getContext(), "Answer = " + object.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
