package com.codepath.travelbud.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.travelbud.MainActivity;
import com.codepath.travelbud.MapsActivity;
import com.codepath.travelbud.Post;
import com.codepath.travelbud.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";

    private AutoCompleteTextView actvLocation;
    private RatingBar rbPost;
    private EditText etDescription;
    private ImageButton btnTakePhoto;
    private Button btnPost;
    private ImageView ivPhoto;
    private ImageView ivProfilePicPost;
    private TextView tvUsername;
    private Button btnMap; // TODO: delete after making bottom nav view
    private TextView tvRating; // TODO : delete

    // camera variables
    public String photoFileName = "photo.jpg";
    File photoFile;
    public final String APP_TAG = "TravelBud";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static final Integer MIN_DESCRIPTION_LEN = 90;

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvUsername.setText(ParseUser.getCurrentUser().getUsername());
        ivProfilePicPost = view.findViewById(R.id.ivProfilePicPost);

        actvLocation = view.findViewById(R.id.actvLocation);
        rbPost = view.findViewById(R.id.rbPost);
        etDescription = view.findViewById(R.id.etDescription);
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
        btnPost = view.findViewById(R.id.btnPost);
        ivPhoto = view.findViewById(R.id.ivPhoto);
        ivProfilePicPost = view.findViewById(R.id.ivProfilePicPost);
        tvRating = view.findViewById(R.id.tvRating);

        // TODO: delete after bottom nav
        btnMap = view.findViewById(R.id.btnMap);

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                Integer rating = rbPost.getNumStars();
                ParseFile image;
                if (description.length() < MIN_DESCRIPTION_LEN) {
                    Toast.makeText(getContext(), "Your caption must be at least 90 characters.", Toast.LENGTH_LONG).show();
                    return;
                } if (photoFile == null || ivPhoto.getDrawable() == null) {
                    image = null;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                image = new ParseFile(photoFile);
                savePost(currentUser, description, rating, image); // TODO: check if image == null
            }
        });

        // TODO: delete after making bottom nav
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goMapsActivity(v);
            }
        });
    }

    private void goMapsActivity(View view) {
        Intent i = new Intent(getContext(), MapsActivity.class);
        startActivity(i);
    }

    private void savePost(ParseUser currentUser, String description, Integer rating, ParseFile image) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(currentUser);
        post.setRating(rating);
        if (image != null) {
            post.setImage(image);
        } else if (image == null) {
            ivPhoto.setVisibility(View.GONE);
        }

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with saving post");
                    Toast.makeText(getContext(), "Issue with saving post!", Toast.LENGTH_SHORT).show(); // TODO: change MainActivity.this to getContext() if using Fragment
                } else {
                    Log.i(TAG, "Post has been saved");
                    Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                    etDescription.setText("");
                    rbPost.setRating(0);
                    ivPhoto.setVisibility(View.INVISIBLE);
                }
                return;
            }
        });
    }
}