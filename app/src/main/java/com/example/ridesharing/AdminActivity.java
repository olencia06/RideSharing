package com.example.ridesharing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class AdminActivity extends AppCompatActivity {

    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(userAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("RideSharing/Users");

        // Set up button listeners
        setupButtonListeners(recyclerView);

        // Fetch users initially
        fetchUsersFromDatabase();
    }

    private void setupButtonListeners(RecyclerView recyclerView) {
        Button btnDisplayUsers = findViewById(R.id.btn_display_users);
        Button btnAddUser = findViewById(R.id.btn_add_user);
        Button btnManageUsers = findViewById(R.id.btn_manage_users);

        btnDisplayUsers.setOnClickListener(view -> {
            recyclerView.setVisibility(View.VISIBLE);
            fetchUsersFromDatabase();  // Fetch users and update RecyclerView
        });

        btnAddUser.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, AddUserActivity.class);
            startActivity(intent);
        });

        btnManageUsers.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUsersFromDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
                if (userList.isEmpty()) {
                    Toast.makeText(AdminActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminActivity", "Error fetching data", error.toException());
                Toast.makeText(AdminActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
