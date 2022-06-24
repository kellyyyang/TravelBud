package com.codepath.travelbud.fragments;

import static android.app.Activity.RESULT_OK;
import static com.codepath.travelbud.BuildConfig.MAPS_API_KEY;
import static com.parse.Parse.getApplicationContext;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
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

import com.codepath.travelbud.BitmapScaler;
import com.codepath.travelbud.MainActivity;
import com.codepath.travelbud.MapsActivity;
import com.codepath.travelbud.Post;
import com.codepath.travelbud.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";

    private RatingBar rbPost;
    private EditText etDescription;
    private ImageButton btnTakePhoto;
    private Button btnPost;
    private ImageView ivPhoto;
    private ImageView ivProfilePicPost;
    private TextView tvUsername;
    private TextView tvRating;
    private AutocompleteSupportFragment autocompleteFragment;
    ActivityResultLauncher<Intent> cameraResultLauncher;
    private LatLng latlong;

    // camera variables
    public String photoFileName = "photo.jpg";
    File photoFile;
    public final String APP_TAG = "TravelBud";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;

    public static final Integer MIN_DESCRIPTION_LEN = 90;

    Activity activity = new Activity();

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
//        PlacesClient placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                latlong = place.getLatLng();
            }
        });

        rbPost.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                tvRating.setText("Your rating is: " + rbPost.getRating());
            }
        });

        // by this point we have the camera photo on disk
        // RESIZE BITMAP, see section below
        // Load the taken image into a preview
        // Result was a failure
        // TODO: change
        cameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // by this point we have the camera photo on disk

                            DisplayMetrics displaymetrics = new DisplayMetrics();
                            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                            int height = displaymetrics.heightPixels;
                            int width = displaymetrics.widthPixels;

                            Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(photoFileName));
                            // by this point we have the camera photo on disk
                            Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                            Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, width); // TODO: change

                            // Configure byte output stream
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            // Compress the image further
                            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                            // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                            File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                            try {
                                resizedFile.createNewFile();
                            } catch (IOException e) {
                                Log.e(TAG, "Error with creating a new file for resized bitmap: " + e);
                            }
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(resizedFile);
                            } catch (FileNotFoundException e) {
                                Log.e(TAG, "Error with FileOutputStream: " + e);
                            }
                            // Write the bytes of the bitmap to file
                            try {
                                fos.write(bytes.toByteArray());
                            } catch (IOException e) {
                                Log.e(TAG, "Error with writing the bytes of the bitmap to file: " + e);
                            }
                            try {
                                fos.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Error with closing: " + e);
                            }
                            ivPhoto.setImageBitmap(BitmapFactory.decodeFile(resizedFile.getPath()));

                        } else { // Result was a failure
                            Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                        }
                }
        );

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                float rating = rbPost.getRating();
                ParseFile image = null;
//                if (description.length() < MIN_DESCRIPTION_LEN) {
//                    Toast.makeText(getContext(), "Your caption must be at least 90 characters.", Toast.LENGTH_LONG).show();
//                    return;
//                }
                if (photoFile == null || ivPhoto.getDrawable() == null) {
                    image = null;
                } else if (photoFile != null && ivPhoto.getDrawable() != null) {
                    image = new ParseFile(photoFile);
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(currentUser, description, rating, image, latlong);
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCamera(v);
            }
        });
    }

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.capstone.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            cameraResultLauncher.launch(intent);
//            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }
        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                // by this point we have the camera photo on disk
//                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
//                // RESIZE BITMAP, see section below
//                // Load the taken image into a preview
//
//                ivPhoto.setImageBitmap(takenImage);
//            } else { // Result was a failure
//                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void savePost(ParseUser currentUser, String description, Float rating, ParseFile image, LatLng latlong) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(currentUser);
        post.setRating(rating);
        post.setLocation(new ParseGeoPoint(latlong.latitude, latlong.longitude));
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
                    Toast.makeText(getContext(), "Issue with saving post!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Post has been saved");
                    Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                    etDescription.setText("");
                    rbPost.setRating(0);
                    ivPhoto.setVisibility(View.INVISIBLE);
                    autocompleteFragment.setText("");
                }
                return;
            }
        });
    }
}