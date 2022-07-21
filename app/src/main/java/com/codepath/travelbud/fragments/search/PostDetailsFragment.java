package com.codepath.travelbud.fragments.search;

import static android.view.View.GONE;
import static com.codepath.travelbud.fragments.profile.ProfileFragment.KEY_PROFILE_PIC;
import static com.codepath.travelbud.utils.HideSoftKeyboard.hideSoftKeyboard;
import static com.codepath.travelbud.models.Post.KEY_HASHTAGS;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.travelbud.R;
import com.codepath.travelbud.models.Hashtag;
import com.codepath.travelbud.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostDetailsFragment extends Fragment {

    public static final String TAG = "PostDetailsFragment";
    Post post;

    private TextView tvUsernamePD;
    private TextView tvLocationPD;
    private TextView tvTimeAgoPD;
    private TextView tvDescriptionPD;
    private TextView tvHashtagsOnPostPD;
    private RatingBar rbRatingBarPD;
    private ImageView ivPhotoPD;
    private ImageView ivProfilePicPD;

    List<Hashtag> hashtags;

    public PostDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hideSoftKeyboard(requireActivity());
        assert getArguments() != null;
        post = getArguments().getParcelable("POST");

        hashtags = new ArrayList<>();

        tvUsernamePD = view.findViewById(R.id.tvUsernamePD);
        tvLocationPD = view.findViewById(R.id.tvLocationPD);
        tvTimeAgoPD = view.findViewById(R.id.tvTimeAgoPD);
        tvDescriptionPD = view.findViewById(R.id.tvDescriptionPD);
        tvHashtagsOnPostPD = view.findViewById(R.id.tvHashtagsOnPostPD);
        rbRatingBarPD = view.findViewById(R.id.rbRatingBarPD);
        ivPhotoPD = view.findViewById(R.id.ivPhotoPD);
        ivProfilePicPD = view.findViewById(R.id.ivProfilePicPD);

        tvUsernamePD.setText(post.getUser().getUsername());
        tvDescriptionPD.setText(post.getDescription());
        tvLocationPD.setText(post.getLocationString());

        // set hashtags
        ParseRelation<Hashtag> hashtagParseRelation = post.getRelation(KEY_HASHTAGS);
        ParseQuery<Hashtag> hashtagParseQuery = hashtagParseRelation.getQuery();
        hashtagParseQuery.include("hashtags");
        hashtagParseQuery.findInBackground(new FindCallback<Hashtag>() {
            @Override
            public void done(List<Hashtag> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "ParseException: " + e);
                    return;
                }
                hashtags.addAll(objects);

                String hashtagStr = "";
                if (hashtags.size() == 0) {
                    tvHashtagsOnPostPD.setVisibility(GONE);
                } else {
                    for (Hashtag tag : hashtags) {
                        Log.i(TAG, "adding hashtag: " + tag.getHashtag() + " " + post.getLocationString());
                        hashtagStr = hashtagStr.concat("#" + tag.getHashtag() + " ");
                    }
                    tvHashtagsOnPostPD.setText(hashtagStr);
                }
            }
        });

        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(getContext()).load(image.getUrl()).into(ivPhotoPD);
        }
        else {
            ivPhotoPD.setVisibility(GONE);
        }
        ParseFile profilePicFeed = post.getUser().getParseFile(KEY_PROFILE_PIC);
        if (profilePicFeed != null) {
            Picasso
                    .with(getContext())
                    .load(profilePicFeed.getUrl())
                    .resize((int) convertDpToPixel(40, getContext()), (int) convertDpToPixel(40, getContext()))
                    .onlyScaleDown() // the image will only be resized if it's bigger than 6000x2000 pixels.
                    .centerCrop()
                    .into(ivProfilePicPD);
        }
        Date createdAt = post.getCreatedAt();
        String timeAgo = Post.calculateTimeAgo(createdAt);
        tvTimeAgoPD.setText(timeAgo);
        rbRatingBarPD.setRating(post.getRating());
    }

    public float convertDpToPixel(float dp, Context context){
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}