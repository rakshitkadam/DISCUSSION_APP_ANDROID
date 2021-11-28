package com.stabstudio.discussionapp.UI;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import com.stabstudio.discussionapp.Models.User;
import com.stabstudio.discussionapp.R;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.stabstudio.discussionapp.Fragments.DiscussionFragment.discussionList;

public class AddDiscussionActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference dRef;
    private StorageReference sRef;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private ProgressDialog progressDialog;
    private String userId;
    private String usersVisible[];
    private int totalNumberOfUsers;
    private boolean chosenVisibleUsers[];
    private ArrayList<Integer> finalListOfChosenUsers;  //Indexes of the chosen users
    private ArrayList<String>namesOfStudents;
    private ArrayList<String>idOfUsers;

    @BindView(R.id.dis_topic) EditText topic;
    @BindView(R.id.dis_content) EditText content;
    @BindView(R.id.dis_image) TextView showPeople;
    @BindView(R.id.post_discussion) Button publish;

    private int mYear;
    private int mMonth;
    private int mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_discussion);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        dRef = FirebaseDatabase.getInstance().getReference();
        sRef = FirebaseStorage.getInstance().getReference();

        preferences = getSharedPreferences("MetaData", Context.MODE_PRIVATE);
        editor = preferences.edit();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Publishing");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        idOfUsers = new ArrayList<String>();
        finalListOfChosenUsers = new ArrayList<Integer>();
        namesOfStudents = new ArrayList<String>();
        totalNumberOfUsers = 0;
        dRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    idOfUsers.add(user.getId());
                    namesOfStudents.add(user.getFirst_name()+" "+user.getLast_name());
                    totalNumberOfUsers++;
                    // fetched the attributes of Users and initializing it.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        for(int i=0;i<idOfUsers.size();i++) {
            usersVisible[i] = idOfUsers.get(i);
        }
        showPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPeopleChooser();
            }
        });
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishDiscussion();
            }
        });

    }


    private void getDate(){
        DatePickerDialog.OnDateSetListener mDateSetListener =
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mDay = dayOfMonth;
                        mMonth = monthOfYear;
                        mYear = year;
                        mMonth++;
                        String dateStr = mMonth + "," + mDay + "," + mYear;
                    }
                };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void publishDiscussion(){
        String topicStr = topic.getText().toString();
        String contentStr = content.getText().toString();
        if(!TextUtils.isEmpty(topicStr) && !TextUtils.isEmpty(contentStr) && finalListOfChosenUsers.size()!=0){
            progressDialog.show();
            String visibleUsersId = "";
            for(int i=0;i<finalListOfChosenUsers.size();i++) {
                  visibleUsersId  += idOfUsers.get(finalListOfChosenUsers.get(i)) + ",";
                  // Get all the allowed visiblee users in one string
              }
            DatabaseReference disRef = dRef.child("Discussions");
            String placeId = preferences.getString("user_place", "no_place");
            String disId = disRef.push().getKey();
            String userId = preferences.getString("user_id", "null");
            String timeStamp = DateTime.now().getSecondOfMinute() + "/" +
                               DateTime.now().getMinuteOfHour() + "/" +
                               DateTime.now().getHourOfDay() + "/" +
                               DateTime.now().getDayOfMonth() + "/" +
                               DateTime.now().getMonthOfYear() + "/" +
                               DateTime.now().getYear();
            Discussion newDiscussion = new Discussion(disId, placeId, userId, topicStr, visibleUsersId, contentStr, timeStamp, 0, 0);
            // Push the new discussion in the database
            disRef.child(disId).setValue(newDiscussion);

            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Discussion Published", Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            Toast.makeText(getApplicationContext(), "Please fill all the values", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPeopleChooser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose people who can see it");
        builder.setCancelable(false);
        finalListOfChosenUsers.clear();
        usersVisible = new String[idOfUsers.size()];
        for(int i=0;i<idOfUsers.size();i++)
        {
            usersVisible[i] = namesOfStudents.get(i);
            // Copy all the visible users into the userVisible array
        }
        builder.setMultiChoiceItems(usersVisible, chosenVisibleUsers, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked) {
                    if(finalListOfChosenUsers.contains(which)==false) {
                        finalListOfChosenUsers.add(which);
                    }
                }
                else {
                    int getIndex = -1;
                    for(int i=0;i<finalListOfChosenUsers.size();i++) {
                        if(finalListOfChosenUsers.get(i)==which){
                            getIndex = i;
                            // get the index of the element to be deleted in the getIndex variable.
                        }
                    }
                    if(getIndex!=-1)
                        finalListOfChosenUsers.remove(getIndex);
                }
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finalListOfChosenUsers.clear(); }
        });
        builder.setNeutralButton("Select All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finalListOfChosenUsers.clear();
                for(int i=0;i<totalNumberOfUsers;i++){
                    finalListOfChosenUsers.add(i);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.still, R.anim.slide_out_down);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
