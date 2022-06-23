package com.codepath.travelbud.fragments;

import static com.codepath.travelbud.BuildConfig.MAPS_API_KEY;
import static com.parse.Parse.getApplicationContext;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.Arrays;

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
    private TextView tvRating; // TODO : delete
    private AutocompleteSupportFragment autocompleteFragment;

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

        // Initialize the SDK
        ApplicationInfo appInfo = null;
        String apiKey = null;
        if (!Places.isInitialized()) {
            try {
                appInfo = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Error with getting meta data: ", e);
            }
            if (appInfo != null) {
                apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
            }

            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }
        });

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