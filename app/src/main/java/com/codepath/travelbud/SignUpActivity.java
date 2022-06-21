package com.codepath.travelbud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "SignUpActivity";

    private EditText etEmailSignUp;
    private EditText etUsernameSignUp;
    private EditText etPasswordSignUp;
    private Button btnSignUpSU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsernameSignUp = findViewById(R.id.etUsernameSignUp);
        etPasswordSignUp = findViewById(R.id.etPasswordSignUp);
        etEmailSignUp = findViewById(R.id.etEmailSignUp);
        btnSignUpSU = findViewById(R.id.btnSignUpSU);

        btnSignUpSU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailSignUp.getText().toString();
                String username = etUsernameSignUp.getText().toString();
                String password = etPasswordSignUp.getText().toString();
                try {
                    signUpUser(email, username, password);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void signUpUser(String email, String username, String password) throws ParseException {
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

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with signup", e);
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