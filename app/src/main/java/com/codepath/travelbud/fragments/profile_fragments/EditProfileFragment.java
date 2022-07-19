package com.codepath.travelbud.fragments.profile_fragments;

import static com.codepath.travelbud.FollowRequestsAdapter.KEY_FULL_NAME;
import static com.codepath.travelbud.fragments.profile_fragments.ProfileFragment.KEY_BIO;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.codepath.travelbud.R;
import com.codepath.travelbud.fragments.profile_fragments.ProfileFragment;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    public static final String TAG = "EditProfileFragment";
    public static final String KEY_IS_PRIVATE = "is_private";

    private EditText etFullNameEP;
    private EditText etUsernameEP;
    private EditText etEmailEP;
    private Switch switchProfile;
    private Button btnSave;
    private EditText etBioEP;

    private String newUsername;
    private String newFullname;
    private String newEmail;
    private String newBio;

    private boolean isPrivate;
    private ParseUser currentUser;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFullNameEP = view.findViewById(R.id.etFullNameEP);
        etUsernameEP = view.findViewById(R.id.etUsernameEP);
        etEmailEP = view.findViewById(R.id.etEmailEP);
        switchProfile = view.findViewById(R.id.switchPrivate);
        btnSave = view.findViewById(R.id.btnSave);
        etBioEP = view.findViewById(R.id.etBioEP);

        currentUser = ParseUser.getCurrentUser();
        isPrivate = currentUser.getBoolean(KEY_IS_PRIVATE);

        etFullNameEP.setText(currentUser.getString(KEY_FULL_NAME));
        etUsernameEP.setText(currentUser.getUsername());
        etEmailEP.setText(currentUser.getEmail());
        etBioEP.setText(currentUser.getString(KEY_BIO));

        newFullname = currentUser.getString(KEY_FULL_NAME);
        newUsername = currentUser.getUsername();
        newEmail = currentUser.getEmail();
        newBio = currentUser.getString(KEY_BIO);

        if (currentUser.getBoolean(KEY_IS_PRIVATE)) {
            switchProfile.setChecked(true);
        }

        switchProfile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isPrivate = true;
                } else {
                    isPrivate = false;
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newFullname = etFullNameEP.getText().toString();
                newEmail = etEmailEP.getText().toString();
                newUsername = etUsernameEP.getText().toString();
                newBio = etBioEP.getText().toString();

                saveInfo(newFullname, newUsername, newEmail, newBio, isPrivate);
            }
        });

    }

    private void saveInfo(String newFullname, String newUsername, String  newEmail, String newBio, boolean isPrivate) {
        currentUser.put(KEY_FULL_NAME, newFullname);
        currentUser.setEmail(newEmail);
        currentUser.setUsername(newUsername);
        currentUser.put(KEY_BIO, newBio);
        currentUser.put(KEY_IS_PRIVATE, isPrivate);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with edit profile", e);
                    Toast.makeText(getContext(), "Issue with edit profile", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(getContext(), "Success on edit profile!", Toast.LENGTH_SHORT).show();
                    goProfileFragment();
                }
            }
        });
    }

    private void goProfileFragment() {
        Fragment fragment = new ProfileFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flContainer, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}