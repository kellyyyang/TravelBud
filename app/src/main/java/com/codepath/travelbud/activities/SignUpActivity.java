package com.codepath.travelbud.activities;

import static com.codepath.travelbud.adapters.FollowRequestsAdapter.KEY_FULL_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.travelbud.R;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "SignUpActivity";
    public static final String KEY_INTERESTS = "interests";

    private EditText etEmailSignUp;
    private EditText etUsernameSignUp;
    private EditText etPasswordSignUp;
    private Button btnSignUpSU;
    private EditText etFullNameSignUp;

    private TextView tvInterestsList;
    private EditText etInterests;
    private ImageButton btnInterests;

    private List<String> interests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsernameSignUp = findViewById(R.id.etUsernameSignUp);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);
        etEmailSignUp = findViewById(R.id.etEmailSignUp);
        btnSignUpSU = findViewById(R.id.btnSignUpSU);
        etFullNameSignUp = findViewById(R.id.etFullNameSignUp);

        tvInterestsList = findViewById(R.id.tvInterestsList);
        etInterests = findViewById(R.id.etInterests);
        btnInterests = findViewById(R.id.btnEnterInterests);

        interests = new ArrayList<>();

        btnInterests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etInterests.length() > 0) {
                    interests.add(etInterests.getText().toString());
                    tvInterestsList.append("#" + etInterests.getText().toString() + " ");
                    etInterests.setText("");
                }
            }
        });

        btnSignUpSU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailSignUp.getText().toString();
                String username = etUsernameSignUp.getText().toString();
                String password = etPasswordSignUp.getText().toString();
                String fullname = etFullNameSignUp.getText().toString();
                try {
                    signUpUser(email, username, password, fullname, interests);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void signUpUser(String email, String username, String password, String fullname, List<String> interests) throws ParseException {
        ParseUser user = new ParseUser();
        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        if (query.whereEqualTo("username", username).count() > 0) {
            Toast.makeText(this, "This username is taken!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (query.whereEqualTo("email", email).count() > 0) {
            Toast.makeText(this, "This email is taken!", Toast.LENGTH_SHORT).show();
            return;
        }
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.addAll(KEY_INTERESTS, interests);
        user.put(KEY_FULL_NAME, fullname);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(SignUpActivity.this, "Issue with signup", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    goMainActivity();
                }
            }
        });
    }

    private void goMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}