package com.codepath.travelbud;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private AutoCompleteTextView actvLocation;
    private RatingBar rbPost;
    private EditText etDescription;
    private ImageButton btnTakePhoto;
    private Button btnPost;
    private ImageView ivPhoto;
//    private ImageView ivProfilePicPost;

    public String photoFileName = "photo.jpg";
    File photoFile;

    public final String APP_TAG = "TravelBud";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static final Integer MIN_DESCRIPTION_LEN = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        actvLocation = findViewById(R.id.actvLocation);
        rbPost = findViewById(R.id.rbPost);
        etDescription = findViewById(R.id.etDescription);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnPost = findViewById(R.id.btnPost);
        ivPhoto = findViewById(R.id.ivPhoto);
        ivProfilePicPost = findViewById(R.id.ivProfilePicPost);

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                Integer rating = rbPost.getNumStars();
                ParseFile image;
                if (description.length() < MIN_DESCRIPTION_LEN) {
                    Toast.makeText(MainActivity.this, "Your caption must be at least 90 characters.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, "Issue with saving post!", Toast.LENGTH_SHORT).show(); // TODO: change MainActivity.this to getContext() if using Fragment
                } else {
                    Log.i(TAG, "Post has been saved");
                    Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                    etDescription.setText("");
                    rbPost.setRating(0);
                    ivPhoto.setVisibility(View.INVISIBLE);
                }
                return;
            }
        });
    }
}