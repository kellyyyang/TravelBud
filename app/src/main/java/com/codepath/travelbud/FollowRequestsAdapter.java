package com.codepath.travelbud;

import static com.codepath.travelbud.fragments.ProfileFragment.KEY_PROFILE_PIC;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

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
            Follow newFollow = new Follow();
            newFollow.setFollower(user);
            newFollow.setFollowing(currentUser);
            newFollow.saveInBackground();
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
    }
}
