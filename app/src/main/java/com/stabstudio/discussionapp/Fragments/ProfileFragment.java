package com.stabstudio.discussionapp.Fragments;


import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stabstudio.discussionapp.Models.Discussion;
import com.stabstudio.discussionapp.Models.Places;
import com.stabstudio.discussionapp.Models.User;
import com.stabstudio.discussionapp.R;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseRef;
    private DatabaseReference usersRef;
    private DatabaseReference placesRef;
    private DatabaseReference discussionsRef;
    private StorageReference storageRef;
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    @BindView(R.id.first_name) TextView firstName;
    @BindView(R.id.last_name) TextView lastName;
    @BindView(R.id.user_email) TextView email;
    @BindView(R.id.user_place) TextView place;
    @BindView(R.id.profileimg) ImageView profilePic;
    @BindView(R.id.ll1) LinearLayout firstNameLl;
    @BindView(R.id.ll2) LinearLayout lastNameLl;
    @BindView(R.id.ll3) LinearLayout emailLl;
    @BindView(R.id.ll4) LinearLayout placeLl;
    @BindView(R.id.ll6) LinearLayout logoutLl;

    private User snapshot;
    private Places snapshotPlace;
    private String userId;
    private String userEmail;
    private String userName;
    private Uri imageFile;
    private Uri defaultUri;
    private HashMap<String,String>idToCity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();
        userEmail = user.getEmail();
        userName = user.getDisplayName();
        defaultUri = user.getPhotoUrl();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        placesRef = FirebaseDatabase.getInstance().getReference().child("Places");
        storageRef = FirebaseStorage.getInstance().getReference();
        idToCity = new HashMap<String,String>();
        placesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    snapshotPlace = snapshot.getValue(Places.class);
                    idToCity.put(snapshotPlace.getId(),snapshotPlace.getAddress());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        StorageReference riversRef = storageRef.child(userId + "/" + "profile_image.jpg");
        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with(getActivity()).load(uri).fitCenter().into(profilePic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Cannot load profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vi = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.bind(this, vi);

        preferences = getActivity().getSharedPreferences("MetaData", Context.MODE_PRIVATE);
        editor = preferences.edit();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference();


        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChooser();
            }
        });

        firstNameLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(1, firstName.getText().toString());
            }
        });

        lastNameLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(2, lastName.getText().toString());
            }
        });

        placeLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(3, place.getText().toString());
            }
        });

        logoutLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setMessage("Do you wish to sign out?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });

        return vi;
    }

    private void showImageChooser(){
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        //chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        startActivityForResult(chooserIntent, 500);
    }

    private void logout(){
        auth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        getActivity().finish();
                    }
                }
        );
    }

    private void updateData(){
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                snapshot = dataSnapshot.child(userId).getValue(User.class);
                firstName.setText(snapshot.getFirst_name());
                lastName.setText(snapshot.getLast_name());
                email.setText(userEmail);
                place.setText(idToCity.get(snapshot.getPlace_id()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showAlertDialog(final int n, String str){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_layout, null);
        final EditText editText = (EditText) view.findViewById(R.id.et_change);
        editText.setText(str);
        dialog.setTitle("Edit Value");
        dialog.setView(view);
        dialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                User temp = snapshot;
                if(n == 1){
                    temp.setFirst_name(editText.getText().toString());
                }else if(n == 2){
                    temp.setLast_name(editText.getText().toString());
                }else if(n == 3){
                    temp.setPhoneNo(editText.getText().toString());
                }
                usersRef.child(userId).setValue(temp);
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
        updateData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 500 && resultCode == getActivity().RESULT_OK && data != null){
            imageFile = data.getData();
            uploadImage();
            Glide.with(getActivity()).load(imageFile).into(profilePic);
        }
    }

    private void uploadImage(){
        if(imageFile != null){
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            //progressDialog.setTitle("Updating");
            progressDialog.setMessage("Updating Profile Pic...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            StorageReference riversRef = storageRef.child(userId + "/" + "profile_image.jpg");
            riversRef.putFile(imageFile)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
        }
        else{
            Toast.makeText(getActivity(), "File not Uploaded", Toast.LENGTH_SHORT).show();
        }
    }

}
