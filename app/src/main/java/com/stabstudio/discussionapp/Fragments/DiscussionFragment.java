package com.stabstudio.discussionapp.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stabstudio.discussionapp.Adapters.DiscussionsAdapter;
import com.stabstudio.discussionapp.Models.Discussion;
import com.stabstudio.discussionapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DiscussionFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private String userId;

    private DatabaseReference usersRef;
    private DatabaseReference placesRef;
    private DatabaseReference commentsRef;
    private DatabaseReference discussionsRef;
    private DatabaseReference placeDisRef;

    private LinearLayoutManager layoutManager;
    private DiscussionsAdapter adapter;
    private ProgressDialog progressDialog;
    private RecyclerView rv;
    private SwipeRefreshLayout refreshLayout;


    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @BindView(R.id.linlaHeaderProgress) LinearLayout progressLayout;

    public static ArrayList<Discussion> discussionList = new ArrayList<>();
    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vi = inflater.inflate(R.layout.fragment_discussion, container, false);
        ButterKnife.bind(this, vi);

        preferences = getActivity().getSharedPreferences("MetaData", Context.MODE_PRIVATE);
        editor = preferences.edit();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading Discussions");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressLayout.setVisibility(View.VISIBLE);
        rv = (RecyclerView) vi.findViewById(R.id.discussion_recycler_view);
        layoutManager = new LinearLayoutManager(rv.getContext());
        rv.setHasFixedSize(true);
        rv.setLayoutManager(layoutManager);
        refreshLayout = (SwipeRefreshLayout) vi.findViewById(R.id.dis_refreshlayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDiscussions();
            }
        });

        return vi;
    }

    @Override
    public void onStart() {
        super.onStart();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        userId = user.getUid();
        loadDiscussions();
    }

    private void loadDiscussions(){
        usersRef = databaseRef.child("Users");

        placesRef = databaseRef.child("Places");
        commentsRef = databaseRef.child("Comments");
        discussionsRef = databaseRef.child("Discussions");
        placeDisRef = databaseRef.child("place-discussion");
        final String placeId = preferences.getString("user_place", "null");
        Boolean ok = false;
        discussionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                discussionList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Discussion discussion = snapshot.getValue(Discussion.class);
                    if(discussion.getVisibleToID().contains(userId)) {
                        discussionList.add(discussion);
                    }
                }

                Collections.reverse(discussionList);
                adapter = new DiscussionsAdapter(getActivity());
                rv.setAdapter(adapter);
                progressLayout.setVisibility(View.GONE);
                refreshLayout.setRefreshing(false);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private List<String> getRandomSublist(String[] array, int amount) {
        ArrayList<String> list = new ArrayList<>(amount);
        Random random = new Random();
        while (list.size() < amount) {
            list.add(array[random.nextInt(array.length)]);
        }
        return list;
    }

}
