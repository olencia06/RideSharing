package com.example.ridesharing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference databaseReference;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        RecyclerView recyclerView = findViewById(R.id.recyclerView); // Ensure this matches the XML ID

        progressBar = findViewById(R.id.progress_bar);
        userList = new ArrayList<>();

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("RideSharing/Users");

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(userAdapter);

        fetchUsers();
    }

    private void fetchUsers() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(snapshot.getKey()); // Assuming User has a setId() method
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ManageUsersActivity", "Error fetching users", databaseError.toException());
                Toast.makeText(ManageUsersActivity.this, "Failed to load users.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
